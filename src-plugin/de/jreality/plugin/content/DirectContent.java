package de.jreality.plugin.content;

import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class DirectContent extends Content {
		
	@Override
	public void setContent(SceneGraphNode node) {
		SceneGraphComponent root = getContentRoot();
		boolean fire = getContentNode() != node;
		if (getContentNode() != null) {
			SceneGraphUtility.removeChildNode(root, getContentNode());
		}
		setContent(node);
		if (getContentNode() != null) { 
			SceneGraphUtility.addChildNode(root, getContentNode());
		}
		if (fire) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
			cce.node = node;
			fireContentChanged(cce);
		}
	}

	
	public void uninstall(Scene scene, Controller c) {
		setContent(null);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Direct Content", "jReality Group");
	}

}
