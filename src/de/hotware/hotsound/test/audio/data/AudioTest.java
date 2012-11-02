package de.hotware.hotsound.test.audio.data;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.Test;

import de.hotware.hotsound.audio.data.BasicPlaybackAudio;
import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.IAudio.AudioException;
import de.hotware.hotsound.audio.player.BasicPlaybackSong;
import de.hotware.hotsound.audio.player.ISong;
import de.hotware.hotsound.audio.player.MusicPlayerException;


public class AudioTest {

	@Test
	public void testSongs() {
		try {
			this.testSong(new BasicPlaybackSong(new File("test_resources/test.wav")));
		} catch(MalformedURLException e) {
			fail(this + " is borked " + e.getMessage());
		}
	}
	
	public void testSong(ISong pSong) {
		IAudio audio = null;
		try {
			audio = (BasicPlaybackAudio) pSong.getAudio();
		} catch(MusicPlayerException e) {
			fail("couldn't initialize BasicPlaybackAudio");
		}
		try {
			audio.open();
			assertTrue(audio.getAudioFormat() != null);
			assertTrue(pSong.getAudioFormat() != null);
		} catch(AudioException e) {
			fail("couldn't open BasicPlaybackAudio");
		}
		try {
			byte[] test = new byte[128];
			audio.read(test, 0, 128);
		} catch(AudioException e) {
			fail("couldn't read from BasicPlaybackAudio");
		}
		try {
			audio.close();
		} catch(AudioException e) {
			fail("coudln't close BasicPlaybackAudio");
		}
		
	}
	

}
