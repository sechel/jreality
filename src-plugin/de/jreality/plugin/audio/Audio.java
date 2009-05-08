package de.jreality.plugin.audio;

import java.lang.reflect.Method;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.audio.AudioRenderer;
import de.jreality.audio.javasound.AbstractJavaSoundRenderer;
import de.jreality.audio.javasound.StereoRenderer;
import de.jreality.audio.javasound.VbapRenderer;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.View;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Audio extends Plugin implements ChangeListener {

	public static enum BackendType {
		noSound,
		javaSound,
		javaSoundVBAP,
		jackAmbisonicsFO,
		jackAmbisonicsSO,
		jackAmbisonicsPSO;
	}
	
	private AudioPreferences 
		prefs = null;
	private View 
		view = null;
	private AudioRenderer 
		renderer = null;
	
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
		view = c.getPlugin(View.class);
		c.getPlugin(CameraStand.class);		
		prefs = c.getPlugin(AudioPreferences.class);
		prefs.addChangeListener(this);
		try {
			updateAudioRenderer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void updateAudioRenderer() throws Exception {
		if (renderer != null) {
			try {
				renderer.unlaunch();
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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		case jackAmbisonicsPSO:
			try {
					renderer = (AudioRenderer) Class.forName("de.jreality.audio.jack.JackAmbisonicsPlanar2ndOrderRenderer").newInstance();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		case jackAmbisonicsSO:
			try {
					renderer = (AudioRenderer) Class.forName("de.jreality.audio.jack.??").newInstance();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			break;
		default:
			break;
		}
		
		if (renderer == null) return;
		
		renderer.setMicrophonePath(view.getCameraPath());
		renderer.setSceneRoot(view.getSceneRoot());
		
		if (renderer instanceof AbstractJavaSoundRenderer) {
			AbstractJavaSoundRenderer javaSoundRenderer = (AbstractJavaSoundRenderer) renderer;
			javaSoundRenderer.setFrameSize(prefs.getJavaSoundFrameSize());
		}

		if (renderer.getClass().getName().contains("jack")) {
			Method setLabelMethod = renderer.getClass().getMethod("setLabel", String.class);
			Method setTargetMethod = renderer.getClass().getMethod("setTarget", String.class);
			setLabelMethod.invoke(renderer, prefs.getJackLabel());
			setTargetMethod.invoke(renderer, prefs.getJackTarget());
		}
		
		renderer.launch();

	}

	@Override
	public void uninstall(Controller c) throws Exception {
		if (renderer != null) renderer.unlaunch();
	}

	public void stateChanged(ChangeEvent e) {
		try {
			updateAudioRenderer();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
