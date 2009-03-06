package de.jreality.audio;

import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * @deprecated
 * 
 * Minimal implementation that treats sound as instantaneous, now largely obsolete
 *
 * TODO: handle curved geometries
 * 
 */
public class InstantaneousPath implements SoundPath {

	private float gain = AudioAttributes.DEFAULT_GAIN;
	private DistanceCue distanceCue = AudioAttributes.DEFAULT_DIRECTED_CUE;
	
	private int sampleRate;
	private SampleReader reader;
	private float samples[];
	
	private float x0, y0, z0;
	private boolean firstFrame = true;

	public InstantaneousPath(SampleReader reader, int sampleRate) {
		this.sampleRate = sampleRate;
		this.reader = ConvertingReader.createReader(reader, sampleRate);
	}
	
	public boolean processFrame(SoundEncoder enc, int frameSize, Matrix curPos, Matrix micInvMatrix, float[] directionlessBuffer) {
		if (samples==null || samples.length<frameSize) {
			samples = new float[frameSize];
		}
		
		int nRead = reader.read(samples, 0, frameSize);
		if (nRead==0) {
			firstFrame = true;
			return false;
		}
		
		curPos.multiplyOnLeft(micInvMatrix);
		float x1 = (float) curPos.getEntry(0, 3);
		float y1 = (float) curPos.getEntry(1, 3);
		float z1 = (float) curPos.getEntry(2, 3);
		
		if (firstFrame) {
			x0 = x1;
			y0 = y1;
			z0 = z1;
			firstFrame = false;
		}
		
		float dx = (x1-x0)/frameSize;
		float dy = (y1-y0)/frameSize;
		float dz = (z1-z0)/frameSize;
		
		for (int i=0; i<nRead; i++) {
			float r = (float) Math.sqrt(x0*x0+y0*y0+z0*z0);
			float v = distanceCue.nextValue(samples[i]*gain, r);
			enc.encodeSample(v, i, x0, y0, z0);
			
			x0 += dx;
			y0 += dy;
			z0 += dz;
		}

		return true;
	}	

	public void setProperties(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(AudioAttributes.VOLUME_GAIN_KEY, AudioAttributes.DEFAULT_GAIN);
		List<Class<? extends DistanceCue>> cueChain = (List<Class<? extends DistanceCue>>) eapp.getAttribute(AudioAttributes.DISTANCE_CUE_KEY, null, List.class);
		try {
			DistanceCue c = DistanceCueChain.create(cueChain);
			c.setSampleRate(sampleRate);
			distanceCue = c;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
