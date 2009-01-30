package de.jreality.audio.plugin;

import de.jreality.audio.AudioLauncher;
import de.jreality.scene.Viewer;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.image.ImageHook;
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
			AudioLauncher.suggestSampleRate(22050);
			AudioLauncher.launch(viewer);
		}

		@Override
		public void uninstall(Controller c) throws Exception {
			super.uninstall(c);
		}

}
