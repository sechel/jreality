package de.jreality.audio;

import de.jreality.scene.data.SampleReader;
import de.jreality.shader.EffectiveAppearance;

public interface SampleProcessor extends SampleReader {
	public void initialize(SampleReader reader);
	public void setProperties(EffectiveAppearance app);
}
