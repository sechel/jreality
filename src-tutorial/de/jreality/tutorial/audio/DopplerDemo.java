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
	
	public DopplerDemo() {
		audioCmp.setAudioSource(asrc);
		try {
			SceneGraphComponent bear = Readers.read(Input.getInput("jrs/baer.jrs"));
			MatrixBuilder.euclidean().rotateZ(Math.PI).translate(0,0,2).scale(0.002).assignTo(bear);
			audioCmp.addChild(bear);
		} catch (IOException ioe) {
			// no bear available
			audioCmp.setGeometry(Primitives.icosahedron());
		}
		centerCmp.addChild(audioCmp);
		setLocationForTime(0);
	}
	
	double omega = 1;
	
	double[] gamma = new double[3];
	double[] gammaDot = new double[3];
	
	double[] gammaDot0 = new double[]{0,1,0};
	
	void setLocationForTime(double t) {
		double angle = t*omega;
		gamma[0] = a*Math.cos(angle);
		gamma[1] = b*Math.sin(angle);
		
		gammaDot[0] = -omega*a*Math.sin(t*omega);
		gammaDot[1] = omega*b*Math.cos(t*omega);
		
		MatrixBuilder.euclidean().translate(gamma).rotateFromTo(gammaDot0, gammaDot).assignTo(audioCmp);
	}
	
	public static void main(String[] args) {
		DopplerDemo dd = new DopplerDemo();

		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.setPropertiesFile("AudioExample.jrw");
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(dd.centerCmp);
		v.startup();
		
		dd.asrc.start();
		
		long st = System.currentTimeMillis();
		while (true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long ct = System.currentTimeMillis();
			double t = (ct-st)*0.001;
			dd.setLocationForTime(t);
		}
	}
}
