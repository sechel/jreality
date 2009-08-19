package de.jreality.tutorial.audio;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.jreality.audio.SynthSource;
import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.AudioSource;
import de.jreality.tutorial.tool.AnimationExample;
import de.jreality.util.Input;

/**
 * This demo is based on the AnimationExample from the tools section.
 * 
 * Here, we simply add an audio source to the moving component, and
 * we increase the size of the ellipse to get more speed and more
 * doppler shift.
 * 
 * @author weissman
 *
 */
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

		InputStream wavFile = AudioExample.class.getResourceAsStream("70936__guitarguy1985__police.wav");
		AudioSource source;
		try {
			source = new CachedAudioInputStreamSource("siren", Input.getInput("siren", wavFile), true);
			// add the audio source to the moving component of the animation example:
			example.getMovingComponent().setAudioSource(source);
			source.start();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set the center component of the ellipse as content:
		v.setContent(example.getCenterComponent());
		
		// startup the viewer:
		v.startup();

	}
}
