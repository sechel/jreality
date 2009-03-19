package de.jreality.audio;

import java.util.Arrays;

/**
 * Simple simulation of early reflections, based on Moorer's "About This Reverberation Business."
 * 
 * @author brinkman
 *
 */
public class EarlyReflections implements DistanceCue {

	private static final float[] tapTimes = {.0199f, .0354f, .0389f, .0414f, .0699f, .0796f}; // numbers from Table 3
	private static final float[] gains = {1.02f, .818f, .635f, .719f, .267f, .242f};
	private static final int nTaps = tapTimes.length;
	private int[] offsets = new int[nTaps];
	
	private float[] delayLine;
	private int maxDelay;
	private int index = 0;
	private int samplesLeft = 0;
	
	public boolean hasMore() {
		return samplesLeft>0;
	}

	public float nextValue(float v, float r, float mic, float mic2, float mic3) {
		float u = v;
		for(int i=0; i<nTaps; i++) {
			u += delayLine[(index+offsets[i]) % maxDelay]*gains[i];
		}
		delayLine[index++] = v;
		if (index>=maxDelay) {
			index -= maxDelay;
		}
		if (v>AudioAttributes.HEARING_THRESHOLD) {
			samplesLeft = maxDelay;
		} else if (samplesLeft>0){
			samplesLeft--;
		}
		return u;
	}

	public void reset() {
		Arrays.fill(delayLine, 0);
		samplesLeft = 0;
	}

	public void setSampleRate(float sr) {
		int m = 0;
		for(int i=0; i<nTaps; i++) {
			int n = offsets[i] = (int) (sr*tapTimes[i]+0.5);
			if (n>m) {
				m = n;
			}
		}
		delayLine = new float[m];
		maxDelay = m;
	}
}
