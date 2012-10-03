package de.hotware.hotsound.audio.data;

import javax.sound.sampled.AudioFormat;


public abstract class BaseAudioDevice implements IAudioDevice {
	
	protected boolean mPaused;
	protected boolean mClosed;
	protected AudioFormat mAudioFormat;
	
	protected BaseAudioDevice() {
		this.mClosed = true;
		this.mPaused = false;
		this.mAudioFormat = null;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		if(this.mPaused || this.mClosed) {
			throw new IllegalStateException("The Device is either paused, stopped or has never been started yet");
		}
		return 0;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		if(!this.mClosed) {
			throw new IllegalStateException("The AudioDevice is already opened");
		}
		this.mAudioFormat = pAudioFormat;
		this.mClosed = false;
	}

	@Override
	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	public void pause(boolean pPause) {
		this.mPaused = pPause;
	}

	@Override
	public void close() throws AudioDeviceException {
		this.mClosed = true;
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

}
