package de.jreality.audio;

import java.util.Arrays;

/**
 * 
 * Simple first-order Ambisonics encoder; see http://www.muse.demon.co.uk/ref/speakers.html
 * for mathematical background.
 *
 */
public abstract class AmbisonicsSoundEncoder implements SoundEncoder {

	protected static final float W_SCALE = (float) Math.sqrt(0.5);
	protected float[] bw, bx, by, bz;

	public void startFrame(int framesize) {
		if (bw == null || bw.length != framesize) {
			bw=new float[framesize];
			bx=new float[framesize];
			by=new float[framesize];
			bz=new float[framesize];
		} else {
			Arrays.fill(bw, 0f);
			Arrays.fill(bx, 0f);
			Arrays.fill(by, 0f);
			Arrays.fill(bz, 0f);
		}
	}

	public void encodeSample(float v, int idx, float x, float y, float z, float r) {
		if (r>1e-6f) {
			// The point (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics.
			encodeAmbiSample(v, idx, -z/r, -x/r, y/r);
		}
	}

	protected void encodeAmbiSample(float v, int idx, float x, float y, float z) {
		bw[idx] += v*W_SCALE;
		bx[idx] += v*x;
		by[idx] += v*y;
		bz[idx] += v*z;
	}

	public abstract void finishFrame();
}
