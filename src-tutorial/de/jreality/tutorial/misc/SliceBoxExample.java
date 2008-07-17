package de.jreality.tutorial.misc;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.SliceBoxFactory;
import de.jreality.geometry.SphereUtility;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.PickUtility;
import de.jreality.util.SceneGraphUtility;


/**
 * A tutorial demonstrating the use of {@link SliceBoxFactory}
 * 
 * @author Charles Gunn
 *
 */
public class SliceBoxExample{

	public static void main(String[] args) throws IOException {
		SliceBoxExample lcpe = new SliceBoxExample();
		SceneGraphComponent root = lcpe.makeExample();
		ViewerApp va = ViewerApp.display(root);
	}
	  
	  SceneGraphComponent makeExample()	{
		  	SceneGraphComponent worldSGC = makeWorld();
		  	SliceBoxFactory sbf = new SliceBoxFactory(worldSGC);
		  	sbf.update();
			SceneGraphComponent foo = sbf.getSliceBoxSGC();
			foo.addTool(new ClickWheelCameraZoomTool());
			return foo;
	  }
	  
	  /**
	   * This is just a sphere -- for real applications, replace this sgc with your own.
	   * @return	the scene graph component to be sliced.
	   */
	  protected SceneGraphComponent makeWorld()	{	
			SceneGraphComponent world;
			world = SceneGraphUtility.createFullSceneGraphComponent("container");
			PickUtility.setPickable(world, false);
			world.addChild(SphereUtility.tessellatedCubeSphere(SphereUtility.SPHERE_SUPERFINE));
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"name","twoSide");
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
			world.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
			world.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+
					CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
			world.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
			world.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+
					CommonAttributes.POINT_RADIUS, 2*CommonAttributes.POINT_RADIUS_DEFAULT);
			return world;
	  }
	  
}
