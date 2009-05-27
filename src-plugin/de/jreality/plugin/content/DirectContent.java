package de.jreality.plugin.content;

import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.tool.Tool;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class DirectContent extends Plugin implements Content {
		
	protected SceneGraphComponent contentCmp;
	
	protected SceneGraphNode oldContent;

	public void setContent(SceneGraphNode content) {
		if (oldContent != null) SceneGraphUtility.removeChildNode(contentCmp, oldContent);
		oldContent = content;
		if (content != null) SceneGraphUtility.addChildNode(contentCmp, content);
	}

	@Override
	public void install(Controller c) throws Exception {
		contentCmp = c.getPlugin(Scene.class).getContentComponent();
		if (contentCmp == null) System.out.println("DirectContent.install() contentCmp == null!!!");
	}
	
	public void uninstall(Scene scene, Controller c) {
		setContent(null);
	}

	public boolean addContentTool(Tool tool) {
		if (!contentCmp.getTools().contains(tool)) contentCmp.addTool(tool);
		return true;
	}
	
	public boolean removeContentTool(Tool tool) {
		return contentCmp.removeTool(tool);
	}

	public void contentChanged() {
		setContent(oldContent);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Direct Content", "jReality Group");
	}

}
