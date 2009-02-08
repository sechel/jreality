package de.jreality.tutorial.audio;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.RingBuffer;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.vr.ViewerVR;

public class DopplerDemo {
	
	SceneGraphComponent centerCmp = new SceneGraphComponent("center cmp");
	SceneGraphComponent audioCmp = new SceneGraphComponent("source");
	
	final double a=50, b=10;

	AudioSource asrc = new AudioSource("sin") {
		float[] buf;
		{
			sampleRate = 44100;
			ringBuffer = new RingBuffer(sampleRate);
			buf = new float[sampleRate];
		}
		double frequency=440;
		long index=0;
		@Override
		protected void writeSamples(int n) {
			for (int i=0; i<n; i++) {
				double t = (double)(index+i)/(double)sampleRate;
				buf[i] = (float) Math.sin(t*frequency*2*Math.PI);
			}
			ringBuffer.write(buf, 0, n);
			index+=n;
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
