package de.jreality.plugin.job;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class JobMonitorPlugin extends ShrinkPanelPlugin {

	public JobMonitorPlugin() {
		
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
