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
	private LowPassFilter lpf;
	
	public LowPassReader(SampleReader reader, int sampleRate, float cutOff) {
		this.reader = reader;
		lpf = new LowPassFilter(sampleRate, cutOff);
	}
	
	public void setCutOff(float cutOff) {
		lpf.setCutOff(cutOff);
	}

	public float getCutoff() {
		return lpf.getCutOff();
	}
	
	public void clear() {
		reader.clear();
		lpf.initialize(0);
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		int nRead = reader.read(buffer, initialIndex, nSamples);
		
		for(int i = initialIndex; i<nRead; i++) {
			buffer[i] = lpf.nextValue(buffer[i]);
		}
		
		return nRead;
	}
}
