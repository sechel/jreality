package de.jreality.plugin.basic.content;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.scene.SceneShrinkPanel;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

public class ContentPanel extends SceneShrinkPanel {

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		setTriggerComponent(c.getPlugin(Scene.class).getContentComponent());
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Content settings");
	}
	
}
