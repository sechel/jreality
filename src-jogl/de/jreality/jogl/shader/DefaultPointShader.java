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

import javax.media.opengl.GL;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPointShader  extends AbstractPrimitiveShader implements PointShader {
	double pointSize = 1.0;
	// on my mac, the only value for the following array that seems to "work" is {1,0,0}.  WHY?
	float[] pointAttenuation = {1.0f, .0f, 0.00000f};
	double	pointRadius = .1;		
	Color diffuseColor = java.awt.Color.RED;
	float[] diffuseColorAsFloat;
	float[] specularColorAsFloat = {0f,1f,1f,1f};		// for texturing point sprite to simulate sphere
	boolean sphereDraw = false, lighting = true;
	PolygonShader polygonShader = null;
	Appearance a=new Appearance();
	Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", a, true);
	Texture2D currentTex;
	double specularExponent = 60.0;
	
	/**
	 * 
	 */
	public DefaultPointShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		sphereDraw = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPHERES_DRAW), CommonAttributes.SPHERES_DRAW_DEFAULT);
		lightDirection = (double[]) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHT_DIRECTION),lightDirection);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		pointSize = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_SIZE), CommonAttributes.POINT_SIZE_DEFAULT);
		pointRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.POINT_DIFFUSE_COLOR_DEFAULT);	
		double t = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, t);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");

		if (!sphereDraw)	{
	      if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap))
	    	  currentTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap);
	      else {
	  			Rn.normalize(lightDirection, lightDirection);
	  			specularColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
	  			specularColorAsFloat = specularColor.getRGBComponents(null);
	  			specularExponent = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
	  			setupTexture();
	  			currentTex=tex;
	      }
	  }
	}


	byte[] sphereTex;
	double[] lightDirection = {1,-1,2};
	
	private void setupTexture() {
		int I = 0, II = 0;
		double[] reflected = new double[3];
		if (sphereTex != null) return;
		//System.out.println("specular color is "+specularColor.toString());
		//if (sphereTex == null) 
			sphereTex = new byte[textureSize * textureSize * 4];
		for (int i = 0; i<textureSize; ++i)	{
			for (int j = 0; j< textureSize; ++j)	{
				if (sphereVertices[I][0] != -1)	{	
					double diffuse = Rn.innerProduct(lightDirection, sphereVertices[I]);
					if (diffuse < 0) diffuse = 0;
					if (diffuse > 1.0) diffuse =1.0;
					double z = sphereVertices[I][2];
					reflected[0] = 2*sphereVertices[I][0]*z;
					reflected[1] = 2*sphereVertices[I][1]*z;
					reflected[2] = 2*z*z-1;
					double specular = Rn.innerProduct(lightDirection, reflected);
					if (specular < 0.0) specular = 0.0;
					if (specular > 1.0) specular = 1.0;
					specular = Math.pow(specular, specularExponent);
					for (int k = 0; k<3; ++k)	{
						double f = (diffuse * diffuseColorAsFloat[k] + specular * specularColorAsFloat[k]);
						if (f < 0) f = 0;
						if (f > 1) f = 1;
						sphereTex[II+k] =  (byte) (255 * f); 
					}
					sphereTex[II+3] = sphereVertices[I][2] < .1 ? (byte) (2550*sphereVertices[I][2]) : -128;
				}
				else	{
					sphereTex[II] =  sphereTex[II+1] = sphereTex[II+2] = sphereTex[II+3]  = 0;  
					}
				II += 4;
				I++;
				}
			}
			ImageData id = new ImageData(sphereTex, textureSize, textureSize) ;
			tex.setImage(id);
			tex.setApplyMode(Texture2D.GL_MODULATE);
	}

	/**
	 * @return
	 */
	public boolean isSphereDraw() {
		return sphereDraw;
	}

	/**
	 * @return
	 */
	public double getPointSize() {
		return pointSize;
	}

	/**
	 * @return
	 */
	public double getPointRadius() {
		return pointRadius;
	}

	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}
	/**
	 * @param globalHandle
	 * @param jpc
	 */
	static final int textureSize = 128;
	static double[][] sphereVertices = new double[textureSize * textureSize][3];
	private Color specularColor;
	static {
		double x,y,z;
		int I = 0;
		for (int i = 0; i<textureSize; ++i)	{
			y = 2*(i+.5)/textureSize - 1.0;
			for (int j = 0; j< textureSize; ++j)	{
				x = 2*(j+.5)/textureSize - 1.0;
				double dsq = x*x+y*y;
				if (dsq <= 1.0)	{	
					z = Math.sqrt(1.0-dsq);
					sphereVertices[I][0] = x; sphereVertices[I][1] = y; sphereVertices[I][2] = z;
					}
				else sphereVertices[I][0] = sphereVertices[I][1] = sphereVertices[I][2] = -1;
				I++;
			}
		}
	}
	private void preRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GL gl = jrs.getGL();
//		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat,0);
//			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
//		}
		
		if (!sphereDraw)	{
			lighting = false;
			gl.glPointSize((float) getPointSize());
			try {
				gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation,0);
			} catch (Exception e){
				//TODO: i dont know - got error on ati radeon 9800
			}
			gl.glEnable(GL.GL_POINT_SMOOTH);
			gl.glEnable(GL.GL_POINT_SPRITE_ARB);
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
			if (currentTex == tex && (jrs.getCurrentGeometry() instanceof PointSet) && 
					((PointSet)jrs.getCurrentGeometry()).getVertexAttributes(Attribute.COLORS) != null)
				tex.setApplyMode(Texture2D.GL_MODULATE);
			else tex.setApplyMode(Texture2D.GL_REPLACE);
			Texture2DLoaderJOGL.render(gl, currentTex);
			gl.glEnable(GL.GL_TEXTURE_2D);
		} else	{
			// really need to call the preRender() method on the polygonShader, but it doesn't exist.
			Geometry g = jrs.getCurrentGeometry();
			jrs.setCurrentGeometry(null);
			polygonShader.render(jrs);
			jrs.setCurrentGeometry(g);
		}
		
			jr.getRenderingState().lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
		
	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer(); 
		if (!sphereDraw)	{
			GL gl = jr.getGL();
			gl.glDisable(GL.GL_POINT_SPRITE_ARB);
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
			gl.glDisable(GL.GL_TEXTURE_2D);
		} else {
			polygonShader.postRender(jrs);
		}
	}

	public boolean providesProxyGeometry() {		
		return sphereDraw;
	}
	
	public int proxyGeometryFor(JOGLRenderingState jrs)	{
		Geometry original = jrs.getCurrentGeometry();
		JOGLRenderer jr = jrs.getRenderer();
		int sig = jrs.getCurrentSignature();
		boolean useDisplayLists = jrs.isUseDisplayLists();
		GL gl = 	jr.getGL();
		if (original instanceof PointSet)	{
			PointSet ps = (PointSet) original;
			DataList vertices = ps.getVertexAttributes(Attribute.COORDINATES);
			DataList vertexColors = ps.getVertexAttributes(Attribute.COLORS);
			DataList radii = ps.getVertexAttributes(Attribute.RADII);
			DoubleArray da = null;
			if (radii != null) da = radii.toDoubleArray();
			//JOGLConfiguration.theLog.log(Level.INFO,"VC is "+vertexColors);
			int colorLength = 0;
			if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
			int n = ps.getNumPoints();
			int resolution = 1;
			if (jr.getRenderingState().levelOfDetail == 0.0) resolution = 0;
			int dlist = JOGLSphereHelper.getSphereDLists(resolution, jr);
			int nextDL = -1;
			if (useDisplayLists)	{
				nextDL = gl.glGenLists(1);
				gl.glNewList(nextDL, GL.GL_COMPILE);				
			}
			double[] mat = Rn.identityMatrix(4);
			double[] scale = Rn.identityMatrix(4);
			scale[0] = scale[5] = scale[10] = pointRadius;
			int length = n;
			//JOGLConfiguration.theLog.log(Level.INFO,"Signature is "+sig);
			//sig = Pn.EUCLIDEAN;
			boolean pickMode = jr.isPickMode();
			if (pickMode) gl.glPushName(JOGLPickAction.GEOMETRY_POINT);
			for (int i = 0; i< length; ++i)	{
				double[] transVec = null;
				gl.glPushMatrix();
				if (radii != null)	{
					double radius = radii.toDoubleArray().getValueAt(i);
					scale[0] = scale[5] = scale[10] = radius;
				}
				transVec =  vertices.item(i).toDoubleArray().toDoubleArray(null);	
				P3.makeTranslationMatrix(mat, transVec,sig);
				Rn.times(mat, mat, scale);
				gl.glMultTransposeMatrixd(mat,0);
				if (vertexColors != null)	{
					da = vertexColors.item(i).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					} else if (colorLength == 4) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), 1.0*da.getValueAt(3));
					} 
				}
				if (pickMode) gl.glPushName(i);
				gl.glCallList(dlist);
				if (pickMode) gl.glPopName();
				gl.glPopMatrix();
			}
			if (pickMode) gl.glPopName();
			if (useDisplayLists) gl.glEndList();
			return nextDL;
		}
		return -1;
	}
	
	public Shader getPolygonShader() {
		return polygonShader;
	}

	int dList = -1, dListProxy =- 1;
	public void render(JOGLRenderingState jrs)	{
		Geometry g = jrs.getCurrentGeometry();
		JOGLRenderer jr = jrs.getRenderer();
		boolean useDisplayLists = jrs.isUseDisplayLists();
		if ( !(g instanceof PointSet))	{
			throw new IllegalArgumentException("Must be PointSet");
		}
		preRender(jrs);
		if (g != null)	{
			if (providesProxyGeometry())	{
				if (!useDisplayLists || jr.isPickMode() || dListProxy == -1) {
					dListProxy  = proxyGeometryFor(jrs);
				}
				jr.getGL().glCallList(dListProxy);
			}
			else {
				if (!useDisplayLists || jr.isPickMode()) {
					JOGLRendererHelper.drawVertices(jr, (PointSet) g,   jr.getRenderingState().diffuseColor[3]);
				} else {
					if (useDisplayLists && dList == -1)	{
						dList = jr.getGL().glGenLists(1);
						jr.getGL().glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
						JOGLRendererHelper.drawVertices(jr, (PointSet) g,  jr.getRenderingState().diffuseColor[3]);
						jr.getGL().glEndList();	
					}
					jr.getGL().glCallList(dList);
				} 
			}			
		}
	}

	public void flushCachedState(JOGLRenderer jr) {
		if (dList != -1) jr.getGL().glDeleteLists(dList, 1);
		//TODO !!!
    //if (dListProxy != -1) jr.getGL().glDeleteLists(dListProxy,1);
		dList = dListProxy = -1;
    //dList = -1;
	}
	



}
