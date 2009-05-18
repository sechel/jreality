package de.jreality.plugin.basic;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class CenteredAndScaledContent extends BasicContent {

	double size = 5;
	
	@Override
	public void setContent(SceneGraphNode content) {
		super.setContent(content);
		SceneGraphComponent cmp;
		if (content instanceof SceneGraphComponent) cmp = (SceneGraphComponent) content;
		else {
			cmp = new SceneGraphComponent("wrapper");
			SceneGraphUtility.addChildNode(cmp, content);
		}

		Rectangle3D bds = BoundingBoxUtility.calculateBoundingBox(cmp);
		
		double[] ext = bds.getExtent();
		double objectSize = Math.max(Math.max(ext[0], ext[1]), ext[2]);
		
		double[] c = bds.getCenter();

		MatrixBuilder.euclidean().scale(size/objectSize).translate(-c[0], -c[1], -c[2]).assignTo(contentCmp);
	}
	
	@Override
	public void contentChanged() {
		setContent(oldContent);
	}
}
