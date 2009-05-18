package de.jreality.plugin.basic;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;

public interface Content {

	void setContent(SceneGraphNode content);
	SceneGraphComponent getContentRoot();
	Appearance getContentAppearance();
	
}
