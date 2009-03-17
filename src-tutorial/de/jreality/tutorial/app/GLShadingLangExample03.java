package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.tutorial.util.SimpleTextureFactory;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;

/**
 * A simple example of using an OpenGL shading language shader in a jReality scene graph.
 * @author Charles Gunn
 *
 */
public class GLShadingLangExample03 {

	public static void main(String[] args)	{
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		Appearance ap = world.getAppearance();
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
   			ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
//		dgs.createPolygonShader("glsl");
		dgs.createPolygonShader("default");
		DefaultPolygonShader dps = (DefaultPolygonShader) dgs.getPolygonShader();
		dps.setDiffuseColor(Color.white);
//		ap.setAttribute("useVertexArrays", false);
		ap.setAttribute("useGLSL", true);
		GlslProgram brickProg = null;		
		world.addChild(SphereUtility.tessellatedCubeSphere(3)); 
		SimpleTextureFactory stf = new SimpleTextureFactory();
		stf.setColor(0, new Color(0,0,0,0));	// gap color in weave pattern is totally transparent
		stf.setColor(1, new Color(255,0,100));
		stf.update();
		ImageData id = stf.getImageData();
		Texture2D tex = TextureUtility.createTexture(ap, POLYGON_SHADER,id);
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(5).getMatrix());
		try {
			brickProg = new GlslProgram(ap, "polygonShader",   
					null,
					Input.getInput("de/jreality/jogl/shader/resources/sampler.frag")
			    );
		} catch (IOException e) {
			e.printStackTrace();
		}
		brickProg.setUniform("sampler", Texture2D.GL_TEXTURE0);
		ViewerApp va = ViewerApp.display(world);
		CameraUtility.encompass(va.getCurrentViewer());
	}
}
