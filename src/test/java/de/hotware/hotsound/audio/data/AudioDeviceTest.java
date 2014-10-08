/**
 * File AudioDeviceTest.java
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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import de.hotware.hotsound.audio.data.BasicPlaybackAudioDevice;
import de.hotware.hotsound.audio.data.AudioDevice;
import de.hotware.hotsound.audio.data.AudioDevice.AudioDeviceException;
import de.hotware.hotsound.audio.data.RecordingAudioDevice;

public class AudioDeviceTest {
	
	@Test
	public void testAudioDevices() {
		this.testAudioDevice(new BasicPlaybackAudioDevice());
		this.testAudioDevice(new RecordingAudioDevice(new File("test")));
	}
	
	public void testAudioDevice(AudioDevice pAudioDevice) {
		//TODO: Testing for opened AudioDevices
		AudioDevice dev = new BasicPlaybackAudioDevice();
		try {
			dev.pause(true);
			if(!dev.isPaused()) {
				fail("device wasn't paused even though pause(true) was called");
			}
			dev.pause(false);
			if(dev.isPaused()) {
				fail("device was paused even though pause(false) was called");
			}
		} catch(Exception e) {
			fail("Exception occured during pausing");;
		}
		try {
			dev.open(null);
			fail("IAudioDevices shouldn't accept null as a AudioFormat value");
		} catch (NullPointerException e) {
			byte[] bytes = new byte[128];
			try {
				dev.write(bytes, 0, 128);
			} catch(AudioDeviceException e1) {
				fail("No AudioDeviceException should occur if writing to a Device that couldn't be opened");
			} catch(IllegalStateException e1) {
				
			}
			try {
				dev.flush();
			} catch(RuntimeException e1) {
				fail("No Exceptions should occur if writing to a device that couldn't be opened, or isn't open");
			}
		} catch (AudioDeviceException e) {
			fail("No AudioDeviceException should occur if null is passed to dev.open");
		}
		try {
			dev.close();
			if(!dev.isClosed()) {
				fail("The AudioDevice wasn't closed after closing");
			}
		} catch(AudioDeviceException e) {
			
		} catch(RuntimeException e) {
			fail("No RuntimeExceptions should occur if closing an audiodevice");
		}
	}

}
