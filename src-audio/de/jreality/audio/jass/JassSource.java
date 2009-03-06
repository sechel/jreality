package de.jreality.audio.jass;

import jass.engine.BufferNotAvailableException;
import jass.engine.Sink;
import jass.engine.SinkIsFullException;
import jass.engine.Source;

import java.util.ArrayList;
import java.util.List;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

public class JassSource extends AudioSource implements Sink {

	long t=0;
	private List<Source> sourceContainer=new ArrayList<Source>();
	protected int bufferSize;
	private float[] tempBuf;

	public JassSource(String name, int sampleRate, int bufferSize) {
		super(name);
		this.sampleRate = sampleRate;
		this.bufferSize=bufferSize;
		tempBuf = new float[bufferSize];
		ringBuffer = new RingBuffer(sampleRate);
	}

	protected float[] getNextBuffer() {
		for(int k=0;k<bufferSize;k++) {
			tempBuf[k] = 0;
		}
		// Mix down all sources
		for(Source source : sourceContainer) {
			try {
				float[] y = source.getBuffer(t);
				for(int k=0;k<bufferSize;k++) {
					tempBuf[k] += y[k];
				}
			} catch (BufferNotAvailableException bnae) {
				bnae.printStackTrace();
			}
		}

		t++;
		return tempBuf;
	}

	@Override
	protected void reset() {
		startWriter();
		try {
			t=0;
			for(Source s: sourceContainer) {
				if (s.getTime()!=t) {
					s.setTime(t);
				}
			}
		} finally {
			finishWriter();
		}
	}

	@Override
	protected void writeSamples(int n) {
		int written = 0;
		while (written < n) {
			float[] buffer = getNextBuffer();
			ringBuffer.write(buffer, 0, buffer.length);
			written+=buffer.length;
		}
	}

	public Object addSource(Source s) throws SinkIsFullException {
		startWriter();
		try {
			sourceContainer.add(s);
		} finally {
			finishWriter();
		}
		return null;
	}

	public Source[] getSources() {
		Source[] result = new Source[sourceContainer.size()];
		return sourceContainer.toArray(result);
	}

	public void removeSource(Source s) {
		startWriter();
		try {
			sourceContainer.remove(s);
		} finally {
			finishWriter();
		}
	}
}
