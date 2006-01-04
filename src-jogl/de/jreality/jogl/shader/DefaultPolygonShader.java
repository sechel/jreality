/*
 * Created on May 7, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.*;
import de.jreality.util.Input;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader implements PolygonShader {

	public static final int FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;
	public static final int FRONT = GL.GL_FRONT;
	public static final int BACK = GL.GL_BACK;
	
	boolean		smoothShading = true;		// interpolate shaded values between vertices
	Texture2D texture2Dnew;
  Texture2D lightMapNew;
   CubeMap reflectionMapNew;
	int frontBack = FRONT_AND_BACK;
	public VertexShader vertexShader = null;
	boolean useGLSL =true;
	static double[] idmat = Rn.identityMatrix(4);
	int texUnit = 0, refMapUnit = 0;
	Appearance ap = new Appearance();
	GlslDefaultPolygonShader glslShader;
	EffectiveAppearance myEap = null;
	/**
		 * 
		 */
		public DefaultPolygonShader() {
			super();
			vertexShader = new DefaultVertexShader();
			if (useGLSL)	{
				glslShader  = new GlslDefaultPolygonShader();
			}
		}

		
	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{

		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SMOOTH_SHADING), CommonAttributes.SMOOTH_SHADING_DEFAULT);	
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"texture2d"), eap))
	    	texture2Dnew = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"texture2d"), eap);
	    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name,"reflectionMap"), eap))
	    	reflectionMapNew = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name,"reflectionMap"));
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap))
	    	lightMapNew = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap);
      
	    if (useGLSL)		{
		    glslShader.setFromEffectiveAppearance(eap,name+".vertexShader");
	    } //else
			vertexShader = (VertexShader) ShaderLookup.getShaderAttr(eap, name, CommonAttributes.VERTEX_SHADER);

 	}

		/**
		 * @return
		 */
		public boolean isSmoothShading() {
			return smoothShading;
		}

	/**
	 * @return
	 */
	public Color getDiffuseColor() {
		return vertexShader.getDiffuseColor(); //diffuseColor;
	}

	public float[] getDiffuseColorAsFloat() {
		return vertexShader.getDiffuseColorAsFloat();
	}

	/**
	 * @return
	 */
//	public de.jreality.scene.Texture2D getTexture2D() {
//		return texture2D;
//	}
//
	public void setSmoothShading(boolean b) {
		smoothShading = b;
	}
	
	public int getFrontBack() {
		return frontBack;
	}
	public void setFrontBack(int frontBack) {
		this.frontBack = frontBack;
	}
	
	public void render(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		
//		if (smoothShading != jr.openGLState.smoothShading)	{
			if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
			else		gl.glShadeModel(GL.GL_FLAT);
			jr.openGLState.smoothShading = smoothShading;
//		}
		texUnit = GL.GL_TEXTURE0;

    if (texture2Dnew != null) {
	      gl.glActiveTexture(texUnit);
		  texUnit++;
	      Texture2DLoaderJOGL.render(theCanvas, texture2Dnew);
	      testTextureResident(jr, gl);
	      gl.glEnable(GL.GL_TEXTURE_2D);
    }

    if (lightMapNew != null) {
	      gl.glActiveTexture(texUnit);
	      texUnit++;
	      Texture2DLoaderJOGL.render(theCanvas, lightMapNew);
	      testTextureResident(jr, gl);
	      gl.glEnable(GL.GL_TEXTURE_2D);
    }
    
    if (reflectionMapNew != null)  {
	      gl.glActiveTexture(texUnit);
	      refMapUnit = texUnit;
	      texUnit++;
	      Texture2DLoaderJOGL.render(jr, reflectionMapNew);
	      //testTextureResident(jr, gl);
	      gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
     } 
    
    if (useGLSL)		{
    	glslShader.render(jr);
    } //else {
        vertexShader.setFrontBack(frontBack);
    	vertexShader.render(jr);    	
   // }
}
	
	private void testTextureResident(JOGLRenderer jr, GL gl) {
		int[] res = new int[1];
		gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
		JOGLConfiguration.theLog.log(Level.FINEST,"Texture is resident"+res[0]);
		if (res[0] == 0) { jr.texResident = false; }
	}


	public void postRender(JOGLRenderer jr) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		for (int i = GL.GL_TEXTURE0; i < texUnit; ++i) {
			gl.glActiveTexture(i);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
		if (reflectionMapNew != null) {
			gl.glActiveTexture(refMapUnit);
			gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
			gl.glDisable(GL.GL_TEXTURE_GEN_S);
			gl.glDisable(GL.GL_TEXTURE_GEN_T);
			gl.glDisable(GL.GL_TEXTURE_GEN_R);
		}
		if (useGLSL)
			glslShader.postRender(jr);
	}

	public boolean providesProxyGeometry() {		
		return false;
	}
	public int  proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig, boolean useDisplayLists) {
		return -1;
	}


//	public static void renderNew(de.jreality.shader.DefaultPolygonShader polygonShaderNew, JOGLRenderer globalHandle) {
//		
//	}

}
