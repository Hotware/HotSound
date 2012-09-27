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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SavingAudioDevice extends BasicAudioDevice {

	protected ByteArrayOutputStream mByteArrayOutputStream;
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
		this.mByteArrayOutputStream = new ByteArrayOutputStream();
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		int ret = super.write(pData, pStart, pLength);
		this.mByteArrayOutputStream.write(pData, pStart, pLength);
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
			throw new IOException("an UnsupportedAudioFileException occured during writing to the file",
					e);
		} finally {
			try {
				this.mByteArrayOutputStream.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveData() throws IOException, UnsupportedAudioFileException {
		if(this.mFile.exists()) {
			throw new IllegalStateException("File " + this.mFile +
					" already exists");
		}
		AudioFormat audioFormat = this.mSourceDataLine.getFormat();
		byte[] data = this.mByteArrayOutputStream.toByteArray();
		try(ByteArrayInputStream input = new ByteArrayInputStream(data);
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
				AudioInputStream audioInputStream = new AudioInputStream(input,
						audioFormat,
						data.length / audioFormat.getFrameSize());) {
			AudioSystem.write(audioInputStream,
					AudioFileFormat.Type.AIFF,
					outputStream);
			input.close();
			audioInputStream.close();
			outputStream.close();
		}
	}

}
