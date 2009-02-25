package de.jreality.audio;

/**
 * Simple interface for encapsulating various distance cues, such as volume attenuation,
 * low-pass filtering, reverberation, etc.  To be used as an appearance property.
 * 
 * @author brinkman
 *
 */
public interface DistanceCue {

	public static abstract class Attenuation implements DistanceCue {
		public void setSampleRate(float sr) {}
		public void reset() {}
	}
	
	public static final DistanceCue CONSTANT = new Attenuation() {
		public float nextValue(float v, float r) {
			return v;
		}
	};
	
	public static final DistanceCue LINEAR = new Attenuation() {
		public float nextValue(float v, float r) {
			return v/Math.max(r, 1);
		}
	};
	
	public static final DistanceCue EXPONENTIAL = new Attenuation() {
		public float nextValue(float v, float r) {
			return v/(float) Math.exp(r);
		}
	};
	
	void setSampleRate(float sr);
	float nextValue(float v, float r);
	void reset();
}
