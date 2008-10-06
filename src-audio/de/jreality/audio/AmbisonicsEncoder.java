package de.jreality.audio;

/*
 * Note that computer graphics and Ambisonics use different conventions regarding the the labelling of
 * coordinate axes.  Both use right-handed coordinate systems, but graphics has the x-axis pointing right
 * and the y-axis pointing up, while Ambisonics has the x-axis pointing forward and the y-axis pointing
 * left.  In other words, the point (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics.
 */
public class AmbisonicsEncoder {
	
	private static final boolean distanceFalloff = false;
		
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
	
	public void addSignal(float raw[], int nSamples, float bw[], float bx[], float by[], float bz[], float x, float y, float z, boolean ramp) {
		float r = (float) (Math.sqrt(x*x+y*y+z*z)+1e-5);
		x /= r;
		y /= r;
		z /= r;
		
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
			
			float v = distanceFalloff(raw[i], r);
			bw[i] += v*wScale;
			bx[i] += v*xPos;
			by[i] += v*yPos;
			bz[i] += v*zPos;
		}
	}

	private static final float distanceFalloff(float f, float r) {
		if (!distanceFalloff) return f;
		final float falloffMin=5, falloffMax=100;
		float damping = (r-falloffMin)/(falloffMax-falloffMin);
		if (damping < 0f) damping = 0f;
		if (damping > 1f) damping = 1f;
		return f*(1f-damping);
	}
}
