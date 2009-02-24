package de.jreality.audio;

/**
 * 
 * Simple low-pass filter (discretization of an RC low-pass filter); see http://en.wikipedia.org/wiki/Low-pass_filter
 * for background.
 * 
 * @author brinkman
 *
 */
class LowPassFilter {
	private float sampleRate;
	private float cutOff;
	private float alpha;
	private float value = 0f;
	
	public LowPassFilter(float sampleRate, float cutOff) {
		this.sampleRate = sampleRate;
		setCutOff(cutOff);
	}
	
	public void setCutOff(float cutOff) {
		this.cutOff = cutOff;
		float tau = (float) (1/(2*Math.PI*cutOff));  // RC time constant
		alpha = 1/(1+tau*sampleRate);
	}
	
	public float getCutOff() {
		return cutOff;
	}

	public float initialize(float v) {
		value = v;
		return v;
	}
	
	public float nextValue(float v) {
		value += alpha*(v-value);
		return value;
	}
}
