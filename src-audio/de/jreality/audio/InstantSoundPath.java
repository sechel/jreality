package de.jreality.audio;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * A simple sound path that treats sound as instantaneous, i.e., delays and Doppler shifts and
 * such are disregarded.
 *
 * TODO: handle curved geometries
 * 
 */
public class InstantSoundPath implements SoundPath {

	private float gain = 1f;
	
	private SampleReader reader;
	private float samples[];
	
	private float x0, y0, z0;
	private boolean firstFrame = true;

	public InstantSoundPath(SampleReader reader) {
		this.reader = reader;
	}
	
	public int processFrame(SoundEncoder enc, int frameSize, Matrix curPos, Matrix micInvMatrix) {
		if (samples==null || samples.length<frameSize) {
			samples = new float[frameSize];
		}
		
		int nRead = reader.read(samples, 0, frameSize);
		if (nRead==0) {
			return 0;
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
			enc.encodeSample(samples[i]*gain, i, x0, y0, z0);
			
			x0 += dx;
			y0 += dy;
			z0 += dz;
		}

		return nRead;
	}	

	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		gain = eapp.getAttribute("volumeCoefficient", 1f);
	}
}
