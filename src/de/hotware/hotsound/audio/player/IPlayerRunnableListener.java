package de.hotware.hotsound.audio.player;



public interface IPlayerRunnableListener {
	
	public void onEnd(MusicEndEvent pEvent);
	
	public void onException(MusicExceptionEvent pEvent);

}
