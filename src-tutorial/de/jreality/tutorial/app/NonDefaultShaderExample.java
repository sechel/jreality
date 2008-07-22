package de.jreality.tutorial.app;

import java.awt.Color;

import de.jreality.geometry.SphereUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

public class NonDefaultShaderExample {

	public static void main(String[] args)	{
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(SphereUtility.sphericalPatch(0, 0.0, 180.0, 90.0, 40, 40, 1.0));
		Appearance ap = world.getAppearance();
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
   			ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		// setting the nondefault shaders via shader interfaces is currently broken in jogl backend(gunn, 21.07.08)
//		TwoSidePolygonShader tsps = (TwoSidePolygonShader) dgs.createPolygonShader("twoSide");
//		DefaultPolygonShader dps = (DefaultPolygonShader) tsps.createFront("default");
//		DefaultPolygonShader dps2 = (DefaultPolygonShader) tsps.createBack("default");
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"name", "twoSide");
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+".front."+CommonAttributes.DIFFUSE_COLOR, new Color(0,204,204));
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+".back."+CommonAttributes.DIFFUSE_COLOR, new Color(204,204,0));
//		dps.setDiffuseColor(new Color(0,204,204));
//		dps2.setDiffuseColor(new Color(204,204,0));
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(true);
		dls.setTubeRadius(.005);
		dls.setDiffuseColor(Color.red);

		ViewerApp.display(world);
	}

}
