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
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudioDevice;
import de.hotware.hotsound.audio.data.IAudioDevice.AudioDeviceException;
import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.ISeekableAudio;
import de.hotware.hotsound.audio.player.IMusicListener.MusicEvent;

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
 * @author Martin Braun
 */
public class StreamPlayerCallable implements Callable<Void> {

	protected Lock mLock;
	protected boolean mPause;
	protected boolean mStop;
	protected IMusicListener mMusicListener;
	protected IAudio mAudio;
	protected IAudioDevice mAudioDevice;

	/**
	 * initializes the StreamPlayerRunnable without a
	 * {@link #PlayerThreadListener} and the default Mixer
	 * 
	 * @throws AudioDeviceException
	 */
	public StreamPlayerCallable(IAudio pAudio, IAudioDevice pAudioDevice) {
		this(pAudio, pAudioDevice, null);
	}

	/**
	 * initializes the StreamPlayerRunnable with the given
	 * {@link #PlayerThreadListener} and the given Mixer
	 * 
	 * @throws AudioDeviceException
	 */
	public StreamPlayerCallable(IAudio pAudio,
			IAudioDevice pAudioDevice,
			IMusicListener pMusicListener) {
		if(pAudioDevice == null || pAudio == null) {
			throw new IllegalArgumentException("the audiodevice and the audio may not be null");
		}
		this.mAudio = pAudio;
		this.mAudioDevice = pAudioDevice;
		this.mPause = false;
		this.mStop = true;
		this.mLock = new ReentrantLock();
		this.mMusicListener = pMusicListener;
	}

	/**
	 * @inheritDoc
	 * 
	 * @throws IOException
	 *             if cleanup fails after finished with loading
	 */
	@Override
	public Void call() throws MusicPlayerException {
		this.mStop = false;
		int nBytesRead = 0;
		MusicPlayerException exception = null;
		boolean failure = false;
		this.mLock.lock();
		try(IAudioDevice dev = this.mAudioDevice; IAudio audio = this.mAudio;) {
			audio.open();
			dev.open(audio.getAudioFormat());
			AudioFormat format = audio.getAudioFormat();
			int bufferSize = (int) format.getSampleRate() *
					format.getFrameSize();
			byte[] abData = new byte[bufferSize];
			while(nBytesRead != -1 && !this.mStop) {
				this.mLock.unlock();
				nBytesRead = audio.read(abData, 0, bufferSize);
				if(nBytesRead != -1) {
					dev.write(abData, 0, nBytesRead);
				}
				this.mLock.lock();
			}
		} catch(MusicPlayerException e) {
			failure = true;
			exception = e;
			throw exception;
		} catch(IOException e) {
			failure = true;
			exception = new MusicPlayerException("an IOException occured during closing the Musicplayers' resources",
					e);
			throw exception;
		} finally {
			this.mLock.unlock();
			this.mStop = true;
			if(this.mMusicListener != null) {
				this.mMusicListener.onEnd(new MusicEvent(this,
						failure ? MusicEvent.Type.FAILURE
								: MusicEvent.Type.SUCCESS, exception));
			}
		}
		return null;
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

	public void pausePlayback() {
		if(this.mPause) {
			throw new IllegalStateException("Player is already paused!");
		}
		this.mLock.lock();
		this.mAudioDevice.pause();
		this.mPause = true;
	}

	public void unpausePlayback() {
		if(!this.mPause) {
			throw new IllegalStateException("Player is not paused!");
		}
		this.mPause = false;
		this.mAudioDevice.unpause();
		this.mLock.unlock();
	}

	public void stopPlayback() throws MusicPlayerException {
		this.mLock.lock();
		try {
			if(!this.mStop) {
				try {
					this.mAudioDevice.close();
				} catch(IOException e) {
					throw new MusicPlayerException("the AudioDevice couldn't be closed",
							e);
				}
			}
			this.mStop = true;
		} finally {
			this.mLock.unlock();
		}
	}

	public boolean isStopped() {
		this.mLock.lock();
		try {
			return this.mStop;
		} finally {
			this.mLock.unlock();
		}
	}

	public boolean isPaused() {
		boolean newLocked = false;
		try {
			return newLocked = this.mLock.tryLock();
		} finally {
			if(newLocked) {
				this.mLock.unlock();
			}
		}
	}

	public AudioFormat getAudioFormat() {
		this.mLock.lock();
		try {
			return this.mAudio.getAudioFormat();
		} finally {
			this.mLock.unlock();
		}
	}

	public IAudioDevice getAudioDevice() {
		this.mLock.lock();
		try {
			return this.mAudioDevice;
		} finally {
			this.mLock.unlock();
		}
	}

}