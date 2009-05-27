package de.jreality.plugin.content;

import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;

public abstract class ContentPanel extends SceneShrinkPanel {

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		setTriggerComponent(c.getPlugin(Scene.class).getContentComponent());
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
}
