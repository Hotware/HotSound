/**
 * File IMusicPlayer.java
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

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudioDevice;

/**
 * Music Player interface. provides basic player methods and getters for a
 * player.
 * 
 * @author Martin Braun
 */
public interface IMusicPlayer extends AutoCloseable {

	/**
	 * Inserts a Song to the the Player. Possible difference between
	 * implementations: some could allow multiple songs to be added, but some
	 * could only hold one song at a time
	 * 
	 * uses a default AudioDevice if there is not already one in usage (not
	 * closed)
	 * 
	 * @throws MusicPlayerException
	 *             if the insertion fails
	 */
	public void insert(ISong pSong) throws MusicPlayerException;

	/**
	 * inserts a Song to the the Player possible difference between
	 * implementations: some could allow multiple songs to be added, but some
	 * could only hold one song at a time
	 */
	public void insert(ISong pSong, IAudioDevice pAudioDevice) throws MusicPlayerException;

	/**
	 * starts the playback
	 * 
	 * @throws IllegalStateException
	 *             if the player hasn't been initialized yet (insert not called)
	 * @throws MusicPlayerException
	 *             if there was an error during the start of the playback
	 */
	public void start() throws MusicPlayerException;

	/**
	 * pauses/unpauses the playback
	 * 
	 * @throws IllegalStateException
	 *             if the player hasn't been initialized yet (insert not called)
	 */
	public void pause(boolean pPause);

	/**
	 * stops the playback but doesn't reset the Player. you can restart it via
	 * restartPlayback afterwards
	 * 
	 * may lock the call if in multithreaded mode and the player is currently
	 * starting
	 * 
	 * @throws MusicPlayerException
	 *             if stopping fails
	 * 
	 * @throws IllegalStateException
	 *             if the player hasn't been initialized yet (insert not called)
	 */
	public void stop() throws MusicPlayerException;

	/**
	 * @inheritDoc only closes resources it has opened itself (everything that
	 *             got passed in by methods won't get closed)
	 * @throws MusicPlayerException
	 *             if close fails
	 */
	@Override
	public void close() throws MusicPlayerException;

	/**
	 * @return true if stopped (not started or ended), false otherwise
	 */
	public boolean isStopped();

	/**
	 * @return true if started and paused (not ended), false otherwise
	 */
	public boolean isPaused();

	/**
	 * @return the AudioFormat of the current song
	 */
	public AudioFormat getAudioFormat();

	/**
	 * @return the AudioDevice of the current song
	 */
	public IAudioDevice getAudioDevice();

	/**
	 * seeks to the given position
	 * 
	 * @param pPosition
	 *            position in microseconds
	 */
	public void seek(int pPosition);

	public void restart();

	public void setMusicListener(IMusicListener pMusicListener);

}
