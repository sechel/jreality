package de.jreality.audio;

import de.jreality.math.Matrix;
import de.jreality.shader.EffectiveAppearance;

/**
 * 
 * Represents the physical sound path from the sound source
 * to the microphone at (0,0,0). This class plays the role of a SoundShader
 * comparable to the various GeometryShaders and will be configured from an
 * EffectiveAppearance.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public interface SoundPath {	
	void setFromEffectiveAppearance(EffectiveAppearance eapp);
	int processFrame(SampleReader reader, SoundEncoder enc, int frameSize, Matrix curPos, Matrix micInvMatrix);
}
