package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.jreality.audio.RingBuffer;
import de.jreality.audio.RingBufferSource;

/**
 * Base class for audio sources that read from Jack inputs.  Combined with MIDI or OSC tools, this class should serve
 * as universal glue between jReality and Jack-enabled audio software.
 * 
 * @author brinkman
 *
 */
public abstract class AbstractJackNode extends RingBufferSource implements JJackAudioProcessor {

	String clientName;
	
	public AbstractJackNode(String name, String clientName) throws JJackException {
		super(name);
		sampleRate = JJackNativeClient.getSampleRate();
		ringBuffer = new RingBuffer(sampleRate);
		this.clientName = clientName;
		JackClient.addProcessor(clientName, this);
	}

	public void detachFromClient() {
		JackClient.removeProcessor(clientName, this);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			detachFromClient();
		} finally {
			super.finalize();
		}
	}
	
	protected void reset() {
		// do nothing
	}

	protected void writeSamples(int n) {
		// do nothing; samples are written in process callback
	}
}
