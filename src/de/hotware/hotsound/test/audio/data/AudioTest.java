package de.hotware.hotsound.test.audio.data;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.junit.Test;

import de.hotware.hotsound.audio.data.IAudio;
import de.hotware.hotsound.audio.data.RecordAudio;
import de.hotware.hotsound.audio.data.IAudio.AudioException;
import de.hotware.hotsound.audio.player.BasicPlaybackSong;
import de.hotware.hotsound.audio.player.ISong;
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
	
	public void testSong(ISong pSong) {
		IAudio audio = null;
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
		try {
			audio.close();
		} catch(AudioException e) {
			fail("coudln't close BasicPlaybackAudio");
		}
		
	}
	

}
