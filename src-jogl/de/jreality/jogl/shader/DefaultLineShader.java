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
/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import javax.media.opengl.GL;

import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.LoggingSystem;

/**
 * @author Charles Gunn
 *
 */
public class DefaultLineShader extends AbstractPrimitiveShader implements LineShader  {
	protected de.jreality.shader.DefaultLineShader templateShader = null;
	FrameFieldType 	tubeStyle = FrameFieldType.PARALLEL;
	double	tubeRadius = 0.05,
		 	lineWidth = 1.0;
	boolean smoothLineShading = false, lighting, vertexColors;
	boolean smoothShading = true;		// this applies to the tubes, not the edges
	int	lineFactor = 1;
	int 	lineStipplePattern = 0x1c47; 
	 
	boolean lineStipple = false;
	boolean tubeDraw = false;
	boolean opaqueTubes = false;
	int faceCount = 0;		
	Color diffuseColor = java.awt.Color.BLACK;
	private PolygonShader polygonShader;
	boolean changedTransp, changedLighting;
	float[] diffuseColorAsFloat;
	 
	public DefaultLineShader(de.jreality.shader.DefaultLineShader orig)	{
		templateShader = orig;
	}
	
	public DefaultLineShader()	{
	}
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		tubeDraw = eap.getAttribute(ShaderUtility.nameSpace(name, CommonAttributes.TUBES_DRAW), CommonAttributes.TUBES_DRAW_DEFAULT);
		tubeRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
		opaqueTubes = eap.getAttribute(ShaderUtility.nameSpace(name, CommonAttributes.OPAQUE_TUBES_AND_SPHERES), CommonAttributes.OPAQUE_TUBES_AND_SPHERES_DEFAULT);
		tubeStyle = (FrameFieldType) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_STYLE),CommonAttributes.TUBE_STYLE_DEFAULT);
		smoothLineShading = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SMOOTH_LINE_SHADING), CommonAttributes.SMOOTH_LINE_SHADING_DEFAULT);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), false);
		vertexColors = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.VERTEX_COLORS_ENABLED), false);
		lineStipple = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
		lineFactor = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.LINE_DIFFUSE_COLOR_DEFAULT);
		double transp = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transp, JOGLRenderingState.useOldTransparency);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		if (templateShader != null)  {
			polygonShader = DefaultGeometryShader.createFrom(templateShader.getPolygonShader());
			polygonShader.setFromEffectiveAppearance(eap, name+".polygonShader");
		}
		else polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");
		smoothShading = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SMOOTH_LINE_SHADING), CommonAttributes.SMOOTH_LINE_SHADING_DEFAULT);
		//LoggingSystem.getLogger(this).info("Line shader is smooth: "+smoothShading);
	}

	public void preRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.renderer;
		GL gl = jrs.renderer.globalGL;
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, diffuseColorAsFloat,0);
		gl.glColor4fv( diffuseColorAsFloat,0);
		System.arraycopy(diffuseColorAsFloat, 0, jr.renderingState.diffuseColor, 0, 4);

		if (smoothShading) gl.glShadeModel(GL.GL_SMOOTH);
		else		gl.glShadeModel(GL.GL_FLAT);
		jrs.smoothShading = smoothShading;
		gl.glLineWidth((float) lineWidth);
		jrs.lineWidth = lineWidth;
		if (lineStipple) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineStipple(lineFactor, (short) lineStipplePattern);
		} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);

		if (tubeDraw)	{
			Geometry g = jrs.currentGeometry;
			jrs.currentGeometry = null;
			polygonShader.render(jrs);
			jrs.currentGeometry = g;
			lighting=true;
		} else lighting = smoothShading;
//		jr.renderingState.lighting = lighting;
		changedLighting = false;
		if (lighting != jrs.lighting)	{
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
			changedLighting = true;
		}

		// this little bit of code forces tubes to be opaque: could add
		// transparency-enable flag to the line shader to allow this to be controlled
		changedTransp = false;
//		if (tubeDraw) {
			// check to see if we're different from most recent rendering hints
			if (opaqueTubes == jrs.transparencyEnabled)	{	// change of state!
				if (opaqueTubes)	{
					gl.glDepthMask(true);
					gl.glDisable(GL.GL_BLEND);	
				} else {
					gl.glEnable (GL.GL_BLEND);
					gl.glDepthMask(jrs.zbufferEnabled);
//					System.err.println("enabled = "+jrs.zbufferEnabled);
					gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);								
				}
				changedTransp = true;					
			}
//		}

		if (!tubeDraw) gl.glDepthRange(0.0d, jrs.depthFudgeFactor);
	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.renderer;
		GL gl = jr.globalGL;
		if (!tubeDraw) {
			jr.globalGL.glDepthRange(0.0d, 1d);
		} else 
			polygonShader.postRender(jrs);
		if (changedTransp) {
			if (jrs.transparencyEnabled) {
				gl.glEnable (GL.GL_BLEND);
				gl.glDepthMask(jrs.zbufferEnabled);
				gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);			
			} else {
				gl.glDepthMask(true);
				gl.glDisable(GL.GL_BLEND);						
			}
		}
		if (changedLighting)	{
			if (jrs.lighting)  gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);			
		}


	}

	public boolean providesProxyGeometry() {		
		return tubeDraw;
	}
	
	public int proxyGeometryFor(JOGLRenderingState jrs)	{
		final Geometry original = jrs.currentGeometry;
		final JOGLRenderer jr = jrs.renderer;
		final int sig = jrs.currentSignature;
		final boolean useDisplayLists = jrs.useDisplayLists;
//		if ( !(original instanceof IndexedLineSet)) return -1;
		if (tubeDraw && original instanceof IndexedLineSet)	{
	        final int[] dlist = new int[1];
	        Scene.executeReader(original, new Runnable() {
	        	public void run() {
	    			dlist[0] = createTubesOnEdgesAsDL((IndexedLineSet) original, tubeRadius, 1.0, jr, sig, false, useDisplayLists);
				    //JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes with radius "+tubeRadius);
	        	}
	        });
			return dlist[0];
		}
		return -1;
	}
	
	int[] tubeDL = null;
	boolean testQMS = true;
	public int createTubesOnEdgesAsDL(IndexedLineSet ils, double rad,  double alpha, JOGLRenderer jr, int sig, boolean pickMode, boolean useDisplayLists)	{
		GL gl = jr.globalGL;
		double[] p1 = new double[4],
			p2 = new double[4];
		p1[3] = p2[3] = 1.0;
		double[][] oneCurve = null;
		double[][] crossSection = TubeUtility.octagonalCrossSection;
//		if (jr.renderingState.levelOfDetail == 0.0) crossSection = TubeUtility.diamondCrossSection;
		DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
		DataList radiidl = ils.getEdgeAttributes(Attribute.RELATIVE_RADII);
		DoubleArray radii = null;
		if (radiidl != null) radii = radiidl.toDoubleArray();
		if (ils.getNumPoints() <= 1) return -1;
//		JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes for "+ils.getName());
		if (tubeDL == null)	{
			tubeDL = new int[3];
		}
		if (tubeDL[sig+1] == 0)	{
			tubeDL[sig+1] = gl.glGenLists(1);
			//LoggingSystem.getLogger(this).fine("LineShader: Allocating new dlist "+tubeDL[sig+1]+" for gl "+jr.globalGL);
			gl.glNewList(tubeDL[sig+1], GL.GL_COMPILE);
			JOGLRendererHelper.drawFaces(jr, TubeUtility.urTube[sig+1], smoothShading , alpha );
			gl.glEndList();	
		}
		faceCount = 0;
		int tubeFaces = TubeUtility.urTube[sig+1].getNumFaces();
		int nextDL = -1;
		if (useDisplayLists) {
			nextDL = gl.glGenLists(1);
			//LoggingSystem.getLogger(this).fine("LineShader: Allocating new dlist "+nextDL+" for gl "+jr.globalGL);
			gl.glNewList(nextDL, GL.GL_COMPILE);
		}
		int  k, l;
		DoubleArray da;
		double[] mat = new double[16];
		DataList edgec =  ils.getEdgeAttributes(Attribute.COLORS);
		int n = ils.getNumEdges();
		for (int i = 0; i<n; ++i)	{
			IntArray ia = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
			int m = ia.size();
			double effectiveRadius = rad;
			if (radii != null)	{
				effectiveRadius = rad*radii.getValueAt(i);
			}
			if (pickMode)	gl.glPushName(i);
			DoubleArray edgecolor = null;
			int clength = 3;
			if (edgec != null) {
				edgecolor = edgec.item(i).toDoubleArray();
				clength = edgecolor.size();
				if (clength == 3) gl.glColor3d(edgecolor.getValueAt(0), edgecolor.getValueAt(1), edgecolor.getValueAt(2));
				else gl.glColor4d(edgecolor.getValueAt(0), edgecolor.getValueAt(1), edgecolor.getValueAt(2), edgecolor.getValueAt(3));
			}
		    //System.err.println(m+" edges");
			if ((m == 2  && !vertexColors)  || pickMode)	{		// probably an IndexedFaceSet 
				faceCount += (m-1)*tubeFaces;

				for (int j = 0; j<m-1; ++j)	{
					k = ia.getValueAt(j);
					da = vertices.item(k).toDoubleArray();
					l = da.size();
					for (int xx=0; xx<l; ++xx) p1[xx] = da.getValueAt(xx);
					k = ia.getValueAt(j+1);
					da = vertices.item(k).toDoubleArray();
					l = da.size();
					for (int xx=0; xx<l; ++xx) p2[xx] = da.getValueAt(xx);
					SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, effectiveRadius, crossSection, sig);
					if (cc.getGeometry() != null)	{
						if (pickMode) gl.glPushName(j);
						gl.glPushMatrix();
						gl.glMultTransposeMatrixd(cc.getTransformation().getMatrix(mat),0);
						gl.glCallList(tubeDL[sig+1]);						
						gl.glPopMatrix();
						if (pickMode) 	gl.glPopName();					
					}
					
				}
			}
			else {		// the assumption is that this is a genuine IndexedLineSet (not subclass with faces)
				oneCurve = IndexedLineSetUtility.extractCurve(oneCurve, ils, i);
				double[][] clrs = null;
				if (vertexColors) clrs = IndexedLineSetUtility.extractCurveColors(clrs, ils, i);
				PolygonalTubeFactory ptf = new PolygonalTubeFactory(oneCurve);
				ptf.setClosed(false);
				if (clrs != null) {
					ptf.setVertexColors(clrs);
					ptf.setVertexColorsEnabled(true);
				}
				ptf.setCrossSection(crossSection);
				ptf.setFrameFieldType(tubeStyle);
				ptf.setSignature(sig);
				ptf.setRadius(effectiveRadius);
				ptf.update();
				IndexedFaceSet tube = ptf.getTube();
				if (tube != null)	{
					JOGLRendererHelper.drawFaces(jr, tube, smoothShading, alpha);			
					faceCount += tube.getNumFaces();
				}
			}
			if (pickMode) 	gl.glPopName();					
		}
		
		if (useDisplayLists) gl.glEndList();
		// problems with display list validity when switching to full-screen mode: kill them
		return nextDL;
	}

	public void render(JOGLRenderingState jrs)	{
		Geometry g = jrs.currentGeometry;
		JOGLRenderer jr = jrs.renderer;
		boolean useDisplayLists = jrs.useDisplayLists;
		if ( !(g instanceof IndexedLineSet))	{
			throw new IllegalArgumentException("Must be IndexedLineSet");
		}
		preRender(jrs);
		if (g != null)	{
			if (providesProxyGeometry())	{
				//System.err.println("count is: "+jr.getRenderingState().polygonCount);
				if (!useDisplayLists  || (useDisplayLists && dListProxy  == -1)) {
					dListProxy  = proxyGeometryFor(jrs);						
//					System.err.println("rendering tubes w/ dlist");
					displayListsDirty = false;
				}
				jr.globalGL.glCallList(dListProxy);
				jr.renderingState.polygonCount += faceCount;
			}
			else 	{
				if (!useDisplayLists ) {
//					System.err.println("rendering lines w/o dlist");
					JOGLRendererHelper.drawLines(jr, (IndexedLineSet) g,  smoothLineShading, jr.renderingState.diffuseColor[3]);
				} else {
					if (useDisplayLists && dList== -1)	{
						dList = jr.globalGL.glGenLists(1);
						//LoggingSystem.getLogger(this).fine("LineShader: Allocating new dlist "+dList+" for gl "+jr.globalGL);
						jr.globalGL.glNewList(dList, GL.GL_COMPILE); //_AND_EXECUTE);
						JOGLRendererHelper.drawLines(jr, (IndexedLineSet) g,  smoothLineShading, jr.renderingState.diffuseColor[3]);
//						System.err.println("rendering lines w/ dlist");
						jr.globalGL.glEndList();									
						displayListsDirty = false;
					}
					jr.globalGL.glCallList(dList);
				} 
			}
		}
	}

	public void flushCachedState(JOGLRenderer jr) {
		LoggingSystem.getLogger(this).fine("LineShader: Flushing display lists "+dList+" : "+dListProxy);
		if (dList != -1) jr.globalGL.glDeleteLists(dList, 1);
		if (dListProxy != -1) jr.globalGL.glDeleteLists(dListProxy,1);
		dList = dListProxy = -1;
		displayListsDirty = true;
		if (tubeDL != null) {
			//LoggingSystem.getLogger(this).fine("LineShader: Flushing display lists "+tubeDL[0]+" : "+tubeDL[1]+" : "+tubeDL[2]);
					for (int i = 0; i<3; ++i)
				if (tubeDL[i] != 0)	{
					jr.globalGL.glDeleteLists(tubeDL[i], 1);
					tubeDL[i] = 0;
				}
				tubeDL = null;
		}
	}
	

}
