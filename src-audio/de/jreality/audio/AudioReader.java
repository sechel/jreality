package de.jreality.audio;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * 
 * Takes an AudioSource and wraps it in a SampleReader, plugging in sample rate converters as necessary.
 * Each AudioSource can have several SampleReaders attached to it at the same time; they can all read samples
 * in parallel without interfering with each other.  The basic idea is that each audio renderer requests a
 * reader for each occurrence of an AudioSource in the scene graph.
 * 
 * TODO: add a quality option for different types of sample rate converters, navigating the trade-off between
 * speed and quality.
 * 
 * @author brinkman
 *
 */
public class AudioReader implements SampleReader {

	private AudioSource node;
	private RingBuffer.Reader reader;
	
	private AudioReader(AudioSource node) {
		this.node = node;
		reader = node.createReader();
	}
	
	public static SampleReader createReader(AudioSource node, int targetRate) {
		int sourceRate = node.getSampleRate();
		SampleReader reader = new AudioReader(node);
		return (sourceRate == targetRate) ? reader : new ConvertingReader(reader, sourceRate, targetRate);
	}
	
	public void clear() {
		reader.clear();
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		return node.readSamples(reader, buffer, initialIndex, nSamples);
	}
}
