package de.jreality.tutorial.app;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.awt.Color;
import java.io.IOException;

import de.jreality.geometry.SphereUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
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
		ap.setAttribute("transparencyEnabled", true);
		GlslProgram brickProg = null;		
		world.setGeometry(SphereUtility.sphericalPatch(0, 0, 90, 90, 30, 30, 1)); 
		MatrixBuilder.euclidean().rotateY(-Math.PI/2).assignTo(world);
		byte[] imageRGBAdata = new byte[256 * 4]; 
		for (int x = 0; x <256; x++) 
		{ 
		   imageRGBAdata[x*4] = (byte) (x>>1); 
		   imageRGBAdata[x*4+1] = (byte) x; 
		   imageRGBAdata[x*4+2] = (byte) x; 
		   imageRGBAdata[x*4+3] = (byte) x; 
		} 
		       
		ImageData id = new de.jreality.shader.ImageData(imageRGBAdata, 256, 1); 
		Texture2D tex = TextureUtility.createTexture(ap, POLYGON_SHADER, 0, id);
		// rotate this texture by 90 degrees
		tex.setTextureMatrix(MatrixBuilder.euclidean().scale(4).rotateZ(Math.PI/2).getMatrix());
		tex.setApplyMode(Texture2D.GL_DECAL);

		Texture2D gradTexture = (Texture2D) TextureUtility.createTexture(ap, POLYGON_SHADER, 1, id); 
		gradTexture.setTextureMatrix(MatrixBuilder.euclidean().scale(3).getMatrix());
		gradTexture.setApplyMode(Texture2D.GL_COMBINE);
		// sampler.frag:
//		uniform sampler2D  sampler;
//		uniform sampler2D sampler2;
//		void main(void)
//		{
//		    vec4 currentSample = texture2D(sampler,gl_TexCoord[0].st); 
//		    vec4 currentSample2 = texture2D(sampler2,gl_TexCoord[1].st); 
//		    gl_FragColor = currentSample*currentSample2 * gl_Color; 
//		}
		try {
			brickProg = new GlslProgram(ap, "polygonShader",   
					null,
					Input.getInput("de/jreality/jogl/shader/resources/sampler.frag")
			    );
		} catch (IOException e) {
			e.printStackTrace();
		}
		brickProg.setUniform("sampler",0);
		brickProg.setUniform("sampler2",1);		
		JRViewer.display(world);
//		CameraUtility.encompass(va.getCurrentViewer());
	}
}
