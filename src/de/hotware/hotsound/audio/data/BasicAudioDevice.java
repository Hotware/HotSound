/**
 * File BasicAudioDevice.java
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
package de.hotware.hotsound.audio.data;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

public class BasicAudioDevice implements IAudioDevice {

	protected Mixer mMixer;
	protected SourceDataLine mSourceDataLine;
	protected int mRecommendedBufferSize;
	protected boolean mPaused;
	protected boolean mStopped;

	public BasicAudioDevice() {
		this(null);
	}

	protected BasicAudioDevice(Mixer pMixer) {
		this.mMixer = pMixer;
		this.mRecommendedBufferSize = AudioSystem.NOT_SPECIFIED;
		this.mStopped = true;
		this.mPaused = false;
	}
	
	@Override
	public void start(AudioFormat pAudioFormat) throws LineUnavailableException {
		if(!this.mStopped) {
			throw new IllegalStateException("The AudioDevice is not stopped");
		}
		DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class,
				pAudioFormat,
				this.mRecommendedBufferSize);
		if(this.mMixer == null) {
			this.mSourceDataLine = (SourceDataLine) AudioSystem
					.getLine(dataLineInfo);
		} else {
			this.mSourceDataLine = (SourceDataLine) this.mMixer
					.getLine(dataLineInfo);
		}
		this.mSourceDataLine.open(pAudioFormat);
		this.mSourceDataLine.start();
		this.mStopped = false;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) {
		if(this.mPaused || this.mStopped) {
			throw new IllegalStateException("The Device is either paused, stopped or has never been started yet");
		}
		return this.mSourceDataLine.write(pData, pStart, pLength);
	}
	
	@Override
	public void setMixer(Mixer pMixer) {
		if(!this.mStopped) {
			throw new IllegalStateException("can't set the mixer if the AudioDevice is not stopped");
		}
		this.mMixer = pMixer;
	}

	@Override
	public void pause() {
		if(this.mPaused) {
			throw new IllegalStateException("The AudioDevice is already paused");
		}
		this.mSourceDataLine.stop();
		this.mPaused = true;
	}

	@Override
	public void unpause() {
		if(this.mPaused) {
			throw new IllegalStateException("The AudioDevice is not paused");
		}
		this.mSourceDataLine.start();
		this.mPaused = false;
	}

	@Override
	public void stop() {
		this.mSourceDataLine.stop();
		this.mSourceDataLine.flush();
		this.mStopped = true;
	}

	@Override
	public DataLine getDataLine() {
		return this.mSourceDataLine;
	}

}