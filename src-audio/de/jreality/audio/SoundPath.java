package de.jreality.audio;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Represents the physical sound path from the sound source
 * to the microphone at (0,0,0). One frame of emitted samples
 * from the source are passed in together with the positions
 * at the beginning and at the end of the frame.
 * 
 * The sound path overwrites the given sample buffer to contain
 * the samples arriving at the microphone at the same time
 * as well as the positions p0, p1. They will contain the positions
 * from where the current frame was emitted.
 * 
 * This class plays the role of a SoundShader comparable to the various
 * GeometryShaders and will be configured from an EffectiveAppearance.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public interface SoundPath {

	/**
	 * Process sound emission from an AudioSource (given in samples),
	 * and overwrite with the perceived samples at the same time at
	 * the microphone located at zero. The matrices correspond to positions
	 * before the first sample and at the last sample - these will be
	 * overwritten with the positions of the received samples.
	 *
	 */
	int processSamples(float[] samples, int nSamples, Matrix p0, Matrix p1);
	
	void setFromEffectiveAppearance(EffectiveAppearance eapp);
}
