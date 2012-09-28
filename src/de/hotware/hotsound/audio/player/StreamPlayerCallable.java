/**
 * File StreamPlayerRunnable.java
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

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.IAudioDevice;
import de.hotware.hotsound.audio.data.IAudioDevice.AudioDeviceException;
import de.hotware.hotsound.audio.data.ISeekableAudio;
import de.hotware.hotsound.audio.player.IMusicListener.MusicEndEvent;
import de.hotware.hotsound.audio.player.IMusicListener.MusicExceptionEvent;

/**
 * To be used with ExecutionServices.
 *
 * all playback functions are thread-safe. Player inspired by Matthias
 * Pfisterer's examples on JavaSound (jsresources.org). Because of the fact,
 * that this Software is meant to be Open-Source and I don't want to get anybody
 * angry about me using parts of his intelligence without mentioning it, I
 * hereby mention him as inspiration, because his code helped me to write this
 * class.
 *
 * TODO: extra listener for callable that gets redirected in StreamMusicPlayer
 *
 * TODO: Why is this a Callable<Void> instead of a simple Runnable? Why?
 *
 * TODO: extra listener for callable that gets redirected in StreamMusicPlayer
 * 
 * @author Martin Braun
 */
class StreamPlayerCallable implements Runnable {

	protected IMusicPlayer mMusicPlayer;
	protected boolean mStartLock;
	protected boolean mPrematureStop;
	protected boolean mMultithreaded;
	protected Lock mLock;
	protected boolean mPause;
	protected boolean mStop;
	protected IMusicListener mMusicListener;
	protected IAudio mAudio;
	protected IAudioDevice mAudioDevice;

	/**
	 * initializes the StreamPlayerRunnable without a
	 * listener and the default Mixer
	 * 
	 * @throws AudioDeviceException
	 */
	public StreamPlayerCallable(IAudio pAudio,
			IAudioDevice pAudioDevice,
			boolean pMultiThreaded,
			IMusicPlayer pMusicPlayer) {
		this(pAudio, pAudioDevice, pMultiThreaded, pMusicPlayer, null);
	}

	/**
	 * initializes the StreamPlayerRunnable with the given
	 * listenerand the given Mixer
	 *
	 * @throws AudioDeviceException
	 */
	public StreamPlayerCallable(IAudio pAudio,
			IAudioDevice pAudioDevice,
			boolean pMultiThreaded,
			IMusicPlayer pMusicPlayer,
			IMusicListener pMusicListener) {
		if(pAudioDevice == null || pAudio == null) {
			throw new IllegalArgumentException("the audiodevice and the audio may not be null");
		}
		this.mMusicPlayer = pMusicPlayer;
		this.mAudio = pAudio;
		this.mAudioDevice = pAudioDevice;
		this.mPause = false;
		this.mStop = true;
		this.mLock = new ReentrantLock(true);
		this.mMusicListener = pMusicListener;
		this.mStartLock = true;
		this.mPrematureStop = false;
		this.mMultithreaded = pMultiThreaded;
	}

	/**
	 * @throws IOException
	 *             if cleanup fails after finished with loading
	 */
	@Override
	public void run() {
		try {
			//wait for possible stop calls to be active
			this.mLock.lock();
		} finally {
			this.mLock.unlock();
		}
		if(!this.mPrematureStop) {
			this.mStop = false;
			this.mStartLock = false;
			int nBytesRead = 0;
			MusicPlayerException exception = null;
			boolean failure = false;
			try(IAudio audio = this.mAudio;) {
				IAudioDevice dev = this.mAudioDevice;
				audio.open();
				dev.open(audio.getAudioFormat());
				AudioFormat format = audio.getAudioFormat();
				int bufferSize = (int) format.getSampleRate() *
						format.getFrameSize();
				byte[] abData = new byte[bufferSize];
				while(nBytesRead != -1 && !this.mStop) {
					if(!this.mAudioDevice.isClosed()) {
						this.mLock.lock();
						try {
							nBytesRead = audio.read(abData, 0, bufferSize);
							if(nBytesRead != -1) {
								dev.write(abData, 0, nBytesRead);
							}
						} finally {
							this.mLock.unlock();
						}
					}
				}
			} catch(Exception e) {
				//catch all the exceptions so the user can handle them even if he just can via the listener
				failure = true;
				exception = new MusicPlayerException("An Exception occured during Playback",
						e);
				this.mMusicListener.onExeption(new MusicExceptionEvent(this.mMusicPlayer, exception));
			} finally {
				this.mStop = true;
				try {
					//has to be closed and therefore handled specifically because of possible savior behavior
					//that might want to be checked by the user (he gets the response out of the Event in the
					//Listener
					try {
						this.mAudioDevice.close();
					} catch(AudioDeviceException e) {
						failure = true;
						this.mMusicListener.onExeption(new MusicExceptionEvent(this.mMusicPlayer, e));
					}
				} finally {
					if(this.mMusicListener != null) {
						this.mMusicListener.onEnd(new MusicEndEvent(this.mMusicPlayer,
								failure ? MusicEndEvent.Type.FAILURE
										: MusicEndEvent.Type.SUCCESS));
					}
				}
			}
		}
	}

	public void seek(int pFrame) {
		if(!(this.mAudio instanceof ISeekableAudio)) {
			throw new UnsupportedOperationException("seeking is not possible on the current AudioFile");
		}
	}

	public void skip(int pFrames) {
		if(!(this.mAudio instanceof ISeekableAudio)) {
			throw new UnsupportedOperationException("skipping is not possible on the current AudioFile");
		}
	}

	public boolean isSkippingPossible() {
		return this.mAudio instanceof ISeekableAudio;
	}

	public void pause() {
		if(!this.mPause) {
			this.mLock.lock();
			this.mPause = true;
			this.mAudioDevice.pause();
		}
	}

	public void unpause() {
		if(this.mPause) {
			this.mAudioDevice.unpause();
			this.mLock.unlock();
		}
	}

	public boolean isPaused() {
		return this.mPause;
	}

	public boolean isStopped() {
		return this.mStop;
	}

	/**
	 * blocks until start has been called
	 *
	 * @throws MusicPlayerException
	 */
	public void stop() throws MusicPlayerException {
		boolean unlock = false;
		//only lock if in multithreaded mode.
		try {
			if(this.mStartLock && this.mMultithreaded) {
				this.mLock.lock();
				this.mStartLock = false;
				this.mPrematureStop = true;
				unlock = true;
			}
			this.mStop = true;
		} finally {
			if(unlock) {
				this.mLock.unlock();
			}
		}
	}

	public AudioFormat getAudioFormat() {
		return this.mAudio.getAudioFormat();
	}

	public IAudioDevice getAudioDevice() {
		return this.mAudioDevice;
	}

}