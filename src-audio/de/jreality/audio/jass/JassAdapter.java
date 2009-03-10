package de.jreality.audio.jass;

import java.util.Arrays;

import de.jreality.audio.RingBuffer;

import jass.engine.BufferNotAvailableException;
import jass.engine.Source;

/**
 * 
 * This class provides an adapter between jReality's ring buffers and Jass sources.
 * It treats time somewhat casually; when asked for a future buffer, it always returns
 * the next batch of samples from the ring buffer.  If sufficiently many new samples are
 * not available, it returns zeros.  When asked for the current buffer or a buffer in the
 * past, then it returns the current buffer.  In other words, it never throws a
 * BufferNotAvailableException; this is the desired behavior for an adapter between audio
 * components that may use widely different frame sizes.
 * 
 * @author brinkman
 *
 */
public class JassAdapter implements Source {

	private long sourceTime = 0;
	private float[] buffer;
	private int bufferSize;
	private RingBuffer.Reader reader;

	public JassAdapter(RingBuffer ringBuffer, int bufferSize) {
		setBufferSize(bufferSize);
		reader = ringBuffer.createReader();
	}

	public void clearBuffer() {
		Arrays.fill(buffer, 0);
	}

	public float[] getBuffer(long t) throws BufferNotAvailableException {
		if (t>=sourceTime) {
			if (reader.valuesLeft()>=bufferSize) {
				reader.read(buffer, 0, bufferSize);
				sourceTime = t+1;
			} else {
				clearBuffer(); // return zeros when no new samples are available
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

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		buffer = new float[bufferSize];
	}

	public void setTime(long t) {
		sourceTime = t;
	}
}
