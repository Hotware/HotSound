package de.hotware.hotsound.audio.player;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.SavingAudio;


public class SavingSong extends BasicSong {
	
	public SavingSong(URL pURL) {
		super(pURL);
	}

	public SavingSong(URI pURI) throws MalformedURLException {
		super(pURI);
	}

	public SavingSong(File pFile) throws MalformedURLException {
		super(pFile);
	}
	
	@Override
	public IAudio getAudio() throws MusicPlayerException {
		try {
			return new SavingAudio(this.getInputStream());
		} catch(IOException e) {
			throw new MusicPlayerException("IOException occured while getting the IAudio from this ISong", e);
		}
	}

}
