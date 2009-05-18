package de.jreality.plugin.basic;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.varylab.jrworkspace.plugin.Plugin;

public abstract class AbstractContent extends Plugin implements Content {

	protected abstract SceneGraphComponent getToolCmp();
	
	public void addContentTool(Tool tool) {
		SceneGraphComponent cmp = getToolCmp();
		if (!cmp.getTools().contains(tool)) cmp.addTool(tool);
	}
	
	public boolean removeContentTool(Tool tool) {
		return getToolCmp().removeTool(tool);
	}
	
}
