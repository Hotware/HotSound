package de.hotware.hotsound.audio.player;

public class MusicPlayerException extends Exception {

	private static final long serialVersionUID = 8381906976505908003L;

	public MusicPlayerException() {
		super();
	}

	public MusicPlayerException(String pMessage) {
		super(pMessage);
	}

	public MusicPlayerException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	public MusicPlayerException(Throwable pCause) {
		super(pCause);
	}

}