package de.jreality.plugin.content;

import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class DirectContent extends Content {
		
	protected SceneGraphNode 
		content = null;
	
	@Override
	public void setContent(SceneGraphNode node) {
		SceneGraphComponent root = getContentRoot();
		if (content != null) {
			SceneGraphUtility.removeChildNode(root, content);
		}
		content = node;
		if (content != null) { 
			SceneGraphUtility.addChildNode(root, content);
		}
		super.setContent(node);
	}

	
	public void uninstall(Scene scene, Controller c) {
		setContent(null);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Direct Content", "jReality Group");
	}

}
