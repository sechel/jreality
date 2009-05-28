package de.jreality.plugin.scene;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.ui.ShrinkPanelAggregator;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

public class VRPanel extends ShrinkPanelAggregator {

	public VRPanel() {
		shrinkPanel.setTitle("VR Controls");
		setInitialPosition(SHRINKER_RIGHT);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		setTriggerComponent(c.getPlugin(Scene.class).getBackdropComponent());
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("VR Panel", "jReality Group");
		info.icon = ImageHook.getIcon("controller.png");
		return info;
	}

}
