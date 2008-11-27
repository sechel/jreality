package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;

public class Content extends ChangeEventSource implements ChangeListener {

	private ContentManager contentManager;
	private SceneGraphComponent alignmentComponent;
	private SceneGraphComponent contentParent;
	private Appearance contentAppearance;
	private Appearance scaledAppearance;
	private Transformation toolTransformation;
	
	public Appearance getContentAppearance() {
		return contentAppearance;
	}

	public Content() {

		alignmentComponent = new SceneGraphComponent("alignment");
		contentAppearance = new Appearance("content appearance");
		alignmentComponent.setAppearance(contentAppearance);
		alignmentComponent.setTransformation(new Transformation("align transformation"));
		
		contentParent = new SceneGraphComponent("content parent");
		toolTransformation = new Transformation("tool transformation");
		contentParent.setTransformation(toolTransformation);
		scaledAppearance = new Appearance("scaled appearance");
		contentParent.setAppearance(scaledAppearance);
		alignmentComponent.addChild(contentParent);
		
		contentManager = new SceneViewContentManager(alignmentComponent, contentParent);
		contentManager.addChangeListener(this);
	}
	
	public SceneGraphComponent getAlignmentComponent() {
		return alignmentComponent;
	}
	
	public Appearance getScaledAppearance() {
		return scaledAppearance;
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

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == contentManager) {
			fireStateChanged();
		}
	}
}
