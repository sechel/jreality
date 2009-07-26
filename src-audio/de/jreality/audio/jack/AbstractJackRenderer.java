package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;


/**
 * Base class for all JACK backends.  Pretty straightforward, except for one wrinkle: You can, in principle,
 * have several JACK backends rendering audio from various different perspectives.  The default case is that
 * there is only one JACK backend, in which case this class automatically takes care of launching and closing
 * the JACK client.  If you want more than one JACK backend, then you need to call setSingle(false), and you
 * will also need to call {@link JackManager.launch} and {@link JackManager.shutdown} at the appropriate times.
 * 
 * @author brinkman
 *
 */
public abstract class AbstractJackRenderer extends AbstractAudioRenderer implements JJackAudioProcessor {

	protected JJackAudioEvent currentJJackEvent;
	protected String target = null;
	protected SoundEncoder encoder;
	protected int nPorts;  // number of ports; must be set by subclasses
	protected long key;
	private static boolean singleBackend = true;
	
	
	/**
	 * Determines whether there will be only one JACK backend
	 * 
	 * @param single
	 */
	public static void setSingle(boolean single) {
		singleBackend = single;
	}

	/**
	 * Sets the name of the JACK client to connect to, e.g., an Ambisonics decoder.
	 * 
	 * @param target: regular expression determining the client (and ports, possibly) to connect to
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Sets the name of the native JACK client
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		JackManager.setLabel(label);
	}
	
	public synchronized void launch() throws JJackException {
		shutdown();  // just in case...
		backend = new AudioBackend(root, microphonePath, JJackNativeClient.getSampleRate(), interpolationFactory, soundPathFactory);
		key = JackManager.requestOutputPorts(nPorts, target);
		JackManager.addOutput(this);
		if (singleBackend) JackManager.launch();
	}

	public synchronized void shutdown() {
		if (backend!=null) {
			if (singleBackend) JackManager.shutdown();
			JackManager.removeOutput(this);
			JackManager.releasePorts(key);
			backend.dispose();
			backend = null;
		}
	}

	public void process(JJackAudioEvent ev) {
		currentJJackEvent = ev;
		try { // NullPointerException is conceivable if singleBackend is false and shutdown() is called while process callback is pending...
			backend.processFrame(encoder, ev.getOutput().capacity());
		} catch (NullPointerException e) {
			// do nothing
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			shutdown();
		} finally {
			super.finalize();
		}
	}
}
