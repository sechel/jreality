package de.jreality.audio.jack;

import de.gulden.framework.jjack.JJackException;
import de.jreality.audio.AmbisonicsSoundEncoder;
import de.jreality.audio.AudioBackend;
import de.jreality.audio.Interpolation;
import de.jreality.audio.SoundPath;
import de.jreality.scene.Viewer;

/**
 * 
 * Simple Jack back-end for Ambisonics.  All the real work occurs in {@link AudioBackend}; this class
 * merely takes care of the Jack init (which is a bit dicey; see comment in {@link JackSource}), collects
 * the samples and writes them to the Jack output buffers.
 * 
 * Use the {@code launch}-method to activate this renderer for a given {@link Viewer}.
 * 
 * @author brinkman
 *
 */
public class JackAmbisonicsRenderer extends AbstractJackRenderer {

	public JackAmbisonicsRenderer() {
		encoder = new AmbisonicsSoundEncoder() {
			public void finishFrame() {
				currentJJackEvent.getOutput(0).put(bw);
				currentJJackEvent.getOutput(1).put(bx);
				currentJJackEvent.getOutput(2).put(by);
				currentJJackEvent.getOutput(3).put(bz);
			}
		};
	}
	
	public int highestPort() {
		return 3;
	}

	/**
	 * 
	 * @param viewer
	 * @param label
	 * @param iFactory
	 * @param spFactory
	 * @throws JJackException
	 * 
	 * @deprecated - use constructor or a plugin
	 */
	public static void launch(Viewer viewer, String label, Interpolation.Factory iFactory, SoundPath.Factory spFactory) throws JJackException {
		launch(viewer, label, "", iFactory, spFactory);
	}
	
	/**
	 * 
	 * @param viewer
	 * @param label
	 * @param target
	 * @param ifactory
	 * @param spFactory
	 * @throws JJackException
	 * 
	 * @deprecated - use constructor or a plugin
	 */
	public static void launch(Viewer viewer, String label, String target, Interpolation.Factory ifactory, SoundPath.Factory spFactory) throws JJackException {
		JackAmbisonicsRenderer renderer = new JackAmbisonicsRenderer();
		renderer.setSceneRoot(viewer.getSceneRoot());
		renderer.setMicrophonePath(viewer.getCameraPath());
		renderer.setInterpolationFactory(ifactory);
		renderer.setSoundPathFactory(spFactory);
		renderer.setLabel(label);
		renderer.setTarget(target);
		renderer.launch();
		
	}
}
