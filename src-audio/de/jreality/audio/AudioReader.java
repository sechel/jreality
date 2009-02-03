package de.jreality.audio;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * 
 * Takes an AudioSource and wraps it in a SampleReader, typically the first element of a chain of sample readers.
 * 
 * Each AudioSource can have several SampleReaders attached to it at the same time; they can all read samples
 * in parallel without interfering with each other.  The basic idea is that each audio renderer requests a
 * reader for each occurrence of an AudioSource in the scene graph.
 * 
 * @author brinkman
 *
 */
public class AudioReader implements SampleReader {

	private AudioSource node;
	private RingBuffer.Reader reader;
	
	public AudioReader(AudioSource node) {
		this.node = node;
		reader = node.createReader();
	}
	
	public int getSampleRate() {
		return node.getSampleRate();
	}
	
	public void clear() {
		reader.clear();
	}

	public int read(float[] buffer, int initialIndex, int nSamples) {
		return node.readSamples(reader, buffer, initialIndex, nSamples);
	}
}
