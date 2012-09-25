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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.hotware.hotsound.audio.player.IPlaybackListener.PlaybackEndEvent;


/**
 * all playback functions are thread-safe. Player inspired by Matthias
 * Pfisterer's examples on JavaSound (jsresources.org). Because of the fact,
 * that this Software is meant to be Open-Source and I don't want to get anybody
 * angry about me using parts of his intelligence without mentioning it, I
 * hereby mention him as inspiration, because his code helped me to write this
 * class.
 * 
 * @author Martin Braun
 */
public class StreamPlayerRunnable implements Runnable {

	protected Lock mLock;
	protected AudioInputStream mAudioInputStream;
	protected SourceDataLine mSourceDataLine;
	protected AudioFormat mAudioFormat;
	protected boolean mPause;
	protected boolean mStop;
	protected IPlaybackListener mPlayerThreadListener;

	/**
	 * initializes the StreamPlayerRunnable without a
	 * {@link #PlayerThreadListener} and the default Mixer
	 */
	public StreamPlayerRunnable(ISong pSong) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this(pSong, null);
	}

	/**
	 * initializes the StreamPlayerRunnable with the given
	 * {@link #PlayerThreadListener} and the default Mixer
	 */
	public StreamPlayerRunnable(ISong pSong,
			IPlaybackListener pPlayerThreadListener) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this(pSong, pPlayerThreadListener, null);
	}

	/**
	 * initializes the StreamPlayerRunnable with the given
	 * {@link #PlayerThreadListener} and the given Mixer
	 */
	public StreamPlayerRunnable(ISong pSong,
			IPlaybackListener pPlayerThreadListener,
			Mixer pMixer) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this.mAudioInputStream = AudioUtil.getAudioInputStreamFromSong(pSong);
		this.mAudioFormat = this.mAudioInputStream.getFormat();
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
				this.mAudioFormat,
				pSong.getInternalBufferSize());
		if(pMixer == null) {
			this.mSourceDataLine = (SourceDataLine) AudioSystem
					.getLine(dataLineInfo);
		} else {
			this.mSourceDataLine = (SourceDataLine) pMixer
					.getLine(dataLineInfo);
		}
		this.mSourceDataLine.open(this.mAudioFormat);
		this.mSourceDataLine.start();
		this.mPause = false;
		this.mStop = true;
		this.mLock = new ReentrantLock();
		this.mPlayerThreadListener = pPlayerThreadListener;
	}

	@Override
	public void run() {
		this.mStop = false;
		int nBytesRead = 0;
		int bufferSize = (int) this.mAudioFormat.getSampleRate() *
				this.mAudioFormat.getFrameSize();
		byte[] abData = new byte[bufferSize];
		this.mLock.lock();
		try {
			while(nBytesRead != -1 && !this.mStop &&
					this.mSourceDataLine != null) {
				this.mLock.unlock();
				try {
					nBytesRead = this.mAudioInputStream.read(abData,
							0,
							bufferSize);
				} catch(IOException e) {
					nBytesRead = -1;
				}
				if(nBytesRead != -1) {
					this.mSourceDataLine.write(abData, 0, nBytesRead);
				}
				this.mLock.lock();
			}
		} finally {
			this.mLock.unlock();
		}
		try {
			this.cleanUp();
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.mStop = true;
		if(this.mPlayerThreadListener != null) {
			this.mPlayerThreadListener.onEnd(new PlaybackEndEvent(this));
		}
	}

	public void pausePlayback() {
		if(this.mPause) {
			throw new IllegalStateException("Player is already paused!");
		}
		this.mLock.lock();
		this.mPause = true;
	}

	public void unpausePlayback() {
		if(!this.mPause) {
			throw new IllegalStateException("Player is not paused!");
		}
		this.mPause = false;
		this.mLock.unlock();
	}

	public void stopPlayback() {
		this.mLock.lock();
		try {
			this.mStop = true;
			this.mSourceDataLine.flush();
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
			return this.mAudioFormat;
		} finally {
			this.mLock.unlock();
		}
	}

	public DataLine getDataLine() {
		this.mLock.lock();
		try {
			return this.mSourceDataLine;
		} finally {
			this.mLock.unlock();
		}
	}

	private void cleanUp() throws IOException {
		if(this.mSourceDataLine != null) {
			this.mSourceDataLine.drain();
			this.mSourceDataLine.stop();
			this.mSourceDataLine.close();
			this.mAudioInputStream.close();
		}
	}

}