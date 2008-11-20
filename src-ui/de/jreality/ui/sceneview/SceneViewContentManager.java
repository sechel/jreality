package de.jreality.ui.sceneview;

import de.jreality.scene.SceneGraphComponent;

public class SceneViewContentManager implements SceneContentManager {

	private SceneView sceneView;
	private SceneGraphComponent content;
	
	@Override
	public void setContent(SceneGraphComponent content) {
		if (this.content != null) {
			sceneView.getContentParent().removeChild(this.content);
		}
		if (content != null) {
			sceneView.getContentParent().addChild(content);
		}
		this.content = content;
	}
	
	public void install(SceneView sceneView) {
		this.sceneView = sceneView;
	}

}
