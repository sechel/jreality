package de.jreality.tutorial.audio;

import java.io.InputStream;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.DraggingTool;
import de.jreality.util.Input;


public class AudioExample {

	public static SceneGraphComponent getAudioComponent() throws Exception {
		InputStream wavFile = AudioExample.class.getResourceAsStream("zarathustra.wav");
		final AudioSource source = new CachedAudioInputStreamSource("zarathustra", Input.getInput("zarathustra", wavFile), true);
		SceneGraphComponent audioComponent = new SceneGraphComponent("monolith");
		audioComponent.setAudioSource(source);
		audioComponent.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().translate(0, 5, 0).scale(2, 4.5, .5).assignTo(audioComponent);
		audioComponent.addTool(new DraggingTool());
		source.start();
		return audioComponent;
	}

	
	public static void main(String[] args) throws Exception {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(getAudioComponent());
		v.startup();
	}
	
}
