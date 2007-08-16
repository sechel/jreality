/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import javax.media.opengl.GL;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.LoggingSystem;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

	public static final int FRONT_AND_BACK = GL.GL_FRONT_AND_BACK;
	public static final int FRONT = GL.GL_FRONT;
	public static final int BACK = GL.GL_BACK;
	
	boolean		smoothShading = true;		// interpolate shaded values between vertices
	Texture2D texture2D;
	JOGLTexture2D joglTexture2D;
	Texture2D lightMap;
	JOGLTexture2D joglLightMap;
	CubeMap reflectionMap;
	int frontBack = FRONT_AND_BACK;
	public VertexShader vertexShader = null;
	boolean useGLSL = false;
	static double[] idmat = Rn.identityMatrix(4);
	int texUnit = 0, refMapUnit = 0;
	Appearance ap = new Appearance();
	GlslDefaultPolygonShader glslShader;
	EffectiveAppearance myEap = null;
	boolean inheritGLSL = false;
	boolean fastAndDirty = false;
	boolean geometryHasTextureCoordinates = false;
	private transient boolean needsChecked = true;
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
		fastAndDirty = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.FAST_AND_DIRTY), fastAndDirty);	
		useGLSL = eap.getAttribute(ShaderUtility.nameSpace(name,"useGLSL"), false);	
	    joglLightMap = null;
	    reflectionMap = null;
	    joglTexture2D = null;
	    if (!fastAndDirty) {
			if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,CommonAttributes.TEXTURE_2D), eap)) {
				texture2D = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,CommonAttributes.TEXTURE_2D), eap);			
		    	//LoggingSystem.getLogger(this).fine("Got texture 2d for eap "+((Appearance) eap.getAppearanceHierarchy().get(0)).getName());
				joglTexture2D = new JOGLTexture2D(texture2D);
			}
		    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name,"reflectionMap"), eap)){
		    	reflectionMap = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name,"reflectionMap"));		    	
		    }
		    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap)) {
		    	lightMap = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"lightMap"), eap);		    	
		    	joglLightMap = new JOGLTexture2D(lightMap);
		    }
	    	
	    }
      
		inheritGLSL= eap.getAttribute(ShaderUtility.nameSpace(name,"inheritGLSL"), false);	
	    if (!inheritGLSL)		{
		    if (useGLSL)		{
				if (glslShader == null)	{
					String vertShader = (String) eap.getAttribute(ShaderUtility.nameSpace(name,"glslVertexShader"), "standard3dlabs.vert");	
					String fragmentShader = (String) eap.getAttribute(ShaderUtility.nameSpace(name,"glslFragmentShader"), "");	
					if (fragmentShader == "") fragmentShader = null;
					System.err.println("Setting glsl vertex shader to "+vertShader);
					glslShader  = new GlslDefaultPolygonShader(vertShader, fragmentShader);
				}
			glslShader.setFromEffectiveAppearance(eap,name+".vertexShader");
		    }
	    }  else useGLSL = false;
	    
		vertexShader = (VertexShader) ShaderLookup.getShaderAttr(eap, name, CommonAttributes.VERTEX_SHADER);
//		System.err.println("Found vertex shader "+vertexShader.getClass().getName());
		geometryHasTextureCoordinates = false;
		needsChecked = true;
 	}

	public boolean isSmoothShading() {
		return smoothShading;
	}

	public Color getDiffuseColor() {
		return vertexShader.getDiffuseColor(); //diffuseColor;
	}

	public float[] getDiffuseColorAsFloat() {
		return vertexShader.getDiffuseColorAsFloat();
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
	
	public void preRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.renderer;
		GL gl = jr.globalGL;
		if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
		else		gl.glShadeModel(GL.GL_FLAT);
		jrs.smoothShading = smoothShading;
		int texunitcoords = 0;
		texUnit = GL.GL_TEXTURE0; // jr.getRenderingState().texUnitCount + GL.GL_TEXTURE0; //
	    if (joglLightMap != null) {
		    gl.glActiveTexture(texUnit);
		    gl.glEnable(GL.GL_TEXTURE_2D);
			Texture2DLoaderJOGL.render(gl, joglLightMap);
			//testTextureResident(jr, gl);
		    texUnit++;
		    texunitcoords++;
	    }
	    if (joglTexture2D != null) {
		    Geometry curgeom = jr.renderingState.currentGeometry;
		    if (needsChecked)	// assume geometry stays constant between calls to setFromEffectiveAppearance() ...
		    	if ((curgeom instanceof IndexedFaceSet) &&
		    		((IndexedFaceSet) curgeom).getVertexAttributes(Attribute.TEXTURE_COORDINATES) != null) {
		    			geometryHasTextureCoordinates = true; 
		    			needsChecked = false;
		    	}
		    if (geometryHasTextureCoordinates) {
		    	gl.glActiveTexture(texUnit);
		      	gl.glEnable(GL.GL_TEXTURE_2D);
				Texture2DLoaderJOGL.render(gl, joglTexture2D);
				//testTextureResident(jr, gl);
			    texUnit++;
			    texunitcoords++;		    	
		    }
	    }

	    if (reflectionMap != null)  {
	      	gl.glActiveTexture(texUnit);
			gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
			refMapUnit = texUnit;
			if (useGLSL) glslShader.reflectionTextureUnit = texUnit;
			Texture2DLoaderJOGL.render(jr, reflectionMap);
			//testTextureResident(jr, gl);
			texUnit++;
		} else if (useGLSL)
			glslShader.reflectionTextureUnit = -1;    	
    
	jr.renderingState.texUnitCount = texunitcoords; 
    vertexShader.setFrontBack(frontBack);
	vertexShader.render(jrs);    	
    if (useGLSL)		{
    	//if (frontBack == FRONT_AND_BACK)	gl.glDisable(GL.GL_VERTEX_PROGRAM_TWO_SIDE_ARB);
    	//else gl.glEnable(GL.GL_VERTEX_PROGRAM_TWO_SIDE_ARB);
    	glslShader.render(jrs);
    } //else {
   // }
    jrs.currentAlpha = vertexShader.getDiffuseColorAsFloat()[3];
    jrs.smoothShading = smoothShading;
}
	
	private void testTextureResident(JOGLRenderer jr, GL gl) {
		int[] res = new int[1];
		gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res,0);
		JOGLConfiguration.theLog.log(Level.FINEST,"Texture is resident"+res[0]);
		if (res[0] == 0) { jr.setTextureResident(false); }
	}


	public boolean providesProxyGeometry() {		
		return false;
	}

	static Color[] cdbg = {Color.BLUE, Color.GREEN, Color.YELLOW,  Color.RED,Color.GRAY, Color.WHITE};
	public void render(JOGLRenderingState jrs)	{
		Geometry g = jrs.currentGeometry;
		JOGLRenderer jr = jrs.renderer;
		boolean useDisplayLists = jrs.useDisplayLists;
		preRender(jrs);
		if (g != null)	{
			if (g instanceof Sphere || g instanceof Cylinder)	{	
				int i = 3;
//				if (jr.debugGL)	{
//					double lod = jr.getRenderingState().levelOfDetail;
//					i = JOGLSphereHelper.getResolutionLevel(jr.context.getObjectToNDC(), lod);
//				}
				int dlist;
				if (g instanceof Sphere) {
					jr.renderingState.polygonCount += 24*(i*(i+1)+3);
					dlist = jr.renderingState.getSphereDisplayLists(i);
				}
				else 			{
					jr.renderingState.polygonCount += 4*Math.pow(2, i);
					dlist = jr.renderingState.getCylinderDisplayLists(i);
				}
				if (jr.isPickMode()) jr.globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
//				if (jr.debugGL) 
//					jr.getGL().glColor4fv(cdbg[i].getRGBComponents(null));
				jr.globalGL.glCallList(dlist);
				displayListsDirty = false;
				if (jr.isPickMode()) jr.globalGL.glPopName();
			}
			else if ( g instanceof IndexedFaceSet)	{
				jr.renderingState.polygonCount += ((IndexedFaceSet) g).getNumFaces();
				if (providesProxyGeometry())	{
					if (!useDisplayLists || jr.isPickMode() || dListProxy == -1) {
						dListProxy  = proxyGeometryFor(jrs);
						displayListsDirty = false;
					}
					jr.globalGL.glCallList(dListProxy);
				}
				else 	{
					if (!jr.isPickMode() && useDisplayLists)	{
						if (dList == -1)	{
							dList = jr.globalGL.glGenLists(1);
							LoggingSystem.getLogger(this).fine("PolygonShader: Allocating new dlist "+dList+" for gl "+jr.globalGL);
							jr.globalGL.glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
							JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);
							jr.globalGL.glEndList();	
							displayListsDirty = false;
						}
						jr.globalGL.glCallList(dList);
					} else
						JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g, smoothShading, vertexShader.getDiffuseColorAsFloat()[3]);			
				}	
			}
		}
	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.renderer;
		GL gl = jrs.renderer.globalGL;
		if (useGLSL)
			glslShader.postRender(jrs);
			for (int i = GL.GL_TEXTURE0; i < texUnit; ++i) {
				gl.glActiveTexture(i);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			if (reflectionMap != null) {
				gl.glActiveTexture(refMapUnit);
				gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
				gl.glDisable(GL.GL_TEXTURE_GEN_S);
				gl.glDisable(GL.GL_TEXTURE_GEN_T);
				gl.glDisable(GL.GL_TEXTURE_GEN_R);
			}
		jr.renderingState.texUnitCount=0;
		// TODO fix this to return to previous state -- maybe textures NOT active
	}

    public static void defaultPolygonRender(JOGLRenderingState jrs)	{
    	Geometry g = jrs.currentGeometry;
    	JOGLRenderer jr = jrs.renderer;
    	
		if (g instanceof Sphere || g instanceof Cylinder)	{	
			int i = 3;
//			if (jr.debugGL)	{
//				double lod = jr.getRenderingState().levelOfDetail;
//				i = JOGLSphereHelper.getResolutionLevel(jr.context.getObjectToNDC(), lod);
//			}
			int dlist;
			if (g instanceof Sphere) dlist = jr.renderingState.getSphereDisplayLists(i);
			else 			 dlist = jr.renderingState.getCylinderDisplayLists(i);
			if (jr.isPickMode()) jr.globalGL.glPushName(JOGLPickAction.GEOMETRY_BASE);
//			if (jr.debugGL) 
//				jr.getGL().glColor4fv(cdbg[i].getRGBComponents(null));
			jr.globalGL.glCallList(dlist);
			if (jr.isPickMode()) jr.globalGL.glPopName();
		}
		else if ( g instanceof IndexedFaceSet)	{
      JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g, jrs.smoothShading, jrs.diffuseColor[3]);			
		}

    }
	public void flushCachedState(JOGLRenderer jr) {
		LoggingSystem.getLogger(this).fine("PolygonShader: Flushing display lists "+dList+" : "+dListProxy);
		if (dList != -1) jr.globalGL.glDeleteLists(dList, 1);
		if (dListProxy != -1) jr.globalGL.glDeleteLists(dListProxy,1);
		dList = dListProxy = -1;
		displayListsDirty = true;
	}
}
