/**
 * File BaseAudioDevice.java
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

public abstract class BaseAudioDevice implements IAudioDevice {
	
	protected boolean mPaused;
	protected boolean mClosed;
	protected AudioFormat mAudioFormat;
	
	protected BaseAudioDevice() {
		this.mClosed = true;
		this.mPaused = false;
		this.mAudioFormat = null;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		if(this.mPaused || this.mClosed) {
			throw new IllegalStateException("The Device is either paused, stopped or has never been started yet");
		}
		return 0;
	}
	
	@Override
	public void flush() throws AudioDeviceException {
		if(this.mPaused || this.mClosed) {
			throw new IllegalStateException("The Device is either paused, stopped or has never been started yet");
		}
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		if(!this.mClosed) {
			throw new IllegalStateException("The AudioDevice is already opened");
		}
		this.mAudioFormat = pAudioFormat;
		this.mClosed = false;
	}

	@Override
	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	public void pause(boolean pPause) {
		this.mPaused = pPause;
	}

	@Override
	public void close() throws AudioDeviceException {
		this.mClosed = true;
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

}
