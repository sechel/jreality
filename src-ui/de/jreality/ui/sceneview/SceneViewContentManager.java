package de.jreality.ui.sceneview;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;

public class SceneViewContentManager extends ChangeEventSource implements ChangeListener, ContentManager {

	private SceneView sceneView;
	private SceneGraphComponent content;
	private double contentScale = 1;
	private SceneGraphComponent alignmentComponent;
	private SceneGraphComponent contentParent;
	private double contentSize = 6;
	
	public SceneViewContentManager() {
		alignmentComponent = new SceneGraphComponent(" alignment");
	}
	
	@Override
	public void setContent(SceneGraphComponent content) {
		if (this.content != null || content == null) {
			alignmentComponent.removeChild(this.content);
		}
		double objectSize = 1;
		if (content != null) {
			alignmentComponent.addChild(content);
			Rectangle3D bounds =
				GeometryUtility.calculateChildrenBoundingBox(content);
			double[] e = bounds.getExtent();
			objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
		}
		double scale = contentSize/objectSize;
		MatrixBuilder.euclidean().scale(scale).assignTo(alignmentComponent);
		this.content = content;
		if (scale != contentScale) {
			contentScale = scale;
			System.out.println("firing scale = "+contentScale);
			fireStateChanged();
		}
	}
	
	public void install(SceneView sceneView) {
		this.sceneView = sceneView;
		sceneView.getContentParent().addChild(alignmentComponent);
		sceneView.addChangeListener(this);
	}

	@Override
	public double getContentScale() {
		return contentScale;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (sceneView.getContentParent() != contentParent) {
			if (content != null) {
				contentParent.removeChild(alignmentComponent);
				sceneView.getContentParent().addChild(alignmentComponent);
			}
			contentParent = sceneView.getContentParent();
		}
	}

	@Override
	public void unInstall() {
		sceneView.removeChangeListener(this);
	}

	@Override
	public void setContentSize(double size) {
		contentSize = size;
	}
}