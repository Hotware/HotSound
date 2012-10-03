package de.hotware.hotsound.audio.data;

import javax.sound.sampled.AudioFormat;


public abstract class BaseAudio implements IAudio {

	protected boolean mClosed;
	protected AudioFormat mAudioFormat;
	
	protected BaseAudio() {
		this.mClosed = true;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.mAudioFormat;
	}

	@Override
	public int read(byte[] pData, int pStart, int pLength) throws AudioException {
		if(this.mClosed) {
			throw new IllegalStateException("The Audio is not opened");
		}
		return 0;
	}

	@Override
	public void open() throws AudioException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Audio is already opened");
		}
		this.mClosed = false;
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

	@Override
	public void close() throws AudioException {
		this.mClosed = true;
	}

}
