package de.jreality.audio;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

public class AudioReader implements SampleReader {

	private AudioSource node;
	private RingBuffer.Reader reader;
	
	public AudioReader(AudioSource node) {
		this.node = node;
		reader = node.createReader();
	}
	
	public void clear() {
		reader.clear();
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		return node.readSamples(reader, buffer, initialIndex, nSamples);
	}
	
	public static SampleReader createReader(AudioSource node, int targetRate) {
		int sourceRate = node.getSampleRate();
		SampleReader reader = new AudioReader(node);
		return (sourceRate == targetRate) ? reader : new ConvertingReader(reader, sourceRate, targetRate);
	}
}
