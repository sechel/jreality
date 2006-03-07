/*
 * Created on May 7, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLCylinderUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

	public static final int FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;
	public static final int FRONT = GL.GL_FRONT;
	public static final int BACK = GL.GL_BACK;
	
	boolean		smoothShading = true;		// interpolate shaded values between vertices
	Texture2D texture2Dnew;
  Texture2D lightMapNew;
   CubeMap reflectionMapNew;
	int frontBack = FRONT_AND_BACK;
	public VertexShader vertexShader = null;
	boolean useGLSL = false;
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
		}

		
	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap,name);
		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SMOOTH_SHADING), CommonAttributes.SMOOTH_SHADING_DEFAULT);	
		useGLSL = eap.getAttribute(ShaderUtility.nameSpace(name,"useGLSL"), false);	
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"texture2d"), eap))
	    	texture2Dnew = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"texture2d"), eap);
	    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name,"reflectionMap"), eap))
	    	reflectionMapNew = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name,"reflectionMap"));
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap))
	    	lightMapNew = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap);
      
	    if (useGLSL)		{
			if (glslShader == null)	{
				glslShader  = new GlslDefaultPolygonShader();
			}
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
	
	private void preRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
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
	      if (useGLSL) glslShader.reflectionTextureUnit = texUnit;
     } else
    	 if (useGLSL) glslShader.reflectionTextureUnit = -1;
    
    vertexShader.setFrontBack(frontBack);
	vertexShader.render(jrs);    	
    if (useGLSL)		{
    	glslShader.render(jrs);
    } //else {
   // }
}
	
	private void testTextureResident(JOGLRenderer jr, GL gl) {
		int[] res = new int[1];
		gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
		JOGLConfiguration.theLog.log(Level.FINEST,"Texture is resident"+res[0]);
		if (res[0] == 0) { jr.texResident = false; }
	}


	public boolean providesProxyGeometry() {		
		return false;
	}

	static Color[] cdbg = {Color.BLUE, Color.GREEN, Color.YELLOW,  Color.RED,Color.GRAY, Color.WHITE};
	int dList = -1, dListProxy = -1;
	public void render(JOGLRenderingState jrs)	{
		Geometry g = jrs.getCurrentGeometry();
		JOGLRenderer jr = jrs.getRenderer();
		boolean useDisplayLists = jrs.isUseDisplayLists();
		preRender(jrs);
		if (g != null)	{
			if (g instanceof Sphere || g instanceof Cylinder)	{	
				int i = 3;
				if (jr.debugGL)	{
					double lod = jr.openGLState.levelOfDetail;
					i = JOGLSphereHelper.getResolutionLevel(jr.context.getObjectToNDC(), lod);
				}
				int dlist;
				if (g instanceof Sphere) dlist = jr.openGLState.getSphereDisplayLists(i);
				else 			 dlist = jr.openGLState.getCylinderDisplayLists(i);
				if (jr.pickMode) jr.globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
				if (jr.debugGL) 
					jr.globalGL.glColor4fv(cdbg[i].getRGBComponents(null));
				jr.globalGL.glCallList(dlist);
				if (jr.pickMode) jr.globalGL.glPopName();
			}
			else if ( g instanceof IndexedFaceSet)	{
				if (providesProxyGeometry())	{
					if (!useDisplayLists || jr.pickMode || dListProxy == -1) {
						dListProxy  = proxyGeometryFor(jrs);
					}
					jr.globalGL.glCallList(dListProxy);
				}
				else 	{
					if (!jr.pickMode && useDisplayLists)	{
						if (dList == -1)	{
							dList = jr.globalGL.glGenLists(1);
							jr.globalGL.glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
							jr.helper.drawFaces((IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);
							jr.globalGL.glEndList();	
						}
						jr.globalGL.glCallList(dList);
					} else
						jr.helper.drawFaces((IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);			
				}	
			}
		}
	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
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
			glslShader.postRenderOld(jr);
	}


	public void flushCachedState(JOGLRenderer jr) {
		if (dList != -1) jr.globalGL.glDeleteLists(dList, 1);
		if (dListProxy != -1) jr.globalGL.glDeleteLists(dListProxy,1);
		dList = dListProxy = -1;
	}
}
