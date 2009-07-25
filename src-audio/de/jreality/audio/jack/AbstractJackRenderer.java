package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;

public abstract class AbstractJackRenderer extends AbstractAudioRenderer implements JJackAudioProcessor {

	protected JJackAudioEvent currentJJackEvent;
	protected String target = null;
	protected SoundEncoder encoder;
	protected long key;

	public void process(JJackAudioEvent ev) {
		currentJJackEvent = ev;
		backend.processFrame(encoder, ev.getOutput().capacity());
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setLabel(String label) {
		JackManager.setLabel(label);
	}
	
	public synchronized void launch() throws JJackException {
		shutdown();  // just in case...
		backend = new AudioBackend(root, microphonePath, JJackNativeClient.getSampleRate(), interpolationFactory, soundPathFactory);
		key = registerPorts();
		JackManager.addOutput(this);
		JackManager.launch();
	}

	protected abstract long registerPorts();

	public synchronized void shutdown() {
		if (backend!=null) {
			JackManager.shutdown();
			JackManager.removeOutput(this);
			JackManager.releasePorts(key);
			backend.dispose();
			backend = null;
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
