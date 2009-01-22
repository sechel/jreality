package de.jreality.audio;

/**
 * 
 * Sound encoder interface; implementations of this interface are the very last step in the audio
 * processing chain.  In particular, the encodeSample callback encodes one sample at a time, with
 * a location given in euclidean coordinates, with the assumption that the speakers will be located
 * in a euclidean space.  Any other processing (e.g., reverberation, attenuation, interpolation,
 * handling of noneuclidean coordinates) is the responsibility of implementations of {@link SampleReader}
 * or {@link SoundPath}.
 *
 */
public interface SoundEncoder {
	void startFrame(int framesize);
	
	/**
	 * 
	 * Encode one sample.
	 * 
	 * @param v:       sample, already processed for attenuation, reverberation, interpolation, etc.
	 * @param idx:     index of sample
	 * @param x, y, z: position of sound source relative to listener
	 * @param r:       distance of sound source from listener
	 */
	void encodeSample(float v, int idx, float x, float y, float z, float r);
	
	void finishFrame();
}
