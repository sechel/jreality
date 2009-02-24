package de.jreality.audio;

/**
 * Simple interface for encapsulating various distance cues, such as volume attenuation,
 * low-pass filtering, reverberation, etc.  To be used as an appearance property.
 * 
 * @author brinkman
 *
 */
public interface DistanceCue {

	public static final DistanceCue CONSTANT = new DistanceCue() {
		public float nextValue(float v, float r) {
			return v;
		}
		public void reset() {}
	};
	
	public static final DistanceCue LINEAR = new DistanceCue() {
		public float nextValue(float v, float r) {
			return v/Math.max(r, 1);
		}
		public void reset() {}
	};
	
	public static final DistanceCue EXPONENTIAL = new DistanceCue() {
		public float nextValue(float v, float r) {
			return v/(float) Math.exp(r);
		}	
		public void reset() {}
	};
	
	void reset();
	float nextValue(float v, float r);
}
