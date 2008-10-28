package de.jreality.audio;

import de.jreality.math.Matrix;

public interface SoundEncoder {

	void startFrame(int framesize);
	void encodeSignal(float[] samples, int nSamples, Matrix p0, Matrix p1);
	void finishFrame();
}
