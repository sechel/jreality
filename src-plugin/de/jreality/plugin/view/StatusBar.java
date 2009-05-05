package de.jreality.plugin.view;

import de.jreality.plugin.view.image.ImageHook;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.StatusFlavor;

public class StatusBar extends Plugin implements StatusFlavor {

	private StatusChangedListener
		statusChangedListener = null;

	
	public void setStatus(String status) {
		if (statusChangedListener != null) {
			statusChangedListener.statusChanged(status);
		}
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Status Bar", "Stefan Sechelmann");
		info.icon = ImageHook.getIcon("textfield.png");
		return info;
	}

	public void setStatusListener(StatusChangedListener scl) {
		statusChangedListener = scl;
	}

}
