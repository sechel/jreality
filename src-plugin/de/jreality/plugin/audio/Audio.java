package de.jreality.plugin.audio;

import de.jreality.audio.util.AudioLauncher;
import de.jreality.plugin.audio.image.ImageHook;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.View;
import de.jreality.scene.Viewer;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Audio extends Plugin {

		@Override
		public PluginInfo getPluginInfo() {
			PluginInfo info = new PluginInfo();
			info.name = "Audio";
			info.vendorName = "Ulrich Pinkall"; 
			info.icon = ImageHook.getIcon("Volume-Normal-48x48.png");
			return info;
		}

		@Override
		public void install(Controller c) throws Exception {
			Viewer viewer = c.getPlugin(View.class).getViewer();
			c.getPlugin(CameraStand.class);
			try {
				AudioLauncher.launch(viewer); 
			} catch (Exception e) {
				System.out.println("Could not launch audio engine: " + e);
			}
		}

		@Override
		public void uninstall(Controller c) throws Exception {
			super.uninstall(c);
		}

}
