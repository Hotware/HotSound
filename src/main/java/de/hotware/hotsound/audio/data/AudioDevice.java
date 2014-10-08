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

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.player.MusicPlayerException;

/**
 * Base interface for all classes that receive all the Sound input and plays
 * it back or whatever it should do with it. Has to be reopenable.
 *
 * @author Martin Braun
 */
public interface AudioDevice extends AutoCloseable {

	/**
	 * writes to the IAudioDevice's output. Normally the way to write to its
	 * DataLine
	 *
	 * @throws IllegalStateException
	 *             if not opened yet
	 * @throws AudioDeviceException if writing fails
	 * @return number of bytes written
	 */
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException;

	/**
	 * opens and initializes the AudioDevice for playback
	 *
	 * @throws NullPointerException
	 *				if null is passed as a parameter
	 * @throws IllegalStateException
	 *             if opened while not being closed
	 * @throws AudioDeviceException if opening fails   
	 */
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException;
	
	/**
	 * reopens the AudioDevice with the given AudioFormat.
	 * if the current AudioFormat is the same as the passed one
	 * and the AudioDevice is already opened, nothing should be done.
	 * However, if the AudioFormat is different, every resource that
	 * cannot be reused is closed and all actions needed to act as
	 * a newly opened AudioDevice are done.
	 * 
	 * @throws IllegalStateException if called and the AudioDevice is closed
	 */
	public void reopen(AudioFormat pAudioFormat) throws AudioDeviceException;

	public boolean isPaused();

	/**
	 * pauses/unpauses the IAudioDevice and the playback
	 */
	public void pause(boolean pPause);

	/**
	 * stops the IAudioDevice and closes all the opened resources
	 * may be reused afterwards!
	 * additionally flushes the device before it is being closed
	 */
	@Override
	public void close() throws AudioDeviceException;
	
	/**
	 * discards all audio that is currently in the buffer
	 */
	public void flush();

	public boolean isClosed();

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
