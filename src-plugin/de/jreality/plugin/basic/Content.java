package de.jreality.plugin.basic;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.tool.Tool;

public interface Content {

	void setContent(SceneGraphNode content);
	Appearance getContentAppearance();
	void addContentTool(Tool tool);
	boolean removeContentTool(Tool tool);
	void contentChanged();

}
