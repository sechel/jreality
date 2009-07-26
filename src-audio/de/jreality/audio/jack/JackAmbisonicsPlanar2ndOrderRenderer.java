 package de.jreality.audio.jack;

import de.jreality.audio.AmbisonicsPlanar2ndOrderSoundEncoder;

/**
 * Jack back-end for Second Order Planar Ambisonics.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 */
public class JackAmbisonicsPlanar2ndOrderRenderer extends AbstractJackRenderer {

	{
		nPorts = 5;
	}
	
	public JackAmbisonicsPlanar2ndOrderRenderer() {
		encoder=new AmbisonicsPlanar2ndOrderSoundEncoder() {
			public void finishFrame() {
				int port = JackManager.getPort(key);
				currentJJackEvent.getOutput(port+0).put(bw);
				currentJJackEvent.getOutput(port+1).put(bx);
				currentJJackEvent.getOutput(port+2).put(by);
				currentJJackEvent.getOutput(port+3).put(bu);
				currentJJackEvent.getOutput(port+4).put(bv);
			}
		};
	}
}
