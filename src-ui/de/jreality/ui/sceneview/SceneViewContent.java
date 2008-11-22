package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphComponent;

public class SceneViewContent extends ChangeEventSource implements ChangeListener, ContentManager {

	private ContentManager contentManager;
	private SceneView sceneView;

	public SceneViewContent() {
		contentManager = new SceneViewContentManager();
		contentManager.addChangeListener(this);
	}
	
	@Override
	public void install(SceneView sceneView) {
		this.sceneView = sceneView;
		contentManager.install(sceneView);
	}
	
	@Override
	public void unInstall() {
		sceneView = null;
		contentManager.unInstall();
	}
	
	@Override
	public void setContent(SceneGraphComponent content) {
		contentManager.setContent(content);
	}
	
	@Override
	public double getContentScale() {
		return contentManager.getContentScale();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		fireStateChanged();
	}
	
	public void setContentManager(ContentManager contentManager) {
		if (contentManager == null) {
			throw new IllegalArgumentException("content manager cannot be null");
		}
		this.contentManager.unInstall();
		this.contentManager.removeChangeListener(this);
		this.contentManager = contentManager;
		if (sceneView != null) {
			contentManager.install(sceneView);
		}
		contentManager.addChangeListener(this);
	}

	@Override
	public void setContentSize(double size) {
		contentManager.setContentSize(size);
	}
}
