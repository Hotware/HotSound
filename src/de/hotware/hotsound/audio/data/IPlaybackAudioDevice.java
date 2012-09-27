package de.hotware.hotsound.audio.data;

import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;


public interface IPlaybackAudioDevice extends IAudioDevice {
	
	public void setMixer(Mixer pMixer);

	/**
	 * @return the DataLine to which is being written (with that you can control
	 *         the volume, etc.)
	 */
	public DataLine getDataLine();

}
