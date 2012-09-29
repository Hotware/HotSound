/**
 * File RecordAudio.java
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

import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import de.hotware.hotsound.audio.util.AudioUtil;

public class RecordAudio implements IAudio {

	protected Mixer mMixer;
	protected AudioFormat mAudioFormat;
	protected TargetDataLine mTargetDataLine;
	protected boolean mClosed;

	public RecordAudio(Mixer pMixer) {
		this(pMixer, null);
	}

	public RecordAudio(Mixer pMixer, AudioFormat pAudioFormat) {
		if(pMixer == null) {
			throw new IllegalArgumentException("pMixer may not be null");
		}
		this.mMixer = pMixer;
		this.mAudioFormat = pAudioFormat;
		this.mClosed = true;
	}

	@Override
	public void open() throws AudioException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Audio is already opened");
		}
		DataLine.Info info = new DataLine.Info(TargetDataLine.class,
				this.mAudioFormat);
		try {
			if(!this.mMixer.isOpen()) {
				this.mMixer.open();
			}
			this.mTargetDataLine = (TargetDataLine) this.mMixer.getLine(info);
			if(this.mAudioFormat != null) {
				this.mTargetDataLine.open(this.mAudioFormat);
			} else {
				this.mAudioFormat = this.mTargetDataLine.getFormat();
				this.mTargetDataLine.open();
			}
			this.mTargetDataLine.start();
		} catch(LineUnavailableException e) {
			throw new AudioException("Error during opening the TargetDataLine");
		}
		this.mClosed = false;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.mAudioFormat;
	}

	@Override
	public int read(byte[] pData, int pStart, int pLength) throws AudioException {
		if(this.mClosed) {
			throw new IllegalStateException("The Audio is not opened");
		}
		return this.mTargetDataLine.read(pData, pStart, pLength);
	}

	@Override
	public void close() {
		this.mTargetDataLine.close();
		this.mClosed = true;
	}

	public static List<Mixer> getRecordMixers() {
		return AudioUtil.getCompatibleMixers(TargetDataLine.class);
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

}