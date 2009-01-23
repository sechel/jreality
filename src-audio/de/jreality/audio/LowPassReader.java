package de.jreality.audio;

/**
 * 
 * Simple reader with a low-pass filter, mostly as a proof of concept.
 * 
 * @author brinkman
 *
 */
public class LowPassReader implements SampleReader {

	private SampleReader reader;
	private int sampleRate;
	private float cutOff;
	private float alpha;
	private LowPassFilter lpf = new LowPassFilter();
	private float samples[];
	
	public LowPassReader(SampleReader reader, int sampleRate, float cutoff) {
		this.reader = reader;
		this.sampleRate = sampleRate;
		setCutOff(cutoff);
	}
	
	public void setCutOff(float cutOff) {
		this.cutOff = cutOff;
		alpha = LowPassFilter.filterCoefficient(sampleRate, cutOff);
	}

	public float getCutoff() {
		return cutOff;
	}
	
	public void clear() {
		reader.clear();
		lpf.reset();
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		if (samples==null || samples.length<nSamples) {
			samples = new float[nSamples];
		}
		int nRead = reader.read(samples, 0, nSamples);
		
		for(int i = initialIndex; i<nRead; i++) {
			buffer[i] = lpf.nextValue(samples[i], alpha);
		}
		
		return nRead;
	}
}
