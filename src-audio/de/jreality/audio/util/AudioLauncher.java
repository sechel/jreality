package de.jreality.audio.util;

import javax.sound.sampled.LineUnavailableException;

import de.jreality.audio.javasound.StereoRenderer;
import de.jreality.scene.Viewer;

/**
 * 
 * Convenience class for launching audio backends.
 * 
 * @deprecated see launch
 */
public class AudioLauncher {

	private AudioLauncher() {}
	
	/**
	 * Launches java sound stereo renderer. This is depricated and should be
	 * replaced by directly launching a sound renderer ur by the audio plugin.
	 */
	public static void launch(Viewer v) {
		StereoRenderer sr = new StereoRenderer();
		sr.setSceneRoot(v.getSceneRoot());
		sr.setMicrophonePath(v.getCameraPath());
		try {
			sr.launch();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
