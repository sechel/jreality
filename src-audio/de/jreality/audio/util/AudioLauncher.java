package de.jreality.audio.util;

import java.beans.Statement;

import javax.sound.sampled.LineUnavailableException;

import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.audio.javasound.JavaSoundUtility;
import de.jreality.audio.javasound.VbapSurroundRenderer;
import de.jreality.scene.Viewer;

/**
 * 
 * Convenience class for launching audio backends.
 *
 */
public class AudioLauncher {

	public static boolean TRY_JACK=true;
	public static boolean PLANAR=false;
	public static boolean TRY_5_1=false;


	private AudioLauncher() {}
	

	/**
	 * Launches Jack backend if possible, otherwise Java sound backend.
	 *   
	 * @param v A viewer that defines the scene root and the microphone path (which is for now the camera path).
	 * 
	 * @return flag indicating whether a sound renderer was successfully launched.
	 */
	public static boolean launch(Viewer v) {
		if (TRY_JACK) {
			Class<?> jackrenderer = null;
			try {	
				String classname = PLANAR ?	"de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer" :
											"de.jreality.audio.jack.JackAmbisonicsRenderer";
				jackrenderer = Class.forName(classname);
			} catch (ClassNotFoundException e1) {
				// ignore this, just use java sound.
			}
			if (jackrenderer != null) try {
				new Statement(jackrenderer, "launch", new Object[]{v, "jR Ambisonics", "StereoDecoder"}).execute();
				System.out.println("Jack launch OK.");
				return true;
			} catch (Exception e) {
				System.err.println("Jack launch FAILED (fallback to java sound):");
			}
		}
		try {
			if (TRY_5_1 && JavaSoundUtility.supportsChannels(5)) {
				System.out.println("Launching 5.1 backend...");
				VbapSurroundRenderer.launch(v, "jR VBAP");
			} else {
				System.out.println("Launching stereo backend...");
				JavaAmbisonicsStereoDecoder.launch(v, "jR Stereo");
			}
			return true;
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return false;
	}
}
