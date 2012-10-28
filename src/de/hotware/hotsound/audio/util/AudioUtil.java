/**
 * File AudioUtil.java
 * ---------------------------------------------------------
 *
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com), (c) 1999 - 2001 by Matthias Pfisterer
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
package de.hotware.hotsound.audio.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioUtil {
	
	private AudioUtil() {}

	/**
	 * retrieves a AudioInputStream from a given Inputstream. converts the
	 * AudioInputStream if needed
	 *
	 * @param pSong
	 *            object from which the AudioInputStream should be retrieved
	 * @return a AudioInputStream-object from which can be played
	 * @throws UnsupportedAudioFileException
	 *             if the given Song is not supported on the system
	 * @throws IOException
	 *             if an underlying call throws it
	 */
	public static AudioInputStream getSupportedAudioInputStreamFromInputStream(
			InputStream pInputStream)
					throws UnsupportedAudioFileException, IOException {
		AudioInputStream sourceAudioInputStream = AudioSystem
				.getAudioInputStream(pInputStream);
		return getSupportedAudioInputStreamFromAudioInputStream(sourceAudioInputStream,
				false);
	}

	/**
	 * retrieves a compatible AudioInputStream from a given AudioInputStream.
	 * converts the AudioInputStream if needed
	 *
	 * @param pSong
	 *            object from which the AudioInputStream should be retrieved
	 * @return a AudioInputStream-object from which can be played
	 * @throws UnsupportedAudioFileException
	 *             if the given Song is not supported on the system
	 * @throws IOException
	 *             if an underlying call throws it
	 */
	public static AudioInputStream getSupportedAudioInputStreamFromAudioInputStream(
			AudioInputStream pAudioInputStream) throws UnsupportedAudioFileException {
		return getSupportedAudioInputStreamFromAudioInputStream(pAudioInputStream,
				false);
	}

	/**
	 * same as getSupportedAudioInputStreamFromInputStream(AudioInputStream) but
	 * specifically always converts to PCM_SIGNED
	 */
	public static AudioInputStream getPCMSignedAudioInputStreamFromAudioInputStream(
			AudioInputStream pAudioInputStream) {
		return getSupportedAudioInputStreamFromAudioInputStream(pAudioInputStream,
				true);
	}

	/**
	 * same as getSupportedAudioInputStreamFromInputStream(InputStream) but
	 * specifically always converts to PCM_SIGNED
	 *
	 * @throws IOException if the Retrieval fails due to an I/O-Error
	 * @throws UnsupportedAudioFileException if the stream does not point to
	 * 				valid audio file data recognized by the system
	 */
	public static AudioInputStream getPCMSignedAudioInputStreamFromAudioInputStream(InputStream pInputStream) throws UnsupportedAudioFileException,
			IOException {
		AudioInputStream sourceAudioInputStream = AudioSystem
				.getAudioInputStream(pInputStream);
		return getSupportedAudioInputStreamFromAudioInputStream(sourceAudioInputStream,
				true);
	}

	/**
	 * Ensures audio format support for a given stream.
	 *
	 * The Stream will be converted to the audio format {@link Encoding.PCM_SIGNED PCM_SIGNED}
	 * if one of the following conditions is true:
	 *
	 * <ul>
	 *  <li>The stream's format is not supported on the system</li>
	 *  <li>{@code pAlwaysConvert} is true <em>and</em> the stream's format is not already
	 *  		{@link Encoding.PCM_SIGNED PCM_SIGNED}</li>
	 * </ul>
	 *
	 * @param pAudioInputStream The audio stream to get a supported version of
	 * @param pAlwaysConvert true to force a audio format conversion, false to perform it on an
	 * 			as-needed basis.
	 * @return The converted stream. If no conversion is performed, the input stream
	 * 			({@code pAudioInputStream}) is returned.
	 */
	public static AudioInputStream getSupportedAudioInputStreamFromAudioInputStream(AudioInputStream pAudioInputStream,
			boolean pAlwaysConvert) {
		AudioInputStream ret = pAudioInputStream;
		AudioFormat sourceAudioFormat = pAudioInputStream.getFormat();
		DataLine.Info supportInfo = new DataLine.Info(SourceDataLine.class,
				sourceAudioFormat,
				AudioSystem.NOT_SPECIFIED);
		boolean directSupport = AudioSystem.isLineSupported(supportInfo);
		if(sourceAudioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED && pAlwaysConvert ||
				!directSupport) {
			// Audio format is not supported -> Convert to a supported format
			float sampleRate = sourceAudioFormat.getSampleRate();
			int channels = sourceAudioFormat.getChannels();
			AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
					sampleRate,
					16,
					channels,
					channels * 2,
					sampleRate,
					false);
			AudioInputStream convertedAudioInputStream = AudioSystem
					.getAudioInputStream(newFormat, pAudioInputStream);
			ret = convertedAudioInputStream;
		}
		return ret;
	}

	/**
	 * @return a mixer info object belonging to the Mixer named as pMixerName or
	 *         null if not found
	 */
	public static Mixer.Info getMixerInfo(String pMixerName) {
		Mixer.Info[] ret = AudioSystem.getMixerInfo();
		for(Info element : ret)
		{
			if(element.getName().equals(pMixerName)) {
				return element;
			}
		}
		return null;
	}

	public static List<Mixer> getCompatibleMixers(Class<? extends Line> pLineClass) {
		Line.Info lineInfo = new Line.Info(pLineClass);
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		List<Mixer> ret = new ArrayList<>();
		for(Mixer.Info info : mixerInfo) {
			Mixer current = AudioSystem.getMixer(info);
			if(current.isLineSupported(lineInfo)) {
				ret.add(current);
			}
		}
		return ret;
	}

}
