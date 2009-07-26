package de.jreality.audio.jack;

import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AudioBackend;

/**
 * Simple Jack back-end for Ambisonics.  All the real work occurs in {@link AudioBackend}; this class
 * merely takes care of the Jack init, collects the samples and writes them to the Jack output buffers.
 * 
 * @author brinkman
 *
 */
public class JackAmbisonicsRenderer extends AbstractJackRenderer {
	
	{
		nPorts = 4;
	}
	
	public JackAmbisonicsRenderer() {
		encoder = new AmbisonicsSoundEncoder() {
			public void finishFrame() {
				int port = JackManager.getPort(key);
				currentJJackEvent.getOutput(port+0).put(bw);
				currentJJackEvent.getOutput(port+1).put(bx);
				currentJJackEvent.getOutput(port+2).put(by);
				currentJJackEvent.getOutput(port+3).put(bz);
			}
		};
	}
}
