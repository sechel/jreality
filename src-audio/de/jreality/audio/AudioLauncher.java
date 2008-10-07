package de.jreality.audio;

import java.beans.Statement;

import javax.sound.sampled.LineUnavailableException;

import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.scene.Viewer;

public class AudioLauncher {

	private AudioLauncher() {}
	
	/**
	 * Launches Jack backend if possible, otherwise Java sound backend.
	 *   
	 * @param v A viewer which defines the scene root and the microphone path (which is for now the camera path).
	 * 
	 * @return flag indicating whether a sound renderer was successfully launched.
	 */
	public static boolean launch(Viewer v) {
		try {
			new Statement(Class.forName("de.jreality.audio.jack.JackAmbisonicsRenderer"), "launch", new Object[]{v}).execute();
			System.out.println("Jack launch OK.");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Jack launch FAILED. Using JavaSound Stereo Renderer...");
		try {
			JavaAmbisonicsStereoDecoder.launch(v);
			return true;
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
