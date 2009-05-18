package de.jreality.plugin.basic;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class EmptyPickContent extends Plugin implements Content {
	
	protected View view;
	
	public Appearance getContentAppearance() {
		return view.getEmptyPickPath().getLastComponent().getAppearance();
	}

	public SceneGraphComponent getContentRoot() {
		return view.getEmptyPickPath().getLastComponent();
	}

	public void setContent(SceneGraphNode content) {
		SceneGraphUtility.addChildNode(getContentRoot(), content);
	}

	@Override
	public void install(Controller c) throws Exception {
		view = c.getPlugin(View.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("simple content");
	}

}
