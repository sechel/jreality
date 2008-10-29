package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AudioBackend;
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
public class JackAmbisonicsRenderer extends AmbisonicsSoundEncoder implements JackSink {

	private AudioBackend backend;
	
	private SceneGraphComponent root;
	private SceneGraphPath microphonePath;
	
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

	FloatBuffer outbufW;
	FloatBuffer outbufX;
	FloatBuffer outbufY;
	FloatBuffer outbufZ;
	
	public void process(JJackAudioEvent ev) {

		outbufW=ev.getOutput(0);
		outbufX=ev.getOutput(1);
		outbufY=ev.getOutput(2);
		outbufZ=ev.getOutput(3);

		int frameSize = ev.getOutput().capacity();

		backend.encodeSound(this, frameSize);

	}

	@Override
	public void finishFrame() {
		outbufW.put(bw);
		outbufX.put(bx);
		outbufY.put(by);
		outbufZ.put(bz);
	}
	
	public static void launch(Viewer viewer) {
		JackAmbisonicsRenderer renderer = new JackAmbisonicsRenderer();
		renderer.setRootAndMicrophonePath(viewer.getSceneRoot(), viewer.getCameraPath());

		JackHub.setSink(renderer);
		JackHub.initializeClient("jrAmbisonics");
	}
}
