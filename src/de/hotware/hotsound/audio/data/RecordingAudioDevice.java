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
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
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
	public void close() throws IOException {
		this.mRecorder.close();
		this.mClosed = true;
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
