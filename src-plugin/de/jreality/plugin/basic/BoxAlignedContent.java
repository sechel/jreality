package de.jreality.plugin.basic;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;

public class BoxAlignedContent extends EmptyPickContent {

	double size = 1;
		
	SceneGraphComponent translateComponent=new SceneGraphComponent("translate");
	SceneGraphComponent scaleComponent=new SceneGraphComponent("scale");
	
	private SceneGraphComponent oldContent;
	
	public void setContent(SceneGraphNode content) {
		SceneGraphComponent cmp;
		if (content instanceof SceneGraphComponent) cmp = (SceneGraphComponent) content;
		else {
			cmp = new SceneGraphComponent("wrapper");
			SceneGraphUtility.addChildNode(cmp, content);
		}
		Rectangle3D bds = BoundingBoxUtility.calculateBoundingBox(cmp);
		
		double[] ext = bds.getExtent();
		double objectSize = Math.max(Math.max(ext[0], ext[1]), ext[2]);
		
		MatrixBuilder.euclidean().scale(size/objectSize).assignTo(scaleComponent);
		
		double[] c = bds.getCenter();
		
		MatrixBuilder.euclidean().translate(-c[0], -c[1], -c[2]).assignTo(translateComponent);
		
		if (oldContent != null) scaleComponent.removeChild(oldContent);
		oldContent = cmp;
		scaleComponent.addChild(cmp);
		
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		scaleComponent.setAppearance(new Appearance());
		translateComponent.addChild(scaleComponent);
		view.getEmptyPickPath().getLastComponent().addChild(translateComponent);
		SceneGraphPath newEPP = view.getEmptyPickPath().pushNew(translateComponent);
		newEPP.push(scaleComponent);
		view.setEmptyPickPath(newEPP);
	}
	
}
