package de.jreality.audio;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

public class InstantSoundPath implements SoundPath {

	boolean distanceFalloff=false;
	float gain=1f;
	
	public int processSamples(float[] samples, int nSamples, Matrix p0, Matrix p1) {
	
		if (distanceFalloff) {
			// do distance falloff:
			double x0 = p0.getEntry(0, 3);
			double y0 = p0.getEntry(1, 3);
			double z0 = p0.getEntry(2, 3);
			
			float d0 = (float) Math.sqrt(x0*x0+y0*y0+z0*z0);
			
			double x1 = p1.getEntry(0, 3);
			double y1 = p1.getEntry(1, 3);
			double z1 = p1.getEntry(2, 3);
	
			float d1 = (float) Math.sqrt(x1*x1+y1*y1+z1*z1);
			
			float dd = (d1-d0)/nSamples;
			
			for (int i=0; i<nSamples; i++) {
				samples[i]*=gain/Math.max(d0+i*dd, 1E-5);
			}
		} else if (gain != 1f) {
			for (int i=0; i<nSamples; i++) {
				samples[i]*=gain;
			}			
		}
		
		return nSamples;
	}	

	public void setFromEffectiveAppearance(EffectiveAppearance eapp) {
		distanceFalloff = eapp.getAttribute("volumeAttenuation", true);
		gain = eapp.getAttribute("volumeCoefficient", 1f);
	}

}
