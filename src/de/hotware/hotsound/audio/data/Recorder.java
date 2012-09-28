package de.hotware.hotsound.audio.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

import de.hotware.hotsound.audio.data.IAudioDevice.AudioDeviceException;

public class Recorder implements AutoCloseable {

	private BufferedOutputStream mBufferedOutputStream;
	private File mFile;
	private File mTempFile;
	private IHeader mHeader;
	private int mBytesWritten;
	private boolean mClosed;

	public Recorder(File pFile) {
		if(pFile == null) {
			throw new IllegalArgumentException("pFile may not be null");
		}
		this.mFile = pFile;
		this.mTempFile = new File(pFile.getAbsolutePath() + ".tmp");
		this.mBytesWritten = 0;
		this.mClosed = true;
	}

	/**
	 *
	 * @throws IllegalStateException if opened while not being closed;
	 */
	public void open(AudioFormat pAudioFormat) throws AudioDeviceException,
			IOException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Recorder is already opened");
		}
		if(this.mTempFile.exists()) {
			// TODO handle return value of delete(), if file can't be deleted, your invariants break.
			this.mFile.delete();
		}
		// TODO And again, handle return value!
		this.mTempFile.createNewFile();
		this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mTempFile));
		this.mHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
				(short) pAudioFormat.getChannels(),
				(int) pAudioFormat.getSampleRate(),
				(short) pAudioFormat.getSampleSizeInBits(),
				Integer.MAX_VALUE);
		this.mHeader.write(this.mBufferedOutputStream);
		this.mClosed = false;
	}

	public int write(byte[] pData, int pStart, int pLength) throws IOException {
		if(this.mClosed) {
			throw new IllegalStateException("The Recorder is not open");
		}
		this.mBufferedOutputStream.write(pData, pStart, pLength);
		this.mBytesWritten += pLength;
		return pLength;
	}

	public void close() throws IOException {
		if(this.mBufferedOutputStream != null) {
			boolean delete = false;
			try(BufferedInputStream input = new BufferedInputStream(new FileInputStream(this.mTempFile))) {
				this.mBufferedOutputStream.flush();
				this.mBufferedOutputStream.close();
				((WaveHeader) this.mHeader).read(input);
				((WaveHeader) this.mHeader).setNumBytes(this.mBytesWritten);
				if(this.mFile.exists()) {
					// TODO It gets boring... Check your return values!
					this.mFile.delete();
				}

				// TODO I'm repeating myself...
				this.mFile.createNewFile();
				delete = true;
				this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mFile));
				int bytesRead = 0;
				byte[] data = new byte[128000];	// Magic is in the air. The number emits it.
				this.mHeader.write(this.mBufferedOutputStream);
				while((bytesRead = input.read(data, 0, 128000)) != -1) {	// The wild magic number appers once again!
					this.write(data, 0, bytesRead);
				}
			} catch(IOException e) {
				//only delete if a new file has been written over a possible old file
				if(delete) {
					// TODO Check your freakin' return values!
					this.mFile.delete();
				}
				throw e;
			} finally {
				this.mClosed = true;

				// TODO I start to get angry. Check 'em!
				this.mTempFile.delete();

				// No need to flush, is included in close().
				this.mBufferedOutputStream.close();
				this.mBufferedOutputStream = null;
			}
		}
	}

}
