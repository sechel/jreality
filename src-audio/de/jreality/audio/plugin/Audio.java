package de.jreality.audio.plugin;

import de.jreality.audio.util.AudioLauncher;
import de.jreality.scene.Viewer;
import de.jreality.ui.plugin.view.CameraStand;
import de.jreality.ui.plugin.view.View;
import de.jreality.ui.plugin.view.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Audio extends Plugin {

		@Override
		public PluginInfo getPluginInfo() {
			PluginInfo info = new PluginInfo();
			info.name = "Audio";
			info.vendorName = "Ulrich Pinkall"; 
			info.icon = ImageHook.getIcon("radioactive1.png");
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
