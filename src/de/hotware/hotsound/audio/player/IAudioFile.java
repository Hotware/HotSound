package de.hotware.hotsound.audio.player;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;

public interface IAudioFile {
	
	public AudioFormat getAudioFormat();

	/**
	 * this is just a hint
	 * 
	 * @return AudioSystem.NOT_SPECIFIED if default value should be used
	 */
	public int getRecommendedBufferSize();

	public int read(byte[] pData, int pStart, int pBufferSize) throws IOException;

	public void close() throws IOException;

}
