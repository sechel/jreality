package de.jreality.audio;

public interface SampleProcessor extends SampleReader {
	public void write(float[] buf, int initialIndex, int samples);
}
