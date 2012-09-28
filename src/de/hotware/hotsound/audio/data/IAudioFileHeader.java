package de.hotware.hotsound.audio.data;

import java.io.IOException;
import java.io.OutputStream;

public interface IAudioFileHeader {

	int write(OutputStream out) throws IOException;

}
