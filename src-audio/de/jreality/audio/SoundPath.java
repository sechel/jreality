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
 * Convention: Specifying a speed of sound of zero or less means infinite speed
 * of sound, i.e., instantaneous propagation.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public interface SoundPath {	
	
	public static final String SPEED_OF_SOUND_KEY = "speedOfSound";
	public static final String VOLUME_GAIN_KEY = "volumeGain";
	public static final String VOLUME_ATTENUATION_KEY = "volumeAttenuation";
	
	public static final float DEFAULT_GAIN = 1f;
	public static final float DEFAULT_SPEED_OF_SOUND = 300f;
	public static final Attenuation DEFAULT_ATTENUATION = Attenuation.LINEAR;
	
	void setFromEffectiveAppearance(EffectiveAppearance eapp);
	int processFrame(SoundEncoder enc, int frameSize, Matrix curPos, Matrix micInvMatrix);
}
