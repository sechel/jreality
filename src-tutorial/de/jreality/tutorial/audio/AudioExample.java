package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.util.Input;


public class AudioExample {

	public static SceneGraphComponent getAudioComponent() throws Exception {
		InputStream testSoundIn = AudioExample.class.getResourceAsStream("hammond.wav");
		Input wavFile = Input.getInput("Hammond", testSoundIn);
		final AudioSource source = new CachedAudioInputStreamSource("Hammond", wavFile, true);
		SceneGraphComponent audioComponent = new SceneGraphComponent("monolith");
		audioComponent.setAudioSource(source);
		audioComponent.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().translate(0, 5, 0).scale(2, 4.5, .5).assignTo(audioComponent);
	
		ActionTool actionTool = new ActionTool("PanelActivation");
		actionTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (source.getState() == AudioSource.State.RUNNING) {
					source.pause();
				} else {
					source.start();
				}
			}
		});
		audioComponent.addTool(actionTool);
		audioComponent.addTool(new DraggingTool());
		return audioComponent;
	}

	
	public static void main(String[] args) throws Exception {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.setPropertiesFile("AudioExample.jrw");
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(getAudioComponent());
		v.startup();
	}
	
}
