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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * TODO: audiolength
 * 
 * @author Martin Braun
 * 
 */
public class SavingAudioDevice extends BasicAudioDevice {

	protected BufferedOutputStream mBufferedOutputStream;
	protected IHeader mHeader;
	protected File mFile;

	public SavingAudioDevice(File pFile) {
		this(pFile, null);
	}

	protected SavingAudioDevice(File pFile, Mixer pMixer) {
		super(pMixer);
		if(pFile.exists()) {
			throw new IllegalArgumentException("File " + pFile +
					" already exists!");
		}
		this.mFile = pFile;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		super.open(pAudioFormat);
		if(this.mFile.exists()) {
			throw new IllegalStateException("File " + this.mFile +
					" already exists!");
		}
		try {
			this.mFile.createNewFile();
			this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
			AudioFormat format = this.mSourceDataLine.getFormat();
			this.mHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
					(short) format.getChannels(),
					(int) format.getSampleRate(),
					(short) format.getSampleSizeInBits(),
					-1);
			this.mHeader.write(this.mBufferedOutputStream);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't initialize the Streamwriting process",
					e);
		}
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		int ret = super.write(pData, pStart, pLength);
		try {
			this.mBufferedOutputStream.write(pData, pStart, pLength);
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
		try {
			this.saveData();
		} catch(UnsupportedAudioFileException e) {
			throw new IOException("an UnsupportedAudioFileException occured while writing to the file",
					e);
		} finally {
			try {
				this.mBufferedOutputStream.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveData() throws IOException, UnsupportedAudioFileException {
		try {
		} finally {
			this.mBufferedOutputStream.close();
		}
	}

}
