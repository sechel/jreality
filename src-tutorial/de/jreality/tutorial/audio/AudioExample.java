package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;

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


public class AudioExample {

	private SimpleController controller = new SimpleController();
	
	
	public AudioExample() throws IOException, UnsupportedAudioFileException {
		videoSetup();
		audioSetup();
	}
	
	@SuppressWarnings("serial")
	private void videoSetup() {
		ViewMenuBar viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuItem(AudioExample.class, 20.0, new AbstractAction() {
			{
				putValue(AbstractAction.NAME, "Exit");
			}
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		}, "File");
		
		controller.registerPlugin(viewMenuBar);
		controller.registerPlugin(new View());
		controller.registerPlugin(new ViewPreferences());
		controller.registerPlugin(new CameraStand());
		controller.registerPlugin(new Lights());
		controller.registerPlugin(new AlignedContent());
		controller.registerPlugin(new ContentAppearance());
		controller.registerPlugin(new Inspector());
		controller.registerPlugin(new Shell());
		controller.registerPlugin(new Sky());
		controller.registerPlugin(new Terrain());
		controller.registerPlugin(new Avatar());
		controller.registerPlugin(new DisplayOptions());
	}

	private void audioSetup() throws IOException, UnsupportedAudioFileException {
		controller.registerPlugin(new AudioOptions());
		controller.registerPlugin(new AudioLauncher());
		
//		Input wavFile = Input.getInput(getClass().getResource("zarathustra.wav"));
		Input wavFile = Input.getInput("sound/zarathustra.wav");
		final AudioSource source = new CachedAudioInputStreamSource("zarathustra", wavFile, true);
		source.start();
		
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
	
		SceneGraphComponent contentRoot = controller.getPlugin(AlignedContent.class).getScalingComponent();
		contentRoot.addChild(audioComponent);
	}

	public void startup() {
		controller.startup();
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		new AudioExample().startup();
	}
}
