package de.jreality.tutorial.audio;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.audio.AudioLauncher;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.Shell;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.vr.Avatar;
import de.jreality.plugin.vr.Sky;
import de.jreality.plugin.vr.Terrain;
import de.jreality.scene.AudioSource;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.ActionTool;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;


/**
 * Physically simulated Leslie speaker; use page up/down to regulate rotor speed
 * 
 * @author brinkman
 *
 */
public class LeslieDemo {

	private File
		propertiesFile = new File("LeslieDemo.jrw");
	private SimpleController 
		controller = new SimpleController(propertiesFile);
	
	private static class LeslieTask implements AnimatorTask {
		private SceneGraphComponent comp;
		private static final double q = 0.0005;
		private double omegaTarget = 0;
		private double omegaCurrent = 0;
		
		private LeslieTask(SceneGraphComponent comp) {
			this.comp = comp;
		}
		public boolean run(double time, double dt) {
			omegaCurrent += q*dt*(omegaTarget-omegaCurrent);
			MatrixBuilder.euclidean(comp).rotate(omegaCurrent*dt, 0, 1, 0).assignTo(comp);
			return true;
		}
		private void setOmega(double f) {
			omegaTarget = f;
		}
	}
	
	private static class LeslieTool extends AbstractTool {
		private static final InputSlot slot = InputSlot.getDevice("UpDownAxis");
		private LeslieTask task = null;
		private int count = 0;
		private static final double Q = 2*Math.PI/180/6;
		private static final int MAX = 8;
		
		private LeslieTool() {
			addCurrentSlot(slot);
		}
		
		@Override
		public void perform(ToolContext tc) {
			if (task==null) {
				SceneGraphComponent comp = tc.getRootToToolComponent().getLastComponent();
				task = new LeslieTask(comp);
				AnimatorTool.getInstance(tc).schedule(this, task);
			}
			int state = tc.getAxisState(slot).intValue();
			if (state>0) {
				count++;
			} else if (state<0) {
				count--;
			}
			count = (count>MAX) ? MAX : (count<-MAX) ? -MAX : count;
			task.setOmega(count*Q);
		}
	}
	
	
	public LeslieDemo() throws IOException, UnsupportedAudioFileException {
		videoSetup();
		audioSetup();
	}
	
	@SuppressWarnings("serial")
	private void videoSetup() {
		ViewMenuBar viewMenuBar = new ViewMenuBar();
		viewMenuBar.addMenuItem(LeslieDemo.class, 20.0, new AbstractAction() {
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
		
		InputStream is = getClass().getResourceAsStream("hammond.wav");
		final AudioSource source = new CachedAudioInputStreamSource("hammond", Input.getInput("hammond", is), true);
		
		final float r = 0.15f;
		SceneGraphComponent hub = new SceneGraphComponent("LeslieHub");
		int nHorns = 2;
		Geometry cone = Primitives.cone(20, -r);
		for(int i=0; i<nHorns; i++) {
			SceneGraphComponent horn = new SceneGraphComponent("horn"+i);
			horn.setGeometry(cone);
			horn.setAudioSource(source);
			MatrixBuilder.euclidean().rotate(2*i*Math.PI/nHorns, 0, 1, 0).translate(0, 0, r).assignTo(horn);
			hub.addChild(horn);
		}
		MatrixBuilder.euclidean().translate(0, 2, 0).assignTo(hub);
		hub.addTool(new DraggingTool());
		hub.addTool(new RotateTool());
		hub.addTool(new LeslieTool());
		
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
		hub.addTool(actionTool);
		controller.getPlugin(AlignedContent.class).setContent(hub);
	}

	public void startup() {
		controller.startup();
	}
	
	public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
		new LeslieDemo().startup();
	}
}
