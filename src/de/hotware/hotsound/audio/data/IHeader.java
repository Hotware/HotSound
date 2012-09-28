package de.hotware.hotsound.audio.data;

import java.io.IOException;
import java.io.OutputStream;

/**
 * TODO Some information would not harm, I guess ... What kind of header is this?
 */
public interface IHeader {

	int write(OutputStream out) throws IOException;

}
