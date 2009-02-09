package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import de.jreality.audio.csound.CsoundNode;
import de.jreality.audio.plugin.AudioLauncher;
import de.jreality.audio.plugin.AudioOptions;
import de.jreality.reader.Readers;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.ContentAppearance;
import de.jreality.ui.plugin.Inspector;
import de.jreality.ui.plugin.Lights;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.ViewMenuBar;
import de.jreality.util.Input;
import de.jreality.vr.plugin.Avatar;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

/**
 * 
 * Code example for ICMC submission. Not pretty but short.
 * 
 * @author brinkman
 *
 */
public class Example {
	 public static void main(String[] args) 
	      throws IOException {

	  SimpleController c = 
	        new SimpleController();
	 
	  c.registerPlugin(new ViewMenuBar());
	  c.registerPlugin(new View());
	  c.registerPlugin(new CameraStand());
	  c.registerPlugin(new Lights());
	  c.registerPlugin(new Inspector());
	  c.registerPlugin(new Avatar());
	  
	  c.registerPlugin(new AudioOptions());
	  c.registerPlugin(new AudioLauncher());
	  
	  final AudioSource source = new CsoundNode(
	    "csound node", Input.getInput(
	      Example.class.getResource("trapped.csd")));
	  source.start();
	  
	  SceneGraphComponent content = Readers.read(
	    Input.getInput(
	      Example.class.getResource("schwarz.jrs")));
	  content.setAudioSource(source);
	  
	  ActionTool actionTool = 
	        new ActionTool("PanelActivation");
	  actionTool.addActionListener(
	    new ActionListener() {
	      public void actionPerformed(
	              ActionEvent e) {
	        if (source.getState() ==
	              AudioSource.State.RUNNING)
	          source.pause();
	        else
	          source.start();
	      }
	    });
	  content.addTool(actionTool);
	  
	  SceneGraphComponent parent = 
	    c.getPlugin(View.class).getSceneRoot();
	  parent.addChild(content);
	  
	  c.startup();
	 }
	}
