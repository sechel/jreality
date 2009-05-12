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
	protected String label = "jreality_jack_renderer";
	protected String target = "";
	protected SoundEncoder encoder;
	protected JJackNativeClient nativeClient = null;

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
		if (nativeClient!=null) {
			shutdown();
		}
		backend=new AudioBackend(root, microphonePath, JackHub.getSampleRate(), interpolationFactory, soundPathFactory);
		launchNativeClient();
	}
	
	protected abstract void launchNativeClient() throws JJackException;

	public void shutdown() {
		if (nativeClient!=null) {
			nativeClient.close();
			backend.dispose();
			nativeClient = null;
		}
	}

}
