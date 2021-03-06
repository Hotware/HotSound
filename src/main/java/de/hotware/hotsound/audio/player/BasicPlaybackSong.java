/**
 * File BasicPlaybackSong.java
 * ---------------------------------------------------------
 * <p/>
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 * if it breaks something.
 */
package de.hotware.hotsound.audio.player;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import de.hotware.hotsound.audio.data.Audio;
import de.hotware.hotsound.audio.data.BasicPlaybackAudio;

/**
 * Base implementation of a song. instantiable because it already knows enough
 * stuff to retrieve a inputstream
 *
 * @author Martin Braun
 */
public class BasicPlaybackSong implements Song {

	protected URL mURL;
	protected long mFrameLength;
	protected AudioFormat mAudioFormat;

	public BasicPlaybackSong(URL pURL) {
		this.mURL = pURL;
	}

	public BasicPlaybackSong(URI pURI) throws MalformedURLException {
		this( pURI.toURL() );
	}

	public BasicPlaybackSong(File pFile) throws MalformedURLException {
		if ( !pFile.exists() ) {
			throw new IllegalArgumentException( "File does not exist" );
		}
		this.mURL = pFile.toURI().toURL();
	}

	@Override
	public final Audio getAudio() throws MusicPlayerException {
		try {
			AudioFileFormat audioFileFormat;
			if ( this.getPlaybackURL().getProtocol() != null &&
					this.getPlaybackURL().getProtocol().toLowerCase()
							.equals( "file" ) ) {
				try {
					audioFileFormat = AudioSystem
							.getAudioFileFormat( new File( this.mURL.toURI() ) );
				}
				catch (URISyntaxException e) {
					audioFileFormat = AudioSystem
							.getAudioFileFormat( new File( this.mURL.getPath() ) );
				}
			}
			else {
				audioFileFormat = AudioSystem.getAudioFileFormat( this.mURL );
			}
			this.mAudioFormat = audioFileFormat.getFormat();
			URLConnection uc = this.mURL.openConnection();
			return this.getAudio(
					uc.getInputStream(),
					audioFileFormat.getFrameLength()
			);
		}
		catch (IOException | UnsupportedAudioFileException e) {
			throw new MusicPlayerException(
					"Exception occured while getting the Audio from this Song",
					e
			);
		}
	}

	/**
	 * change this if the url passed to this song (i.e. it isn't the actual url
	 * that should be played back)
	 *
	 * @return the playback url for this song
	 * @throws IOException
	 */
	protected URL getPlaybackURL() throws IOException {
		return this.mURL;
	}

	/**
	 * override this method if you want a different audio behaviour
	 *
	 * @return an audio instance according to this implementation
	 */
	protected Audio getAudio(InputStream pInputstream, int pFrameLength) {
		return new BasicPlaybackAudio( pInputstream, pFrameLength );
	}

	@Override
	public long getFrameLength() {
		return this.mFrameLength;
	}

	/**
	 * @return the audioformat of the song. this must not necessarily be the
	 *         audioformat of the audio returned by getAudio()
	 */
	public AudioFormat getAudioFormat() {
		return this.mAudioFormat;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		return builder.append( "[" ).append( this.getClass().getSimpleName() )
				.append( ": " ).append( this.mURL ).append( "]" ).toString();
	}

}
