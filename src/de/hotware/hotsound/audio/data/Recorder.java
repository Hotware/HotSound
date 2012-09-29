package de.hotware.hotsound.audio.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;

class Recorder implements AutoCloseable {

	private BufferedOutputStream mBufferedOutputStream;
	private File mFile;
	private File mTempFile;
	private IAudioFileHeader mHeader;
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
	public void open(AudioFormat pAudioFormat) throws IOException {
		if(!this.mClosed) {
			throw new IllegalStateException("The Recorder is already opened");
		}
		createEmptyFile(this.mTempFile);
		this.mBufferedOutputStream = new BufferedOutputStream(new FileOutputStream(this.mTempFile));
		this.mHeader = new WaveFileHeader(WaveFileHeader.FORMAT_PCM,
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
				((WaveFileHeader) this.mHeader).read(input);
				((WaveFileHeader) this.mHeader).setNumBytes(this.mBytesWritten);
				createEmptyFile(this.mFile);
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
					if(!this.mTempFile.delete()) {
						throw new IOException("couldn't delete failure file", e);
					}
				}
				throw e;
			} finally {
				this.mClosed = true;
				// No need to flush, is included in close().
				try {
					this.mBufferedOutputStream.close();
					this.mBufferedOutputStream = null;
				} finally {
					if(!this.mTempFile.delete()) {
						throw new IOException("couldn't delete the tempfile");
					}
				}
			}
		}
	}
	
	private static void createEmptyFile(File pFile) throws IOException {
		if(pFile.exists()) {
			if(!pFile.delete()) {
				throw new IOException("couldn't delete the old file " + pFile.getAbsolutePath());
			}
		}
		if(!pFile.createNewFile()) {
			throw new IOException("couldn't create the file " + pFile.getAbsolutePath());
		}
	}

}
