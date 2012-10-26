/**
 * File StockParser.java
 * ---------------------------------------------------------
 *
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 *   if it breaks something.
 */
package de.hotware.hotsound.audio.playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hotware.hotsound.audio.player.BasicPlaybackSong;
import de.hotware.hotsound.audio.player.ISong;

/**
 * default Parsers that ship with HotSound
 *
 * @author Martin Braun
 */
public enum StockParser implements IPlaylistParser {
	M3U("m3u") {

		@Override
		public List<ISong> parse(URL pURL) throws IOException {
			try(InputStreamReader streamReader = new InputStreamReader(pURL.openStream());
					BufferedReader buf = new BufferedReader(streamReader)) {
				List<ISong> ret = new ArrayList<ISong>();
				String line;
				while((line = buf.readLine()) != null) {
					// ignore ALL the unnecessary whitespace
					line = line.trim();
					if(!line.startsWith("#")) {
						if(!line.startsWith("http")) {
							File file = new File(line);
							if(file.exists()) {
								//file path was absolute
								ret.add(new BasicPlaybackSong(file));
							} else if(!pURL.getProtocol().startsWith("http")) {
								//file path was relative
								File parentFile = new File(pURL.getFile())
										.getParentFile();
								if(!parentFile.isDirectory()) {
									throw new AssertionError("parent file of url is no directory!");
								}
								file = new File(parentFile, line);
								if(file.exists()) {
									//file path was relative and file exists
									ret.add(new BasicPlaybackSong(file));
								}
							}
						} else {
							ret.add(new BasicPlaybackSong(new URL(line)));
						}
					}
				}
				return ret;
			}
		}

	};

	protected final String[] mKeys;

	private StockParser(String... pKeys) {
		if(pKeys == null) {
			throw new NullPointerException("pKeys may not be null");
		}
		this.mKeys = pKeys;
	}

	@Override
	public String[] getKeys() {
		return Arrays.copyOf(this.mKeys, this.mKeys.length);
	}

}
