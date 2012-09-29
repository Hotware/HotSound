package de.hotware.hotsound.audio.player;

import de.hotware.util.GBaseEvent;

public class MusicExceptionEvent extends GBaseEvent<IMusicPlayer> {
	
	private MusicPlayerException mException;

	public MusicExceptionEvent(IMusicPlayer pSource, MusicPlayerException pException) {
		super(pSource);
		this.mException = pException;
	}
	
	public MusicPlayerException getException() {
		return this.mException;
	}
	
}