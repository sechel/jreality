package de.jreality.audio;

public interface SampleProcessor extends SampleReader {
	public void processInput(float[] buf, int frameSize);
}
