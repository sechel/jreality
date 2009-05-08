package de.jreality.plugin.view;

import de.jreality.plugin.view.View.RunningEnvironment;
import de.jreality.portal.PortalCoordinateSystem;
import de.jreality.swing.jrwindows.JRWindowManager;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class SceneWindowManager extends Plugin {

	private JRWindowManager
		manager = null;
	
	
	public JRWindowManager getWindowManager() {
		return manager;
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		View view = c.getPlugin(View.class);
		if (view.getRunningEnvironment() == RunningEnvironment.PORTAL || view.getRunningEnvironment() == RunningEnvironment.PORTAL_REMOTE) {
			manager = new JRWindowManager(view.getAvatarPath().getLastComponent());
			manager.setPosition(new double[]{0, PortalCoordinateSystem.convertMeters(1.24), PortalCoordinateSystem.convertMeters(-1.24)});
		} else {
			manager = new JRWindowManager(view.getCameraPath().getLastComponent());
			manager.setPosition(new double[]{0, 0, -2});
		}
		manager.setWindowsInScene(true);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Scene Window Manager", "jReality Group");
	}

}
