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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.Audio;
import de.hotware.hotsound.audio.data.AudioDevice;
import de.hotware.hotsound.audio.player.StreamPlayerRunnable.StreamPlayerRunnableListener;

/**
 * always runs the playback in its own thread but you can pass an
 * ExecutorService instead if you want to
 * 
 * TODO: test skipping, etc.
 * 
 * @author Martin Braun
 */
public final class StreamMusicPlayer implements MusicPlayer {

	protected Executor mPlaybackExecutor;
	protected ExecutorService mSignallingExecutor;
	protected boolean mCreateOwnThread;
	protected StreamPlayerRunnable mStreamPlayerRunnable;
	protected MusicListener mMusicListener;
	protected StreamPlayerRunnableListener mPlayerRunnableListener;
	/**
	 * the current song after insertion
	 */
	protected Song mCurrentSong;
	protected Audio mCurrentAudio;
	/**
	 * the current mixer after insertion
	 */
	protected AudioDevice mCurrentAudioDevice;
	private Lock mLock;

	/**
	 * Default Constructor. initializes without an external Listener. An
	 * instance created with it always closes its resources. so you might want
	 * to use a different constructor if you want to use the player several
	 * times
	 */
	public StreamMusicPlayer() {
		this(new MusicListener() {

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
	public StreamMusicPlayer(MusicListener pMusicListener) {
		this(pMusicListener, null);
		this.mCreateOwnThread = true;
	}

	public StreamMusicPlayer(Executor pExecutor) {
		this();
		this.mPlaybackExecutor = pExecutor;
	}

	/**
	 * uses the given ExecutorService to run the tasks. if a musiclistener is
	 * passed here, make sure to shutdown the StreamMusicPlayer correctly or
	 * otherwise bugs might occur
	 */
	public StreamMusicPlayer(MusicListener pMusicListener, Executor pExecutor) {
		this.mLock = new ReentrantLock();
		this.mMusicListener = pMusicListener;
		//this has to be done on a separate one because of the signaling behaviour
		this.mPlayerRunnableListener = new StreamPlayerRunnableListener() {

			@Override
			public void onEnd(final MusicEndEvent pEvent) {
				if(!StreamMusicPlayer.this.mSignallingExecutor.isShutdown()) {
					StreamMusicPlayer.this.mSignallingExecutor
							.execute(new Runnable() {

								@Override
								public void run() {
									if(StreamMusicPlayer.this.mMusicListener != null) {
										StreamMusicPlayer.this.mMusicListener
												.onEnd(pEvent);
									}
								}

							});

				}
			}

			@Override
			public void onException(final MusicExceptionEvent pEvent) {
				if(!StreamMusicPlayer.this.mSignallingExecutor.isShutdown()) {
					StreamMusicPlayer.this.mSignallingExecutor
							.execute(new Runnable() {

								@Override
								public void run() {
									if(StreamMusicPlayer.this.mMusicListener != null) {
										StreamMusicPlayer.this.mMusicListener
												.onException(pEvent);
									}
								}

							});
				}
			}

		};
		this.mPlaybackExecutor = pExecutor;
		this.mSignallingExecutor = Executors.newSingleThreadExecutor();
		this.mCurrentSong = null;
		this.mCurrentAudioDevice = null;
	}

	/**
	 * @inheritDoc if a musiclistener is passed here, make sure to shutdown the
	 *             StreamMusicPlayer correctly or otherwise bugs might occur
	 */
	@Override
	public void setMusicListener(MusicListener pMusicListener) {
		this.mMusicListener = pMusicListener;
	}

	/**
	 * @throws MusicPlayerException
	 * @inheritDoc
	 * @throws SongInsertionException
	 *             if audio file is either not supported, its line is not
	 *             available or an IOException has been thrown in the underlying
	 *             methods
	 */
	@Override
	public void insert(Song pSong, AudioDevice pAudioDevice) throws MusicPlayerException {
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
			if(!this.mStreamPlayerRunnable.isStopped() &&
					this.mStreamPlayerRunnable.isAlreadyStarted()) {
				throw new IllegalStateException("Player is already playing");
			}
			this.mStreamPlayerRunnable.mStopped = false;
			this.mPlaybackExecutor.execute(this.mStreamPlayerRunnable);
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void restart() throws MusicPlayerException {
		if(this.mStreamPlayerRunnable == null) {
			throw new IllegalStateException("can't restart, not started, yet");
		}
		if(this.mStreamPlayerRunnable.isStopped()) {
			try {
				this.mStreamPlayerRunnable.join();
			} catch(InterruptedException e) {
				throw new MusicPlayerException(e);
			}
			this.mStreamPlayerRunnable.reset();
			this.mStreamPlayerRunnable.seek(0);
			this.mPlaybackExecutor.execute(this.mStreamPlayerRunnable);
		} else {
			this.mStreamPlayerRunnable.seek(0);
		}
	}

	@Override
	public void pause(boolean pPause) {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable != null) {
				this.mStreamPlayerRunnable.pause(pPause);
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void stop() throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable != null) {
				this.mStreamPlayerRunnable.stop();
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isStopped() {
		this.mLock.lock();
		try {
			return this.mStreamPlayerRunnable == null ||
					this.mStreamPlayerRunnable.isStopped() ||
					!this.mStreamPlayerRunnable.isAlreadyStarted();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isPaused() {
		this.mLock.lock();
		try {
			return this.mStreamPlayerRunnable == null ||
					this.mStreamPlayerRunnable.isPaused();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void close() throws MusicPlayerException {
		this.mLock.lock();
		//auto close the current audio
		try(Audio audio = this.mCurrentAudio) {
			if(this.mStreamPlayerRunnable != null) {
				this.mStreamPlayerRunnable.stop();
			}
			if(this.mCreateOwnThread && this.mPlaybackExecutor != null) {
				((ExecutorService) this.mPlaybackExecutor).shutdown();
			}
			this.mSignallingExecutor.shutdown();
		} finally {
			if(this.mCreateOwnThread) {
				this.mPlaybackExecutor = null;
			}
			this.mStreamPlayerRunnable = null;
			this.mCurrentAudio = null;
			this.mCurrentAudioDevice = null;
			this.mCurrentSong = null;
			this.mLock.unlock();
		}
	}

	@Override
	public AudioFormat getAudioFormat() {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mStreamPlayerRunnable.getAudioFormat();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public AudioDevice getAudioDevice() {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mStreamPlayerRunnable.getAudioDevice();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void skip(long pFrames) throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(this.mStreamPlayerRunnable == null) {
				throw new IllegalStateException("can't skip");
			}
			if(!this.mStreamPlayerRunnable.isStopped() &&
					!this.mStreamPlayerRunnable.isDone()) {
				this.mStreamPlayerRunnable.skip(pFrames);
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void seek(long pFrame) throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(!this.canSeek()) {
				throw new IllegalStateException("can't seek");
			}
			if(!this.mStreamPlayerRunnable.isStopped() &&
					!this.mStreamPlayerRunnable.isDone()) {
				this.mStreamPlayerRunnable.seek(pFrame);
			} else {
				this.restart();
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean canSeek() {
		return this.mStreamPlayerRunnable != null &&
				this.mStreamPlayerRunnable.canSeek();
	}

	@Override
	public String toString() {
		this.mLock.lock();
		try {
			StringBuilder builder = new StringBuilder();
			return builder.append("[").append(this.getClass().getSimpleName())
					.append(": Current Song: ").append(this.mCurrentSong)
					.append(" ").append("Current AudioDevice: ")
					.append(this.mCurrentAudioDevice).append("]").toString();
		} finally {
			this.mLock.unlock();
		}
	}

	private void insertInternal(Song pSong, AudioDevice pAudioDevice) throws MusicPlayerException {
		if(this.mStreamPlayerRunnable != null) {
			if(!this.mStreamPlayerRunnable.isStopped()) {
				throw new IllegalStateException("You can only insert Songs while the Player is stopped!");
			} else {
				try {
					this.mStreamPlayerRunnable.join();
				} catch(InterruptedException e) {
					throw new MusicPlayerException("couldn't wait until the end of the current audio",
							e);
				}
			}
		}
		if(this.mCreateOwnThread && this.mPlaybackExecutor == null) {
			this.mPlaybackExecutor = Executors.newSingleThreadExecutor();
		}
		if(this.mCurrentAudio != null && !this.mCurrentAudio.isClosed()) {
			this.mCurrentAudio.close();
		}
		try {
			this.mCurrentSong = pSong;
			this.mCurrentAudio = pSong.getAudio();
			this.mCurrentAudio.open();
		} catch(MusicPlayerException e) {
			this.mCurrentAudio = null;
			this.mCurrentSong = null;
			throw e;
		}
		this.mCurrentAudioDevice = pAudioDevice;
		if(this.mCurrentAudioDevice.isClosed()) {
			this.mCurrentAudioDevice.open(this.mCurrentAudio.getAudioFormat());
		}
		this.mStreamPlayerRunnable = new StreamPlayerRunnable(this.mCurrentAudio,
				this.mCurrentAudioDevice,
				this,
				this.mPlayerRunnableListener);
	}

}
