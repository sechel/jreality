package de.jreality.audio;

/**
 * Simple interface for encapsulating various distance cues, such as volume attenuation,
 * low-pass filtering, reverberation, etc.  To be used as an appearance property, with
 * a usable set of simple implementations for most common purposes.
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
		public float nextValue(float v, float r, float x, float y, float z) {
			return v;
		}
	};
	
	public static final class LINEAR extends Attenuation {
		public float nextValue(float v, float r, float x, float y, float z) {
			return v/Math.max(r, 1);
		}
	};
	
	public static final class EXPONENTIAL extends Attenuation {
		public float nextValue(float v, float r, float x, float y, float z) {
			return v/(float) Math.exp(r);
		}
	};
	
	public static final class CONICAL extends Attenuation {
		public float nextValue(float v, float r, float xMic, float yMic, float zMic) {
			float rMic = xMic*xMic+yMic*yMic+zMic*zMic;
			return (zMic>0) ? v*zMic*zMic/rMic : 0;
		}
	}
	
	public static final class LOWPASS extends LowPassFilter implements DistanceCue {
		public float nextValue(float v, float r, float x, float y, float z) {
			setCutOff(44000/(1+r));
			return nextValue(v);
		}
		public void reset() {
			initialize(0);
		}
	};
	
	
	void setSampleRate(float sr);
	
	/**
	 * @return true if there will be audible output in the future, even if all future inputs are zero
	 */
	boolean hasMore();
	
	/**
	 * Computes the next value, based on the new sample v and the distance r, as well as the location of
	 * the microphone relative to the sound source.  Note that r^2 may not be equal to xMic^2+yMic^2+zMic^2
	 * if the transformation from source to microphone coordinates is not an isometry.
	 * 
	 * The microphone position is intended for sound sources with directional characteristics.  Since
	 * directional characteristics will not be used routinely, the sound path is not expected to interpolate
	 * xMic, yMic, and zMic.  Any such operations are the responsibility of implementations of this interface.
	 * 
	 * @param v sample
	 * @param r distance from observer when sample is heard (in microphone coordinates)
	 * @param x, y, z  location of microphone (in source coordinates)
	 * @return updated value based on v and r
	 */
	float nextValue(float v, float r, float xMic, float yMic, float zMic);
	
	void reset();
}
