package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.proxy.scene.SceneGraphPath;

public class Content extends ChangeEventSource implements ChangeListener {

	private ContentManager contentManager;
	private SceneView sceneView;
	private SceneGraphComponent sceneRoot;
	private SceneGraphComponent contentComponent;
	private SceneGraphComponent alignmentComponent;
	private SceneGraphComponent contentParent;
	private Appearance contentAppearance;
	private Appearance scaledAppearance;
	private Transformation toolTransformation;
	
	public Appearance getContentAppearance() {
		return contentAppearance;
	}

	public Content(SceneView sceneView) {
		this.sceneView = sceneView;
		sceneRoot = sceneView.getSceneRoot();
		
		contentComponent = new SceneGraphComponent("content");
		contentAppearance = new Appearance("content appearance");
		contentComponent.setAppearance(contentAppearance);
		sceneRoot.addChild(contentComponent);
		
		alignmentComponent = new SceneGraphComponent("alignment");
		alignmentComponent.setTransformation(new Transformation("align transformation"));
		contentComponent.addChild(alignmentComponent);
		
		contentParent = new SceneGraphComponent("content parent");
		toolTransformation = new Transformation("tool transformation");
		contentParent.setTransformation(toolTransformation);
		scaledAppearance = new Appearance("scaled appearance");
		contentParent.setAppearance(scaledAppearance);
		alignmentComponent.addChild(contentParent);
		
		SceneGraphPath path = new SceneGraphPath();
		path.push(sceneView.getSceneRoot());
		path.push(contentComponent);
		path.push(alignmentComponent);
		path.push(contentParent);
		sceneView.setEmptyPickPath(path);
		
		contentManager = new SceneViewContentManager(alignmentComponent, contentParent);
		contentManager.addChangeListener(this);
	}
	
	public Appearance getScaledAppearance() {
		return scaledAppearance;
	}

	public void unInstall() {
		sceneRoot.removeChild(alignmentComponent);
	}
	
	public SceneGraphComponent getContentParent() {
		return contentParent;
	}
	
	public void setContent(SceneGraphComponent content) {
		contentManager.setContent(content);
	}
	
	public void alignContent() {
		contentManager.alignContent();
	}
	
	public double getContentScale() {
		return contentManager.getContentScale();
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == contentManager) {
			fireStateChanged();
		}
		if (e.getSource() == sceneView) {
			if (sceneView.getSceneRoot() != sceneRoot) {
				sceneRoot.removeChild(alignmentComponent);
				sceneRoot = sceneView.getSceneRoot();
				sceneRoot.addChild(alignmentComponent);
			}
		}
	}
	
	public void setContentManager(ContentManager contentManager) {
		if (contentManager == null) {
			throw new IllegalArgumentException("content manager cannot be null");
		}
		this.contentManager.setPath(null, null);
		this.contentManager.removeChangeListener(this);
		this.contentManager = contentManager;
		contentManager.setPath(alignmentComponent, contentParent);
		contentManager.addChangeListener(this);
	}

	public void setContentSize(double size) {
		contentManager.setContentSize(size);
	}
}
