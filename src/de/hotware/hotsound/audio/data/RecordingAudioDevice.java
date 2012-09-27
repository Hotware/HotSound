package de.hotware.hotsound.audio.data;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;


public class RecordingAudioDevice implements IAudioDevice {

	protected Recorder mRecorder;
	protected boolean mPaused;

	public RecordingAudioDevice(File pFile) {
		this.mRecorder = new Recorder(pFile);
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		try {
			this.mRecorder.write(pData, pStart, pLength);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't write to the File", e);
		}
		return pLength;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		try {
			this.mRecorder.open(pAudioFormat);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't initialize the Streamwriting process",
					e);
		}
	}

	@Override
	public void pause() {
		if(this.mPaused) {
			throw new IllegalStateException("The AudioDevice is already paused");
		}
		this.mPaused = true;
	}

	@Override
	public void unpause() {
		if(!this.mPaused) {
			throw new IllegalStateException("The AudioDevice is not paused");
		}
		this.mPaused = false;
	}

	@Override
	public void close() throws IOException {
		try {
		} finally {
			this.mRecorder.close();
		}
	}

}
