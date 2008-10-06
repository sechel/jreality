package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.jreality.scene.AudioSource;
import de.jreality.scene.data.RingBuffer;

public class JackNode extends AudioSource implements JackSource {

	private int port;
	
	public JackNode(String name, int port) {
		super(name);
		this.port = port;
		JackHub.addSource(this);
	}

	@Override
	protected void reset() {
		// do nothing
	}

	@Override
	protected void setParameterImpl(String name, Object value) {
		// do nothing
	}

	@Override
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
