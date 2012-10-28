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
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import de.hotware.hotsound.audio.util.AudioUtil;

public class RecordAudio extends BaseAudio {

	protected Mixer mMixer;
	protected TargetDataLine mTargetDataLine;
	protected Class<? extends TargetDataLine> mTargetDataLineClass;
	protected int mBufferSize;

	public RecordAudio(Mixer pMixer) {
		this(pMixer, null);
	}

	public RecordAudio(Mixer pMixer, AudioFormat pAudioFormat) {
		this(pMixer, pAudioFormat, AudioSystem.NOT_SPECIFIED);
	}
	
	/**
	 * @param pBufferSize
	 *            the buffer size you want to use. this is just a hint. it may
	 *            not be used
	 */
	public RecordAudio(Mixer pMixer, AudioFormat pAudioFormat, int pBufferSize) {
		this(pMixer, pAudioFormat, pBufferSize, TargetDataLine.class);
	}
	
	
	public RecordAudio(Mixer pMixer, AudioFormat pAudioFormat, int pBufferSize, Class<? extends TargetDataLine> pTargetDataLineClass) {
		super();
		if(pMixer == null) {
			throw new NullPointerException("pMixer may not be null");
		}
		this.mMixer = pMixer;
		this.mAudioFormat = pAudioFormat;
		this.mClosed = true;
		this.mBufferSize = pBufferSize;
		this.mTargetDataLineClass = pTargetDataLineClass;
	}

	@Override
	public void open() throws AudioException {
		super.open();
		DataLine.Info info = new DataLine.Info(this.mTargetDataLineClass,
				this.mAudioFormat);
		try {
			if(!this.mMixer.isOpen()) {
				this.mMixer.open();
			}
			this.mTargetDataLine = (TargetDataLine) this.mMixer.getLine(info);
			if(this.mAudioFormat == null) {
				this.mAudioFormat = this.mTargetDataLine.getFormat();
			}
			this.mTargetDataLine.open(this.mAudioFormat, this.mBufferSize);
			this.mTargetDataLine.start();
		} catch(LineUnavailableException e) {
			this.mClosed = true;
			throw new AudioException("Error during opening the TargetDataLine");
		}
	}

	@Override
	public int read(byte[] pData, int pStart, int pLength) throws AudioException {
		super.read(pData, pStart, pLength);
		return this.mTargetDataLine.read(pData, pStart, pLength);
	}

	@Override
	public void close() throws AudioException {
		super.close();
		this.mTargetDataLine.close();
	}

	public static List<Mixer> getRecordMixers() {
		return AudioUtil.getCompatibleMixers(TargetDataLine.class);
	}

	@Override
	public long getFrameSize() {
		return this.mAudioFormat.getFrameSize();
	}

	@Override
	public float getFrameRate() {
		return this.mAudioFormat.getFrameRate();
	}

}