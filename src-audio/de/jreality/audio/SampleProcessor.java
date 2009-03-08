package de.jreality.audio;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public interface SampleProcessor extends SampleReader {
	public void write(float[] buf, int initialIndex, int samples);
	public void setSampleRate(int sampleRate);
	public void setProperties(EffectiveAppearance app);
}
