package de.jreality.tutorial.audio;

import de.jreality.audio.SynthSource;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.AudioSource;
import de.jreality.tutorial.tool.AnimationExample;


public class DopplerDemo extends AnimationExample {

	static AudioSource asrc = new SynthSource("sin", 44100) {
		final double omega = 2*Math.PI*440;

		protected float nextSample() {
			return (float) Math.sin(omega*index/sampleRate);
		}
	};

	public DopplerDemo() {
		// we increase the size of the ellipse to have a faster
		// moving object. This gives more doppler shift... :
		a=50;
		b=15;
	}
	
	public static void main(String[] args) {
		DopplerDemo example = new DopplerDemo();

		// create a VR viewer that does no alignment to the content:
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addAudioSupport();
		v.addContentSupport(ContentType.Raw);
		
		// add the audio source to the moving component of the animation example:
		example.getMovingComponent().setAudioSource(asrc);
		
		
		// set the center component of the ellipse as content:
		v.setContent(example.getCenterComponent());
		
		// startup the viewer:
		v.startup();

		// start the audio source:
		asrc.start();
	}
}
