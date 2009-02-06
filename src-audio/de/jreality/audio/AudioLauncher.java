package de.jreality.audio;

import de.jreality.scene.Viewer;

/* Removing this class broke too much code in my students' projects.
 * 
 */
public class AudioLauncher {
	
	@Deprecated
	public static void launch(Viewer v) 	{
		de.jreality.audio.util.AudioLauncher.launch(v);
	}
	
	@Deprecated
	public static void suggestSampleRate(int s)	{
		
	}
}
