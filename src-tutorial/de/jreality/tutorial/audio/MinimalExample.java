package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import de.jreality.audio.csound.CsoundNode;
import de.jreality.geometry.Primitives;
import de.jreality.plugin.audio.AudioLauncher;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.vr.Avatar;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

/**
 * 
 * Code example for ICMC submission. Not pretty but short.
 * 
 * @author brinkman
 *
 */
public class MinimalExample {
	public static void main(String[] args) 
	throws IOException {
		SimpleController c = 
			new SimpleController();

		// set up graphics
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new View());
		c.registerPlugin(new CameraStand());
		c.registerPlugin(new Lights());
		c.registerPlugin(new Inspector());
		c.registerPlugin(new Avatar());

		// set up audio
		c.registerPlugin(new AudioLauncher());
		c.registerPlugin(new AudioOptions());

		// create visual content
		SceneGraphComponent content = new SceneGraphComponent("content");
		content.setGeometry(Primitives.sphere(20));
		
		// create audio content
		final AudioSource source = new CsoundNode("csound node", Input.getInput(MinimalExample.class.getResource("trapped.csd")));
		source.start();
		content.setAudioSource(source);

		// create simple tool for pausing/starting audio
		ActionTool tool = 
			new ActionTool("PanelActivation");
		tool.addActionListener(new ActionListener() {
			public void actionPerformed(
					ActionEvent e) {
				if (source.getState() ==
					AudioSource.State.RUNNING)
					source.pause();
				else
					source.start();
			}
		});
		content.addTool(tool);

		// attach content to scene
		SceneGraphComponent parent = 
			c.getPlugin(View.class).getSceneRoot();
		parent.addChild(content);

		// launch viewer
		c.startup();
	}
}
