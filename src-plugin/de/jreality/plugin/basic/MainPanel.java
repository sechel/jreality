package de.jreality.plugin.basic;

import de.jreality.plugin.icon.ImageHook;
import de.jreality.ui.ShrinkPanelAggregator;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

public class MainPanel extends ShrinkPanelAggregator {

	public MainPanel() {
		super();
		shrinkPanel.setTitle("Main Tools");
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Main Shrink Panel", "jReality Group");
		info.icon = ImageHook.getIcon("wrench_orange.png");
		return info;
	}
	
	
}
