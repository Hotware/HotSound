package de.hotware.hotsound.audio.data;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public class RecordingAudioDevice implements IAudioDevice {

	protected Recorder mRecorder;
	protected boolean mPaused;
	protected boolean mClosed;

	public RecordingAudioDevice(File pFile) {
		this.mRecorder = new Recorder(pFile);
		this.mClosed = true;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		if(!this.mClosed) {
			throw new IllegalStateException("The AudioDevice is already opened!");
		}
		try {
			this.mRecorder.open(pAudioFormat);
			this.mClosed = false;
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't initialize the Streamwriting process",
					e);
		}
	}
	
	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		if(this.mClosed || this.mPaused) {
			throw new IllegalStateException("The AudioDevice is not opened");
		}
		try {
			this.mRecorder.write(pData, pStart, pLength);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't write to the File", e);
		}
		return pLength;
	}

	@Override
	public void pause() {
		this.mPaused = true;
	}

	@Override
	public void unpause() {
		this.mPaused = false;
	}

	@Override
	public void close() throws AudioDeviceException {
		try {
			this.mClosed = true;
			this.mRecorder.close();
		} catch(IOException e) {
			throw new AudioDeviceException("IOException occured while closing the Recorder", e);
		}
	}

	@Override
	public boolean isPaused() {
		return this.mPaused;
	}

	@Override
	public boolean isClosed() {
		return this.mClosed;
	}

}
