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


package de.jreality.jogl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.NumericShaper;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import javax.media.opengl.*;
import javax.swing.SwingConstants;

import com.sun.opengl.util.*;

import com.sun.opengl.util.*;
import de.jreality.backends.label.LabelUtility;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.HeightFieldFactory;
import de.jreality.geometry.Primitives;
import de.jreality.jogl.pick.Graphics3D;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.SpotLight;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;

/**
 * @author gunn
 * 
 */
public class JOGLRendererHelper {

	public final static int PER_PART = 1;
	public final static int PER_FACE = 2;
	public final static int PER_VERTEX = 4;
	public final static int PER_EDGE = 8;
	static float val = 1f;
	static float[][] unitsquare = { { val, val }, { -val, val }, { -val, -val },
			{ val, -val } };

	private JOGLRendererHelper() {}
	static Appearance pseudoAp = new Appearance();
	static void handleBackground(JOGLRenderer jr, int width, int height, Appearance topAp) {
    GL gl = jr.globalGL;
    JOGLRenderingState openGLState = jr.renderingState;
		Object bgo = null;
    float[] backgroundColor;
		if (topAp == null) topAp = pseudoAp;
//			return;
		for (int i = 0; i < 6; ++i) {
			gl.glDisable(i + GL.GL_CLIP_PLANE0);
		}
		if (topAp != null)
			bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_COLOR);
		if (bgo != null && bgo instanceof java.awt.Color)
			backgroundColor = ((java.awt.Color) bgo).getComponents(null);
		else
			backgroundColor = CommonAttributes.BACKGROUND_COLOR_DEFAULT.getRGBComponents(null);
		gl.glClearColor(backgroundColor[0], backgroundColor[1],backgroundColor[2], 0.0f); // bg[3] ); //white
		// Here is where we clear the screen and set the color mask
		// It's a bit complicated by the various color masking required by
		// color-channel stereo (see JOGLRenderer#display() ).
		//System.err.println("clearbufferbits = "+jr.openGLState.clearBufferBits);
		//System.err.println("colormask = "+jr.openGLState.colorMask);
		// first set the color mask for the clear
		LoggingSystem.getLogger(JOGLRendererHelper.class).finest("JOGLRRH cbb = "+ openGLState.clearBufferBits);
		if ((openGLState.clearBufferBits & GL.GL_COLOR_BUFFER_BIT) != 0) gl.glColorMask(true, true, true, true);
		//if (openGLState.clearBufferBits != 0) 
				gl.glClear (openGLState.clearBufferBits);
		// now set the color mask for pixel writing
		int cm = openGLState.colorMask;
		gl.glColorMask((cm&1) !=0, (cm&2) != 0, (cm&4) != 0, (cm&8) != 0);

		Object obj = topAp.getAttribute(CommonAttributes.SKY_BOX);
		// only draw background colors or texture if the skybox isn't there
		if (obj == Appearance.INHERITED)	{
			boolean hasTexture = false, hasColors = false;
			double textureAR = 1.0;
			bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_TEXTURE2D);
			Texture2D tex = null;
			if (bgo != null && bgo instanceof List) {
				tex = (Texture2D) ((List)bgo).get(0);
				textureAR = tex.getImage().getWidth()
						/ ((double) tex.getImage().getHeight());
				hasTexture = true;
			}
			double ar = width / ((double) height) / textureAR;
			double xl = 0, xr = 1, yb = 0, yt = 1;
			if (ar > 1.0) {
				xl = 0.0;
				xr = 1.0;
				yb = .5 * (1 - 1 / ar);
				yt = 1.0 - yb;
			} else {
				yb = 0.0;
				yt = 1.0;
				xl = .5 * (1 - ar);
				xr = 1.0 - xl;
			}
			if (jr.offscreenMode)	{
				double xmin = ((double)jr.whichTile[0])/jr.numTiles;
				double xmax = ((double)jr.whichTile[0]+1)/jr.numTiles;
				double ymin = ((double)jr.whichTile[1])/jr.numTiles;
				double ymax = ((double)jr.whichTile[1]+1)/jr.numTiles;
				double nxl, nxr, nyb, nyt;
				nxr = xr + xmin*(xl-xr);
				nxl = xr + xmax*(xl-xr);
				nyt = yt + ymin*(yb-yt);
				nyb = yt + ymax*(yb-yt);
				xl = nxl; xr = nxr; yb = nyb; yt = nyt;
			}
			double[][] texcoords = { { xl, yb }, { xr, yb }, { xr, yt }, { xl, yt } };
			if (!hasTexture)	{
				bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_COLORS);
				if (bgo != null && bgo instanceof Color[]) {
					hasColors = true;
				}				
			}
			if (hasTexture || hasColors) {
				// bgo = (Object) corners;
				float[][] cornersf = new float[4][];
				if (hasTexture)	{
					gl.glPushAttrib(GL.GL_TEXTURE_BIT);
					gl.glActiveTexture(GL.GL_TEXTURE0);
					gl.glEnable(GL.GL_TEXTURE_2D);
					Texture2DLoaderJOGL.render(gl, tex);
				}
//				gl.glPushAttrib(GL.GL_ENABLE_BIT);
				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_LIGHTING);
				gl.glShadeModel(GL.GL_SMOOTH);
				gl.glBegin(GL.GL_POLYGON);
				// gl.glScalef(.5f, .5f, 1.0f);
				for (int q = 0; q < 4; ++q) {
					if (hasTexture) {
						gl.glColor3f(1f, 1f, 1f);
						gl.glTexCoord2dv(texcoords[q], 0);
					} else {
						cornersf[q] = ((Color[]) bgo)[q].getComponents(null);
						gl.glColor3fv(cornersf[q],0);
					}
					gl.glVertex2fv(unitsquare[q],0);
				}
				gl.glEnd();
				// TODO push/pop this correctly (now may overwrite previous values)
//				gl.glPopAttrib();
				gl.glEnable(GL.GL_DEPTH_TEST);
				gl.glEnable(GL.GL_LIGHTING);
				if  (hasTexture) {
					gl.glDisable(GL.GL_TEXTURE_2D);
					gl.glMatrixMode(GL.GL_PROJECTION);
					gl.glPopAttrib();
				}
			}			
		}
		bgo = topAp.getAttribute(CommonAttributes.FOG_ENABLED);
		boolean doFog = CommonAttributes.FOG_ENABLED_DEFAULT;
		if (bgo instanceof Boolean)
			doFog = ((Boolean) bgo).booleanValue();
		if (doFog) {
			gl.glEnable(GL.GL_FOG);
			bgo = topAp.getAttribute(CommonAttributes.FOG_COLOR);
			float[] fogColor = backgroundColor;
			if (bgo != null && bgo instanceof Color) {
				fogColor = ((Color) bgo).getRGBComponents(null);
			}
			gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
			gl.glFogfv(GL.GL_FOG_COLOR, fogColor,0);
			bgo = topAp.getAttribute(CommonAttributes.FOG_DENSITY);
			float density = (float) CommonAttributes.FOG_DENSITY_DEFAULT;
			if (bgo != null && bgo instanceof Double) {
				density = (float) ((Double) bgo).doubleValue();
			}
			gl.glFogf(GL.GL_FOG_DENSITY, density);
		} else
			gl.glDisable(GL.GL_FOG);
	}

	// TODO don't do this every render, attach an Appearance listener and cache the value
	// between changes
	public static void handleSkyBox(JOGLRenderer jr, Appearance topAp, Camera cam) {
		if (topAp == null) return;
	    GL gl = jr.globalGL;
		if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,
					CommonAttributes.SKY_BOX, topAp)) {
			CubeMap cm = (CubeMap) AttributeEntityUtility.createAttributeEntity(CubeMap.class,
					CommonAttributes.SKY_BOX, topAp, true);
			double[] w2c = jr.getContext().getWorldToCamera();
			JOGLSkyBox.render(gl, w2c, cm, cam);
		}
	}

	private static ByteBuffer vBuffer, vcBuffer, vnBuffer, fcBuffer, fnBuffer, tcBuffer;

	private static DataList vLast = null, vcLast = null, vnLast = null;

	public static void drawVertices(JOGLRenderer jr, PointSet sg, double alpha) {
    GL gl = jr.globalGL;
    boolean pickMode=jr.isPickMode();
    JOGLRenderingState openGLState = jr.renderingState;
		if (sg.getNumPoints() == 0)
			return;
		// gl.glPointSize((float)
		// currentGeometryShader.pointShader.getPointSize());
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList piDL = sg.getVertexAttributes(Attribute.INDICES);
		IntArray vind = null;
		if (piDL != null) vind = piDL.toIntArray();
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList pointSize = sg.getVertexAttributes(Attribute.RELATIVE_RADII);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		int colorLength = 0;
		if (vertexColors != null) {
			colorLength = GeometryUtility.getVectorLength(vertexColors);
			if (openGLState.frontBack != DefaultPolygonShader.FRONT_AND_BACK) {
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK,
						GL.GL_DIFFUSE);
				openGLState.frontBack = DefaultPolygonShader.FRONT_AND_BACK;
			}
		}

		DoubleArray da, ra=null;
		if (pointSize != null) ra = pointSize.toDoubleArray();
		if (pickMode)
			gl.glPushName(JOGLPickAction.GEOMETRY_POINT);
		// if (pickMode) JOGLConfiguration.theLog.log(Level.INFO,"Rendering
		// vertices in picking mode");
		if (!pickMode && pointSize == null)
			gl.glBegin(GL.GL_POINTS);
		for (int i = 0; i < sg.getNumPoints(); ++i) {
			// double vv;
			if (vind != null && vind.getValueAt(i) == 0) continue;
			if (pickMode)
				gl.glPushName(i);
			if (pointSize != null) {
				float ps = (float) (jr.renderingState.pointSize * ra.getValueAt(i));
				gl.glPointSize(ps);
			}
			if (pickMode || pointSize != null)
				gl.glBegin(GL.GL_POINTS);
			// if (pointSize != null) gl.glBegin(GL.GL_POINTS);
			if (vertexColors != null) {
				da = vertexColors.item(i).toDoubleArray();
				if (colorLength == 3) {
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) {
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha * da.getValueAt(3));
				}
			}
			da = vertices.item(i).toDoubleArray();
			if (vertexLength == 3)
				gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
			else if (vertexLength == 4)
				gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
			if (pickMode || pointSize != null) 
				gl.glEnd();
			if (pickMode)
				gl.glPopName();
		}
		if (!pickMode)
			gl.glEnd();
		if (pickMode)
			gl.glPopName();
	}

	// static Texture2D tex2d=(Texture2D)
	// AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", a,
	// true);
	// This is upside down since openGl textures are upside down.
	private static IndexedFaceSet bb = Primitives.texturedQuadrilateral(new double[] { 0, 1,
			0, 1, 1, 0, 1, 0, 0, 0, 0, 0 });


	/**
	 * @param sg
	 */
	public static void drawLines(JOGLRenderer jr, IndexedLineSet sg, boolean interpolateVertexColors, double alpha) {
		if (sg.getNumEdges() == 0)
			return;

		GL gl = jr.globalGL;
    
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		DataList edgeColors = sg.getEdgeAttributes(Attribute.COLORS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList lineWidth = sg.getVertexAttributes(Attribute.RELATIVE_RADII);
		DoubleArray ra=null;
		if (lineWidth != null) ra = lineWidth.toDoubleArray();
		boolean hasNormals = vertexNormals == null ? false : true;
		DoubleArray da;
		// SJOGLConfiguration.theLog.log(Level.INFO,"Processing ILS");
//		boolean testArrays =false;
//		 if (testArrays) {
//		 double[] varray = vertices.toDoubleArray(null);
//		 ByteBuffer bb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
//		 bb.asDoubleBuffer().put(varray);
//		 bb.flip();
//		 gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, bb);
//		 
//		 gl.glDisableClientState(GL.GL_COLOR_ARRAY);
//		 gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
//		 gl.glDrawArrays(GL.GL_POINTS, 0, sg.getNumPoints());
//		 
//		 for (int i = 0; i < sg.getNumEdges(); ++i) {
//			 gl.glBegin(GL.GL_LINE_STRIP);
//			 IntArray ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
//			 int m = ed.getLength();
//			 for (int j = 0; j < m; ++j) {
//				 gl.glArrayElement(ed.getValueAt(j));
//			 }
//			 gl.glEnd();
//		 }
//		 return;
//		 }
		if (sg.getEdgeAttributes(Attribute.INDICES) == null)
			return;
		int colorBind = 0, colorLength = 0;
		if (interpolateVertexColors && vertexColors != null) {
			colorBind = PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} else if (edgeColors != null) {
			colorBind = PER_EDGE;
			colorLength = GeometryUtility.getVectorLength(edgeColors);
		} else
			colorBind = PER_PART;
		if (colorBind != PER_PART) {
			if (jr.renderingState.frontBack != DefaultPolygonShader.FRONT_AND_BACK) {
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK,
						GL.GL_DIFFUSE);
				jr.renderingState.frontBack = DefaultPolygonShader.FRONT_AND_BACK;
			}
		}
		boolean pickMode = jr.isPickMode();
		if (pickMode)
			gl.glPushName(JOGLPickAction.GEOMETRY_LINE);
		int numEdges = sg.getNumEdges();
		// if (pickMode) JOGLConfiguration.theLog.log(Level.INFO,"Rendering
		// edges in picking mode");
		for (int i = 0; i < numEdges; ++i) {
			if (pickMode)
				gl.glPushName(i);
//			if (ra != null) {
//				float ps = (float) (jr.renderingState.lineWidth * ra.getValueAt(i));
//				gl.glLineWidth(ps);
//			}
			if (!pickMode)
				gl.glBegin(GL.GL_LINE_STRIP);
			int[] ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			if (!pickMode && colorBind == PER_EDGE) {
				da = edgeColors.item(i).toDoubleArray();
				if (colorLength == 3) {
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) {
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha * da.getValueAt(3));
				}
			}

			for (int j = 0; j < m; ++j) {
				if (pickMode) {
					if (j == m - 1)
						break;
					gl.glPushName(j);
					gl.glBegin(GL.GL_LINES);
				}
				int k = ed[j];
				if (!pickMode && colorBind == PER_VERTEX) {
					da = vertexColors.item(k).toDoubleArray();
					if (colorLength == 3) {
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), alpha);
					} else if (colorLength == 4) {
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), alpha * da.getValueAt(3));
					}
				}
				if (hasNormals) {
					da = vertexNormals.item(k).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da
							.getValueAt(2));
				}
				da = vertices.item(k).toDoubleArray();
				if (vertexLength == 3)
					gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da
							.getValueAt(2));
				else if (vertexLength == 4)
					gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da
							.getValueAt(2), da.getValueAt(3));
				// if (pickMode) gl.glPopName();
				if (pickMode) {
					k = ed[j + 1];
					da = vertices.item(k).toDoubleArray();
					if (vertexLength == 3)
						gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2));
					else if (vertexLength == 4)
						gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), da.getValueAt(3));
					gl.glEnd();
					gl.glPopName();
				}
			}
			if (!pickMode)
				gl.glEnd();
			if (pickMode)
				gl.glPopName();
		}
		if (pickMode)
			gl.glPopName();
		// gl.glDepthRange(0d, 1d);
	}

	public static void drawFaces(JOGLRenderer jr, IndexedFaceSet sg, boolean smooth, double alpha) {
		if (sg.getNumFaces() == 0)
			return;
		GL gl = jr.globalGL;
		boolean pickMode = jr.isPickMode();
		int colorBind = -1, normalBind, colorLength = 3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg
				.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		DataList lightMapCoords = sg.getVertexAttributes(Attribute
				.attributeForName("lightmap coordinates"));
		// JOGLConfiguration.theLog.log(Level.INFO,"Vertex normals are:
		// "+((vertexNormals != null) ? vertexNormals.size() : 0));
		// JOGLConfiguration.theLog.log(Level.INFO,"alpha value is "+alpha);

		// vertex color has priority over face color
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		if (vertexColors != null && smooth) {
			colorBind = PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} else if (faceColors != null && colorBind != PER_VERTEX) {
			colorBind = PER_FACE;
			colorLength = GeometryUtility.getVectorLength(faceColors);
		} else
			colorBind = PER_PART;
		// JOGLConfiguration.theLog.log(Level.INFO,"Color binding is
		// "+colorBind);
		if (colorBind != PER_PART) {
			if (jr.renderingState.frontBack != DefaultPolygonShader.FRONT_AND_BACK) {
				gl.glEnable(GL.GL_COLOR_MATERIAL);
				gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK,
						GL.GL_DIFFUSE);
				jr.renderingState.frontBack = DefaultPolygonShader.FRONT_AND_BACK;
			}
		}
		if (vertexNormals != null && smooth) {
			normalBind = PER_VERTEX;
		} else if (faceNormals != null) {
			normalBind = PER_FACE;
		} else
			normalBind = PER_PART;

		// if (vertices != null) {
		// int vlength = GeometryUtility.getVectorLength(vertices);
		// JOGLConfiguration.theLog.log(Level.INFO,"Vertics have length
		// "+vlength);
		// }
		// if (faceNormals != null) {
		// int vlength = GeometryUtility.getVectorLength(faceNormals);
		// JOGLConfiguration.theLog.log(Level.INFO,("Normals have length
		// "+vlength);
		// }
		DoubleArray da = null;
		boolean isQuadMesh = false;
		boolean isRegularDomainQuadMesh = false;
		Rectangle2D theDomain = null;
		int maxU = 0, maxV = 0, maxFU = 0, maxFV = 0, numV = 0, numF;
		Object qmatt = sg.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
		if (qmatt != null && qmatt instanceof Dimension) {
			Dimension dm = (Dimension) qmatt;
			isQuadMesh = true;
			maxU = dm.width;
			maxV = dm.height;
			numV = maxU * maxV;
			maxFU = maxU - 1;
			maxFV = maxV - 1;
			// Done with GeometryAttributes?
			qmatt = sg.getGeometryAttributes(GeometryUtility.HEIGHT_FIELD_SHAPE);
			if (qmatt != null && qmatt instanceof Rectangle2D) {
				theDomain = (Rectangle2D) qmatt;
				isRegularDomainQuadMesh = true;
			}
		}

		numF = sg.getNumFaces();
		if (!pickMode && isQuadMesh) {
			double[] pt = new double[3];
			// this loops through the "rows" of the mesh (v is constant on each
			// row)
			for (int i = 0; i < maxFV; ++i) {
				gl.glBegin(GL.GL_QUAD_STRIP);
				// each iteration of this loop draws one quad strip consisting
				// of 2 * (maxFU + 1) vertices
				for (int j = 0; j <= maxFU; ++j) {
					int u = j % maxU;
					// if (pickMode) {
					// //JOGLConfiguration.theLog.log(Level.INFO,"+G"+faceCount+"\n");
					// gl.glPushName(faceCount++);
					// }
					// draw two points: one on "this" row, the other directly
					// below on the next "row"
					for (int incr = 0; incr < 2; ++incr) {
						int vnn = (i * maxU + j % maxU + incr * maxU) % numV;
						int fnn = (i * maxFU + j % maxFU + incr * maxFU) % numF;
						int v = (i + incr) % maxV;
						if (normalBind == PER_FACE) {
							if (incr == 0 && j != maxFU) {
								da = faceNormals.item(fnn).toDoubleArray();
								gl.glNormal3d(da.getValueAt(0), da
										.getValueAt(1), da.getValueAt(2));
							}
						} else if (normalBind == PER_VERTEX) {
							da = vertexNormals.item(vnn).toDoubleArray();
							gl.glNormal3d(da.getValueAt(0), da.getValueAt(1),
									da.getValueAt(2));
						}
						if (colorBind == PER_FACE) {
							if (incr == 0) {
								da = faceColors.item(fnn).toDoubleArray();
								if (colorLength == 3) {
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2),
											alpha);
								} else if (colorLength == 4) {
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2),
											alpha * da.getValueAt(3));
								}
							}
						} else if (colorBind == PER_VERTEX) {
							da = vertexColors.item(vnn).toDoubleArray();
							if (colorLength == 3) {
								gl.glColor4d(da.getValueAt(0),da.getValueAt(1), da.getValueAt(2),
										alpha);
							} else if (colorLength == 4) {
								gl.glColor4d(da.getValueAt(0),da.getValueAt(1), da.getValueAt(2),
										alpha * da.getValueAt(3));
							}
						}
						for (int nn = 0; nn<jr.renderingState.texUnitCount; ++nn)	{
							int texunit = GL.GL_TEXTURE0+nn;
							if (nn == 0 && lightMapCoords != null) {
								da = lightMapCoords.item(vnn).toDoubleArray();
							}
							else if (texCoords != null) {
								da = texCoords.item(vnn).toDoubleArray();
							}
							gl.glMultiTexCoord2d(texunit, da.getValueAt(0), da.getValueAt(1));
						}
						da = vertices.item(vnn).toDoubleArray();
						if (vertexLength == 1 && isRegularDomainQuadMesh) {
																			
							double z = da.getValueAt(0);
							HeightFieldFactory.getCoordinatesForUV(pt,
									theDomain, u, v, maxU, maxV);
							gl.glVertex3d(pt[0], pt[1], z);
						} else if (vertexLength == 3)
							gl.glVertex3d(da.getValueAt(0), da.getValueAt(1),
									da.getValueAt(2));
						else if (vertexLength == 4)
							gl.glVertex4d(da.getValueAt(0), da.getValueAt(1),
									da.getValueAt(2), da.getValueAt(3));
					}
				}
				gl.glEnd();
			}
		} else	{
			// signal a geometry
			if (pickMode)
				gl.glPushName(JOGLPickAction.GEOMETRY_FACE); // pickName);

			for (int i = 0; i < sg.getNumFaces(); ++i) {
				if (colorBind == PER_FACE) {
					da = faceColors.item(i).toDoubleArray();
					if (colorLength == 3) {
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), alpha);
					} else if (colorLength == 4) {
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), alpha * da.getValueAt(3));
					}
				}
				if (pickMode) {
					gl.glPushName(i);
				}
				if (normalBind == PER_FACE) {
					da = faceNormals.item(i).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da
							.getValueAt(2));
				}
				IntArray tf = sg.getFaceAttributes(Attribute.INDICES).item(i)
						.toIntArray();
				gl.glBegin(GL.GL_POLYGON);
				for (int j = 0; j < tf.getLength(); ++j) {
					int k = tf.getValueAt(j);
					if (normalBind == PER_VERTEX) {
						da = vertexNormals.item(k).toDoubleArray();
						gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2));
					}
					if (colorBind == PER_VERTEX) {
						da = vertexColors.item(k).toDoubleArray();
						if (colorLength == 3) {
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
									.getValueAt(2), alpha);
						} else if (colorLength == 4) {
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da
									.getValueAt(2), alpha * da.getValueAt(3));
						}
					}
					for (int nn = 0; nn<jr.renderingState.texUnitCount; ++nn)	{
						int texunit = GL.GL_TEXTURE0+nn;
						if (nn == 0 && lightMapCoords != null) {
							da = lightMapCoords.item(k).toDoubleArray();
						}
						else if (texCoords != null) {
							da = texCoords.item(k).toDoubleArray();
						}
						gl.glMultiTexCoord2d(texunit, da.getValueAt(0), da.getValueAt(1));
					}
					da = vertices.item(k).toDoubleArray();
					if (vertexLength == 3)
						gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2));
					else if (vertexLength == 4)
						gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da
								.getValueAt(2), da.getValueAt(3));
				}
				gl.glEnd();
				if (pickMode) {
					gl.glPopName();
				}
			}
			if (pickMode)
				gl.glPopName();
		}
	}

	private static final Texture2D tex2d = (Texture2D) AttributeEntityUtility
	.createAttributeEntity(Texture2D.class, "", new Appearance(), true);
	static {
		tex2d.setRepeatS(Texture2D.GL_CLAMP);
		tex2d.setRepeatT(Texture2D.GL_CLAMP);
	}

	public static void drawPointLabels(JOGLRenderer jr, PointSet ps,
			DefaultTextShader ts) {
		if (!ts.getShowLabels().booleanValue())
			return;

		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createPointImages(ps, font, c);

		renderLabels(jr, img, ps.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(), null, offset, alignment, scale);

	}

	public static void drawEdgeLabels(JOGLRenderer jr, IndexedLineSet ils,
			DefaultTextShader ts) {
		if (!ts.getShowLabels().booleanValue())
			return;

		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createEdgeImages(ils, font, c);

		renderLabels(jr, img, ils.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(), ils.getEdgeAttributes(Attribute.INDICES)
				.toIntArrayArray(), offset, alignment, scale);

	}

	public static void drawFaceLabels(JOGLRenderer jr, IndexedFaceSet ifs,
			DefaultTextShader ts) {
		if (!ts.getShowLabels().booleanValue())
			return;
		Font font = ts.getFont();
		Color c = ts.getDiffuseColor();
		double scale = ts.getScale().doubleValue();
		double[] offset = ts.getOffset();
		int alignment = ts.getAlignment();
		ImageData[] img = LabelUtility.createFaceImages(ifs, font, c);

		renderLabels(jr, img, ifs.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(), ifs.getFaceAttributes(Attribute.INDICES)
				.toIntArrayArray(), offset, alignment, scale);

	}

	private static void renderLabels(JOGLRenderer jr, ImageData[] labels,
			DoubleArrayArray vertices, IntArrayArray indices, double[] offset,
			int alignment, double scale) {
		GL gl = jr.globalGL;
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glEnable(GL.GL_BLEND);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDepthMask(true);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glColor3d(1, 1, 1);
		double[] c2o = jr.getContext().getCameraToObject();
		gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glEnable(GL.GL_TEXTURE_2D);
		jr.renderingState.texUnitCount = 1;
		double[] bbm = new double[16];
		// float[] glc2o = new float[16];
		// double[] dglc2o = new double[16];
		// gl.glGetFloatv(GL.GL_TRANSPOSE_MODELVIEW_MATRIX, glc2o);
		// for (int i = 0; i<16; ++i) dglc2o[i]=glc2o[i];
		// System.err.println("glc2o
		// is"+Rn.matrixToString(Rn.inverse(dglc2o,dglc2o)));
//		System.err.println("Sig is "+jr.renderingState.currentSignature);
		for (int i = 0, n = labels.length; i < n; i++) {
			ImageData img = labels[i];
			tex2d.setImage(img);
			LabelUtility.calculateBillboardMatrix(bbm, img.getWidth() * scale,
					img.getHeight() * scale, offset, alignment, c2o,
					LabelUtility.positionFor(i, vertices, indices),
					jr.renderingState.currentSignature); //)Pn.EUCLIDEAN);
			Texture2DLoaderJOGL.render(gl, tex2d, true);
			gl.glPushMatrix();
			gl.glMultTransposeMatrixd(bbm, 0);
			drawFaces(jr, bb, true, 1.0);
			gl.glPopMatrix();
		}
		gl.glPopAttrib();
		jr.renderingState.texUnitCount = 0;
	}
	private static double[] correctionNDC = null;
	static {
		correctionNDC = Rn.identityMatrix(4);
		correctionNDC[10] = correctionNDC[11] = .5;
	}

	private static double[] clipPlane = { 0d, 0d, -1d, 0d };

	/**
	 * 
	 */
	public static void processClippingPlanes(GL globalGL, List clipPlanes) {

		int clipBase = GL.GL_CLIP_PLANE0;
		int n = clipPlanes.size();
		// globalGL.glDisable(GL.GL_CLIP_PLANE0);
		for (int i = 0; i < n; ++i) {
			SceneGraphPath lp = (SceneGraphPath) clipPlanes.get(i);
			// JOGLConfiguration.theLog.log(Level.INFO,"Light"+i+":
			// "+lp.toString());
			SceneGraphNode cp = lp.getLastElement();
			if (!(cp instanceof ClippingPlane))
				JOGLConfiguration.theLog.log(Level.WARNING,
						"Invalid clipplane class " + cp.getClass().toString());
			else {
				double[] mat = lp.getMatrix(null);
				globalGL.glPushMatrix();
				globalGL.glMultTransposeMatrixd(mat,0);
				globalGL.glClipPlane(clipBase + i, clipPlane,0);
				globalGL.glEnable(clipBase + i);
				globalGL.glPopMatrix();
			}
		}
	}

	//	public static void saveScreenShot(GL gl, File file) {
//		saveScreenShot(gl, theRenderer.getWidth(), can.getHeight(), file);
//	}
//
	/**
	 * @param globalGL
	 * @param file
	 */
	public static void saveScreenShot(GL gl, int width,
			int height, File file) {

		// TODO figure out why channels = 4 doesn't work: transparency
		// getting written into fb even
		// though transparency disabled.
		int channels = 3;
		ByteBuffer pixelsRGBA = BufferUtil.newByteBuffer(width * height
				* channels);

		gl.glReadBuffer(GL.GL_BACK);
		gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

		gl.glReadPixels(0, // GLint x
				0, // GLint y
				width,// GLsizei width
				height, // GLsizei height
				channels == 3 ? GL.GL_RGB : GL.GL_RGBA, // GLenum format
				GL.GL_UNSIGNED_BYTE, // GLenum type
				pixelsRGBA); // GLvoid *pixels

		int[] pixelInts = new int[width * height];

		// Convert RGB bytes to ARGB ints with no transparency. Flip image
		// vertically by reading the
		// rows of pixels in the byte buffer in reverse - (0,0) is at bottom
		// left in OpenGL.

		int p = width * height * channels; // Points to first byte (red) in
		// each row.
		int q; // Index into ByteBuffer
		int i = 0; // Index into target int[]
		int w3 = width * channels; // Number of bytes in each row

		for (int row = 0; row < height; row++) {
			p -= w3;
			q = p;
			for (int col = 0; col < width; col++) {
				int iR = pixelsRGBA.get(q++);
				int iG = pixelsRGBA.get(q++);
				int iB = pixelsRGBA.get(q++);
				int iA = (channels == 3) ? 0xff : pixelsRGBA.get(q++);

				pixelInts[i++] = ((iA & 0x000000FF) << 24)
						| ((iR & 0x000000FF) << 16) | ((iG & 0x000000FF) << 8)
						| (iB & 0x000000FF);
			}

		}

		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);

		try {
			ImageIO.write(bufferedImage, "PNG", file);
			// ImageIO.write(bufferedImage, "TIF", file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JOGLConfiguration.theLog.log(Level.INFO, "Screenshot saved to "
				+ file.getName());
	}

}
