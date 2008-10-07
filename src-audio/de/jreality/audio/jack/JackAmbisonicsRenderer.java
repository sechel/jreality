package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.jreality.audio.AmbisonicsVisitor;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;

/**
 * 
 * Simple Jack back-end for Ambisonics.  All the real work occurs in the Ambisonics visitor; this class
 * merely takes care of the Jack init (which is a bit dicey; see comment in JackSource.java), collects
 * the samples and writes them to the Jack output buffers.
 * 
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author brinkman
 *
 */
public class JackAmbisonicsRenderer implements JackSink {

	private AmbisonicsVisitor visitor = null;
	private SceneGraphComponent root;
	private SceneGraphPath microphonePath;
	
	private float bw[];
	private float bx[];
	private float by[];
	private float bz[];
	
	public JackAmbisonicsRenderer() {
		// do nothing
	}
	
	public void setRootAndMicrophonePath(SceneGraphComponent root, SceneGraphPath microphonePath) {
		this.root = root;
		this.microphonePath = microphonePath;
		if (visitor != null) {
			synchronized(this) {
				visitor.setRoot(root);
				visitor.setMicrophonePath(microphonePath);
			}
		}
	}
	
	public int highestPort() {
		return 3;
	}

	public void init(int sampleRate) {
		visitor = new AmbisonicsVisitor(sampleRate);
		visitor.setRoot(root);
		visitor.setMicrophonePath(microphonePath);
		
		bw = new float[sampleRate];  // way too big, but we don't care
		bx = new float[sampleRate];
		by = new float[sampleRate];
		bz = new float[sampleRate];
	}

	public void process(JJackAudioEvent ev) {
		int frameSize = ev.getOutput().capacity();
		synchronized(this) {
			visitor.collateAmbisonics(bw, bx, by, bz, frameSize);
		}
		ev.getOutput(0).put(bw, 0, frameSize);
		ev.getOutput(1).put(bx, 0, frameSize);
		ev.getOutput(2).put(by, 0, frameSize);
		ev.getOutput(3).put(bz, 0, frameSize);
	}

	public static void launch(Viewer viewer) {
		JackAmbisonicsRenderer renderer = new JackAmbisonicsRenderer();
		renderer.setRootAndMicrophonePath(viewer.getSceneRoot(), viewer.getCameraPath());
		
		JackHub.setSink(renderer);
		JackHub.initializeClient("jrAmbisonics");
	}
}
