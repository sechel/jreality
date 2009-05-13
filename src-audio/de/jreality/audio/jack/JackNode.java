package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.jreality.audio.RingBuffer;


public class JackNode extends AbstractJackNode {

	private int port = 0;

	public JackNode(String name, int port) throws JJackException {
		super(name);
		sampleRate = JJackNativeClient.getSampleRate();
		ringBuffer = new RingBuffer(sampleRate);
		this.port = port;
	}

	public void process(JJackAudioEvent e) {
		if (getState() == State.RUNNING) { // in case jack client gets zombified, remember that getState() is synchronized...
			try {
				FloatBuffer buffer = e.getInput(port);
				buffer.rewind();
				ringBuffer.write(buffer);
			} catch(Exception ex) {
				ex.printStackTrace();
				System.err.println("removing node from list of processors");
				detachFromAllClients();
			}
		}
	}
}
