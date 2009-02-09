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
	  SceneGraphComponent content = Readers.read(
	    Input.getInput(
	      Example.class.getResource("schwarz.jrs")));
	  
	  // create audio content
	  final AudioSource source = new CsoundNode(
	    "csound node", Input.getInput(
	      Example.class.getResource("trapped.csd")));
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
