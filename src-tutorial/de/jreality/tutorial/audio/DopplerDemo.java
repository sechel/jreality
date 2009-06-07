package de.jreality.tutorial.audio;

import java.io.IOException;

import de.jreality.audio.SynthSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.reader.Readers;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.Tool;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.Input;

public class DopplerDemo {
	
	SceneGraphComponent centerCmp = new SceneGraphComponent("center cmp");
	SceneGraphComponent audioCmp = new SceneGraphComponent("source");
	
	final double a=50, b=10;

	AudioSource asrc = new SynthSource("sin", 44100) {
		final double omega = 2*Math.PI*440;

		protected float nextSample() {
			return (float) Math.sin(omega*index/sampleRate);
		}
	};
	
	// a simple tool (always active) that is triggered by SYSTEM_TIME
	Tool animator = new AbstractTool() {
		long st=-1;
		boolean playing=false;
		{
			addCurrentSlot(InputSlot.SYSTEM_TIME, "animation trigger");
		}
		@Override
		public void perform(ToolContext tc) {
			if (st==-1) st = tc.getTime();
			double currentSeconds = 0.001*(tc.getTime()-st);
			// start audio source after two seconds
			if (!playing && st>2) {
				playing = true;
				asrc.start();
			}
			setLocationForTime(currentSeconds);
		}
	};

	
	public DopplerDemo() {
		audioCmp.setAudioSource(asrc);
		try {
			SceneGraphComponent bear = Readers.read(Input.getInput("jrs/baer.jrs"));
			// the bear is a huge geometry and we cannot afford drawing
			// edges and vertices while having real time audio...
			bear.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
			bear.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			MatrixBuilder.euclidean().rotateX(-Math.PI/2).rotateZ(Math.PI).translate(0,0,2).scale(0.002).assignTo(bear);
			audioCmp.addChild(bear);
		} catch (IOException ioe) {
			// no bear available
			SceneGraphComponent icoCmp = new SceneGraphComponent();
			MatrixBuilder.euclidean().translate(0,1.7,0).assignTo(icoCmp);
			icoCmp.setGeometry(Primitives.icosahedron());
			audioCmp.addChild(icoCmp);
		}
		centerCmp.addChild(audioCmp);
		setLocationForTime(0);
		
		centerCmp.addTool(animator);
		
	}
	
	double omega = 1;
	
	double[] gamma = new double[3];
	double[] gammaDot = new double[3];
	
	double[] gammaDot0 = new double[]{0,0,-1};
	
	void setLocationForTime(double t) {
		double angle = t*omega;
		gamma[0] = a*Math.cos(angle);
		gamma[2] = -b*Math.sin(angle);
		
		gammaDot[0] = -omega*a*Math.sin(t*omega);
		gammaDot[2] = -omega*b*Math.cos(t*omega);
		
		MatrixBuilder.euclidean().translate(gamma).rotateFromTo(gammaDot0, gammaDot).assignTo(audioCmp);
	}
	
	public static void main(String[] args) {
		DopplerDemo dd = new DopplerDemo();

		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		
		v.addContentSupport(ContentType.Raw);
		v.setContent(dd.centerCmp);
		v.startup();

	}
}
