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
		public boolean hasMore() { return false; }
		public void reset() {}
	}
	
	public static final class CONSTANT extends Attenuation {
		public float nextValue(float v, float r) {
			return v;
		}
	};
	
	public static final class LINEAR extends Attenuation {
		public float nextValue(float v, float r) {
			return v/Math.max(r, 1);
		}
	};
	
	public static final class EXPONENTIAL extends Attenuation {
		public float nextValue(float v, float r) {
			return v/(float) Math.exp(r);
		}
	};
	
	public static final DistanceCue DEFAULT_CUE = new CONSTANT();
	
	
	void setSampleRate(float sr);
	
	/**
	 * @return true if there will be audible output in the future, even if all future inputs are zero
	 */
	boolean hasMore();
	
	/**
	 * Computes the next value, based on the new sample v and the distance r
	 * @param v sample
	 * @param r distance from observer when sample is heard
	 * @return updated value based on v and r
	 */
	float nextValue(float v, float r);
	
	void reset();
}
