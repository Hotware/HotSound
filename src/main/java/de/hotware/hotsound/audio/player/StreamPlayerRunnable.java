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
import java.util.EventListener;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.Audio;
import de.hotware.hotsound.audio.data.Audio.AudioException;
import de.hotware.hotsound.audio.data.AudioDevice;
import de.hotware.hotsound.audio.data.AudioDevice.AudioDeviceException;
import de.hotware.hotsound.audio.data.SeekableAudio;
import de.hotware.util.Pause;

/**
 * To be used with Executors. Is not thread-safe! Do not execute twice!
 * 
 * Player inspired by Matthias Pfisterer's examples on JavaSound
 * (jsresources.org). Because of the fact, that this Software is meant to be
 * Open-Source and I don't want to get anybody angry about me using parts of his
 * intelligence without mentioning it, I hereby mention him as inspiration,
 * because his code helped me to write this class.
 * 
 * TODO: review the stopping process and change if necessary. works but may be
 * bad code.
 * 
 * @author Martin Braun
 */
final class StreamPlayerRunnable implements Runnable {

	protected MusicPlayer mMusicPlayer;
	protected Pause mPause;
	protected Lock mJoinLock;
	protected Condition mJoinCondition;
	protected StreamPlayerRunnableListener mPlayerRunnableListener;
	protected Audio mAudio;
	protected AudioDevice mAudioDevice;

	protected boolean mAlreadyStarted;
	protected boolean mPrematureStop;
	protected boolean mStopped;
	protected boolean mDone;

	/**
	 * initializes the StreamPlayerRunnable with the given listener and the
	 * given Mixer
	 * 
	 * @throws AudioDeviceException
	 */
	public StreamPlayerRunnable(Audio pAudio,
			AudioDevice pAudioDevice,
			MusicPlayer pMusicPlayer,
			StreamPlayerRunnableListener pPlayerRunnableListener) {
		if(pAudioDevice == null || pAudio == null) {
			throw new NullPointerException("the audiodevice and the audio may not be null");
		}
		this.mMusicPlayer = pMusicPlayer;
		this.mAudio = pAudio;
		this.mAudioDevice = pAudioDevice;
		this.mJoinLock = new ReentrantLock(true);
		this.mJoinCondition = this.mJoinLock.newCondition();
		this.mPlayerRunnableListener = pPlayerRunnableListener;
		this.mPrematureStop = false;
		this.mAlreadyStarted = false;
		this.mStopped = false;
		this.mDone = false;
		this.mPause = new Pause();
	}

	/**
	 * @throws IOException
	 *             if cleanup fails after finished with loading
	 */
	@Override
	public void run() {
		this.mJoinLock.lock();
		try {
			MusicPlayerException exception = null;
			boolean failure = false;
			try {
				if(this.mAlreadyStarted) {
					throw new IllegalStateException("has alredy been started once!");
				}
				this.mAlreadyStarted = true;
				int bytesRead = 0;
				Audio audio = this.mAudio;
				AudioDevice dev = this.mAudioDevice;
				AudioFormat format = audio.getAudioFormat();
				if(format != null) {
					int bufferSize = (int) format.getSampleRate() *
							format.getFrameSize();
					byte[] data = new byte[bufferSize];
					while(bytesRead != -1) {
						this.mPause.probe();
						if(!this.mStopped) {
							bytesRead = audio.read(data, 0, bufferSize);
							if(bytesRead != -1) {
								dev.write(data, 0, bytesRead);
							}
						} else {
							break;
						}
					}
				} else {
					throw new IllegalStateException("The AudioFormat was null");
				}
			} catch(Exception e) {
				//catch all the exceptions so the user can handle them even if he can do that only via the listener
				failure = true;
				exception = new MusicPlayerException("An Exception occured during Playback",
						e);
				this.mPlayerRunnableListener
						.onException(new MusicExceptionEvent(this.mMusicPlayer,
								exception));
			} finally {
				MusicEndEvent.Type type = failure ? MusicEndEvent.Type.FAILURE
						: MusicEndEvent.Type.SUCCESS;
				if(this.mStopped) {
					type = MusicEndEvent.Type.MANUALLY_STOPPED;
				}
				this.mStopped = true;
				final MusicEndEvent.Type finalType = type;
				StreamPlayerRunnable.this.mPlayerRunnableListener
						.onEnd(new MusicEndEvent(StreamPlayerRunnable.this.mMusicPlayer,
								finalType));
				this.mJoinCondition.signal();
				this.mDone = true;
			}
		} finally {
			this.mJoinLock.unlock();
		}
	}

	public void seek(long pFrame) throws AudioDeviceException {
		if(!(this.mAudio instanceof SeekableAudio)) {
			throw new UnsupportedOperationException("seeking is not possible on the current AudioFile");
		}
		if(this.mDone || this.mStopped) {
			throw new IllegalStateException("can't seek if stopped or already done with playing.");
		}
		boolean pause = this.mPause.isPaused();
		try {
			this.pause(true);
			((SeekableAudio) this.mAudio).seek(pFrame);
		} catch(AudioException e) {
			throw new AudioDeviceException("couldn't seek with the current audio");
		} finally {
			this.pause(pause);
		}
	}

	public void skip(long pFrames) throws AudioDeviceException {
		if(!(this.mAudio instanceof SeekableAudio)) {
			throw new UnsupportedOperationException("skipping is not possible on the current AudioFile");
		}
		if(this.mDone || this.mStopped) {
			throw new IllegalStateException("can't skip if stopped or already done with playing.");
		}
		boolean pause = this.mPause.isPaused();
		try {
			this.pause(true);
			((SeekableAudio) this.mAudio).skip(pFrames);
		} catch(AudioException e) {
			throw new AudioDeviceException("couldn't skip with the current audio");
		} finally {
			this.pause(pause);
		}
	}

	public boolean canSeek() {
		return this.mAudio instanceof SeekableAudio &&
				((SeekableAudio) this.mAudio).canSeek();
	}

	public void pause(boolean pPause) {
		this.mPause.pause(pPause);
		this.mAudioDevice.pause(pPause);
	}

	public boolean isPaused() {
		return this.mPause.isPaused();
	}

	public boolean isStopped() {
		return this.mStopped;
	}
	
	public boolean isDone() {
		return this.mDone;
	}

	public boolean isAlreadyStarted() {
		return this.mAlreadyStarted;
	}

	public void stop() throws MusicPlayerException {
		this.mStopped = true;
		this.mAudioDevice.flush();
		this.pause(false);
	}

	public void join() throws InterruptedException {
		this.mJoinLock.lock();
		try {
			if(!this.mDone) {
				this.mJoinCondition.await();
			}
		} finally {
			this.mJoinLock.unlock();
		}
	}
	
	/**
	 * in order to use this together with
	 * stop, you have to call join first to
	 * make sure the playback is completely done.
	 */
	public void reset() {
		if(!this.mDone) {
			throw new IllegalStateException("can only reset if done completely with playing");
		}
		this.mPrematureStop = false;
		this.mAlreadyStarted = false;
		this.mStopped = false;
		this.mDone = false;
	}

	public AudioFormat getAudioFormat() {
		return this.mAudio.getAudioFormat();
	}

	public AudioDevice getAudioDevice() {
		return this.mAudioDevice;
	}

	interface StreamPlayerRunnableListener extends EventListener {

		public void onEnd(MusicEndEvent pEvent);

		public void onException(MusicExceptionEvent pEvent);

	}

}
