package de.jreality.tutorial.tform;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

/**
 * This tutorial demonstrates how the same geometry can be reused in different {@link de.jreality.scene.SceneGraphComponent} instances,
 * each time being rendered according to the {@link de.jreality.scene.Transformation} and {@link de.jreality.scene.Appearance} contained
 * in that instance.
 * 
 * We use direct calls to {@link Appearance#setAttribute(String, Object)} instead of using shader interfaces.
 * @author gunn
 *
 */
public class Icosahedra {

	 public static void main(String[] args) throws IOException {
		Color[] faceColors = {new Color(100, 200, 100), new Color(100, 100, 200), new Color(100,200,200), new Color(200,100,100)};
	    IndexedFaceSet ico = Primitives.sharedIcosahedron;
	    SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	    // set up the colors and sizes for edge and point rendering
		world.getAppearance().setAttribute(CommonAttributes.TUBE_RADIUS, .03);
		world.getAppearance().setAttribute(CommonAttributes.POINT_RADIUS, .05);
		world.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+
				CommonAttributes.DIFFUSE_COLOR, Color.RED); 
		world.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+
				CommonAttributes.DIFFUSE_COLOR, Color.YELLOW); 
		// enable transparency and set it
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		world.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY, .5);
		// force edges and vertices to be drawn opaque
		world.getAppearance().setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);  // default 
	    for (int i = 0; i<2; ++i)	{
	    	for (int j = 0; j<2; ++j)	{
	    		for (int k = 0; k<2; ++k)	{
	    			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("sgc"+i+j+k);
	    			// set translation onto corner of a cube
	    			MatrixBuilder.euclidean().translate(-2+4*i, -2+4*j, -2+4*k).scale(1.5).assignTo(sgc);
	    			// set same geometry 
	    			sgc.setGeometry(ico);
	    			// set appearance individually
	    			sgc.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, faceColors[2*j+k]);
	    			sgc.getAppearance().setAttribute(CommonAttributes.FACE_DRAW, i == 0);
	    			sgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, j == 0);
	    			sgc.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, k == 0);
	    			world.addChild(sgc);
	    		}
	    	}
	    }
	    ViewerApp.display(world);
	}

}
