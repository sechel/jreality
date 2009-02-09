package de.jreality.tutorial.audio;

import de.jreality.audio.SynthSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.vr.ViewerVR;

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
		audioCmp.setGeometry(Primitives.icosahedron());
		centerCmp.addChild(audioCmp);
		setLocationForTime(0);
	}

	double omega = 1;
	
	void setLocationForTime(double t) {
		double angle = t*omega;
		double x = a*Math.cos(angle);
		double y = b*Math.sin(angle);
		MatrixBuilder.euclidean().translate(x, y, 1.0).rotateZ(angle).assignTo(audioCmp);
	}
	
	public static void main(String[] args) {
		DopplerDemo dd = new DopplerDemo();
		
		
		ViewerVR vr = ViewerVR.createDefaultViewerVR(null);
		vr.setDoAlign(false);
		vr.setContent(dd.centerCmp);
		
		ViewerApp va = vr.initialize();
		va.update();
		va.display();
		
		de.jreality.audio.util.AudioLauncher.launch(va.getCurrentViewer());
		
		long st = System.currentTimeMillis();
		
		dd.asrc.start();
		
		while (true) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double t = (System.currentTimeMillis()-st)/1000.;
			dd.setLocationForTime(t);
		}
	}
}
