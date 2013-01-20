/**
 * File AudioTest.java
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
package de.hotware.hotsound.audio.data;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.junit.Test;

import de.hotware.hotsound.audio.data.Audio;
import de.hotware.hotsound.audio.data.RecordAudio;
import de.hotware.hotsound.audio.data.Audio.AudioException;
import de.hotware.hotsound.audio.player.BasicPlaybackSong;
import de.hotware.hotsound.audio.player.Song;
import de.hotware.hotsound.audio.player.MusicPlayerException;
import de.hotware.hotsound.audio.player.RecordSong;
import de.hotware.hotsound.audio.player.SavingSong;

public class AudioTest {

	@Test
	public void testSongs() {
		try {
			this.testSong(new BasicPlaybackSong(new File("test_resources/test.wav")));
			List<Mixer> mixers = RecordAudio.getRecordMixers();
			Mixer mixer = mixers.get(0);
			try {
				mixer.open();
			} catch(LineUnavailableException e) {
				fail("test failed, couln't open mixer for testing");
			}
			this.testSong(new RecordSong(mixer));
			this.testSong(new SavingSong(new File("test_resources/test.wav")));
		} catch(MalformedURLException e) {
			fail(this + " is borked " + e.getMessage());
		}
	}
	
	public void testSong(Song pSong) {
		Audio audio = null;
		try {
			audio = pSong.getAudio();
		} catch(MusicPlayerException e) {
			fail("couldn't initialize BasicPlaybackAudio");
		}
		try {
			audio.open();
			assertTrue(audio.getAudioFormat() != null);
		} catch(AudioException e) {
			fail("couldn't open BasicPlaybackAudio");
		}
		try {
			byte[] test = new byte[128];
			audio.read(test, 0, 128);
		} catch(AudioException e) {
			fail("couldn't read from BasicPlaybackAudio");
		}
		if(audio instanceof SeekableAudio) {
			long framePosition = ((SeekableAudio) audio).getFramePosition();
			try {
				((SeekableAudio) audio).skip(10);
			} catch(AudioException e) {
				fail(e.toString());
			}
			if(((SeekableAudio) audio).getFramePosition() - framePosition != 10) {
				fail("framePosition isn't equal to the position skipped to");
			}
			try {
				((SeekableAudio) audio).seek(10);
			} catch(AudioException e) {
				fail(e.toString());
			}
			if(((SeekableAudio) audio).getFramePosition() != 10) {
				fail("framePosition isn't equal to the position seeked to.\n"+
						"It's at: " + ((SeekableAudio) audio).getFramePosition());
			}
		}
		try {
			audio.close();
		} catch(AudioException e) {
			fail("coudln't close BasicPlaybackAudio");
		}
	}
	

}
