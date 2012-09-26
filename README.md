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

## Here is an example:
    //creates a MusicPlayer object without any listeners or fancy stuff. just plays your audio
    //and converts it if needed
    MusicPlayer player = new StreamMusicPlayer();
    //for the player to be able to play this you need a .ogg SPI
    ISong song = new BasicSong(new URL("http://listen.technobase.fm/tunein-oggvorbis-pls.ogg"));
    //if you need a specific Mixer you can pass it here as a second argument.
    //this method just uses the default system mixer
    player.insert(song);
    //starts your playback
    player.startPlayback();
    //pauses your playback
    player.pausePlayback();
    //unpauses your playback
    player.unpausePlayback();
    //stops your playback
    player.stopPlayback();

## SimplePlayer Example (from HotSoundExamples)
This is a simple showcase how easy the API can be used in real life applications. No need to write all the tricky stuff. It's nearly as easy as pressing play on a MP3 Player.

  package de.hotware.hotsound.examples;
	
	import java.io.File;
	import java.net.MalformedURLException;
	
	import de.hotware.hotsound.audio.player.BasicSong;
	import de.hotware.hotsound.audio.player.IMusicPlayer;
	import de.hotware.hotsound.audio.player.IMusicPlayer.SongInsertionException;
	import de.hotware.hotsound.audio.player.IPlaybackListener;
	import de.hotware.hotsound.audio.player.StreamMusicPlayer;
	
	/**
	 * Player that plays on the command line and it's 37 lines long
	 * 
	 * @author Martin Braun
	 */
	public class SimplePlayer {
	
		public static void main(String[] args) throws MalformedURLException,
				SongInsertionException {
			if(args.length >= 1) {
				IMusicPlayer player = new StreamMusicPlayer(new IPlaybackListener() {
	
					@Override
					public void onEnd(PlaybackEndEvent pEvent) {
						System.out.println("Playback ended");
						System.exit(1);
					}
	
				});
				player.insert(new BasicSong(new File(args[0])));
				player.startPlayback();
			}
		}
	
	}