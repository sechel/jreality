package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;
import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;

public abstract class AbstractJackAmbisonicsRenderer extends AbstractAudioRenderer implements JackSink {

	protected JJackAudioEvent currentJJackEvent;
	private String label = "jreality_jack_renderer";
	private String target = "";
	protected SoundEncoder encoder;

	public abstract int highestPort();
	
	public void init(int sampleRate) {
		backend=new AudioBackend(root, microphonePath, sampleRate, interpolationFactory, soundPathFactory);
	}

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
		JackHub.setSink(this);
		JackHub.initializeClient(label, target);
	}

	public void shutdown() throws JJackException {
		backend.dispose();
		JackHub.removeClient();
		JackHub.setSink(null);
	}

}
