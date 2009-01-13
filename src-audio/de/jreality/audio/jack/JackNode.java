package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

/**
 * An audio source that reads from Jack inputs.  Combined with MIDI or OSC tools, this class should serve
 * as universal glue between jReality and Jack-enabled audio software.
 * 
 * @author brinkman
 *
 */
public class JackNode extends AudioSource implements JackSource {

	private int port;
	
	public JackNode(String name, int port) {
		super(name);
		this.port = port;
		JackHub.addSource(this);
	}
	
	protected void reset() {
		// do nothing
	}

	protected void writeSamples(int n) {
		// do nothing; samples are written in process callback
	}

	public int highestPort() {
		return port;
	}

	public void init(int sampleRate) {
		ringBuffer = new RingBuffer(sampleRate);
		this.sampleRate = sampleRate;
	}

	public void process(FloatBuffer[] sources) {
		if (getState() == State.RUNNING) { // in case jack client gets zombified, remember that getState() is synchronized...
			FloatBuffer buffer = sources[port];
			buffer.rewind();
			ringBuffer.write(buffer);
		}
	}
}
