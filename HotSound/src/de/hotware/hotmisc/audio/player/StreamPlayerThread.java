/**
 * File StreamPlayerThread.java
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
package de.hotware.hotmisc.audio.player;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.hotware.hotmisc.audio.player.StreamPlayerThread.IPlayerThreadListener.PlaybackEndEvent;

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
public class StreamPlayerThread extends Thread {

	protected Lock mLock;
	protected AudioInputStream mAudioInputStream;
	protected SourceDataLine mSourceDataLine;
	protected AudioFormat mAudioFormat;
	protected DataLine.Info mDataLineInfo;
	protected boolean mPause;
	protected boolean mStop;
	protected IPlayerThreadListener mPlayerThreadListener;

	public StreamPlayerThread(ISong pSong,
			IPlayerThreadListener pPlayerThreadListener) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this.insert(pSong);
		this.mPause = false;
		this.mStop = true;
		this.mLock = new ReentrantLock();
		this.mPlayerThreadListener = pPlayerThreadListener;
	}

	public StreamPlayerThread(ISong pSong) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this(pSong, null);
	}

	private void insert(ISong pSong) throws UnsupportedAudioFileException,
			IOException,
			LineUnavailableException {
		this.mAudioInputStream = AudioSystem.getAudioInputStream(pSong
				.getInputStream());
		AudioFormat format = this.mAudioInputStream.getFormat();
		if(format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
			float sampleRate = format.getSampleRate();
			int channels = format.getChannels();
			AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					sampleRate,
					16,
					channels,
					channels * 2,
					sampleRate,
					false);
			AudioInputStream newStream = AudioSystem
					.getAudioInputStream(newFormat, this.mAudioInputStream);
			format = newFormat;
			this.mAudioInputStream = newStream;
		}
		this.mAudioFormat = this.mAudioInputStream.getFormat();
		this.mDataLineInfo = new DataLine.Info(SourceDataLine.class,
				this.mAudioFormat);
		this.mSourceDataLine = (SourceDataLine) AudioSystem
				.getLine(this.mDataLineInfo);
		this.mSourceDataLine.open(this.mAudioFormat);
		this.mSourceDataLine.start();
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
		} finally {
			this.mLock.unlock();
		}
	}

	public boolean isStopped() {
		this.mLock.lock();
		boolean ret;
		try {
			ret = this.mStop;
		} finally {
			this.mLock.unlock();
		}
		return ret;
	}

	public boolean isPaused() {
		boolean newLocked = this.mLock.tryLock();
		if(newLocked) {
			this.mLock.unlock();
		}
		return newLocked;
	}

	public AudioFormat getAudioFormat() {
		return this.mAudioFormat;
	}

	public Control[] getControls() {
		return this.mSourceDataLine.getControls();
	}

	public Control getControl(Control.Type pType) {
		return this.mSourceDataLine.getControl(pType);
	}

	private void cleanUp() throws IOException {
		if(this.mSourceDataLine != null) {
			this.mSourceDataLine.drain();
			this.mSourceDataLine.stop();
			this.mSourceDataLine.close();
			this.mAudioInputStream.close();
		}
	}
	
	public static interface IPlayerThreadListener {

		public void onEnd(PlaybackEndEvent pEvent);
		
		public static class PlaybackEndEvent extends GBaseEvent<StreamPlayerThread> {

			public PlaybackEndEvent(StreamPlayerThread pSource) {
				super(pSource);
			}
			
		}
		
	}

}