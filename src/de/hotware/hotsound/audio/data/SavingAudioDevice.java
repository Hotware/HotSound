package de.hotware.hotsound.audio.data;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SavingAudioDevice extends BasicAudioDevice {

	protected ByteArrayOutputStream mByteArrayOutputStream;
	protected File mFile;

	public SavingAudioDevice(File pFile) {
		this(pFile, null);
	}

	protected SavingAudioDevice(File pFile, Mixer pMixer) {
		super(pMixer);
		if(pFile.exists()) {
			throw new IllegalArgumentException("File " + pFile +
					" already exists!");
		}
		this.mFile = pFile;
	}

	@Override
	public void start(AudioFormat pAudioFormat) throws AudioDeviceException {
		super.start(pAudioFormat);
		if(this.mFile.exists()) {
			throw new IllegalStateException("File " + this.mFile +
					" already exists!");
		}
		this.mByteArrayOutputStream = new ByteArrayOutputStream();
	}

	@Override
	public int write(byte[] pData, int pStart, int pLength) throws AudioDeviceException {
		int ret = super.write(pData, pStart, pLength);
		this.mByteArrayOutputStream.write(pData, pStart, pLength);
		return ret;
	}

	/**
	 * @throws AudioDeviceException
	 * @inheritDoc saves all the data
	 */
	@Override
	public void stop() throws AudioDeviceException {
		super.stop();
		try {
			this.saveData();
		} catch(IOException | UnsupportedAudioFileException e) {
			throw new AudioDeviceException("an IOException occured during writing to the file",
					e);
		} finally {
			try {
				this.mByteArrayOutputStream.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveData() throws IOException, UnsupportedAudioFileException {
		if(this.mFile.exists()) {
			throw new IllegalStateException("File " + this.mFile +
					" already exists");
		}

		AudioFormat audioFormat = this.mSourceDataLine.getFormat();
		byte[] data = this.mByteArrayOutputStream.toByteArray();
		try(ByteArrayInputStream input = new ByteArrayInputStream(data);
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
				AudioInputStream audioInputStream = new AudioInputStream(input,
						audioFormat,
						data.length / audioFormat.getFrameSize());) {
			AudioSystem.write(audioInputStream,
					AudioFileFormat.Type.AIFF,
					outputStream);
			input.close();
			audioInputStream.close();
			outputStream.close();
		}
	}

}
