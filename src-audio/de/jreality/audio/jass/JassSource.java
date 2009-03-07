package de.jreality.audio.jass;

import jass.engine.BufferNotAvailableException;
import jass.engine.Sink;
import jass.engine.SinkIsFullException;
import jass.engine.Source;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * 
 * A simple wrapper class that allows for Jass patches to be used in an audio source.
 * 
 */
public class JassSource extends AudioSource implements Sink {

	protected long time=0;
	private List<Source> sourceContainer=new CopyOnWriteArrayList<Source>();
	protected int bufferSize;
	private float[] buffer;

	
	public JassSource(String name, int sampleRate, int bufferSize) {
		super(name);
		this.sampleRate = sampleRate;
		this.bufferSize=bufferSize;
		buffer = new float[bufferSize];
		ringBuffer = new RingBuffer(sampleRate);
	}

	protected float[] getNextBuffer() {
		Arrays.fill(buffer, 0f);
		// Mix down all sources
		for(Source source : sourceContainer) {
			try {
				float[] y = source.getBuffer(time);
				for(int k=0;k<bufferSize;k++) {
					buffer[k] += y[k];
				}
			} catch (BufferNotAvailableException bnae) {
				bnae.printStackTrace();
			}
		}

		time++;
		return buffer;
	}
	
	@Override
	protected void writeSamples(int n) {
		int written = 0;
		while (written < n) {
			float[] buffer = getNextBuffer();
			ringBuffer.write(buffer, 0, buffer.length);
			written += buffer.length;
		}
	}

	public Object addSource(Source s) throws SinkIsFullException {
		sourceContainer.add(s);
		return null;
	}

	public void removeSource(Source s) {
		sourceContainer.remove(s);
	}

	public Source[] getSources() {
		return (Source[]) sourceContainer.toArray();
	}

	@Override
	protected void reset() {
		time=0;
		for(Source s: sourceContainer) {
			if (s.getTime()!=time) {
				s.setTime(time);
			}
		}
	}
}
