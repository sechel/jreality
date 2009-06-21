package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import de.jreality.audio.csound.CsoundSource;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.reader.Readers;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.util.Input;

/**
 * 
 * Code example for ICMC submission. Not pretty but short.
 * 
 * @author brinkman
 *
 */
public class MinimalExample {

	public static void main(String[] args) throws IOException {
		SceneGraphComponent audioComponent = Readers.read(Input.getInput(MinimalExample.class.getResource("schwarz.jrs")));
		Input input = Input.getInput(MinimalExample.class.getResource("trapped.csd"));
		final AudioSource source = new CsoundSource("csound node", input);
		source.start();
		audioComponent.setAudioSource(source);
	
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
		
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.setPropertiesFile("MinimalExample.jrw");
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(audioComponent);
		v.startup();
	}
}
