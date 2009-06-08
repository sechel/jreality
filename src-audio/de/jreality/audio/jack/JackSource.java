package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;


public class JackSource extends AbstractJackSource {

	private int port = 0;

	public JackSource(String name, String clientName, int port) throws JJackException {
		super(name, clientName);
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
				detachFromClient();
			}
		}
	}
}
