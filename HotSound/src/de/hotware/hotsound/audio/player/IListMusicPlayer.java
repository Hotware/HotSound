package de.hotware.hotsound.audio.player;


public interface IListMusicPlayer extends IMusicPlayer {
	
	public void next() throws SongInsertionException;
	public void previous() throws SongInsertionException;
	public void play(int pX) throws SongInsertionException;

}
