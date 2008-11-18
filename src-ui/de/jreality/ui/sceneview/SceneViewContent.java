package de.jreality.ui.sceneview;

import de.jreality.scene.SceneGraphComponent;

public class SceneViewContent {

	private SceneView view;

	public void addContent(SceneGraphComponent s) {
		view.getEmptyPickPath().getLastComponent().addChild(s);
	}

	public void removeContent(SceneGraphComponent s) {
		view.getEmptyPickPath().getLastComponent().removeChild(s);
	}

	public void install(SceneView view) {
		this.view = view;
	}
}
