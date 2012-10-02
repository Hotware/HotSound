/**
 * File RecordingAudioDevice.java
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

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public class RecordingAudioDevice implements IAudioDevice {

	protected Recorder mRecorder;
	protected boolean mPaused;
	protected boolean mClosed;

	public RecordingAudioDevice(File pFile) {
		this.mRecorder = new Recorder(pFile);
		this.mClosed = true;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		if(!this.mClosed) {
			throw new IllegalStateException("The AudioDevice is already opened!");
		}
		try {
			this.mRecorder.open(pAudioFormat);
			this.mClosed = false;
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't initialize the Streamwriting process",
					e);
		}
	}
	
	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		if(this.mClosed || this.mPaused) {
			throw new IllegalStateException("The AudioDevice is not opened");
		}
		try {
			this.mRecorder.write(pData, pStart, pLength);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't write to the File", e);
		}
		return pLength;
	}

	@Override
	public void pause() {
		this.mPaused = true;
	}

	@Override
	public void unpause() {
		this.mPaused = false;
	}

	@Override
	public void close() throws AudioDeviceException {
		try {
			this.mClosed = true;
			this.mRecorder.close();
		} catch(IOException e) {
			throw new AudioDeviceException("IOException occured while closing the Recorder", e);
		}
	}

	@Override
	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

}
