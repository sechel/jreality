package de.jreality.audio;

import de.jreality.scene.Viewer;

/* *
 * Removing this class broke too much code in my students' projects.
 * 
 * Deprecated: use audio.plugin.AudioLauncher instead, or directly launch the backend you want
 */
@Deprecated
public class AudioLauncher {
	
	@Deprecated
	public static void launch(Viewer v) 	{
		de.jreality.audio.util.AudioLauncher.launch(v);
	}
	
	@Deprecated
	public static void suggestSampleRate(int s)	{
		
	}
}
