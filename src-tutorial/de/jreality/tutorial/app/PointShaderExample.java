package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SPRITE;

import java.awt.Color;
import java.io.IOException;

import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.RenderingHintsShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

public class PointShaderExample {
  
  
public static void main(String[] args) throws IOException {
	PointSetFactory psf = new PointSetFactory();
	int numPoints = 20;
	double[][] verts = new double[numPoints][], 
		vcolors = new double[numPoints][];
	double[] relrad = new double[numPoints];
	for (int i=0; i<numPoints; ++i)	{ 
		double angle = (Math.PI*2*i)/numPoints;
		verts[i] = new double[]{Math.cos(angle), Math.sin(angle), 0};
		vcolors[i] = new double[]{verts[i][0]*.5+.5, verts[i][1]*.5+.5,0.0};
		relrad[i] = verts[i][0]+1.5;
	}
	psf.setVertexCount(numPoints);
	psf.setVertexCoordinates(verts);
	psf.update();
	PointSet ps = psf.getPointSet();
	
	SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
	int numSamples = 6;
	for (int i = 0; i<numSamples; ++i)	{
		SceneGraphComponent child = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.addChild(child);
		if (i == 4 || i == 5)	{
			psf = new PointSetFactory();
			psf.setVertexCount(numPoints);
			psf.setVertexCoordinates(verts);
			psf.setVertexColors(vcolors);
			if (i == 5) psf.setVertexAttribute(Attribute.RELATIVE_RADII, 
					// aahhg! Don't look at this without some form of protective covering!
					StorageModel.DOUBLE_ARRAY.createReadOnly(relrad));
			psf.update();
			ps = psf.getPointSet();			
		}
		else if (i == 5)	{
			psf = new PointSetFactory();
			psf.setVertexCount(numPoints);
			psf.setVertexCoordinates(verts);
			psf.setVertexColors(vcolors);
			psf.update();
			ps = psf.getPointSet();			
		}
		child.setGeometry(ps);
		Appearance ap = child.getAppearance();
		DefaultGeometryShader dgs = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowPoints(true);
		RenderingHintsShader rhs = (RenderingHintsShader) ShaderUtility.createDefaultRenderingHintsShader(ap, true);
		DefaultPointShader dps = (DefaultPointShader) dgs.createPointShader("default");
		DefaultPolygonShader dpls = (DefaultPolygonShader) dps.createPolygonShader("default");
		MatrixBuilder.euclidean().translate(0,0,-i*.5).assignTo(child);
		switch(i)	{
			case 0:		// default rendering with sprites which look like spheres
			case 1:		// sprites again, but this shows an image of the string "SPR"
				dps.setSpheresDraw(false);
				dps.setAttenuatePointSize(false);
				dps.setPointSize(20.0);
				dps.setDiffuseColor(Color.yellow);
				if (i == 1)	{
					ImageData id = new ImageData(LabelUtility.createImageFromString("SPR", null, Color.white));
					Texture2D tex = TextureUtility.createTexture(ap, POINT_SHADER+"."+POINT_SPRITE,id);					
				}
				break;
			case 2:		// transparency activated, but not for spheres coming from appearances, like these
			case 3:		// transparency activated, and extends to the spheres coming from appearances. like these
			case 4:		// opaque spheres with vertex colors
			case 5:		// opaque spheres with vertex colors
				dps.setSpheresDraw(true);		// the default
				dpls.setTransparency(.75);
				rhs.setTransparencyEnabled(true);
				rhs.setOpaqueTubesAndSpheres(i != 3);
				dps.setSpheresDraw(true);
				dps.setPointRadius(.05);
				break;
		}
	}
    	
		ViewerApp.display(world);
  }
}
