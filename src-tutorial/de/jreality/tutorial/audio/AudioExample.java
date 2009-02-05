package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.audio.plugin.Audio;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.ContentAppearance;
import de.jreality.ui.plugin.DisplayPanel;
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
		registerPlugins();
		createAudioContent();
	}
	
	private void createAudioContent() throws IOException, UnsupportedAudioFileException {
		Input wavFile = Input.getInput("sound/zoom.wav");
		final AudioSource wavNode = new CachedAudioInputStreamSource("wavnode", wavFile, true);
		wavNode.start();
		
		SceneGraphComponent audioComponent = new SceneGraphComponent();
		audioComponent.setAudioSource(wavNode);
		audioComponent.setGeometry(new Sphere());
		MatrixBuilder.euclidean().translate(0, 2, 0).assignTo(audioComponent);
	
		ActionTool actionTool = new ActionTool("PanelActivation");
		actionTool.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (wavNode.getState() == AudioSource.State.RUNNING) {
					wavNode.pause();
				} else {
					wavNode.start();
				}
			}
		});
		audioComponent.addTool(actionTool);
		audioComponent.addTool(new DraggingTool());
	
		SceneGraphComponent contentRoot = controller.getPlugin(AlignedContent.class).getScalingComponent();
		contentRoot.addChild(audioComponent);
	}

	private void registerPlugins() {
		ViewMenuBar viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuItem(AudioExample.class, 20.0, new ExitAction(), "File");
		
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
		controller.registerPlugin(new DisplayPanel());
		controller.registerPlugin(new Audio());
	}

	private static class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	public void startup() {
		controller.startup();
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		new AudioExample().startup();
	}
}
