package de.jreality.audio.jack;

import java.nio.FloatBuffer;

/**
 * Interface for audio sources that read Jack input channels, with the necessary callbacks for initialization
 * within JackHub.
 * 
 * @author brinkman
 *
 */
public interface JackSource {
	public int highestPort();	
	public void process(FloatBuffer[] sources);
}
