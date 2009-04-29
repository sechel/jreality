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

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.REFLECTION_MAP;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING_DEFAULT;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D_1;

import java.awt.Color;
import java.io.IOException;

import javax.media.opengl.GL;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.math.Pn;
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
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.Input;
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
	Texture2D texture2D,  texture2D_1, texture2D_2;
	JOGLTexture2D joglTexture2D, joglTexture2D_1, joglTexture2D_2;
	CubeMap reflectionMap;
	JOGLCubeMap joglCubeMap;
	int frontBack = FRONT_AND_BACK;
	public VertexShader vertexShader = new DefaultVertexShader();
	boolean useGLSL = false;
	int texUnit = 0, refMapUnit = 0;
//	GlslPolygonShader glslShader = new GlslPolygonShader();
	GlslProgram glslProgram;
	transient boolean geometryHasTextureCoordinates = false, hasTextures = false;
	transient boolean firstTime = true,
		noneuclideanInitialized = false;
	
	transient de.jreality.shader.DefaultPolygonShader templateShader;
	// try loading the OpenGL shader for the non-euclidean cases
	static GlslProgram noneuclideanShader = null;
	static String shaderLocation = "de/jreality/jogl/shader/resources/noneuclidean.vert";
	JOGLRenderingState theJRS;
	
	public DefaultPolygonShader()	{
		
	}
	
	public DefaultPolygonShader(de.jreality.shader.DefaultPolygonShader ps) {
		templateShader = ps;
	}

	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap,name);
		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,SMOOTH_SHADING), SMOOTH_SHADING_DEFAULT);	
		useGLSL = eap.getAttribute(ShaderUtility.nameSpace(name,"useGLSL"), false);	
	    joglTexture2D = joglTexture2D_1 = joglTexture2D_2 = null;
	    joglCubeMap = null;
	    hasTextures = false;
		if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D), eap)) {
			texture2D = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,CommonAttributes.TEXTURE_2D), eap);			
			joglTexture2D = new JOGLTexture2D(texture2D);
			hasTextures = true;
		}
	    if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class, ShaderUtility.nameSpace(name, REFLECTION_MAP), eap)){
	    	reflectionMap = TextureUtility.readReflectionMap(eap, ShaderUtility.nameSpace(name, REFLECTION_MAP));		    	
	    	joglCubeMap = new JOGLCubeMap(reflectionMap);
	    	hasTextures = true;
	    }
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_1), eap)) {
	    	texture2D_1 = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_1), eap);		    	
	    	joglTexture2D_1 = new JOGLTexture2D(texture2D_1);
	    	hasTextures = true;
	    }
      
	    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,CommonAttributes.TEXTURE_2D_2), eap)) {
	    	texture2D_2 = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,TEXTURE_2D_1), eap);		    	
	    	joglTexture2D_2 = new JOGLTexture2D(texture2D_2);
	    	hasTextures = true;
	    }
      
	    if (useGLSL)		{
			if (GlslProgram.hasGlslProgram(eap, name)) {
				// dummy to write glsl values like "lightingEnabled"
				Appearance app = new Appearance();
				glslProgram = new GlslProgram(app, eap, name);
			} else {
				if (noneuclideanShader == null) {
					try {
						Appearance ap = new Appearance();
						noneuclideanShader = new GlslProgram(ap, POLYGON_SHADER, Input.getInput(shaderLocation), null);
					} catch (IOException e) {
						e.printStackTrace();
					}		
				}
				glslProgram = noneuclideanShader;
				noneuclideanInitialized = false;
			}
	    }
		vertexShader.setFromEffectiveAppearance(eap, name);
		geometryHasTextureCoordinates = false;
		firstTime = true;
 	}

	public Color getDiffuseColor() {
		return vertexShader.getDiffuseColor(); 
	}

	public float[] getDiffuseColorAsFloat() {
		return vertexShader.getDiffuseColorAsFloat();
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
//    	hasTextures = false;
		if (hasTextures) {
			gl.glPushAttrib(GL.GL_TEXTURE_BIT);
			texUnit = GL.GL_TEXTURE0; 
		    Geometry curgeom = jr.renderingState.currentGeometry;
		    if (firstTime)	// assume geometry stays constant between calls to setFromEffectiveAppearance() ...
		    	if (curgeom != null && (curgeom instanceof IndexedFaceSet) &&
		    		((IndexedFaceSet) curgeom).getVertexAttributes(Attribute.TEXTURE_COORDINATES) != null) {
		    			geometryHasTextureCoordinates = true; 
		    	}
		    if (geometryHasTextureCoordinates) {

			    if (joglTexture2D != null) {
			    	gl.glActiveTexture(GL.GL_TEXTURE0);
			      	gl.glEnable(GL.GL_TEXTURE_2D);
					Texture2DLoaderJOGL.render(gl, joglTexture2D);
				    texUnit++;
				    texunitcoords++;		
			    }
			    if (joglTexture2D_1 != null) {
				    gl.glActiveTexture(GL.GL_TEXTURE0+1);
				    gl.glEnable(GL.GL_TEXTURE_2D);
					Texture2DLoaderJOGL.render(gl, joglTexture2D_1);
				    texUnit++;
				    texunitcoords++;
			    }
			    if (joglTexture2D_2 != null) {
			    	gl.glActiveTexture(GL.GL_TEXTURE0+2);
			      	gl.glEnable(GL.GL_TEXTURE_2D);
					Texture2DLoaderJOGL.render(gl, joglTexture2D_2);
				    texUnit++;
				    texunitcoords++;		
			    }
		    }
		}

	    if (joglCubeMap != null)  {
	      	gl.glActiveTexture(texUnit);
			gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
			refMapUnit = texUnit;
			Texture2DLoaderJOGL.render(jr, joglCubeMap);
			texUnit++;
		} 	
    
		jr.renderingState.texUnitCount = texunitcoords; 
	    vertexShader.setFrontBack(frontBack);
		vertexShader.render(jrs); 
	    jrs.currentAlpha = vertexShader.getDiffuseColorAsFloat()[3];
	    if (useGLSL && glslProgram != null)		{
	    	if ( glslProgram == noneuclideanShader)	{
	    		// the only reason we're doing it here is because only now do we know what jrs is
	    		System.err.println("writing glsl shader");
	    		// HACK this is a shoddy attempt to pass over parts of OpenGL state to a hypothetical GLSL shader
	    		// should be done by the specific shader instead, since only it knows which uniform variables
	    		// it has
	    		glslProgram.setUniform("lightingEnabled", jrs.lighting);
	    		glslProgram.setUniform("transparencyEnabled", jrs.transparencyEnabled);
	    		glslProgram.setUniform("transparency", (float) (1.0f - jrs.currentAlpha));
	    		glslProgram.setUniform("numLights", jrs.numLights);
	    		glslProgram.setUniform("fogEnabled", jrs.fogEnabled);
	    		glslProgram.setUniform("hyperbolic", jrs.currentMetric == Pn.HYPERBOLIC);
	    		glslProgram.setUniform("useNormals4", jrs.normals4d);
	    		noneuclideanInitialized = true;
	    	}
			
			GlslLoader.render(glslProgram, jr);
	    }
	    firstTime = false;
}
	
	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.renderer;
		GL gl = jrs.renderer.globalGL;
		if (useGLSL)
			GlslLoader.postRender(glslProgram, gl);
//		for (int i = GL.GL_TEXTURE0; i <  GL.GL_TEXTURE0+3; ++i) {
		if (hasTextures)	{
		    if (joglTexture2D != null) {
				gl.glActiveTexture(GL.GL_TEXTURE0);
				gl.glDisable(GL.GL_TEXTURE_2D);
		    }
		    if (joglTexture2D_1 != null) {
				gl.glActiveTexture(GL.GL_TEXTURE0+1);
				gl.glDisable(GL.GL_TEXTURE_2D);
		    }
		    if (joglTexture2D_2 != null) {
				gl.glActiveTexture(GL.GL_TEXTURE0+2);
				gl.glDisable(GL.GL_TEXTURE_2D);
		    }			
		}
//		}
		if (joglCubeMap != null) {
			gl.glActiveTexture(refMapUnit);
			gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
			gl.glDisable(GL.GL_TEXTURE_GEN_S);
			gl.glDisable(GL.GL_TEXTURE_GEN_T);
			gl.glDisable(GL.GL_TEXTURE_GEN_R);
		}
		jr.renderingState.texUnitCount=0;
		// TODO fix this to return to previous state -- maybe textures NOT active
		if (hasTextures) gl.glPopAttrib();
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
				int dlist;
				if (g instanceof Sphere) {
					jr.renderingState.polygonCount += 24*(i*(i+1)+3);
					dlist = jr.renderingState.getSphereDisplayLists(i);
				}
				else 			{
					jr.renderingState.polygonCount += 4*Math.pow(2, i);
					dlist = jr.renderingState.getCylinderDisplayLists(i);
				}
				jr.globalGL.glCallList(dlist);
				displayListsDirty = false;
			}
			else if ( g instanceof IndexedFaceSet)	{
				jr.renderingState.polygonCount += ((IndexedFaceSet) g).getNumFaces();
				if (providesProxyGeometry())	{
					if (!useDisplayLists  || dListProxy == -1) {
						dListProxy  = proxyGeometryFor(jrs);
						displayListsDirty = false;
					}
					jr.globalGL.glCallList(dListProxy);
				}
				else 	{
					if (useDisplayLists)	{
						if (dList == -1)	{
							dList = jr.globalGL.glGenLists(1);
//							LoggingSystem.getLogger(this).fine(" PolygonShader: is "+this+" Allocating new dlist "+dList+" for gl "+jr.globalGL);
							jr.globalGL.glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
							JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g);
							jr.globalGL.glEndList();	
							displayListsDirty = false;
						}
						jr.globalGL.glCallList(dList);
					} else
						JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g);			
				}	
			}
		}
	}

    public static void defaultPolygonRender(JOGLRenderingState jrs)	{
    	Geometry g = jrs.currentGeometry;
    	JOGLRenderer jr = jrs.renderer;
    	
		if (g instanceof Sphere || g instanceof Cylinder)	{	
			int i = 3;
			int dlist;
			if (g instanceof Sphere) dlist = jr.renderingState.getSphereDisplayLists(i);
			else 			 dlist = jr.renderingState.getCylinderDisplayLists(i);
			jr.globalGL.glCallList(dlist);
		}
		else if ( g instanceof IndexedFaceSet)	{
      JOGLRendererHelper.drawFaces(jr, (IndexedFaceSet) g);			
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
