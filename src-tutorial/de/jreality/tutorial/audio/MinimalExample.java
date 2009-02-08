package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;

import de.jreality.audio.csound.CsoundNode;
import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.audio.plugin.AudioLauncher;
import de.jreality.audio.plugin.AudioOptions;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.ContentAppearance;
import de.jreality.ui.plugin.DisplayOptions;
import de.jreality.ui.plugin.Inspector;
import de.jreality.ui.plugin.Lights;
import de.jreality.ui.plugin.Shell;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.ViewMenuBar;
import de.jreality.ui.plugin.ViewPreferences;
import de.jreality.util.Input;
import de.jreality.vr.plugin.Avatar;
import de.jreality.vr.plugin.Sky;
import de.jreality.vr.plugin.Terrain;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;


public class MinimalExample {

	private SimpleController controller = new SimpleController();
	
	
	public MinimalExample() throws IOException {
		videoSetup();
		audioSetup();
	}
	
	private void videoSetup() {
		controller.registerPlugin(new View());
		controller.registerPlugin(new CameraStand());
		controller.registerPlugin(new Lights());
		controller.registerPlugin(new AlignedContent());
		controller.registerPlugin(new Inspector());
		controller.registerPlugin(new Avatar());
	}

	private void audioSetup() throws IOException {
		controller.registerPlugin(new AudioOptions());
		controller.registerPlugin(new AudioLauncher());
		
		final AudioSource source = new CsoundNode("csnode", Input.getInput(getClass().getResource("trapped.csd")));
		source.start();
		
		SceneGraphComponent audioComponent = new SceneGraphComponent("sphere");
		audioComponent.setAudioSource(source);
		audioComponent.setGeometry(Primitives.sphere(10));
		
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
		
		SceneGraphComponent contentRoot = controller.getPlugin(AlignedContent.class).getScalingComponent();
		contentRoot.addChild(audioComponent);
	}

	public void startup() {
		controller.startup();
	}
	
	public static void main(String[] args) throws IOException {
		new MinimalExample().startup();
	}
}
