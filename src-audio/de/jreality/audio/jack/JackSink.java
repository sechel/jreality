package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackAudioProcessor;

/**
 * An extension of JJackAudioProcessor that provides the necessary callbacks for JackHub.
 * 
 * @author brinkman
 *
 */
public interface JackSink extends JJackAudioProcessor {

	// Slightly hackish init method; see justification in JackSource.java
	public void init(int sampleRate);
	public int highestPort();
	
}
