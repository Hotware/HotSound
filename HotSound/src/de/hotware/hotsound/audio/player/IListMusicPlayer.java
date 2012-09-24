package de.hotware.hotsound.audio.player;

import java.util.List;

public interface IListMusicPlayer extends IMusicPlayer {

	public void setPlaylist(List<ISong> pPlaylist) throws SongInsertionException;

	public void next() throws SongInsertionException;

	public void previous() throws SongInsertionException;

	public void play(int pX) throws SongInsertionException;

	public void insertAt(int pX) throws SongInsertionException;

	public void removeAt(int pX);

	public int getCurrent();

	public int size();

}
