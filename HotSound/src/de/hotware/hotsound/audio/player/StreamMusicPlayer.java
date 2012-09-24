/**
 * File StreamMusicPlayer.java
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
package de.hotware.hotsound.audio.player;

/**
 *  @author Martin Braun
 *  Player inspired by Matthias Pfisterer's examples on JavaSound
 *  (jsresources.org). Because of the fact, that this Software is meant 
 *  to be Open-Source and I don't want to get anybody angry about me 
 *  using parts of his intelligence without mentioning it, I hereby 
 *  mention him as inspiration, because his code helped me to write this class.
 */

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import de.hotware.hotsound.audio.player.StreamPlayerRunnable.IPlayerRunnableListener;

public class StreamMusicPlayer implements IMusicPlayer {

	protected ExecutorService mExecService;
	protected StreamPlayerRunnable mPlayerRunnable;
	protected IPlayerRunnableListener mPlayerThreadListener;
	private Lock mLock;

	public StreamMusicPlayer() {
		this(null);
	}

	public StreamMusicPlayer(IPlayerRunnableListener pPlayerThreadListener) {
		this.mLock = new ReentrantLock();
		this.mPlayerThreadListener = pPlayerThreadListener;
		this.mExecService = Executors.newSingleThreadExecutor();
	}

	/**
	 * @inheritDoc
	 * @throws SongInsertionException
	 *             if audio file is either not supported, its line is not
	 *             available or an IOException has been thrown in the underlying
	 *             methods
	 */
	@Override
	public void insert(ISong pSong) throws SongInsertionException {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable != null &&
					!this.mPlayerRunnable.isStopped()) {
				throw new IllegalStateException("You can only insert Songs while the Player is stopped!");
			}
			try {
				this.mPlayerRunnable = new StreamPlayerRunnable(pSong,
						this.mPlayerThreadListener);
			} catch(UnsupportedAudioFileException
					| IOException
					| LineUnavailableException e) {
				throw new SongInsertionException("Couldn't insert " + pSong, e);
			}
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void startPlayback() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			if(!this.mPlayerRunnable.isStopped()) {
				throw new IllegalStateException("Player is already playing");
			}
			this.mExecService.execute(this.mPlayerRunnable);
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void pausePlayback() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mPlayerRunnable.pausePlayback();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void stopPlayback() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mPlayerRunnable.stopPlayback();
			this.mPlayerRunnable = null;
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isStopped() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mPlayerRunnable.isStopped();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public void unpausePlayback() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			this.mPlayerRunnable.unpausePlayback();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public boolean isPaused() {
		return this.mPlayerRunnable.isPaused();
	}

	@Override
	public AudioFormat getAudioFormat() {
		return this.mPlayerRunnable.getAudioFormat();
	}

	@Override
	public Control[] getControls() {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mPlayerRunnable.getControls();
		} finally {
			this.mLock.unlock();
		}
	}

	@Override
	public Control getControl(Control.Type pType) {
		this.mLock.lock();
		try {
			if(this.mPlayerRunnable == null) {
				throw new IllegalStateException(this +
						" has not been initialized yet!");
			}
			return this.mPlayerRunnable.getControl(pType);
		} finally {
			this.mLock.unlock();
		}
	}

}
