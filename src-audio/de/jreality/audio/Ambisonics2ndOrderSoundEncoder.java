package de.jreality.audio;

import java.util.Arrays;

public abstract class Ambisonics2ndOrderSoundEncoder extends AmbisonicsSoundEncoder {

	protected float[] br, bs, bt, bu, bv;
	
	@Override
	public void startFrame(int framesize) {
		super.startFrame(framesize);
		if (br == null || br.length != framesize) {
			br=new float[framesize];
			bs=new float[framesize];
			bt=new float[framesize];
			bu=new float[framesize];
			bv=new float[framesize];
		} else {
			Arrays.fill(br, 0f);
			Arrays.fill(bs, 0f);
			Arrays.fill(bt, 0f);
			Arrays.fill(bu, 0f);
			Arrays.fill(bv, 0f);
		}

	}
	
	protected void encodeOneSample(int idx, float[] samples, float x0, float y0, float z0) {
		
		super.encodeOneSample(idx, samples, x0, y0, z0);
		
		float v = samples[idx];

		br[idx] += v*(1.5f*z0*z0-0.5);
		bs[idx] += v*(2f*z0*x0);
		bt[idx] += v*(2f*y0*z0);
		bu[idx] += v*(x0*x0-y0*y0);
		bv[idx] += v*(2f*x0*y0);
		
	}

}
