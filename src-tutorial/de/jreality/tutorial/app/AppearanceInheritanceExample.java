package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.io.IOException;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
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
import de.jreality.tutorial.viewer.SelectionExample;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * This shows how to use the constant value {@link Appearance#DEFAULT} to hide any attributes
 * assigned to an Appearance attribute in the nodes <i>above</i> a given scene graph component in
 * a jReality scene graph.
 * @see SelectionExample for another example of the inheritance mechanism.
 * @author gunn
 *
 */
public class AppearanceInheritanceExample {

	public static void main(String[] args) throws IOException {
		  	IndexedFaceSet geom = new CatenoidHelicoid(40);
			SceneGraphComponent sgc = SceneGraphUtility.createFullSceneGraphComponent("AppearanceInheritance");
			sgc.setGeometry(geom);
			DefaultGeometryShader dgs = (DefaultGeometryShader) ShaderUtility.createDefaultGeometryShader(sgc.getAppearance(), true);
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
			// force this child NOT to inherit the texture from its parent
	    	child.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.DEFAULT);
			JRViewer.display(sgc);
	  }

}
