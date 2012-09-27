/**
 * File BasicAudioFile.java
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

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.hotware.hotsound.audio.util.AudioUtil;

public class BasicAudioFile implements ISeekableAudioFile {

	protected InputStream mInputStream;
	protected AudioInputStream mAudioInputStream;
	protected boolean mStopped;

	public BasicAudioFile(InputStream pInputStream) {
		this.mInputStream = pInputStream;
		this.mStopped = true;
	}

	@Override
	public void open() throws AudioFileException {
		if(!this.mStopped) {
			throw new IllegalStateException("The AudioFile is already opened");
		}
		try {
			this.mAudioInputStream = AudioUtil
					.getSupportedAudioInputStreamFromInputStream(this.mInputStream);
		} catch(UnsupportedAudioFileException | IOException e) {
			throw new AudioFileException("Error while opening the audiostream", e);
		}
		this.mStopped = false;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.mAudioInputStream.getFormat();
	}

	@Override
	public int read(byte[] pData, int pStart, int pBufferSize) throws AudioFileException {
		if(this.mStopped) {
			throw new IllegalStateException("The AudioFile is not opened");
		}
		try {
			return this.mAudioInputStream.read(pData, pStart, pBufferSize);
		} catch(IOException e) {
			throw new AudioFileException("IOException while reading from the AudioInputStream");
		}
	}

	@Override
	public void close() throws IOException {
		if(this.mStopped) {
			throw new IllegalStateException("The AudioFile is not opened");
		}
		this.mAudioInputStream.close();
	}

	@Override
	public void seek(int pFrame) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	@Override
	public void skip(int pFrames) {
		throw new UnsupportedOperationException("not implemented yet");
	}

}
