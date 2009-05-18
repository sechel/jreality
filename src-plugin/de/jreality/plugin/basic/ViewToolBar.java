package de.jreality.plugin.basic;

import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ViewToolBar extends ToolBarAggregator {

	public ViewToolBar() {
		
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("View Tool Bar", "Stefan Sechelmann");
	}

	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	@Override
	public double getToolBarPriority() {
		return -10.0;
	}
	
}
