package de.jreality.audio.jack;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import de.jreality.audio.csound.CsoundNode;
import de.jreality.audio.javasound.AudioInputStreamSource;
import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.AudioSource;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.RingBuffer.Reader;
import de.jreality.tools.ActionTool;
import de.jreality.tools.DraggingTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.vr.ViewerVR;

public class JackTest {

	public static void main(String[] args) throws Exception {

		SceneGraphComponent cmp = new SceneGraphComponent();
		DraggingTool dragtool = new DraggingTool();

		
		Input wavWaterdrops = Input.getInput("data/waterdrop.wav");
		final AudioSource s1 = new CachedAudioInputStreamSource("wavnode", wavWaterdrops, true);
		SceneGraphComponent cmp1 = new SceneGraphComponent();
		cmp1.setGeometry(new Sphere());
		MatrixBuilder.euclidean().translate(-4, 0, 0).assignTo(cmp1);
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
		final AudioSource s2 = new JackNode("jack_input", 0);
		cmp2.setGeometry(Primitives.icosahedron());
		MatrixBuilder.euclidean().translate(0, 0, 0).assignTo(cmp2);
		cmp2.setAudioSource(s2);
		ActionTool at2 = new ActionTool("PanelActivation");
		at2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (s2.getState() == AudioSource.State.RUNNING) s2.pause();
				else s2.start();
			}
		});
		cmp2.addTool(at2);
		cmp2.addTool(dragtool);
		s2.start();

		
		SceneGraphComponent cmp3 = new SceneGraphComponent();
		cmp.addChild(cmp3);
		
		// works with mp3spi from javazoom in classpath:
		URL url = new URL("http://www.br-online.de/imperia/md/audio/podcast/import/2008_09/2008_09_29_16_33_02_podcastdienasawird50_a.mp3");
		Input input = Input.getInput(url); 
		final AudioSource s3 = new AudioInputStreamSource("podcast", input, false) {
			@Override
			public int readSamples(Reader reader, float[] buffer,
					int initialIndex, int samples) {
				// TODO Auto-generated method stub
				int ret = super.readSamples(reader, buffer, initialIndex, samples);
				for (int i=0; i<buffer.length; i++) {
					buffer[i]*=0.3f;
				}
				return ret;
			}
		};
			
//		final AudioSource s3 = new CsoundNode("csnode", Input.getInput("data/trapped.csd"));
		
		cmp3.setGeometry(Primitives.cube());
		MatrixBuilder.euclidean().translate(4, 0, 0).assignTo(cmp3);
		cmp3.setAudioSource(s3);
		ActionTool at3 = new ActionTool("PanelActivation");
		at3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (s3.getState() == AudioSource.State.RUNNING) s3.pause();
				else s3.start();
			}
		});
		cmp3.addTool(at3);
		cmp3.addTool(dragtool);
		s3.start();

		
		ViewerVR vr = ViewerVR.createDefaultViewerVR(null);
		ViewerApp va = vr.initialize();
		va.update();
		va.display();

		vr.setContent(cmp);

		JackAmbisonicsRenderer.launch(va.getCurrentViewer());
		
	}

}
