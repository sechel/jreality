package de.jreality.audio.jass;

import jass.engine.SinkIsFullException;
import jass.patches.CombReverb;
import de.jreality.audio.SampleProcessor;
import de.jreality.scene.data.RingBuffer;
import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public class JassReverb implements SampleProcessor {

	private RingBuffer ringBuffer;
	private int sampleRate;
	private static final int bufferSize = 32;
	private CombReverb reverb;
	private SampleReader reader;
	
	
	public JassReverb() {
		// do nothing
	}
	
	public void setProperties(EffectiveAppearance app) {
		// TODO: set reverb properties via appearance
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
		ringBuffer = new RingBuffer(sampleRate);
		reverb = new CombReverb(bufferSize, sampleRate, 3);
		JassSource js = new JassSource("directionless reverb node", sampleRate, bufferSize);
		reader = js.createReader();
		try {
			reverb.addSource(new JassAdapter(ringBuffer, bufferSize));
			reverb.setDryToWet(0);
			js.addSource(reverb);
		} catch (SinkIsFullException e) {
			e.printStackTrace();
		}
		js.start();
	}

	public void write(float[] buf, int initialIndex, int samples) {
		ringBuffer.write(buf, initialIndex, samples);
	}

	public void clear() {
		reader.clear();
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int read(float[] buffer, int initialIndex, int samples) {
		return reader.read(buffer, initialIndex, samples);
	}
}
