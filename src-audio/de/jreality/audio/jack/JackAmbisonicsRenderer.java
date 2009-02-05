package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;
import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AudioBackend;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;

/**
 * 
 * Simple Jack back-end for Ambisonics.  All the real work occurs in {@link AudioBackend}; this class
 * merely takes care of the Jack init (which is a bit dicey; see comment in {@link JackSource}), collects
 * the samples and writes them to the Jack output buffers.
 * 
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author brinkman
 *
 */
public class JackAmbisonicsRenderer extends AmbisonicsSoundEncoder implements JackSink {

	private AudioBackend backend;
	
	private SceneGraphComponent root;
	private SceneGraphPath microphonePath;
	
	private JJackAudioEvent currentJJackEvent;
	
	public void setRootAndMicrophonePath(SceneGraphComponent root, SceneGraphPath microphonePath) {
		this.root = root;
		this.microphonePath = microphonePath;
	}
	
	public int highestPort() {
		return 3;
	}

	public void init(int sampleRate) {
		backend=new AudioBackend(root, microphonePath, sampleRate);
	}

	public void process(JJackAudioEvent ev) {
		currentJJackEvent = ev;
		backend.processFrame(this, ev.getOutput().capacity());
	}

	public void finishFrame() {
		currentJJackEvent.getOutput(0).put(bw);
		currentJJackEvent.getOutput(1).put(bx);
		currentJJackEvent.getOutput(2).put(by);
		currentJJackEvent.getOutput(3).put(bz);
	}
	
	public static void launch(Viewer viewer) throws JJackException {
		JackAmbisonicsRenderer renderer = new JackAmbisonicsRenderer();
		renderer.setRootAndMicrophonePath(viewer.getSceneRoot(), viewer.getCameraPath());

		JackHub.setSink(renderer);
		JackHub.initializeClient("jrAmbisonics");
	}
}
