package de.jreality.audio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import de.jreality.audio.SignalSource;
import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.vr.ViewerVR;

/**
 * Basic test to check spatial audio.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 *
 */
public class TestVR {

	public static void main(String[] args) throws Exception {

		SceneGraphComponent cmp = new SceneGraphComponent();
		DraggingTool dragtool = new DraggingTool();

		Input wavWaterdrops = Input.getInput("data/waterdrop.wav");
		final AudioSource s1 = new CachedAudioInputStreamSource("wavnode", wavWaterdrops, true);
		SceneGraphComponent cmp1 = new SceneGraphComponent();
		cmp1.setGeometry(new Sphere());
		MatrixBuilder.euclidean().translate(-2, 0, 0).assignTo(cmp1);
		cmp.addChild(cmp1);
		cmp1.setAudioSource(s1);
		ActionTool at1 = new ActionTool("PanelActivation");
		at1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (s1.getState() == AudioSource.State.RUNNING) s1.pause();
				else s1.start();
			}
		});
		cmp1.addTool(at1);
		cmp1.addTool(dragtool);
		s1.start();

		
		SceneGraphComponent cmp2 = new SceneGraphComponent();
		cmp.addChild(cmp2);
		
		final SignalSource sin = new SignalSource("wave", 44100) {
			float amplitude=0.03f;
			double frequency=440;
			@Override
			public float evaluateSignal(double t) {
				//double x = 2*(-0.5+Math.random());
				//return (float) x;
				//return x>0.85 ? 1 : 0;
				t*=frequency;
				return amplitude * (float) Math.sin(2*Math.PI*t);
				//return 0.03f*(float) (t-Math.floor(t));
			}
		};
		
		cmp2.setGeometry(Primitives.icosahedron());
		MatrixBuilder.euclidean().translate(2, 0, 0).assignTo(cmp2);
		cmp2.setAudioSource(sin);
		ActionTool at2 = new ActionTool("PanelActivation");
		at2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sin.getState() == AudioSource.State.RUNNING) sin.pause();
				else sin.start();
			}
		});
		cmp2.addTool(at2);
		cmp2.addTool(dragtool);
		sin.start();
		
		ViewerVR vr = ViewerVR.createDefaultViewerVR(null);
		// ViewerApp va = ViewerApp.display(cmp);
		ViewerApp va = vr.initialize();
		va.update();
		va.display();

		vr.setContent(cmp);

		JavaAmbisonicsStereoDecoder.launch(va.getCurrentViewer());
		//JackAmbisonicsRenderer.launch(va.getCurrentViewer());
		
	}

}
