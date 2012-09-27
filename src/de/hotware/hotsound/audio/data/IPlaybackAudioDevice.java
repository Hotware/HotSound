package de.hotware.hotsound.audio.data;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;

/**
 * IPlaybackAudioDevice is used for actual playback devices. the normal
 * interface can play to whatever it likes (even to potatoes if they like the
 * sound of bytes)
 * 
 * @author Martin Braun
 * 
 */
public interface IPlaybackAudioDevice extends IAudioDevice {

	public void setMixer(Mixer pMixer);

	/**
	 * @return the DataLine to which is being written (with that you can control
	 *         the volume, etc.)
	 */
	public DataLine getDataLine();

}
