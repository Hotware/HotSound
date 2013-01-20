/**
 * File Recorder.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

class Recorder implements AutoCloseable {

	/**
	 * the size of the internally used buffer
	 * while writing the file in the end 
	 * of the writing process (in close())
	 */
	private static final int BUFFER_SIZE = 128000;
	
	private BufferedOutputStream mBufferedOutputStream;
	private File mFile;
	private File mTempFile;
	private AudioFileHeader mHeader;
	private int mBytesWritten;
	private boolean mClosed;

	public Recorder(File pFile) {
		if(pFile == null) {
			throw new NullPointerException("pFile may not be null");
		}
		this.mFile = pFile;
		this.mTempFile = new File(pFile.getAbsolutePath() + ".tmp");
		this.mBytesWritten = 0;
		this.mClosed = true;
	}

	/**
	 *
	 * @throws IllegalStateException if opened while not being closed;
	 */
	public void open(AudioFormat pAudioFormat) throws IOException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Recorder is already opened");
		}
		createEmptyFile(this.mTempFile);
		this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mTempFile));
		this.mHeader = new WaveFileHeader(WaveFileHeader.FORMAT_PCM,
				(short) pAudioFormat.getChannels(),
				(int) pAudioFormat.getSampleRate(),
				(short) pAudioFormat.getSampleSizeInBits(),
				Integer.MAX_VALUE);
		this.mHeader.write(this.mBufferedOutputStream);
		this.mClosed = false;
	}

	public int write(byte[] pData, int pStart, int pLength) throws IOException {
		if(this.mClosed) {
			throw new IllegalStateException("The Recorder is not open");
		}
		this.mBufferedOutputStream.write(pData, pStart, pLength);
		this.mBytesWritten += pLength;
		return pLength;
	}

	public void close() throws IOException {
		if(this.mBufferedOutputStream != null) {
			boolean delete = false;
			try(BufferedInputStream input = new BufferedInputStream(new FileInputStream(this.mTempFile))) {
				this.mBufferedOutputStream.flush();
				this.mBufferedOutputStream.close();
				((WaveFileHeader) this.mHeader).read(input);
				((WaveFileHeader) this.mHeader).setNumBytes(this.mBytesWritten);
				createEmptyFile(this.mFile);
				delete = true;
				this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
				int bytesRead = 0;
				byte[] data = new byte[BUFFER_SIZE];
				this.mHeader.write(this.mBufferedOutputStream);
				while((bytesRead = input.read(data, 0, BUFFER_SIZE)) != -1) {
					this.write(data, 0, bytesRead);
				}
			} catch(IOException e) {
				//only delete if a new file has been written over a possible old file
				if(delete) {
					if(!this.mTempFile.delete()) {
						throw new IOException("couldn't delete failure file", e);
					}
				}
				throw e;
			} finally {
				this.mClosed = true;
				// No need to flush, is included in close().
				try {
					this.mBufferedOutputStream.close();
					this.mBufferedOutputStream = null;
				} finally {
					if(!this.mTempFile.delete()) {
						throw new IOException("couldn't delete the tempfile");
					}
				}
			}
		}
	}
	
	private static void createEmptyFile(File pFile) throws IOException {
		if(pFile.exists()) {
			if(!pFile.delete()) {
				throw new IOException("couldn't delete the old file " + pFile.getAbsolutePath());
			}
		}
		if(!pFile.createNewFile()) {
			throw new IOException("couldn't create the file " + pFile.getAbsolutePath());
		}
	}

}
