/**
 * File IAudioFile.java
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

import java.io.Closeable;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.player.MusicPlayerException;

public interface IAudioFile extends Closeable {

	/**
	 * @return the AudioFormat of the IAudioFile
	 */
	public AudioFormat getAudioFormat();

	/**
	 * Reads up to a specified maximum number of bytes of data from the audio
	 * stream, putting them into the given byte array. This method will always
	 * read an integral number of frames. If pLength does not specify an integral
	 * number of frames, a maximum of len - (pLength % frameSize) bytes will be
	 * read.
	 */
	public int read(byte[] pData, int pStart, int pBufferSize) throws AudioFileException;
	
	public void open() throws AudioFileException;

	/**
	 * closes the IAudioFiles resources
	 */
	public void close() throws IOException;
	
	public static class AudioFileException extends MusicPlayerException {

		private static final long serialVersionUID = 2153542499704614401L;
		
		public AudioFileException() {
			super();
		}

		public AudioFileException(String pMessage) {
			super(pMessage);
		}

		public AudioFileException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}
		
	}

}
