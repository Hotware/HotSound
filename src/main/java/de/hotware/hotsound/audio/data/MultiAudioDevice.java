/**
 * File MultiAudioDevice.java
 * ---------------------------------------------------------
 * <p/>
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 * if it breaks something.
 */
package de.hotware.hotsound.audio.data;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.List;

public class MultiAudioDevice implements AudioDevice {

	protected List<AudioDevice> mDevices;

	public MultiAudioDevice(List<AudioDevice> pDevices) {
		if ( pDevices == null ) {
			throw new NullPointerException( "pDevices may not be null" );
		}
		List<AudioDevice> list = new ArrayList<>();
		list.addAll( pDevices );
		this.mDevices = list;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		boolean failed = false;
		int ret = 0;
		for ( AudioDevice dev : this.mDevices ) {
			try {
				ret += dev.write( pData, pStart, pLength );
			}
			catch (Exception e) {
				failed = true;
				//TODO: exception with list
			}
		}
		if ( failed ) {
			throw new AudioDeviceException( "couldn't write to all of the underlying AudioDevices" );
		}
		return ret;
	}

	@Override
	public void flush() {
		for ( AudioDevice dev : this.mDevices ) {
			dev.flush();
		}
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		boolean failed = false;
		for ( AudioDevice dev : this.mDevices ) {
			try {
				dev.open( pAudioFormat );
			}
			catch (AudioDeviceException e) {
				failed = true;
			}
		}
		if ( failed ) {
			throw new AudioDeviceException( "couldn't open all of the underlying AudioDevices" );
		}
	}

	@Override
	public boolean isPaused() {
		boolean ret = true;
		for ( AudioDevice dev : this.mDevices ) {
			ret &= dev.isPaused();
			if ( !ret ) {
				break;
			}
		}
		return ret;
	}

	@Override
	public void pause(boolean pPause) {
		for ( AudioDevice dev : this.mDevices ) {
			dev.pause( pPause );
		}
	}

	@Override
	public void close() throws AudioDeviceException {
		boolean failed = false;
		for ( AudioDevice dev : this.mDevices ) {
			try {
				dev.close();
			}
			catch (AudioDeviceException e) {
				failed = true;
			}
		}
		if ( failed ) {
			throw new AudioDeviceException( "couldn't close all of the underlying AudioDevices" );
		}
	}

	@Override
	public boolean isClosed() {
		boolean ret = true;
		for ( AudioDevice dev : this.mDevices ) {
			ret &= dev.isClosed();
			if ( !ret ) {
				break;
			}
		}
		return ret;
	}

	@Override
	public void reopen(AudioFormat pAudioFormat) throws AudioDeviceException {
		boolean failed = false;
		for ( AudioDevice dev : this.mDevices ) {
			try {
				dev.reopen( pAudioFormat );
			}
			catch (AudioDeviceException e) {
				failed = true;
			}
		}
		if ( failed ) {
			throw new AudioDeviceException( "couldn't open all of the underlying AudioDevices" );
		}
	}

}
