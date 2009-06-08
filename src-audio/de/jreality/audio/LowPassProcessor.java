package de.jreality.audio;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Simple reader with a low-pass filter, mostly as a proof of concept.
 * 
 * @author brinkman
 *
 */
public class LowPassProcessor implements SampleProcessor {

	private SampleReader reader;
	private LowPassFilter lpf;
	
	public LowPassProcessor() {
		// do nothing
	}
	
	public LowPassProcessor(SampleReader reader) {
		initialize(reader);
	}

	public void initialize(SampleReader reader) {
		this.reader = reader;
		lpf = new LowPassFilter(reader.getSampleRate());
	}
	
	public int getSampleRate() {
		return reader.getSampleRate();
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

	public void setProperties(EffectiveAppearance app) {
		setCutOff(app.getAttribute("lowPassProcessorCutOff", 44000));
	}
}
