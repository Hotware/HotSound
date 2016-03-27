/**
 * File RecordSong.java
 * ---------------------------------------------------------
 * <p/>
 * Copyright (C) 2012 Martin Braun (martinbraun123@aol.com)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * - The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * - The origin of the software must not be misrepresented.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * TL;DR: As long as you clearly give me credit for this Software, you are free to use as you like, even in commercial software, but don't blame me
 * if it breaks something.
 */
package de.hotware.hotsound.audio.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import de.hotware.hotsound.audio.data.Audio;
import de.hotware.hotsound.audio.data.RecordAudio;

public class RecordSong implements Song {

	protected AudioFormat mAudioFormat;
	protected Class<? extends TargetDataLine> mTargetDataLineClass;
	protected Mixer mMixer;
	protected int mBufferSize;

	public RecordSong(Mixer pMixer) {
		this( pMixer, null );
	}

	public RecordSong(Mixer pMixer, AudioFormat pAudioFormat) {
		this( pMixer, pAudioFormat, AudioSystem.NOT_SPECIFIED );
	}

	/**
	 * @param pBufferSize (hint)
	 */
	public RecordSong(Mixer pMixer, AudioFormat pAudioFormat, int pBufferSize) {
		this( pMixer, pAudioFormat, pBufferSize, TargetDataLine.class );
	}

	public RecordSong(
			Mixer pMixer,
			AudioFormat pAudioFormat,
			int pBufferSize,
			Class<? extends TargetDataLine> pTargetDataLineClass) {
		if ( pMixer == null ) {
			throw new NullPointerException( "pMixer may not be null" );
		}
		this.mMixer = pMixer;
		this.mAudioFormat = pAudioFormat;
		this.mBufferSize = pBufferSize;
		this.mTargetDataLineClass = pTargetDataLineClass;
	}

	@Override
	public Audio getAudio() throws MusicPlayerException {
		return new RecordAudio( this.mMixer, this.mAudioFormat, this.mBufferSize, this.mTargetDataLineClass );
	}

	@Override
	public long getFrameLength() {
		return AudioSystem.NOT_SPECIFIED;
	}

	@Override
	public String toString() {
		return "RecordSong " + this.mAudioFormat;
	}

}
