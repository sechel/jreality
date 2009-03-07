package de.jreality.audio;

import de.jreality.scene.data.SampleReader;

public interface SampleProcessor extends SampleReader {
	public void write(float[] buf, int initialIndex, int samples);
}
