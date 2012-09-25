package de.hotware.hotsound.audio.player;

import de.hotware.util.GBaseEvent;

public interface IPlaybackListener {

	public void onEnd(IPlaybackListener.PlaybackEndEvent pEvent);

	public static class PlaybackEndEvent extends
			GBaseEvent<StreamPlayerRunnable> {

		public PlaybackEndEvent(StreamPlayerRunnable pSource) {
			super(pSource);
		}

	}

}