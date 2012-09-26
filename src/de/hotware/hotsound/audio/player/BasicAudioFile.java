package de.hotware.hotsound.audio.player;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class BasicAudioFile implements ISeekableAudioFile {

	protected AudioFormat mAudioFormat;
	protected AudioInputStream mAudioInputStream;
	protected int mRecommendedBufferSize;

	protected BasicAudioFile() {

	}

	public static BasicAudioFile getBasicAudioFileFromSong(ISong pSong) throws UnsupportedAudioFileException,
			IOException {
		BasicAudioFile ret = new BasicAudioFile();
		ret.mAudioInputStream = AudioUtil.getAudioInputStreamFromSong(pSong);
		ret.mAudioFormat = ret.mAudioInputStream.getFormat();
		return ret;
	}
	
	@Override
	public int getRecommendedBufferSize() {
		return AudioSystem.NOT_SPECIFIED;
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.mAudioFormat;
	}

	@Override
	public int read(byte[] pData, int pStart, int pBufferSize) throws IOException {
		return this.mAudioInputStream.read(pData, pStart, pBufferSize);
	}

	@Override
	public void close() throws IOException {
		this.mAudioInputStream.close();
	}

	@Override
	public void seek(int pFrame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skip(int pFrames) {
		// TODO Auto-generated method stub
		
	}

}
