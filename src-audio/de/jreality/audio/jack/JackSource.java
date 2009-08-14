package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.jreality.audio.RingBuffer;
import de.jreality.audio.RingBufferSource;


public class JackSource extends RingBufferSource implements JJackAudioProcessor {

	private long key;
	
	public JackSource(String name, String target) throws JJackException {
		super(name);
		sampleRate = JJackNativeClient.getSampleRate();
		ringBuffer = new RingBuffer(sampleRate);
		key = JackManager.requestInputPorts(1, target);
		JackManager.addInput(this);
	}

	@Override
	protected void finalize() throws Throwable {
		JackManager.releasePorts(key);
		super.finalize();
	}
	
	public void process(JJackAudioEvent e) {
		if (getState() == State.RUNNING) {
			FloatBuffer buffer = e.getInput(JackManager.getPort(key));
			buffer.rewind();
			ringBuffer.write(buffer);
		}
	}

	@Override
	protected void writeSamples(int n) {
		// do nothing; samples are written in process callback
	}
}
