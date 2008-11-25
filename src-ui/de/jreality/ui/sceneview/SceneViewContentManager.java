package de.jreality.ui.sceneview;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Rectangle3D;

public class SceneViewContentManager extends ChangeEventSource implements ContentManager {

	private SceneGraphComponent content;
	private double contentScale = 1;
	private SceneGraphComponent alignmentComponent;
	private SceneGraphComponent contentParent;


	private double contentSize = 20;
	
	public SceneViewContentManager(
			SceneGraphComponent alignmentComponent,
			SceneGraphComponent contentParent
	) {
		this.alignmentComponent = alignmentComponent;
		this.contentParent = contentParent;
	}
	
	@Override
	public void setPath(
			SceneGraphComponent alignmentComponent,
			SceneGraphComponent contentParent) {
		this.alignmentComponent = alignmentComponent;
		this.contentParent = contentParent;
	}
	
	@Override
	public void setContent(SceneGraphComponent content) {
		if (this.content != null || content == null) {
			contentParent.removeChild(this.content);
		}
		this.content = content;
		if (content != null) {
			contentParent.addChild(content);
		}
		alignContent();
	}
	
	@Override
	public void alignContent() {
		double objectSize = 1;
		if (content != null) {
			Rectangle3D bounds =
				GeometryUtility.calculateChildrenBoundingBox(content);
			double[] e = bounds.getExtent();
			objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
		}
		double scale = contentSize/objectSize;
		MatrixBuilder.euclidean().scale(scale).assignTo(alignmentComponent);
		MatrixBuilder.euclidean().assignTo(contentParent);
		if (scale != contentScale) {
			contentScale = scale;
			fireStateChanged();
		}
	}

	@Override
	public double getContentScale() {
		return contentScale;
	}

	@Override
	public void setContentSize(double size) {
		contentSize = size;
	}
}