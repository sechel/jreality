/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.acl.LastOwnerException;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.util.BufferUtils;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.RegularDomainQuadMesh;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.PolygonShader;
import de.jreality.jogl.shader.ShaderLookup;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Light;
import de.jreality.scene.PointLight;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SpotLight;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.util.ClippingPlaneCollector;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.LightCollector;
import de.jreality.util.Pn;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JOGLRendererHelper {

	static float [] bg = {0f, 0f, 0f, 1f};
	public static void handleBackground(GLCanvas theCanvas, Appearance topAp)	{
			GL gl = theCanvas.getGL();
			Object bgo = null;
			
			for (int i = 0; i<8; ++i)	{
				gl.glDisable(i+GL.GL_CLIP_PLANE0);
			}
			//TODO replace BackPlane class with simple quad drawn here, keyed to "backgroundColors" in topAp
			if (topAp != null)	bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_COLOR);
			if (bgo != null && bgo instanceof java.awt.Color) bg = ((java.awt.Color) bgo).getComponents(null);
			else bg = CommonAttributes.BACKGROUND_COLOR_DEFAULT.getRGBComponents(null);
			gl.glClearColor(bg[0], bg[1], bg[2], bg[3] ); //white 
			
			if (topAp != null) bgo =  topAp.getAttribute("backgroundColors");
			float val = 1f;
			float[][] unitsquare = {{-val, -val},{val,-val},{val,val},{-val,val}};
			//float[][] unitsquare = {{0, 0},{val,0},{val,val},{0,val}};
			//Color[] corners = { new Color(.5f,.5f,1f), new Color(.5f,.5f,.5f),new Color(1f,.5f,.5f),new Color(.5f,1f,.5f) };
			if (bgo != null && bgo instanceof Color[])	{
				//bgo = (Object) corners;
				float[][] cornersf = new float[4][];
				gl.glDisable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_LIGHTING);
				gl.glShadeModel(GL.GL_SMOOTH);
				gl.glBegin(GL.GL_POLYGON);
				//gl.glScalef(.5f, .5f, 1.0f);
				for (int q = 0; q<4; ++q)		{
					cornersf[q] = ((Color[]) bgo)[q].getComponents(null);
					gl.glColor3fv(cornersf[q]);
					gl.glVertex2fv(unitsquare[q]);
				}
				gl.glEnd();
				gl.glEnable(GL.GL_DEPTH_TEST);
			}
	}
/**
	 * @param sg
	 */
	// ultimately all this should happen in various visit() methods
	public  static void initializeGLState(GLCanvas theCanvas)	{
		GL gl = theCanvas.getGL();
		// set drawing color and point size
		gl.glColor3f( 0.3f, 0.0f, 0.6f ); 
		gl.glEnable(GL.GL_DEPTH_TEST);							// Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL);								// The Type Of Depth Testing To Do
		gl.glClearDepth(1.0f);  
		gl.glEnable(GL.GL_NORMALIZE);
		//gl.glDisable(GL.GL_COLOR_MATERIAL);		
		gl.glEnable(GL.GL_MULTISAMPLE_ARB);	
		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		boolean doFog = false;
		if (doFog)	{
			gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
			float[] fogcolor = {.5f, .5f, .5f, 1.0f};
			gl.glEnable(GL.GL_FOG);
			gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
			gl.glFogfv(GL.GL_FOG_COLOR, bg);
			gl.glFogf(GL.GL_FOG_DENSITY, .4f);
		}
	}
	// for converting double arrays to native buffers:
//	static ByteBuffer bb = ByteBuffer.allocateDirect(444/* array.length*8 */).order(ByteOrder.nativeOrder());
//	bb.asDoubleBuffer().put(array);
//	bb.flip();
//	
//	gl.glVertexPointer(3, GL.GL_DOUBLE, 8*3, bb);
	// can re-use after checking that it's long enough, use some method to reallocate
	
	static boolean testArrays = false;
	static ByteBuffer vBuffer, vcBuffer, vnBuffer, fcBuffer, fnBuffer, tcBuffer;
	static DataList vLast = null, vcLast = null, vnLast = null;
	public static void drawVertices( PointSet sg, JOGLRenderer jr, boolean drawSpheres, double pointRadius, double alpha) {
		GLCanvas theCanvas = jr.theCanvas;
		GL gl = theCanvas.getGL(); 
//		gl.glPointSize((float) currentGeometryShader.pointShader.getPointSize());
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList pointSize = sg.getVertexAttributes(Attribute.POINT_SIZE);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		int colorLength = 0;
		if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
		DoubleArray da;
		gl.glColorMaterial(GL.GL_FRONT, GL.GL_DIFFUSE);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		if (drawSpheres)	{
			int[] dlists = JOGLSphereHelper.getSphereDLists(gl);
			double size = pointRadius;
			
			gl.glEnable(GL.GL_COLOR_MATERIAL);		
			gl.glColorMaterial(GL.GL_FRONT, GL.GL_DIFFUSE);
			for (int i = 0; i< sg.getNumPoints(); ++i)	{
				da = vertices.item(i).toDoubleArray();	
				//TODO figure out how to draw these spheres correctly in non-euclidean case
				double x=0,y=0,z=0,w=0;
				if (da.getLength() == 4)	{
					w = da.getValueAt(3);
					if (w != 0) w  = 1.0/w;
					else w = 1.0;
					x = w * da.getValueAt(0);
					y = w * da.getValueAt(1);
					z = w * da.getValueAt(2);
				} else {
					x = da.getValueAt(0);
					y = da.getValueAt(1);
					z = da.getValueAt(2);					
				}
				gl.glPushMatrix();
				gl.glTranslated(x,y,z);
				gl.glScaled(size, size, size);
				if (vertexColors != null)	{
					da = vertexColors.item(i).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
					} else if (colorLength == 4) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
					} 
				}
				gl.glCallList(dlists[1]);
				gl.glPopMatrix();
			}
		} else {
			if (testArrays)	{
					double[] varray = vertices.toDoubleArray(null);
					ByteBuffer bb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
					bb.asDoubleBuffer().put(varray);
					bb.flip();				
					gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, bb);	
				
				double[] carray = null;
				if (vertexColors != null) {
					vertexColors.toDoubleArray(null);
					ByteBuffer cb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
					cb.asDoubleBuffer().put(varray);
					cb.flip();
					gl.glColorPointer(colorLength, GL.GL_DOUBLE, 0, cb);
				}
				// can re-use after checking that it's long enough, use some method to reallocate
				if (vertexColors != null) gl.glEnableClientState(GL.GL_COLOR_ARRAY);
				else gl.glDisableClientState(GL.GL_COLOR_ARRAY);
				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
				gl.glDrawArrays(GL.GL_POINTS, 0, sg.getNumPoints());
				gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
				gl.glDisableClientState(GL.GL_COLOR_ARRAY);
				
			} else {
				gl.glBegin(GL.GL_POINTS);
				for (int i = 0; i< sg.getNumPoints(); ++i)	{
					//double vv;
					if (pointSize != null) {
						float ps = (float) pointSize.item(i).toDoubleArray().getValueAt(0);
						gl.glPointSize( ps);
						//vv =  (ps < 1) ? ps : (1d - (Math.ceil(ps) - ps) * 0.25d);

					}
					//if (pointSize != null)	gl.glBegin(GL.GL_POINTS);
					if (vertexColors != null)	{
						da = vertexColors.item(i).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
						} else if (colorLength == 4) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
						} 
					}
					da = vertices.item(i).toDoubleArray();				
					if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
				}
				gl.glEnd();
				
			}
		}
		gl.glDisable(GL.GL_COLOR_MATERIAL);
//		}
		// TODO decide whether to keep this
//		if (currentGeometryShader.pointShader.isNormalsDraw())	{
//			if (currentGeometryShader.lineShader.isLineStipple()) {
//				gl.glEnable(GL.GL_LINE_STIPPLE);
//				gl.glLineStipple(currentGeometryShader.lineShader.getLineFactor(), (short) currentGeometryShader.lineShader.getLineStipplePattern());
//				} 
//			else gl.glDisable(GL.GL_LINE_STIPPLE);
//			gl.glDisable(GL.GL_LIGHTING);
//			gl.glLineWidth((float) currentGeometryShader.lineShader.getLineWidth());
//			DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
//			double[][] vn;
//			double[][] vv = vertices.toDoubleArrayArray(null);
//			if (vertexNormals == null) {
//				// TODO signal error!
//				//if (! (sg instanceof IndexedFaceSet) ) return;
//				vn = GeometryUtility.calculateVertexNormals((IndexedFaceSet) sg);
//				//sg.setVertexAttributes(Attribute.NORMALS, new DataList(StorageModel.DOUBLE_ARRAY.array(3), vn));
//			}
//			else vn = vertexNormals.toDoubleArrayArray(null);
//			gl.glColor4fv(currentGeometryShader.lineShader.getDiffuseColorAsFloat());
//			double[] tip = new double[4];
//			int n = sg.getNumPoints();
//			for (int i = 0; i< n; ++i)	{
//				double ns = currentGeometryShader.pointShader.getNormalScale();
//				gl.glBegin(GL.GL_LINES);
//				gl.glVertex3dv(vv[i]);
//				Rn.add(tip, Rn.times(tip, ns, vn[i]), vv[i]);
//				gl.glVertex3dv(tip);
//				tip[3] = 0.0;
//				gl.glEnd();
//			}
//		}
//		if (renderingHints.isLightingEnabled()) gl.glEnable(GL.GL_LIGHTING);
//		if (renderingHints.isAntiAliasingEnabled())	{
//			gl.glDisable (GL.GL_LINE_SMOOTH);
//			gl.glDisable (GL.GL_BLEND);
//		}
	}
	/**
	 * @param sg
	 */
	public static void drawLines(IndexedLineSet sg, GLCanvas theCanvas, double alpha) {
		GL gl = theCanvas.getGL();
//		DefaultGeometryShader currentGeometryShader = jpc.geometryShader;
//		RenderingHintsShader renderingHints = jpc.renderingHints;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		DataList colors = sg.edgeAttributes.getList(Attribute.COLORS);
		// vertex color has priority over face color
		// should also check for override behavior
		//if (vc != null) 		colorBind = ElementBinding.PER_VERTEX;
		if (sg.getEdgeAttributes(Attribute.INDICES) == null) return;
//		int colorBind = ElementBinding.PER_PART;
//		if (colors != null) 	colorBind = ElementBinding.PER_EDGE;
		DoubleArray da;
		// TODO support for colors per vertex?
		if (testArrays)	{
			double[] varray = vertices.toDoubleArray(null);
			ByteBuffer bb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
			bb.asDoubleBuffer().put(varray);
			bb.flip();				
			gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, bb);	
			
			gl.glDisableClientState(GL.GL_COLOR_ARRAY);
			gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			gl.glDrawArrays(GL.GL_POINTS, 0, sg.getNumPoints());
			
			for (int i = 0; i< sg.getNumEdges(); ++i)	{
				gl.glBegin(GL.GL_LINE_STRIP);
				IntArray ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray();
				int m = ed.getLength();
				for (int j = 0; j<m; ++j)	{
					gl.glArrayElement(ed.getValueAt(j));
				}
				gl.glEnd();
			}
			gl.glDisableClientState(GL.GL_COLOR_ARRAY);
			gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
			
		}	else {
			for (int i = 0; i< sg.getNumEdges(); ++i)	{
				gl.glBegin(GL.GL_LINE_STRIP);
				int[] ed = sg.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
				int m = ed.length;
				for (int j = 0; j<m; ++j)	{
					int k = ed[j];
					da = vertices.item(k).toDoubleArray();				
					if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
				}
				gl.glEnd();
			}
		}
		gl.glDepthRange(0d, 1d);
	}

	/**
	 * @param sg
	 */
	public static void drawFaces( IndexedFaceSet sg, GL gl, boolean pickMode, boolean smooth, double alpha) {

		//if (jr.pickMode && (insidePointSet || insideLineSet)) return;

		//DefaultGeometryShader currentGeometryShader = jpc.geometryShader;
		//RenderingHintsShader renderingHints = jpc.renderingHints;
		int colorBind,normalBind, colorLength=3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		//System.out.println("Vertex normals are: "+((vertexNormals != null) ? vertexNormals.size() : 0));
		//System.out.println("alpha value is "+alpha);
		
		//double[][] vv = null, vn=null, fn=null, vc=null, fc=null, tc = null;
		// signal a geometry
		if (pickMode)	gl.glPushName(10000);
		
//		if (texCoords != null && currentGeometryShader.polygonShader.isTextureEnabled())	{
//			Texture2DLoaderJOGL tl = Texture2DLoaderJOGL.FactoryLoader;
//			Object tex =  currentGeometryShader.polygonShader.getTexture2D();
//			if (tex instanceof Texture2D)		{
//				tl.bindTexture2D(theCanvas, (Texture2D) tex);
//				int[] res = new int[1];
//				gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
//				System.out.println("Texture is resident: "+res[0]);
//				if (res[0] == 0)	{ jr.texResident = false; }
//			}
//			else texCoords = null;
//			gl.glEnable(GL.GL_TEXTURE_2D);			
//		} else
//			texCoords = null;
		// vertex color has priority over face color
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
//		boolean shadePerFace = false;
//		if (!currentGeometryShader.polygonShader.isSmoothShading() ||
//		(currentGeometryShader.polygonShader.isSmoothShading() && currentGeometryShader.polygonShader.isFaceNormals())) 	{
//			shadePerFace = true;
//		}

		if (vertexColors != null && smooth) 		{
			colorBind = ElementBinding.PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} 
		else if (faceColors != null && (vertexColors == null || !smooth)) 	{
			colorBind = ElementBinding.PER_FACE;
			colorLength = GeometryUtility.getVectorLength(faceColors);
		} 
		else 	colorBind = ElementBinding.PER_PART;
		//System.out.println("Color binding is "+colorBind);
		
		if (vertexNormals != null && smooth)	{
				normalBind = ElementBinding.PER_VERTEX;
			}
		else 	if (faceNormals != null  && (vertexNormals == null || !smooth)) {
				normalBind = ElementBinding.PER_FACE;
		}
		else normalBind = ElementBinding.PER_PART;
		
		DoubleArray da;
		if (colorBind != ElementBinding.PER_PART){
			  gl.glEnable(GL.GL_COLOR_MATERIAL);		
			  gl.glColorMaterial(GL.GL_FRONT, GL.GL_DIFFUSE);
		}
		if (!pickMode && sg instanceof QuadMeshShape)	{
			QuadMeshShape qm = (QuadMeshShape) sg;
			RegularDomainQuadMesh rdqm = null;
			int type = Pn.EUCLIDEAN;
			if (qm instanceof RegularDomainQuadMesh) {
				rdqm = (RegularDomainQuadMesh) qm;
				type = rdqm.getType();
			}
			int maxU = qm.getMaxU();
			int maxV = qm.getMaxV();
			int numV = maxU * maxV;
			int maxFU = qm.isClosedInUDirection() ? maxU : maxU-1;
			int maxFV = qm.isClosedInVDirection() ? maxV : maxV-1;
			int numF = qm.getNumFaces();
			double[] pt = new double[3];
			// this loops through the "rows" of  the mesh (v is constant on each row)
			for (int i = 0; i< maxFV ; ++i)	{
				gl.glBegin(GL.GL_QUAD_STRIP);
				// each iteration of this loop draws one quad strip consisting of 2 * (maxFU + 1) vertices
				for (int j = 0; j <= maxFU; ++j)	{
					int u = j%maxU;
//					if (pickMode) {
//						//System.out.print("+G"+faceCount+"\n");
//						gl.glPushName(faceCount++);
//					}
					// draw two points: one on "this" row, the other directly below on the next "row"
					for (int incr = 0; incr< 2; ++incr)	{
						int vnn = (i*maxU + j%maxU + incr*maxU)%numV;
						int fnn = (i*maxFU + j%maxFU + incr*maxFU)%numF;
						int v = (i+incr)%maxV;
						if (normalBind == ElementBinding.PER_FACE) {
							if (incr == 0 && j != maxFU) 	{
								da = faceNormals.item(fnn).toDoubleArray();
								gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));								
							}
						} else 
						if (normalBind == ElementBinding.PER_VERTEX) {
							da = vertexNormals.item(vnn).toDoubleArray();
							gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
						} 
						if (colorBind == ElementBinding.PER_FACE) 		{	
							if (incr == 0) {
								da = faceColors.item(fnn).toDoubleArray();
								if (colorLength == 3) 	{
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
								} else if (colorLength == 4) 	{
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
								} 
							}
						} else
						if (colorBind == ElementBinding.PER_VERTEX) {
							da = vertexColors.item(vnn).toDoubleArray();
							if (colorLength == 3) 	{
								gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
							} else if (colorLength == 4) 	{
								gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
							} 
						}
						if (texCoords != null)	 {
							da = texCoords.item(vnn).toDoubleArray();
							gl.glTexCoord2d(da.getValueAt(0), da.getValueAt(1));
						}
						da = vertices.item(vnn).toDoubleArray();
						if (vertexLength == 1)	{		// Regular domain quad mesh
							double z = da.getValueAt(0);
							rdqm.getPointForIndices(u, v, pt);
							if (type == Pn.EUCLIDEAN)		gl.glVertex3d(pt[0], pt[1], z);
							else							gl.glVertex3d(z*pt[0], z*pt[1], z*pt[2]);
						}
						if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
						else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));								
					}
//					if (pickMode) {
//						//System.out.print("-");
//						gl.glPopName();
//					}
				}
				gl.glEnd();
			}				
		}
		else
			if (testArrays)	{
				gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
				gl.glDisableClientState(GL.GL_COLOR_ARRAY);
				double[] varray = vertices.toDoubleArray(null);
				ByteBuffer bb = ByteBuffer.allocateDirect(8*varray.length).order(ByteOrder.nativeOrder());
				bb.asDoubleBuffer().put(varray);
				bb.flip();				
				gl.glVertexPointer(vertexLength, GL.GL_DOUBLE, 0, bb);	
				gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
			
				double[] carray = null;
				if (colorBind == ElementBinding.PER_VERTEX) {
					carray = vertexColors.toDoubleArray(null);
					ByteBuffer cb = ByteBuffer.allocateDirect(8*carray.length).order(ByteOrder.nativeOrder());
					cb.asDoubleBuffer().put(carray);
					cb.flip();
					gl.glColorPointer(colorLength, GL.GL_DOUBLE, 0, cb);
				} 
				if (carray != null) gl.glEnableClientState(GL.GL_COLOR_ARRAY);
				double[] narray = null;
				if (normalBind == ElementBinding.PER_VERTEX) {
					narray = vertexNormals.toDoubleArray(null);
					ByteBuffer nb = ByteBuffer.allocateDirect(8*narray.length).order(ByteOrder.nativeOrder());
					nb.asDoubleBuffer().put(narray);
					nb.flip();
					gl.glColorPointer(colorLength, GL.GL_DOUBLE, 0, nb);
				} 
				if (narray != null) gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
				
				for (int i = 0; i< sg.getNumFaces(); ++i)	{
					IntArray tf = sg.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
					if (colorBind == ElementBinding.PER_FACE) 		{					
						da = faceColors.item(i).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
						} else if (colorLength == 4) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
						} 
					}
					if (pickMode) {
						//System.out.print("+G"+i+"\n");
						gl.glPushName( i);
				}
					if (normalBind == ElementBinding.PER_FACE) {
						da = faceNormals.item(i).toDoubleArray();
						gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					} 
					gl.glBegin(GL.GL_POLYGON);
					for (int j = 0; j<tf.getLength(); ++j)	{
						int k = tf.getValueAt(j);
						gl.glArrayElement(k);
						}
					gl.glEnd();
					if (pickMode) {
						//System.out.print("-");
						gl.glPopName();
					}
				}
				gl.glDisableClientState(GL.GL_COLOR_ARRAY);
				gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
				gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
				
			}	else {
		for (int i = 0; i< sg.getNumFaces(); ++i)	{
			IntArray tf = sg.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
			if (colorBind == ElementBinding.PER_FACE) 		{					
				da = faceColors.item(i).toDoubleArray();
				if (colorLength == 3) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
				} else if (colorLength == 4) 	{
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
				} 
			}
			if (pickMode) {
				//System.out.print("+G"+i+"\n");
				gl.glPushName( i);
		}
			if (normalBind == ElementBinding.PER_FACE) {
				da = faceNormals.item(i).toDoubleArray();
				gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
			} 
			gl.glBegin(GL.GL_POLYGON);
			for (int j = 0; j<tf.getLength(); ++j)	{
				int k = tf.getValueAt(j);
				if (normalBind == ElementBinding.PER_VERTEX) {
					da = vertexNormals.item(k).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				} 
				if (colorBind == ElementBinding.PER_VERTEX) {
					da = vertexColors.item(k).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha);
						} else if (colorLength == 4) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), alpha*da.getValueAt(3));
						} 
				}
				if (texCoords != null)	 {
					da = texCoords.item(k).toDoubleArray();
					gl.glTexCoord2d(da.getValueAt(0), da.getValueAt(1));
				}
				da = vertices.item(k).toDoubleArray();
				if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
			}
			gl.glEnd();
			if (pickMode) {
				//System.out.print("-");
				gl.glPopName();
			}
		}
			}
		// pop to balance the glPushName(10000) above
		if (pickMode) gl.glPopName();
		if (colorBind != ElementBinding.PER_PART)  gl.glDisable(GL.GL_COLOR_MATERIAL);
		
//		gl.glDisable(GL.GL_TEXTURE_2D);
//
//		if (renderingHints.isTransparencyEnabled())	{
//		  	gl.glDisable (GL.GL_BLEND);
//			gl.glDepthMask(true);
//		}
	}
	public void processLights(GL globalGL, List lights) {
		int lightCount =  GL.GL_LIGHT0;
		
		// collect and process the lights
		// with a peer structure we don't do this but once, and then
		// use event listening to keep our list up-to-date
		// DEBUG: see what happens if we always reuse the light list
		int n = lights.size();
		double[] zDirectiond = {0d,0d,1d,0d};
		double[] origind = {0d,0d,0d,1d};
		globalGL.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		for (int i = 0; i<8; ++i)	{
			globalGL.glDisable(GL.GL_LIGHT0+i);
		}
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) lights.get(i);
			//System.out.println("Light"+i+": "+lp.toString());
			double[] mat = lp.getMatrix(null);
			double[] mat2 = Rn.identityMatrix(4);
			double[] dir, trans;
			dir = Rn.matrixTimesVector(null, mat, zDirectiond);
			trans = Rn.matrixTimesVector(null, mat,origind);
			Pn.dehomogenize(dir, dir);
			Pn.dehomogenize(trans, trans);
			for (int j=0; j<3; ++j) mat2[4*j+2] = dir[j];
			for (int j=0; j<3; ++j) mat2[4*j+3] = trans[j];
			//System.out.println("Light matrix is: "+Rn.matrixToString(mat));
			globalGL.glPushMatrix();
			globalGL.glMultTransposeMatrixd(mat2);
			SceneGraphNode light = lp.getLastElement();
			if (light instanceof SpotLight)		wisit((SpotLight) light, globalGL, lightCount);
			else if (light instanceof PointLight)		wisit((PointLight) light, globalGL, lightCount);
			else if (light instanceof DirectionalLight)		wisit((DirectionalLight) light,globalGL, lightCount);
			else System.out.println("Invalid light class "+light.getClass().toString());
			globalGL.glPopMatrix();
			lightCount++;
			if (lightCount > GL.GL_LIGHT7)	{
			  	System.out.println("Max. # lights exceeded");
			  	break;
			}
		}
	}
	
	private static float[] zDirection = {0,0,1,0};
	private static float[] mzDirection = {0,0,1,0};
	private static float[] origin = {0,0,0,1};
	public static void wisit(Light dl, GL globalGL, int lightCount)	{
		  //System.out.println("Visiting directional light");
		  //gl.glLightfv(lightCount, GL.GL_AMBIENT, lightAmbient);
		  globalGL.glLightfv(lightCount, GL.GL_DIFFUSE, dl.getScaledColorAsFloat());
		  float f = (float) dl.getIntensity();
		  float[] specC = {f,f,f};
		  globalGL.glLightfv(lightCount, GL.GL_SPECULAR, specC);
		  //gl.glLightfv(lightCount, GL.GL_SPECULAR, white);	
	}
	
	public static void wisit(DirectionalLight dl, GL globalGL, int lightCount)		{
		  wisit( (Light) dl, globalGL, lightCount);
		  globalGL.glLightfv(lightCount, GL.GL_POSITION, zDirection);
		  globalGL.glEnable(lightCount);
		  lightCount++;
	}
	
	public static  void wisit(PointLight dl, GL globalGL, int lightCount)		{
		  if (lightCount >= GL.GL_LIGHT7)	{
		  	System.out.println("Max. # lights exceeded");
		  	return;
		  }
		  //gl.glLightfv(lightCount, GL.GL_AMBIENT, lightAmbient);
		  wisit((Light) dl, globalGL, lightCount);
		  globalGL.glLightfv(lightCount, GL.GL_POSITION, origin);
		  globalGL.glLightf(lightCount, GL.GL_CONSTANT_ATTENUATION, (float) dl.getFalloffA0());
		  globalGL.glLightf(lightCount, GL.GL_LINEAR_ATTENUATION, (float) dl.getFalloffA1());
		  globalGL.glLightf(lightCount, GL.GL_QUADRATIC_ATTENUATION, (float) dl.getFalloffA2());
		  if (!(dl instanceof SpotLight)) 	{
			  	globalGL.glEnable(lightCount);
		  		lightCount++;
		  }
	}
	
	public static void wisit(SpotLight dl, GL globalGL, int lightCount)		{
		  if (lightCount >= GL.GL_LIGHT7)	{
		  	System.out.println("Max. # lights exceeded");
		  	return;
		  }
		  PointLight pl = dl;
		  wisit(pl, globalGL, lightCount);
		  globalGL.glLightf(lightCount, GL.GL_SPOT_CUTOFF, (float) ((180.0/Math.PI) * dl.getConeAngle()));
		  globalGL.glLightfv(lightCount, GL.GL_SPOT_DIRECTION, mzDirection);
		  globalGL.glLightf(lightCount, GL.GL_SPOT_EXPONENT, (float) dl.getDistribution());
		  globalGL.glEnable(lightCount);
		  lightCount++;
	}
	
	static double[] clipPlane = {0d, 0d, -1d, 0d};
	
	public static void wisit(ClippingPlane cp, GL globalGL, int which)	{
		
		globalGL.glClipPlane(which, clipPlane);
		globalGL.glEnable(which);
	}

	/**
	 * 
	 */
	public void processClippingPlanes(GL globalGL, List clipPlanes) {
		
		int clipCount = 0;
		int clipBase = GL.GL_CLIP_PLANE0;
		// collect and process the lights
		// with a peer structure we don't do this but once, and then
		// use event listening to keep our list up-to-date
		// DEBUG: see what happens if we always reuse the light list
		int n = clipPlanes.size();
		//globalGL.glDisable(GL.GL_CLIP_PLANE0);
		for (int i = 0; i<n; ++i)	{
			SceneGraphPath lp = (SceneGraphPath) clipPlanes.get(i);
			//System.out.println("Light"+i+": "+lp.toString());
			double[] mat = lp.getMatrix(null);
			globalGL.glPushMatrix();
			globalGL.glMultTransposeMatrixd(mat);
			SceneGraphNode cp = lp.getLastElement();
			if (cp instanceof ClippingPlane)		wisit((ClippingPlane) cp, globalGL, clipBase+i);
			else System.out.println("Invalid clipplane class "+cp.getClass().toString());
			globalGL.glPopMatrix();
		}
	}
	/**
	 * @param globalGL
	 * @param file
	 */
	public static void saveScreenShot(GLDrawable drawable, File file) {
			 
			int width = drawable.getSize().width; 
			int height = drawable.getSize().height; 
			//TODO figure out why channels = 4 doesn't work: transparency getting written into fb even
			// though transparency disabled.
			 int channels = 3;
			ByteBuffer pixelsRGBA = BufferUtils.newByteBuffer(width * height * channels); 
			 
			GL gl = drawable.getGL(); 
			
			gl.glReadBuffer(GL.GL_BACK); 
			gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1); 
			
			gl.glReadPixels(0, 	// GLint x 
					0, // GLint y 
			width,// GLsizei width 
			height, // GLsizei height 
			channels == 3 ? GL.GL_RGB : GL.GL_RGBA, // GLenum format 
			GL.GL_UNSIGNED_BYTE, // GLenum type 
			pixelsRGBA); // GLvoid *pixels 
			
			int[] pixelInts = new int[width * height]; 
			
			// Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the 
			// rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL. 
	
		int p = width * height * channels; // Points to first byte (red) in each row. 
			int q;   // Index into ByteBuffer 
			int i = 0;   // Index into target int[] 
			int w3 = width*channels;    // Number of bytes in each row 
			
			for (int row = 0; row < height; row++) { 
			p -= w3; 
			q = p; 
			for (int col = 0; col < width; col++) { 
			 int iR = pixelsRGBA.get(q++); 
			int iG = pixelsRGBA.get(q++); 
			int iB = pixelsRGBA.get(q++); 
			int iA = (channels == 3) ? 0xff : pixelsRGBA.get(q++); 
			
			   pixelInts[i++] =  
			      ((iA & 0x000000FF) << 24) 
			     | ((iR & 0x000000FF) << 16) 
			     | ((iG & 0x000000FF) << 8) 
			     | (iB & 0x000000FF); 
			  } 
			 
			 } 
			 
			 BufferedImage bufferedImage = 
			  new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
			 
			 bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width); 
			 
			 try { 
			  ImageIO.write(bufferedImage, "PNG", file); 
			 } catch (IOException e) { 
			  e.printStackTrace(); 
			 } 
			 
		System.out.println("Screenshot saved to "+file.getName());
	}

}
