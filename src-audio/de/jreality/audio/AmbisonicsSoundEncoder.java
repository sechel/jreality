package de.jreality.audio;

import java.util.Arrays;

import de.jreality.math.Matrix;

public abstract class AmbisonicsSoundEncoder implements SoundEncoder {

	private static final float W_SCALE = (float) Math.sqrt(0.5);
	protected float[] bw, bx, by, bz;
	
	public void encodeSignal(float[] samples, int nSamples, Matrix p0, Matrix p1) {
		
		// read start and dest directions from matrices: 
		float x0 = (float) p0.getEntry(0, 3);
		float y0 = (float) p0.getEntry(1, 3);
		float z0 = (float) p0.getEntry(2, 3);

		float x1 = (float) p1.getEntry(0, 3);
		float y1 = (float) p1.getEntry(1, 3);
		float z1 = (float) p1.getEntry(2, 3);
		
		// the point (x, y, z) in graphics corresponds to (-z, -x, y) in Ambisonics
		encodeFrame(samples, nSamples, bw, bx, by, bz, -z0, -x0, y0, -z1, -x1, y1);
	}

	public abstract void finishFrame();

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

	public static void encodeFrame(float[] raw, int nSamples, float[] bw, float[] bx,
			float[] by, float[] bz, float x0, float y0, float z0, float x1,
			float y1, float z1) {
		
		float dx = (x1-x0)/nSamples;
		float dy = (y1-y0)/nSamples;
		float dz = (z1-z0)/nSamples;
		
		for(int i = 0; i<nSamples; i++) {
			x0 += dx;
			y0 += dy;
			z0 += dz;

			float r = (float) (Math.sqrt(x0*x0+y0*y0+z0*z0)+1e-5);
			
			float v = raw[i];
			bw[i] += v*W_SCALE;
			bx[i] += v*x0/r;
			by[i] += v*y0/r;
			bz[i] += v*z0/r;
		}
	}

}
