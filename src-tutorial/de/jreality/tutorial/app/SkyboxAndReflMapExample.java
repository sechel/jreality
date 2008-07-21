package de.jreality.tutorial.app;

import java.io.IOException;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TextureUtility;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SceneGraphUtility;

public class SkyboxAndReflMapExample {

	public static void main(String[] args)	{
		SceneGraphComponent worldSGC = SceneGraphUtility.createFullSceneGraphComponent("SkyboxExample");
		worldSGC.setGeometry(new CatenoidHelicoid(40));
		Appearance ap = worldSGC.getAppearance();
		DefaultGeometryShader dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
		// basic class needed for both reflection maps and skyboxes is a CubeMap
		CubeMap rm = null;
		// we load this over the net since 
		try {
			rm = TextureUtility.createReflectionMap(ap,"polygonShader",
			  	"http://www3.math.tu-berlin.de/jreality/download/data/reflectionMap/grace_cross_",
			     new String[]{"rt","lf","up", "dn","bk","ft"},
			     ".jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}
		ViewerApp va = ViewerApp.display(worldSGC);
		ImageData[] sides = TextureUtility.getCubeMapImages(rm);
		// attach a skybox to the scene root
		TextureUtility.createSkyBox(va.getCurrentViewer().getSceneRoot().getAppearance(), sides);
	}
}
