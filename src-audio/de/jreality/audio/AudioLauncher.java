package de.jreality.audio;

import java.beans.Statement;

import javax.sound.sampled.LineUnavailableException;

import de.jreality.audio.javasound.JavaAmbisonicsStereoDecoder;
import de.jreality.audio.javasound.JavaSoundUtility;
import de.jreality.audio.javasound.VbapSurroundRenderer;
import de.jreality.scene.Viewer;

public class AudioLauncher {

	public static boolean TRY_JACK=true;
	public static boolean PLANAR=false;
	public static boolean TRY_5_1=false;
	
	private AudioLauncher() {}
	
	/**
	 * Launches Jack backend if possible, otherwise Java sound backend.
	 *   
	 * @param v A viewer which defines the scene root and the microphone path (which is for now the camera path).
	 * 
	 * @return flag indicating whether a sound renderer was successfully launched.
	 */
	public static boolean launch(Viewer v) {
		if (TRY_JACK) {
			Class<?> jackrenderer = null;
			try {
				
				String classname = PLANAR ? 
			"de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer"
		:	"de.jreality.audio.jack.JackAmbisonicsRenderer";
				
				jackrenderer = Class.forName(classname);
				
			} catch (ClassNotFoundException e1) {
				// ignore this, just use java sound.
			}
			if (jackrenderer != null) try {
				new Statement(jackrenderer, "launch", new Object[]{v}).execute();
				System.out.println("Jack launch OK.");
				return true;
			} catch (Exception e) {
				System.err.println("Jack launch FAILED (fallback to java sound):");
				e.printStackTrace();
			}
		}
		try {
			if (TRY_5_1 && JavaSoundUtility.supportsChannels(5)) {
				System.out.println("Launching 5.1 backend...");
				VbapSurroundRenderer.launch(v);
			} else {
				// stereo backend...
				//BinauralDecoder.launch(v);
				JavaAmbisonicsStereoDecoder.launch(v);
			}
			return true;
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
}
