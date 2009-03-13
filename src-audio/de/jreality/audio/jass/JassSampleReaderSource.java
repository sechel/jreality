package de.jreality.audio.jass;

import jass.engine.BufferNotAvailableException;
import jass.engine.Source;

import java.util.Arrays;

import de.jreality.scene.data.SampleReader;

/**
 * 
 * This class provides an adapter between jReality's sample readers and Jass sources.
 * It treats time somewhat casually; when asked for a future buffer, it always returns
 * the next batch of samples from the ring buffer.  If sufficiently many new samples are
 * not available, it returns as many samples as it has at the time.  When asked for the
 * current buffer or a buffer in the past, then it returns the current buffer.  In other
 * words, it never throws a BufferNotAvailableException; this is the desired behavior for
 * an adapter between audio components that may use widely different frame sizes.
 * 
 * @author brinkman
 *
 */
public class JassSampleReaderSource implements Source {

	private long sourceTime = 0;
	private float[] buffer;
	private int bufferSize;
	private int currentPosition = 0;
	private SampleReader reader;

	public JassSampleReaderSource(SampleReader reader, int bufferSize) {
		setBufferSize(bufferSize);
		this.reader = reader;
	}

	public void clearBuffer() {
		Arrays.fill(buffer, 0);
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		buffer = new float[bufferSize];
	}

	public float[] getBuffer(long t) throws BufferNotAvailableException {
		if (t>=sourceTime) {
			currentPosition += reader.read(buffer, currentPosition, bufferSize-currentPosition);
			if (currentPosition>=bufferSize) {
				currentPosition = 0;
				sourceTime = t+1;
			}
		}
		return buffer;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public long getTime() {
		return sourceTime;
	}

	public void setTime(long t) {
		sourceTime = t;
	}
}
