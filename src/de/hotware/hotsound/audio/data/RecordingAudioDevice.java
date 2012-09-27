package de.hotware.hotsound.audio.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public class RecordingAudioDevice implements IAudioDevice {

	protected BufferedOutputStream mBufferedOutputStream;
	protected IHeader mHeader;
	protected File mFile;
	protected boolean mPaused;

	public RecordingAudioDevice(File pFile) {
		this.mFile = pFile;
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		try {
			this.mBufferedOutputStream.write(pData, pStart, pLength);
		} catch(IOException e) {
			throw new AudioDeviceException("couldn't write to the File", e);
		}
		return pLength;
	}

	@Override
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException {
		if(this.mFile.exists()) {
			this.mFile.delete();
		}
		try {
			this.mFile.createNewFile();
			this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
			this.mHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
					(short) pAudioFormat.getChannels(),
					(int) pAudioFormat.getSampleRate(),
					(short) pAudioFormat.getSampleSizeInBits(),
					-1);
			this.mHeader.write(this.mBufferedOutputStream);
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
			this.mBufferedOutputStream.close();
		}
	}

}
