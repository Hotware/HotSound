package de.hotware.hotsound.audio.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudioDevice.AudioDeviceException;

public class Recorder implements AutoCloseable {

	private BufferedOutputStream mBufferedOutputStream;
	private File mFile;
	private File mTempFile;
	private IHeader mHeader;
	private int mBytesWritten;

	public Recorder(File pFile) {
		if(pFile == null) {
			throw new IllegalArgumentException("pFile may not be null");
		}
		this.mFile = pFile;
		this.mTempFile = new File(pFile.getAbsolutePath() + ".tmp");
		this.mBytesWritten = 0;
	}

	public void open(AudioFormat pAudioFormat) throws AudioDeviceException,
			IOException {
		if(this.mTempFile.exists()) {
			this.mFile.delete();
		}
		this.mTempFile.createNewFile();
		this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mTempFile));
		this.mHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
				(short) pAudioFormat.getChannels(),
				(int) pAudioFormat.getSampleRate(),
				(short) pAudioFormat.getSampleSizeInBits(),
				Integer.MAX_VALUE);
		this.mHeader.write(this.mBufferedOutputStream);
	}

	public int write(byte[] pData, int pStart, int pLength) throws IOException {
		this.mBufferedOutputStream.write(pData, pStart, pLength);
		this.mBytesWritten += pLength;
		return pLength;
	}

	public void close() throws IOException {
		try(BufferedInputStream input = new BufferedInputStream(new FileInputStream(this.mTempFile))) {
			if(this.mBufferedOutputStream != null) {
				this.mBufferedOutputStream.flush();
				this.mBufferedOutputStream.close();
				;
				((WaveHeader) this.mHeader).read(input);
				((WaveHeader) this.mHeader).setNumBytes(this.mBytesWritten);
				if(this.mFile.exists()) {
					this.mFile.delete();
				}
				this.mFile.createNewFile();
				this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
				int bytesRead = 0;
				byte[] data = new byte[128000];
				this.mHeader.write(this.mBufferedOutputStream);
				while((bytesRead = input.read(data, 0, 128000)) != -1) {
					this.write(data, 0, bytesRead);
				}
			}
		} finally {
			this.mTempFile.delete();
			this.mBufferedOutputStream.flush();
			this.mBufferedOutputStream.close();
			this.mBufferedOutputStream = null;
		}
	}

}
