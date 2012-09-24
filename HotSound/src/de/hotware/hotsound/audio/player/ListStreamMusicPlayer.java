package de.hotware.hotsound.audio.player;

import java.util.ArrayList;
import java.util.List;

public class ListStreamMusicPlayer extends StreamMusicPlayer implements IListMusicPlayer {
	
	protected List<ISong> mSongs;
	protected int mCurrent;
	
	public ListStreamMusicPlayer() {
		super();
		this.mSongs = new ArrayList<ISong>();
		this.mCurrent = 0;
	}
	
	@Override
	public void insert(ISong pSong) throws SongInsertionException {
		this.mLock.lock();
		try {
			if(this.mPlayerThread == null || this.mPlayerThread.isStopped()) {
				super.insert(pSong);
			}
			this.mSongs.add(pSong);
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void next() throws SongInsertionException {
		if(this.mCurrent == this.mSongs.size() - 1) {
			this.mCurrent = 0;
		} else {
			this.mCurrent++;
		}
		super.insert(this.mSongs.get(this.mCurrent));
	}

	@Override
	public void previous() throws SongInsertionException {
		if(this.mCurrent == 0) {
			this.mCurrent = this.mSongs.size() - 1;
		} else {
			this.mCurrent--;
		}
		super.insert(this.mSongs.get(this.mCurrent));
	}
	
	@Override
	public void play(int pX) throws SongInsertionException {
		if(pX < 0 || pX >= this.mSongs.size()) {
			throw new IllegalArgumentException("Song-Index not available");
		}
		this.mCurrent = pX;
		super.insert(this.mSongs.get(pX));
		super.startPlayback();
	}

}
