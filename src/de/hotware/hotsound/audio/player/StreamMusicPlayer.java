/**
 * File StreamMusicPlayer.java
 * ---------------------------------------------------------
 *
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 *   if it breaks something.
 */
package de.hotware.hotsound.audio.player;

/**
 *  @author Martin Braun
 *  Player inspired by Matthias Pfisterer's examples on JavaSound
 *  (jsresources.org). Because of the fact, that this Software is meant
 *  to be Open-Source and I don't want to get anybody angry about me
 *  using parts of his intelligence without mentioning it, I hereby
 *  mention him as inspiration, because his code helped me to write this class.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.BasicPlaybackAudioDevice;
import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.IAudioDevice;
import de.hotware.hotsound.audio.data.ISeekableAudio;

/**
 * always runs the playback in its own thread but you can pass an
 * ExecutorService instead if you want to
 * 
 * @author Martin Braun
 */
public class StreamMusicPlayer implements IMusicPlayer {

	protected ExecutorService mExecutorService;
	protected boolean mCreatedOwnThread;
	protected boolean mCreatedOwnAudioDevice;
	protected StreamPlayerRunnable mStreamPlayerRunnable;
	protected IMusicListener mMusicListener;
	protected IPlayerRunnableListener mPlayerRunnableListener;
	/**
	 * the current song after insertion
	 */
	protected ISong mCurrentSong;
	protected IAudio mCurrentAudio;
	/**
	 * the current mixer after insertion
	 */
	protected IAudioDevice mCurrentAudioDevice;
	private Lock mLock;

	/**
	 * Default Constructor. initializes without an external Listener
	 */
	public StreamMusicPlayer() {
		this(new IMusicListener() {

			@Override
			public void onEnd(MusicEndEvent pEvent) {
				try {
					pEvent.getSource().close();
				} catch(MusicPlayerException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onException(MusicExceptionEvent pEvent) {
				pEvent.getException().printStackTrace();
			}

		});
	}

	/**
	 * Default Constructor. initializes with the given listener if a
	 * musiclistener is passed here, make sure to shutdown the StreamMusicPlayer
	 * correctly or otherwise bugs might occur
	 */
	public StreamMusicPlayer(IMusicListener pMusicListener) {
		this(pMusicListener, Executors.newSingleThreadExecutor());
		this.mCreatedOwnThread = true;
	}
	
	
	public StreamMusicPlayer(ExecutorService pExecutorService) {
		this();
		this.mExecutorService = pExecutorService;
	}

	/**
	 * uses the given ExecutorService to run the tasks. if a musiclistener is
	 * passed here, make sure to shutdown the StreamMusicPlayer correctly or
	 * otherwise bugs might occur
	 */
	public StreamMusicPlayer(IMusicListener pMusicListener,
			ExecutorService pExecutorService) {
		this.mLock = new ReentrantLock();
		this.mMusicListener = pMusicListener;
		this.mPlayerRunnableListener = new IPlayerRunnableListener() {

			@Override
			public void onEnd(MusicEndEvent pEvent) {
				if(StreamMusicPlayer.this.mMusicListener != null) {
					StreamMusicPlayer.this.mMusicListener.onEnd(pEvent);
				}
			}

			@Override
			public void onException(MusicExceptionEvent pEvent) {
				if(StreamMusicPlayer.this.mMusicListener != null) {
					StreamMusicPlayer.this.mMusicListener.onException(pEvent);
				}
			}

		};
		this.mExecutorService = pExecutorService;
		this.mCurrentSong = null;
		this.mCurrentAudioDevice = null;
	}

	/**
	 * @inheritDoc if a musiclistener is passed here, make sure to shutdown the
	 *             StreamMusicPlayer correctly or otherwise bugs might occur
	 */
	@Override
	public void setMusicListener(IMusicListener pMusicListener) {
		this.mMusicListener = pMusicListener;
	}

	/**
	 * @throws MusicPlayerException
	 * @inheritDoc uses a BasicPlaybackAudioDevice as AudioDevice if there is
	 *             not already one in usage (not closed)
	 */
	@Override
	public void insert(ISong pSong) throws MusicPlayerException {
		this.mLock.lock();
		try {
			IAudioDevice dev = this.mCurrentAudioDevice;
			if(dev == null || dev.isClosed()) {
				this.mCreatedOwnAudioDevice = true;
				dev = new BasicPlaybackAudioDevice();
			}
			this.insert(pSong, dev);
		} finally {
			this.mLock.unlock();
		}
	}

	/**
	 * @param pMixer
	 *            if null is passed the AudioSystem uses the default Mixer
	 * @throws MusicPlayerException
	 * @inheritDoc
	 * @throws SongInsertionException
	 *             if audio file is either not supported, its line is not
	 *             available or an IOException has been thrown in the underlying
	 *             methods
	 */
	@Override
	public void insert(ISong pSong, IAudioDevice pAudioDevice) throws MusicPlayerException {
		this.mLock.lock();
		try {
			this.insertInternal(pSong, pAudioDevice);
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void start() throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			if(!this.mStreamPlayerRunnable.isStopped()) {
				throw new IllegalStateException("Player is already playing");
			}
			if(this.mExecutorService != null) {
				//run on the thread specified - The Callable really should be a Runnable.
				this.mExecutorService.submit(this.mStreamPlayerRunnable);
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void restart() {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void pause() {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mStreamPlayerRunnable.pause();
		} finally {
			this.mLock.unlock();
		}
	}

	/**
	 * @inheritDoc blocks until start has been called if in multithreaded mode
	 *             (ExecutorService != null)
	 */
	@Override
	public void stop() throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mStreamPlayerRunnable.stop();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isStopped() {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mStreamPlayerRunnable.isStopped();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void unpause() {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mStreamPlayerRunnable.unpause();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isPaused() {
		this.mLock.lock();
		try {
			return this.mStreamPlayerRunnable.isPaused();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void close() throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable != null) {
				this.mStreamPlayerRunnable.stop();
			}
			if(this.mCreatedOwnThread) {
				this.mExecutorService.shutdown();
			}
			if(this.mCreatedOwnAudioDevice) {
				this.mCurrentAudioDevice.close();
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public AudioFormat getAudioFormat() {
		this.mLock.lock();
		try {
			return this.mStreamPlayerRunnable.getAudioFormat();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public IAudioDevice getAudioDevice() {
		this.mLock.lock();
		try {
			return this.mStreamPlayerRunnable.getAudioDevice();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void seek(int pPosition) {
		this.mLock.lock();
		try {
			throw new UnsupportedOperationException("not implemented yet");
			//			// just pause the playback
			//			this.stopPlayback();
			//			try {
			//				this.insertInternal(this.mCurrentSong, this.mCurrentMixer);
			//				if(file != null && this.mPlayerRunnable.mAudioFormat.getFrameLength() != -1)
			//				{	
			//				double skippedPercentage = (double)percentage/100;
			//				System.out.println("Percentage to skip " + skippedPercentage);
			//				long framesToSkip = (long) (getFrameLength() * skippedPercentage);
			//				System.out.println("Skipping " + framesToSkip + " frames with " + getFrameLength() + " available");
			//				long bytesSkipped = 0;
			//				byte[] garbage = new byte[4096];
			//				long bytesDropped = 0;
			//				while(bytesSkipped <= framesToSkip*audioFormat.getFrameSize() && bytesDropped != -1)
			//				{
			//				// System.out.println(bytesSkipped/audioFormat.getFrameSize());
			//				bytesDropped = audioInputStream.read(garbage, 0, garbage.length);
			//				// bytesDropped = audioInputStream.skip(4096);
			//				// System.out.println("Dropped " + bytesDropped + " bytes");
			//				bytesSkipped += bytesDropped;
			//				}
			//				skippedFrames += bytesSkipped/audioFormat.getFrameSize();
			//				System.out.println("Skipped Frames: " + bytesSkipped/audioFormat.getFrameSize());
			//				}
			//				}
			//				catch (IOException e)
			//				{
			//				e.printStackTrace();
			//				}	
			//				catch(Exception e)
			//				{
			//				e.printStackTrace();
			//			}
			//			// start playing again
			//			this.startPlayback();
		} finally {
			this.mLock.unlock();
		}
	}

	private void insertInternal(ISong pSong, IAudioDevice pAudioDevice) throws MusicPlayerException {
		if(this.mStreamPlayerRunnable != null &&
				!this.mStreamPlayerRunnable.isStopped()) {
			throw new IllegalStateException("You can only insert Songs while the Player is stopped!");
		}
		if(this.mCurrentSong != pSong) {
			//the song has changed, update everything and close the old audio
			if(this.mCurrentAudio != null) {
				this.mCurrentAudio.close();
			}
			this.mCurrentSong = pSong;
			this.mCurrentAudio = pSong.getAudio();
			this.mCurrentAudio.open();
		} else if(this.mCurrentSong != null && this.mCurrentAudio != null) {
			if(this.mCurrentSong instanceof ISeekableAudio &&
					!this.mCurrentAudio.isClosed()) {
				//TODO: seek implementation!
				this.mCurrentAudio.close();
			} else {
				this.mCurrentAudio.close();
			}
			this.mCurrentAudio = pSong.getAudio();
			this.mCurrentAudio.open();
		}
		if(this.mCreatedOwnAudioDevice && this.mCurrentAudioDevice != null &&
				!this.mCurrentAudioDevice.isClosed() &&
				this.mCurrentAudioDevice != pAudioDevice) {
			this.mCurrentAudioDevice.close();
		}
		this.mCurrentAudioDevice = pAudioDevice;
		if(this.mCurrentAudioDevice.isClosed()) {
			this.mCurrentAudioDevice.open(this.mCurrentAudio.getAudioFormat());
		}
		this.mStreamPlayerRunnable = new StreamPlayerRunnable(this.mCurrentAudio,
				this.mCurrentAudioDevice,
				this.mExecutorService != null,
				this,
				this.mPlayerRunnableListener);
	}

}
