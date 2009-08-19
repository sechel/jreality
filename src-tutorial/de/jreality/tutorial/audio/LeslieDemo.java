package de.jreality.tutorial.audio;


import java.io.InputStream;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.AudioSource;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.AnimatorTask;
import de.jreality.tools.AnimatorTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.jreality.util.Input;


/**
 * Physically simulated Leslie speaker; use page up/down to regulate rotor speed
 * 
 * @author brinkman
 *
 */
public class LeslieDemo {

	
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
	
	private static SceneGraphComponent getHubComponent() throws Exception {
		InputStream is = LeslieDemo.class.getResourceAsStream("pale.wav");
		final AudioSource source = new CachedAudioInputStreamSource("hammond", Input.getInput("hammond", is), true);
		
		final float r = 0.15f;
		SceneGraphComponent hub = new SceneGraphComponent("LeslieHub");
		int nHorns = 1;
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

		source.start();
		
		return hub;
	}

	
	public static void main(String[] args) throws Exception {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(getHubComponent());
		v.startup();
	}
	
}
