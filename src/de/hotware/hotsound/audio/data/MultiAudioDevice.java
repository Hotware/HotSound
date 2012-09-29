package de.hotware.hotsound.audio.data;

import java.util.List;

import javax.sound.sampled.AudioFormat;

public class MultiAudioDevice implements IAudioDevice {

	protected List<IAudioDevice> mDevices;

	public MultiAudioDevice(List<IAudioDevice> pDevices) {
		if(pDevices == null) {
			throw new IllegalArgumentException("pDevices may not be null");
		}
		this.mDevices = pDevices;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		boolean failed = false;
		for(IAudioDevice dev : this.mDevices) {
			try {
				dev.write(pData, pStart, pLength);
			} catch(AudioDeviceException e) {
				failed = true;
			}
		}
		if(failed) {
			throw new AudioDeviceException("couldn't write to all of the underlying AudioDevices");
		}
		return pLength;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		boolean failed = false;
		for(IAudioDevice dev : this.mDevices) {
			try {
				dev.open(pAudioFormat);
			} catch(AudioDeviceException e) {
				failed = true;
			}
		}
		if(failed) {
			throw new AudioDeviceException("couldn't open all of the underlying AudioDevices");
		}
	}

	@Override
	public boolean isPaused() {
		boolean ret = true;
		for(IAudioDevice dev : this.mDevices) {
			ret &= dev.isPaused();
			if(!ret) {
				break;
			}
		}
		return ret;
	}

	@Override
	public void pause() {
		for(IAudioDevice dev : this.mDevices) {
			dev.pause();
		}
	}

	@Override
	public void unpause() {
		for(IAudioDevice dev : this.mDevices) {
			dev.unpause();
		}
	}

	@Override
	public void close() throws AudioDeviceException {
		boolean failed = false;
		for(IAudioDevice dev : this.mDevices) {
			try {
				dev.close();
			} catch(AudioDeviceException e) {
				failed = true;
			}
		}
		if(failed) {
			throw new AudioDeviceException("couldn't close all of the underlying AudioDevices");
		}
	}

	@Override
	public boolean isClosed() {
		boolean ret = true;
		for(IAudioDevice dev : this.mDevices) {
			ret &= dev.isClosed();
			if(!ret) {
				break;
			}
		}
		return ret;
	}

}
