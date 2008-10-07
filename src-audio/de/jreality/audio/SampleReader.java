package de.jreality.audio;

public interface SampleReader {
	public int read(float buffer[], int initialIndex, int nSamples);
	public void clear();
}
