 package de.jreality.audio.jack;

import de.jreality.audio.AmbisonicsPlanar2ndOrderSoundEncoder;

/**
 * Jack back-end for Second Order Planar Ambisonics.
 * 
 * @author <a href="mailto:weissman@math.tu-berlin.de">Steffen Weissmann</a>
 */
public class JackAmbisonicsPlanar2ndOrderRenderer extends AbstractJackAmbisonicsRenderer {

	public JackAmbisonicsPlanar2ndOrderRenderer() {
		encoder=new AmbisonicsPlanar2ndOrderSoundEncoder() {
			public void finishFrame() {
				currentJJackEvent.getOutput(0).put(bw);
				currentJJackEvent.getOutput(1).put(bx);
				currentJJackEvent.getOutput(2).put(by);
				currentJJackEvent.getOutput(3).put(bu);
				currentJJackEvent.getOutput(4).put(bu);
			}
		};
	}
	
	public int highestPort() {
		return 4;
	}
	
}
