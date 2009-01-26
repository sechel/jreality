package de.jreality.audio;

/**
 * Simple interface for encapsulating different kinds of volume attenuation, to be used as an appearance property
 * 
 * @author brinkman
 *
 */
public interface Attenuation {

	public static final Attenuation CONSTANT = new Attenuation() {
		public float attenuate(float v, float r) {
			return v;
		}
	};
	
	public static final Attenuation LINEAR = new Attenuation() {
		public float attenuate(float v, float r) {
			return v/Math.max(r, 1);
		}
	};
	
	public static final Attenuation EXPONENTIAL = new Attenuation() {
		public float attenuate(float v, float r) {
			return v/(float) Math.exp(r);
		}	
	};
	
	float attenuate(float v, float r);
}
