package de.hotware.hotsound.audio.data;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.hotware.hotsound.audio.util.AudioUtil;

/**
 * same as BasicAudio but always converts
 * 
 * @author Martin Braun
 */
public class WaveAudio extends BasicAudio {

	public WaveAudio(InputStream pInputStream) {
		super(pInputStream);
	}

	@Override
	public void open() throws AudioException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Audio is already opened");
		}
		try {
			this.mAudioInputStream = AudioUtil
					.getPCMSignedAudioInputStreamFromAudioInputStream(this.mInputStream);
		} catch(UnsupportedAudioFileException | IOException e) {
			throw new AudioException("Error while opening the audiostream", e);
		}
		this.mClosed = false;
	}

}