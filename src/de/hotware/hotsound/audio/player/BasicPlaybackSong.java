/**
 * File BasicPlaybackSong.java
 * ---------------------------------------------------------
 *
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 *   if it breaks something.
 */
package de.hotware.hotsound.audio.player;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import de.hotware.hotsound.audio.data.BasicPlaybackAudio;
import de.hotware.hotsound.audio.data.IAudio;

/**
 * Base implementation of a song. instantiable because it already knows enough
 * stuff to retrieve a inputstream
 * 
 * @author Martin Braun
 */
public class BasicPlaybackSong implements ISong {

	protected URL mURL;

	public BasicPlaybackSong(URL pURL) {
		this.mURL = pURL;
	}

	public BasicPlaybackSong(URI pURI) throws MalformedURLException {
		this(pURI.toURL());
	}

	public BasicPlaybackSong(File pFile) throws MalformedURLException {
		if(!pFile.exists()) {
			throw new IllegalArgumentException("File does not exist");
		}
		this.mURL = pFile.toURI().toURL();
	}

	public InputStream getInputStream() throws IOException {
		URLConnection uc = this.mURL.openConnection();
		return uc.getInputStream();
	}

	@Override
	public IAudio getAudio() throws MusicPlayerException {
		try {
			return new BasicPlaybackAudio(this.getInputStream());
		} catch(IOException e) {
			throw new MusicPlayerException("IOException occured while getting the IAudio from this ISong", e);
		}
	}
	
}