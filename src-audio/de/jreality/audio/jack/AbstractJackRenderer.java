package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;
import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;

public abstract class AbstractJackRenderer extends AbstractAudioRenderer implements JackSink {

	protected JJackAudioEvent currentJJackEvent;
	private String label = "jreality_jack_renderer";
	private String target = "";
	protected SoundEncoder encoder;

	public abstract int highestPort();

	public void process(JJackAudioEvent ev) {
		currentJJackEvent = ev;
		backend.processFrame(encoder, ev.getOutput().capacity());
	}

	public void setTarget(String target) {
		this.target=target;
	}

	public void setLabel(String label) {
		this.label=label;
	}

	public void launch() throws JJackException {
		backend=new AudioBackend(root, microphonePath, JackHub.getSampleRate(), interpolationFactory, soundPathFactory);
		JackHub.setClientName(label);
		JackHub.setTargetName(target);
		JackHub.setSink(this);
		JackHub.initializeClient();
	}

	public void shutdown() throws JJackException {
		JackHub.closeClient();
		backend.dispose();
	}

}
