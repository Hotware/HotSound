# Short Introduction
Playing music with HotSound is much easier than playing with JavaSound while you still have all the control over most of the stuff. It doesn't rely on the Clip functionality to be easy to use and runs in a separate Thread if needed.

## Why HotSound?
Because playing Sound with Java isn't easy. It costs time to write that complex code and consumes precious coffee break time :P. No, just kidding. Your allday applications don't have to have their own audio part if they can use this library.

## Does HotSound support encoded Audiotypes?
Yes! If there is a JavaSound SPI any sound format can be played with HotSound. That's being done via .jar files that you have to add to your path.
(some of them are already available with this repository)

Known to work:
* MP3 (via JLayer from javazoom.org)
* OOG-Vorbis (via jogg and jorbis from javazoom.org)
* Flac (via jflac)

A tutorial on what you need to get them to work will follow.

## SimplePlayer Examples (from HotSoundExamples)
This is a simple showcase how easy the API can be used in real life applications. No need to write all the tricky stuff. It's nearly as easy as pressing play on a MP3 Player.

	package de.hotware.hotsound.examples;
	
	import java.io.File;
	import java.net.MalformedURLException;
	
	import de.hotware.hotsound.audio.player.BasicSong;
	import de.hotware.hotsound.audio.player.IMusicPlayer;
	import de.hotware.hotsound.audio.player.MusicPlayerException;
	import de.hotware.hotsound.audio.player.StreamMusicPlayer;
	
	/**
	 * Player that plays on the command line and it's 27 lines long
	 * 
	 * @author Martin Braun
	 */
	public class SimplePlayer {
	
		public static void main(String[] args) throws MusicPlayerException, MalformedURLException {
			if(args.length >= 1) {
				IMusicPlayer player = new StreamMusicPlayer();
				player.insert(new BasicSong(new File(args[0])));
				player.start();
			}
		}
	
	}

And a player that also records all the data he is playing (in .wav format)

	package de.hotware.hotsound.examples;
	
	import java.io.File;
	import java.net.MalformedURLException;
	import java.net.URL;
	import java.util.concurrent.Executors;
	
	import de.hotware.hotsound.audio.data.SavingAudioDevice;
	import de.hotware.hotsound.audio.player.BasicSong;
	import de.hotware.hotsound.audio.player.IMusicListener;
	import de.hotware.hotsound.audio.player.IMusicPlayer;
	import de.hotware.hotsound.audio.player.MusicPlayerException;
	import de.hotware.hotsound.audio.player.StreamMusicPlayer;
	
	
	public class SavingSimplePlayer {
		
		public static void main(String[] args) throws MusicPlayerException, MalformedURLException, InterruptedException {
			if(args.length >= 2) {
				//multithreading because of blocking behaviour
				IMusicPlayer player = new StreamMusicPlayer(new IMusicListener() {
	
					@Override
					public void onEnd(MusicEvent pEvent) {
						System.out.println("stopped");
					}
					
				}, Executors.newSingleThreadExecutor());
				player.insert(new BasicSong(new URL(args[0])), new SavingAudioDevice(new File(args[1])));
				player.start();
				//wait 10 seconds (equals approx. 10 seconds of saved audio)
				Thread.sleep(10000);
				//always stop for bug avoidance in saving the audiofile
				player.stop();
			}
		}
	
	}
	
Playback from Microphone (combine this with the one above and you get yourself an audiorecorder)

	package de.hotware.hotsound.examples;
	
	import java.net.MalformedURLException;
	import java.util.List;
	import java.util.concurrent.Executors;
	
	import javax.sound.sampled.LineUnavailableException;
	import javax.sound.sampled.Mixer;
	
	import de.hotware.hotsound.audio.data.BasicAudioDevice;
	import de.hotware.hotsound.audio.data.RecordAudio;
	import de.hotware.hotsound.audio.player.IMusicListener;
	import de.hotware.hotsound.audio.player.IMusicPlayer;
	import de.hotware.hotsound.audio.player.MusicPlayerException;
	import de.hotware.hotsound.audio.player.RecordSong;
	import de.hotware.hotsound.audio.player.StreamMusicPlayer;
	
	
	public class SimpleMicroPhonePlayer {
		
		public static void main(String[] args) throws MusicPlayerException, MalformedURLException, InterruptedException, LineUnavailableException {
				List<Mixer> mixers = RecordAudio.getRecordMixers();
				if(mixers.size() > 0) {
					IMusicPlayer player = new StreamMusicPlayer(new IMusicListener() {
	
						@Override
						public void onEnd(MusicEvent pEvent) {
							System.out.println("stopped");
						}
						
					}, Executors.newSingleThreadExecutor());
					Mixer mixer = mixers.get(0);
					mixer.open();
					player.insert(new RecordSong(mixer), new BasicAudioDevice());
					player.start();
					//wait 10 seconds (equals approx. 10 seconds of saved audio)
					Thread.sleep(10000);
					player.stop();
				}
		}
	
	}
