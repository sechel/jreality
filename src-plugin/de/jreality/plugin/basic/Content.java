package de.jreality.plugin.basic;

import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.tool.Tool;

public interface Content {
	
	void setContent(SceneGraphNode content);
	boolean addContentTool(Tool tool);
	boolean removeContentTool(Tool tool);
	void contentChanged();

}
