package de.hotware.hotsound.audio.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudioDevice.AudioDeviceException;


public class Recorder implements AutoCloseable {
	
	private BufferedOutputStream mBufferedOutputStream;
	private File mFile;
	private IHeader mHeader;

	public Recorder(File pFile) {
		if(pFile == null) {
			throw new IllegalArgumentException("pFile may not be null");
		}
		this.mFile = pFile;
	}

	public void open(AudioFormat pAudioFormat) throws AudioDeviceException, IOException {
		if(this.mFile.exists()) {
			this.mFile.delete();
		}
		this.mFile.createNewFile();
		this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
		this.mHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
				(short) pAudioFormat.getChannels(),
				(int) pAudioFormat.getSampleRate(),
				(short) pAudioFormat.getSampleSizeInBits(),
				-1);
		this.mHeader.write(this.mBufferedOutputStream);

	}

	public int write(byte[] pData, int pStart, int pLength) throws IOException {
		this.mBufferedOutputStream.write(pData, pStart, pLength);
		return pLength;
	}

	public void close() throws IOException {
		this.mBufferedOutputStream.close();
	}

}
