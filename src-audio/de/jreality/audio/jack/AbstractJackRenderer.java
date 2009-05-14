package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackAudioProcessor;
import de.gulden.framework.jjack.JJackException;
import de.gulden.framework.jjack.JJackNativeClient;
import de.gulden.framework.jjack.JJackNativeClientEvent;
import de.gulden.framework.jjack.JJackNativeClientListener;
import de.jreality.audio.AbstractAudioRenderer;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.SoundEncoder;

public abstract class AbstractJackRenderer extends AbstractAudioRenderer implements JJackAudioProcessor, JJackNativeClientListener {

	protected JJackAudioEvent currentJJackEvent;
	protected String label = "jreality_renderer";
	protected String target = "";
	protected SoundEncoder encoder;
	protected JJackNativeClient nativeClient = null;
	private boolean finished = false;
	private int retries = 0;

	public void process(JJackAudioEvent ev) {
		currentJJackEvent = ev;
		backend.processFrame(encoder, ev.getOutput().capacity());
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}
	
	public void launch() throws JJackException {
		if (nativeClient!=null) {
			shutdown();
		}
		
		createNativeClient();
		nativeClient.addListener(this);
		
		backend=new AudioBackend(root, microphonePath, JJackNativeClient.getSampleRate(), interpolationFactory, soundPathFactory);
		JackClient.addListener(this);
		finished = false;
		
		JackClient.launch();
		nativeClient.start(null, target);
	}

	protected abstract void createNativeClient() throws JJackException;

	public void shutdown() {
		if (nativeClient!=null) {
			nativeClient.removeListener(this);
			nativeClient.close();
			backend.dispose();
			nativeClient = null;
			backend = null;
		}
		JackClient.removeListener(this);
		JackClient.shutdown();
	}

	public void handleShutdown(JJackNativeClientEvent e) {
		if (finished) {
			return;
		}
		if (retries>0) {
			retries--;
			finished = true;
			try {
				Thread.sleep(250); // long enough not to drive the CPU crazy, short enough not to be disconcerting
			} catch (InterruptedException ex) {
				// do nothing
			}
			System.err.println("relaunching jack renderer, "+retries+" attempts left");
			try {
				launch();
			} catch (JJackException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("jack client "+e.getSource()+" zombified; not trying to relaunch");
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
