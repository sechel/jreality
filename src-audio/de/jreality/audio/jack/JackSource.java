package de.jreality.audio.jack;

import java.nio.FloatBuffer;

public interface JackSource {

	/* Slightly hackish init method, to be called by JackHub; we can only initialize JJackSystem after the required number
	 * of ports has been reported, but the jack sample rate only becomes available after JJackSystem has been initialized.
	 * So, we need to create jack sources, have them request input channels, then initialize JJackSystem, then initialize
	 * ring buffers and such for the sample rate.  Ugh.
	 */
	public void init(int sampleRate);
	public int highestPort();
	
	public void process(FloatBuffer[] sources);
}
