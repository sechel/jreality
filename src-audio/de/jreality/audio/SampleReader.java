package de.jreality.audio;

/**
 * Sample reader interface.  Sample readers are intended to be chained, e.g., going from an AudioSource to
 * a sample rate converter to a low-pass filter.
 * 
 * @author brinkman
 *
 */
public interface SampleReader {
	public int read(float buffer[], int initialIndex, int nSamples);
	public void clear();
}
