/*
 * Created on May 7, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.ElementBinding;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.*;
import de.jreality.shader.ShaderFactory;
import de.jreality.shader.Texture2D;
import de.jreality.shader.ReflectionMap;
import de.jreality.util.*;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;
import de.jreality.util.Rn;
import de.jreality.util.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader implements PolygonShader {

	public static final int FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;
	public static final int FRONT = GL.GL_FRONT;
	public static final int BACK = GL.GL_BACK;
	
	boolean		smoothShading = true; 		// interpolate shaded values between vertices
  de.jreality.scene.Texture2D texture2D;
  de.jreality.scene.Texture2D lightMap;
  Texture2D texture2Dnew;
  Texture2D lightMapNew;
  de.jreality.scene.ReflectionMap reflectionMap;
  ReflectionMap reflectionMapNew;
	int frontBack = FRONT_AND_BACK;
	public VertexShader vertexShader = null;
	AbstractJOGLShader glShader = null;
	static double[] idmat = Rn.identityMatrix(4);
	int texUnit = 0, refMapUnit = 0;
	/**
		 * 
		 */
		public DefaultPolygonShader() {
			super();
			vertexShader = new DefaultVertexShader();
		}

		
	public static DefaultPolygonShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		DefaultPolygonShader dgs = new DefaultPolygonShader();
		dgs.setFromEffectiveAppearance(eap, name);
		return dgs;
	}
	
	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		vertexShader = ShaderLookup.getVertexShaderAttr(eap, name, "vertexShader");

		smoothShading = eap.getAttribute(NameSpace.name(name,CommonAttributes.SMOOTH_SHADING), CommonAttributes.SMOOTH_SHADING_DEFAULT);	
//		Object foo = eap.getAttribute(NameSpace.name(name,"texture2d"), null, Texture2D.class);
		Object foo = eap.getAttribute(NameSpace.name(name,"texture2d"), null, de.jreality.scene.Texture2D.class);
		if (foo instanceof de.jreality.scene.Texture2D)	texture2D = (de.jreality.scene.Texture2D) foo;
    foo = eap.getAttribute(NameSpace.name(name,"reflectionMap"), null, de.jreality.scene.ReflectionMap.class);
    if (foo instanceof de.jreality.scene.ReflectionMap)	reflectionMap = (de.jreality.scene.ReflectionMap) foo;
	  foo = eap.getAttribute(NameSpace.name(name,"lightMap"), null, de.jreality.scene.Texture2D.class);
    if (foo instanceof de.jreality.scene.Texture2D) lightMap = (de.jreality.scene.Texture2D) foo;

    if (AttributeEntityFactory.hasAttributeEntity(Texture2D.class, NameSpace.name(name,"texture2d"), eap))
      texture2Dnew = (Texture2D) AttributeEntityFactory.createAttributeEntity(Texture2D.class, NameSpace.name(name,"texture2d"), eap);
    if (AttributeEntityFactory.hasAttributeEntity(ReflectionMap.class, NameSpace.name(name,"reflectionMap"), eap))
      reflectionMapNew = ShaderFactory.readReflectionMap(eap, NameSpace.name(name,"reflectionMap"));
    if (AttributeEntityFactory.hasAttributeEntity(Texture2D.class, NameSpace.name(name,"lightMap"), eap))
      lightMapNew = (Texture2D) AttributeEntityFactory.createAttributeEntity(Texture2D.class, NameSpace.name(name,"lightMap"), eap);
      
		//TODO this is a hack. 
		if (eap.getAttribute(NameSpace.name(name,"useGLShader"), false) == true)	{
			Object obj =  eap.getAttribute(NameSpace.name(name,"GLShader"), null, AbstractJOGLShader.class);
			if (obj instanceof AbstractJOGLShader) {
				glShader = (AbstractJOGLShader) obj;
			}
		} else glShader = null;
	
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
	public de.jreality.scene.Texture2D getTexture2D() {
		return texture2D;
	}

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
		
		if (smoothShading != jr.openGLState.smoothShading)	{
			if (isSmoothShading()) gl.glShadeModel(GL.GL_SMOOTH);
			else		gl.glShadeModel(GL.GL_FLAT);
			jr.openGLState.smoothShading = smoothShading;
		}
		texUnit = GL.GL_TEXTURE0;

    if (texture2Dnew != null) {
      gl.glActiveTexture(texUnit);
	  texUnit++;
      Texture2DLoaderJOGL.render(theCanvas, texture2Dnew);
      testTextureResident(jr, gl);
      gl.glEnable(GL.GL_TEXTURE_2D);
    }

	if (texture2D != null) {
      gl.glActiveTexture(texUnit);
      texUnit++;
      Texture2DLoaderJOGL.render(theCanvas, texture2D);
      testTextureResident(jr, gl);
      gl.glEnable(GL.GL_TEXTURE_2D);
    } //else
    if (lightMapNew != null) {
      gl.glActiveTexture(texUnit);
      texUnit++;
      Texture2DLoaderJOGL.render(theCanvas, lightMapNew);
      testTextureResident(jr, gl);
      gl.glEnable(GL.GL_TEXTURE_2D);
    }
    if (lightMap != null) {
      gl.glActiveTexture(texUnit);
      texUnit++;
     Texture2DLoaderJOGL.render(theCanvas, lightMap);
     testTextureResident(jr, gl);
      gl.glEnable(GL.GL_TEXTURE_2D);
    } //else
    if (reflectionMap != null)  {
      gl.glActiveTexture(texUnit);
      refMapUnit = texUnit;
      texUnit++;
      Texture2DLoaderJOGL.render(jr, reflectionMap);
      //testTextureResident(jr, gl);
     gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
    } 
    if (reflectionMapNew != null)  {
      gl.glActiveTexture(texUnit);
      refMapUnit = texUnit;
      texUnit++;
      Texture2DLoaderJOGL.render(jr, reflectionMapNew);
      //testTextureResident(jr, gl);
      gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
     } 
    vertexShader.setFrontBack(frontBack);
	vertexShader.render(jr);
	if (glShader != null) {
		glShader.render(jr);
	}
}
	
	private void testTextureResident(JOGLRenderer jr, GL gl) {
		int[] res = new int[1];
		gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
		JOGLConfiguration.theLog.log(Level.FINE,"Texture is resident"+res[0]);
		if (res[0] == 0) { jr.texResident = false; }
	}


	public void postRender(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		for (int i = GL.GL_TEXTURE0; i< texUnit; ++i)	{
			gl.glActiveTexture(i);
			gl.glDisable(GL.GL_TEXTURE_2D);			
		}
    if (reflectionMap != null)  {
      gl.glActiveTexture(refMapUnit);
      gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
      gl.glDisable(GL.GL_TEXTURE_GEN_S);
      gl.glDisable(GL.GL_TEXTURE_GEN_T);
      gl.glDisable(GL.GL_TEXTURE_GEN_R);      
    }
    if (reflectionMapNew != null)  {
      gl.glActiveTexture(refMapUnit);
      gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
      gl.glDisable(GL.GL_TEXTURE_GEN_S);
      gl.glDisable(GL.GL_TEXTURE_GEN_T);
      gl.glDisable(GL.GL_TEXTURE_GEN_R);      
    }
//		if (glShader != null) glShader.deactivate(theCanvas);
	}

	public boolean providesProxyGeometry() {		
		return false;
	}
	public int  proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig) {
		return -1;
	}

}
