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
	private float value;
	private boolean firstValue = true;
	
	/**
	 * 
	 * convenience method, intended for use in instances of {@link SoundEncoder}
	 * 
	 * @param sampleRate
	 * @param frameSize
	 * @param cutOff
	 * @return
	 */
	static float filterCoefficient(int sampleRate, int frameSize, float cutOff) {
		return filterCoefficient(((float) sampleRate)/frameSize, cutOff);
	}
	
	static float filterCoefficient(float rate, float cutOff) {
		float tau = (float) (1/(2*Math.PI*cutOff));  // RC time constant
		float alpha = 1/(1+tau*rate);
		return alpha;
	}
	
	/**
	 * 
	 * @param v: next sample
	 * @param alpha: filter coefficient
	 * @return
	 */
	float nextValue(float v, float alpha) {
		if (firstValue) {
			firstValue = false;
			value = v;
		} else {
			value += alpha*(v-value);
		}
		return value;
	}
	
	void reset() {
		firstValue = true;
	}
}
