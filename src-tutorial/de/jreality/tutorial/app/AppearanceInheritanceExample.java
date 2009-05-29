package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.io.IOException;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;

public class AppearanceInheritanceExample {

	public static void main(String[] args) throws IOException {
		  	IndexedFaceSet geom = new CatenoidHelicoid(40);
			SceneGraphComponent sgc = new SceneGraphComponent("AppearanceInheritance");
			sgc.setGeometry(geom);
			Appearance ap = new Appearance();
			sgc.setAppearance(ap);
			DefaultGeometryShader dgs = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(ap, true);
			dgs.setShowLines(false);
			dgs.setShowPoints(false);
			DefaultPolygonShader dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
			dps.setDiffuseColor(Color.white);
			ImageData id = null;
			double scale = 1;
			// get the image for the texture first
			if (args.length > 0) {
				id = ImageData.load(Input.getInput(args[0]));
			} else { // use a procedural texture
				SimpleTextureFactory stf = new SimpleTextureFactory();
				stf.setColor(0, new Color(0,0,0,0));	// gap color in weave pattern is totally transparent
				stf.setColor(1, new Color(255,0,100));
				stf.setColor(2, new Color(255,255,0));
				stf.update();
				id = stf.getImageData();
				scale = 10;
				dps.setDiffuseColor(Color.white);
			}
			Texture2D tex = TextureUtility.createTexture(sgc.getAppearance(), POLYGON_SHADER,id);
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
			SceneGraphComponent child = new SceneGraphComponent("AppearanceInheritance child");
			sgc.addChild(child);
			child.setGeometry(geom);
			MatrixBuilder.euclidean().translate(8,0,0).assignTo(child);
			child.setAppearance(new Appearance());
	    	child.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.DEFAULT);
			ViewerApp va = ViewerApp.display(sgc);
			CameraUtility.encompass(va.getCurrentViewer());
	  }

}
