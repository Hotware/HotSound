package de.hotware.hotmisc.audio.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class ListStreamMusicPlayer extends StreamMusicPlayer {
	
	protected List<ISong> mSongs;
	
	public ListStreamMusicPlayer() {
		super();
		this.mSongs = new ArrayList<ISong>();
	}
	
	@Override
	public void insert(ISong pSong) throws SongInsertionException {
		this.mLock.lock();
		try {
			if(this.mPlayerThread == null || this.mPlayerThread.isStopped()) {
				try {
					this.mPlayerThread = new StreamPlayerThread(pSong);
				} catch(UnsupportedAudioFileException
						| IOException
						| LineUnavailableException e) {
					throw new SongInsertionException("Couldn't insert " + pSong, e);
				}
			} else {
				this.mSongs.add(pSong);
			}
		} finally {
			this.mLock.unlock();
		}
	}

}
