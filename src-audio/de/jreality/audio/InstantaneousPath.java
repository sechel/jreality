package de.jreality.audio;

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

	private float gain = DEFAULT_GAIN;
	private Attenuation attenuation = DEFAULT_ATTENUATION;
	
	private SampleReader reader;
	private float samples[];
	
	private float x0, y0, z0;
	private boolean firstFrame = true;

	public InstantaneousPath(SampleReader reader, int sampleRate) {
		this.reader = ConvertingReader.createReader(reader, sampleRate);
	}
	
	public boolean processFrame(SoundEncoder enc, int frameSize, Matrix curPos, Matrix micInvMatrix) {
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
			float v = attenuation.attenuate(samples[i]*gain, r);
			enc.encodeSample(v, i, x0, y0, z0);
			
			x0 += dx;
			y0 += dy;
			z0 += dz;
		}

		return true;
	}	

	public void setProperties(EffectiveAppearance eapp) {
		gain = eapp.getAttribute(VOLUME_GAIN_KEY, DEFAULT_GAIN);
		attenuation = (Attenuation) eapp.getAttribute(VOLUME_ATTENUATION_KEY, DEFAULT_ATTENUATION, Attenuation.class);
	}
}
