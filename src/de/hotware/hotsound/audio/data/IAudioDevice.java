/**
 * File IAudioDevice.java
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

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;

import de.hotware.hotsound.audio.player.MusicPlayerException;

public interface IAudioDevice extends AutoCloseable {

	/**
	 * writes to the IAudioDevice's output. Normally the way to write to its
	 * DataLine
	 * 
	 * @return number of bytes written
	 */
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException;

	/**
	 * starts and initializes the IAudioDevice for playback
	 * @throws IOException 
	 */
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException;
	
	public void setMixer(Mixer pMixer);

	/**
	 * pauses the IAudioDevice and the playback
	 */
	public void pause();

	/**
	 * unpauses the IAudioDevice and the playback
	 */
	public void unpause();

	/**
	 * stops the IAudioDevice and closes all the opened resources
	 * @throws AudioDeviceException 
	 */
	public void close() throws IOException;

	/**
	 * @return the DataLine to which is being written (with that you can control
	 *         the volume, etc.)
	 */
	public DataLine getDataLine();
	
	public static class AudioDeviceException extends MusicPlayerException {

		private static final long serialVersionUID = 2153542499704614401L;
		
		public AudioDeviceException() {
			super();
		}

		public AudioDeviceException(String pMessage) {
			super(pMessage);
		}

		public AudioDeviceException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}
		
	}

}
