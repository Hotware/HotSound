package de.hotware.hotsound.audio.playlist;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import de.hotware.hotsound.audio.player.ISong;

public interface IPlaylistParser {

	public List<ISong> parse(URL pURL) throws IOException;

	public String[] getKeys();

}
