package de.jreality.audio;

import java.util.Arrays;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

/**
 * Simple simulation of early reflections, based on Moorer's "About This Reverberation Business."
 * 
 * @author brinkman
 *
 */
public class EarlyReflections implements SampleProcessor {

	private static final float[] tapTimes = {.0199f, .0354f, .0389f, .0414f, .0699f, .0796f}; // numbers from Table 3
	private static final float[] gains = {1.02f, .818f, .635f, .719f, .267f, .242f};
	private static final int nTaps = tapTimes.length;
	private int[] offsets = new int[nTaps];
	
	private float[] delayLine;
	private int maxDelay;
	private int index = 0;
	
	SampleReader reader;

	public EarlyReflections() {
		// do nothing
	}

	public void setProperties(EffectiveAppearance app) {
		// do nothing for the time being
	}

	public void initialize(SampleReader reader) {
		this.reader = reader;
		maxDelay = 0;
		for(int i=0; i<nTaps; i++) {
			int n = offsets[i] = (int) (reader.getSampleRate()*tapTimes[i]+0.5);
			if (n>maxDelay) {
				maxDelay = n;
			}
		}
		delayLine = new float[maxDelay];
	}

	public void clear() {
		Arrays.fill(delayLine, 0);
		reader.clear();
	}

	public int getSampleRate() {
		return reader.getSampleRate();
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		int nRead = reader.read(buffer, initialIndex, nSamples);
		for(int i=initialIndex; i<initialIndex+nSamples; i++) {
			float u = buffer[i];
			for(int j=0; j<nTaps; j++) {
				u += delayLine[(index+offsets[j]) % maxDelay]*gains[j];
			}
			delayLine[index++] = buffer[i];
			if (index>=maxDelay) {
				index -= maxDelay;
			}
			buffer[i] = u;
		}
		return nRead;
	}
	
	public boolean hasMore() {
		return false;  // TODO: implement properly
	}
}
