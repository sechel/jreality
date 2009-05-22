package de.jreality.plugin.basic.audio;

import java.lang.reflect.Method;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.audio.AudioAttributes;
import de.jreality.audio.AudioRenderer;
import de.jreality.audio.Interpolation;
import de.jreality.audio.javasound.AbstractJavaSoundRenderer;
import de.jreality.audio.javasound.StereoRenderer;
import de.jreality.audio.javasound.VbapRenderer;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphPath;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Audio extends Plugin implements ChangeListener {

	public static enum BackendType {
		noSound,
		javaSound,
		javaSoundVBAP,
		jackAmbisonicsFO,
		jackAmbisonicsPSO;
	}
	
	public static enum InterpolationType {
		noInterpolation,
		linearInterpolation,
		cosineInterpolation,
		cubicInterpolation;
	}
	
	private AudioPreferences 
		prefs = null;
	private Scene
		scene = null;
	private AudioRenderer 
		renderer = null;
	private Interpolation.Factory
		interpolationFactory = AudioAttributes.DEFAULT_INTERPOLATION_FACTORY;
	
	private SceneGraphPath lastMicrophonePath;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Audio";
		info.vendorName = "jReality Group"; 
		info.icon = ImageHook.getIcon("sound.png");
		return info;
	}

	@Override
	public void install(Controller c) {
		scene = c.getPlugin(Scene.class);	
		prefs = c.getPlugin(AudioPreferences.class);
		prefs.addChangeListener(this);
		scene.addChangeListener(this);
		try {
			updateAudioRenderer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void updateAudioRenderer() throws Exception {
		if (renderer != null) {
			try {
				renderer.shutdown();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		switch (prefs.getBackendType()) {
		case noSound:
			renderer=null;
			return;
		case javaSound:
			renderer = new StereoRenderer();
			break;
		case javaSoundVBAP:
			renderer = new VbapRenderer();
			break;
		case jackAmbisonicsFO:
			try {
					renderer = (AudioRenderer) Class.forName("de.jreality.audio.jack.JackAmbisonicsRenderer").newInstance();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		case jackAmbisonicsPSO:
			try {
					renderer = (AudioRenderer) Class.forName("de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer").newInstance();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		default:
			break;
		}
		switch (prefs.getInterpolationType()) {
		case noInterpolation:
			interpolationFactory = Interpolation.SampleHold.FACTORY;
			break;
		case linearInterpolation:
			interpolationFactory = Interpolation.Linear.FACTORY;
			break;
		case cosineInterpolation:
			interpolationFactory = Interpolation.Cosine.FACTORY;
			break;
		case cubicInterpolation:
			interpolationFactory = Interpolation.Cubic.FACTORY;
			break;
		}
		
		if (renderer == null) return;
		
		renderer.setSceneRoot(scene.getSceneRoot());
		SceneGraphPath micPath = scene.getMicrophonePath();
		renderer.setMicrophonePath(micPath);
		lastMicrophonePath = new SceneGraphPath(micPath);
		
		renderer.setInterpolationFactory(interpolationFactory);
		
		if (renderer instanceof AbstractJavaSoundRenderer) {
			AbstractJavaSoundRenderer javaSoundRenderer = (AbstractJavaSoundRenderer) renderer;
			javaSoundRenderer.setFrameSize(prefs.getJavaSoundFrameSize());
		}

		if (renderer.getClass().getName().contains("jack")) {
			Method setLabelMethod = renderer.getClass().getMethod("setLabel", String.class);
			Method setTargetMethod = renderer.getClass().getMethod("setTarget", String.class);
			Method setRetriesMethod = renderer.getClass().getMethod("setRetries", int.class);
			setLabelMethod.invoke(renderer, prefs.getJackLabel());
			setTargetMethod.invoke(renderer, prefs.getJackTarget());
			setRetriesMethod.invoke(renderer, prefs.getJackRetries());
		}
		
		renderer.launch();
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		if (renderer != null) renderer.shutdown();
	}

	public void stateChanged(ChangeEvent e) {
		try {
			if (e.getSource() instanceof AudioPreferences) updateAudioRenderer();
			if (e.getSource() instanceof Scene) {
				SceneGraphPath newMicPath = scene.getMicrophonePath();
				if (newMicPath == lastMicrophonePath) return;
				if (lastMicrophonePath != null && lastMicrophonePath.isEqual(newMicPath)) return;
				updateAudioRenderer();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
