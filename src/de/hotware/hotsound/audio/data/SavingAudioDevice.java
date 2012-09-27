/**
 * File SavingAudioDevice.java
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
import javax.sound.sampled.Mixer;

/**
 * TODO: audiolength
 * 
 * @author Martin Braun
 * 
 */
public class SavingAudioDevice extends BasicAudioDevice {

	protected Recorder mRecorder;

	public SavingAudioDevice(File pFile) {
		this(pFile, null);
	}

	public SavingAudioDevice(File pFile, Mixer pMixer) {
		super(pMixer);
		this.mRecorder = new Recorder(pFile);
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		super.open(pAudioFormat);
		try {
			this.mRecorder.open(pAudioFormat);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't initialize the Streamwriting process",
					e);
		}
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		int ret = super.write(pData, pStart, pLength);
		try {
			this.mRecorder.write(pData, pStart, pLength);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't write to the File", e);
		}
		return ret;
	}

	/**
	 * @throws AudioDeviceException
	 * @inheritDoc saves all the data
	 */
	@Override
	public void close() throws IOException {
		super.close();
		this.mRecorder.close();
	}

}
