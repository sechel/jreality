package de.jreality.audio;

/**
 * 
 * A simple Ambisonics encoder that takes an array of samples as well as a location in space and computes
 * a signal in Ambisonics B-format.
 * 
 * Note that computer graphics and Ambisonics use different conventions regarding the the labelling of
 * coordinate axes.  Both use right-handed coordinate systems, but graphics has the x-axis pointing right
 * and the y-axis pointing up, while Ambisonics has the x-axis pointing forward and the y-axis pointing
 * left.  In other words, the point (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics.
 * 
 * TODO: add distance cues such as delay, attenuation, low-pass filtering, and reverberation; implement
 * interpolation of delays to achieve Doppler effects; contemplate waves other than spherical waves
 * 
 * @author brinkman
 * 
 */
public class AmbisonicsEncoder {
	
	private float xPos, yPos, zPos;
	private boolean hasPos = false;
	private static final float wScale = (float) Math.sqrt(.5);
	
	public AmbisonicsEncoder() {
		// do nothing
	}

	public static void clearBuffers(float[] bw, float[] bx, float[] by, float[] bz, int nSamples) {
		for(int i = 0; i<nSamples; i++) {
			bw[i] = bx[i] = by[i] = bz[i] = 0.0f;
		}
	}
	
	public void addSignal(float raw[], int nSamples, float bw[], float bx[], float by[], float bz[], float x, float y, float z, boolean ramp, float volume, boolean falloff) {
		
		if (!hasPos || !ramp) {
			xPos = x;
			yPos = y;
			zPos = z;
			hasPos = true;
		}
		
		float dx = (x-xPos)/nSamples;
		float dy = (y-yPos)/nSamples;
		float dz = (z-zPos)/nSamples;
		
		
		
		for(int i = 0; i<nSamples; i++) {
			xPos += dx;
			yPos += dy;
			zPos += dz;

			float r = (float) (Math.sqrt(xPos*xPos+yPos*yPos+zPos*zPos)+1e-5);
			
			float v = volume*distanceFalloff(falloff, raw[i], r);
			bw[i] += v*wScale;
			bx[i] += v*xPos/r;
			by[i] += v*yPos/r;
			bz[i] += v*zPos/r;
		}
	}

	private static final float distanceFalloff(boolean falloff, float signal, float r) {
		if (!falloff) return signal;
		float den = (float) Math.sqrt(r*r+1);
		return signal/den;
	}
}