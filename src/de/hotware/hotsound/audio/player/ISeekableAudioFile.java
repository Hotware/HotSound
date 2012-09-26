package de.hotware.hotsound.audio.player;


public interface ISeekableAudioFile extends IAudioFile {
	
	public void seek(int pFrame);
	
	public void skip(int pFrames);

}
