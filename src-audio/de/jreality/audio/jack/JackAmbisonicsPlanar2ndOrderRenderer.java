 package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.gulden.framework.jjack.JJackException;
import de.jreality.audio.AmbisonicsPlanar2ndOrderSoundEncoder;
import de.jreality.audio.AudioBackend;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;

/**
 * Jack back-end for Second Order Planar Ambisonics.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 */
public class JackAmbisonicsPlanar2ndOrderRenderer extends AmbisonicsPlanar2ndOrderSoundEncoder implements JackSink {
	
	private SceneGraphComponent root;
	private SceneGraphPath microphonePath;
	
	private AudioBackend backend;
	private JJackAudioEvent currentJJackEvent;
	
	public void setRootAndMicrophonePath(SceneGraphComponent root, SceneGraphPath microphonePath) {
		this.root = root;
		this.microphonePath = microphonePath;
	}
	
	public int highestPort() {
		return 4;
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
		currentJJackEvent.getOutput(3).put(bu);
		currentJJackEvent.getOutput(4).put(bu);
	}
	
	public static void launch(Viewer viewer, String label) throws JJackException {
		JackAmbisonicsPlanar2ndOrderRenderer renderer = new JackAmbisonicsPlanar2ndOrderRenderer();
		renderer.setRootAndMicrophonePath(viewer.getSceneRoot(), viewer.getCameraPath());

		JackHub.setSink(renderer);
		JackHub.initializeClient(label);
	}
}
