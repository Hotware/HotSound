package de.hotware.hotsound.audio.player;

import de.hotware.util.GBaseEvent;

public class MusicEndEvent extends
		GBaseEvent<IMusicPlayer> {

	private MusicEndEvent.Type mType;

	public MusicEndEvent(IMusicPlayer pSource,
			MusicEndEvent.Type pType) {
		super(pSource);
		this.mType = pType;
	}

	public MusicEndEvent.Type getType() {
		return this.mType;
	}

	public static enum Type {
		SUCCESS,
		FAILURE
	}

}