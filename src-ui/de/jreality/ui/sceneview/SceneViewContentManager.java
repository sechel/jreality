package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;

public class SceneViewContentManager extends ChangeEventSource implements ChangeListener, ContentManager {

	private SceneView sceneView;
	private SceneGraphComponent content;
	private double contentScale = 1;
	private SceneGraphComponent contentParent;
	
	@Override
	public void setContent(SceneGraphComponent content) {
		if (this.content != null || content == null) {
			contentParent.removeChild(this.content);
		}
		double scale = contentScale;
		if (content != null) {
			sceneView.getContentParent().addChild(content);
			Rectangle3D bounds =
				GeometryUtility.calculateChildrenBoundingBox(content);
			double[] e = bounds.getExtent();
			scale = Math.max(Math.max(e[0], e[1]), e[2]);
		} else {
			scale = 1;
		}
		this.content = content;
		if (scale != contentScale) {
			contentScale = scale;
			fireStateChange();
		}
	}
	
	public void install(SceneView sceneView) {
		this.sceneView = sceneView;
	}

	@Override
	public double getContentScale() {
		return contentScale;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (sceneView.getContentParent() != contentParent) {
			if (content != null) {
				contentParent.removeChild(content);
				sceneView.getContentParent().addChild(content);
			}
			contentParent = sceneView.getContentParent();
		}
	}
}
