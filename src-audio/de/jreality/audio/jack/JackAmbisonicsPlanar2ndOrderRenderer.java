package de.jreality.audio.jack;

import java.nio.FloatBuffer;

import de.gulden.framework.jjack.JJackAudioEvent;
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

	private AudioBackend backend;
	
	private SceneGraphComponent root;
	private SceneGraphPath microphonePath;
	
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

	FloatBuffer outbufW;
	FloatBuffer outbufX;
	FloatBuffer outbufY;
	FloatBuffer outbufU;
	FloatBuffer outbufV;
	
	public void process(JJackAudioEvent ev) {

		outbufW=ev.getOutput(0);
		outbufX=ev.getOutput(1);
		outbufY=ev.getOutput(2);
		outbufU=ev.getOutput(3);
		outbufV=ev.getOutput(4);

		int frameSize = ev.getOutput().capacity();

		backend.encodeSound(this, frameSize);

	}

	@Override
	public void finishFrame() {
		outbufW.put(bw);
		outbufX.put(bx);
		outbufY.put(by);
		outbufU.put(bu);
		outbufV.put(bu);
	}
	
	public static void launch(Viewer viewer) {
		JackAmbisonicsPlanar2ndOrderRenderer renderer = new JackAmbisonicsPlanar2ndOrderRenderer();
		renderer.setRootAndMicrophonePath(viewer.getSceneRoot(), viewer.getCameraPath());

		JackHub.setSink(renderer);
		JackHub.initializeClient("jrPlanar2ndOrderAmbisonics");
	}
}
