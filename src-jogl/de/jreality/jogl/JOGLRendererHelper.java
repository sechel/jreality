/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import java.awt.Color;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.RegularDomainQuadMesh;
import de.jreality.geometry.SphereHelper;
import de.jreality.jogl.shader.DefaultGeometryShader;
import de.jreality.jogl.shader.DefaultLineShader;
import de.jreality.jogl.shader.DefaultPointShader;
import de.jreality.jogl.shader.DefaultPolygonShader;
import de.jreality.jogl.shader.RenderingHintsShader;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.Texture2D;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.util.Pn;
import de.jreality.util.Rn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JOGLRendererHelper {

	static float[] backgroundColor = {0f, 0f, 0f, 1f};
	static float [] bg = {0f, 0f, 0f, 1f};
	public static void handleBackground(GLCanvas theCanvas, Appearance topAp)	{
			GL gl = theCanvas.getGL();
			Object bgo = null;
			
			//TODO replace BackPlane class with simple quad drawn here, keyed to "backgroundColors" in topAp
			if (topAp != null)	bgo = topAp.getAttribute("backgroundColor");
			if (bgo != null && bgo instanceof java.awt.Color) bg = ((java.awt.Color) bgo).getComponents(null);
			else bg = backgroundColor;
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
		gl.glEnable(GL.GL_MULTISAMPLE_ARB);	
		boolean doFog = false;
		if (doFog)	{
			gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
			float[] fogcolor = {.5f, .5f, .5f, 1.0f};
			gl.glEnable(GL.GL_FOG);
			gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
			gl.glFogfv(GL.GL_FOG_COLOR, bg);
			gl.glFogf(GL.GL_FOG_DENSITY, .4f);
		}
		// do the lights, etc.
		// these will be done soon in visit(Material m), visit(Appearance ap), visit(Light l)
		 {
		  float[] ambient = {(float) 0.1,(float) 0.1,(float).1};
		  float[] diffuse = {(float)0.8,(float).1,(float).4, (float) .5};
		  float[] white = {(float)1,(float)1,(float)1};

		  gl.glShadeModel(GL.GL_SMOOTH);  // Enable Smooth Shading
		  gl.glEnable(GL.GL_NORMALIZE);
		  //gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
		  gl.glColorMaterial(GL.GL_FRONT, GL.GL_DIFFUSE);
		  gl.glEnable(GL.GL_COLOR_MATERIAL);
 		
		  //gl.glEnable(GL.GL_LIGHT_MODEL_TWO_SIDE);
//		  gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse);
//		  gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, white);
//		  gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, (float) 100.0);
		
		  float[] position = {(float)2,(float)2,(float)-2};
		  float[] lightAmbient = {0.1f,0.0f,0.0f, 1.0f};
		  float[] lightDiffuse = {0.8f, 0.8f, 1.0f, 1.0f};

		  float[] lightPosition = {1.0f, 1.0f, 1.0f, 0.0f};
		  int cl = GL.GL_LIGHT0;
		  gl.glLightfv(cl, GL.GL_AMBIENT, lightAmbient);
		  gl.glLightfv(cl, GL.GL_DIFFUSE, lightDiffuse);
		  gl.glLightfv(cl, GL.GL_SPECULAR, white);
		  //gl.glLightf(cl, GL.GL_CONSTANT_ATTENUATION, (float)1.0);
		  gl.glLightfv(cl, GL.GL_POSITION, lightPosition);
		  //gl.glEnable(cl);
		 }
		 {
		  float[] white = {(float).8,(float).8,(float).2};
		  float[] lightAmbient = {0.1f, 0.1f, 0.1f, 1.0f};
		  float[] lightDiffuse = {.9f, .9f, .4f, 1.0f};
		  float[] lightPosition = {-2.0f, -1.0f, 5.0f, 0.0f};

		
		
		  int cl = GL.GL_LIGHT1;
		  gl.glLightfv(cl, GL.GL_AMBIENT, lightAmbient);
		  gl.glLightfv(cl, GL.GL_DIFFUSE, lightDiffuse);
		  gl.glLightfv(cl, GL.GL_SPECULAR, white);
		  //gl.glLightf(cl, GL.GL_CONSTANT_ATTENUATION, (float)1.0);
		  gl.glLightfv(cl, GL.GL_POSITION, lightPosition);
		  //gl.glEnable(cl);
		 }
		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
		gl.glDisable(GL.GL_CLIP_PLANE0);
	
	}
	public static void drawVertices( PointSet sg, GLCanvas theCanvas, DefaultGeometryShader currentGeometryShader, RenderingHintsShader renderingHints, 
			JOGLRenderer jr, boolean pickMode) {
		GL gl = theCanvas.getGL(); 
		DefaultPointShader pts = (DefaultPointShader) currentGeometryShader.getPointShader();
		if (pts != null) gl.glPointSize((float) pts.getPointSize());
		if (renderingHints.isAntiAliasingEnabled())	{
		  gl.glEnable (GL.GL_POINT_SMOOTH);
		  gl.glEnable (GL.GL_BLEND);
		  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		  gl.glHint (GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
		}
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList pointSize = sg.getVertexAttributes(Attribute.POINT_SIZE);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		int colorLength = 0;
		if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
		DoubleArray da;
		if (true)	{
			if (pts != null) gl.glColor4fv(pts.getDiffuseColorAsFloat());
		} 	
		if (currentGeometryShader.isVertexDraw()) {
			if (currentGeometryShader.pointShader.isSphereDraw())	{
				double size = currentGeometryShader.pointShader.getPointRadius();
				Appearance ap = SphereHelper.pointAsSphereAp;
				//ap.setAttribute(CommonAttributes.CommonAttributes.DIFFUSE_COLOR, currentGeometryShader.pointShader.getDiffuseColor());
				jr.visit(ap);
				//Sphere sph = new Sphere();
				for (int i = 0; i< sg.getNumPoints(); ++i)	{
					da = vertices.item(i).toDoubleArray();				
					gl.glPushMatrix();
					gl.glTranslated(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					gl.glScaled(size, size, size);
					//visit(sph);
					jr.visit(SphereHelper.newspheres[1]);
					gl.glPopMatrix();
				}
			} else {
				gl.glDisable(GL.GL_LIGHTING);
				if (renderingHints.isAntiAliasingEnabled())	{
					  gl.glEnable (GL.GL_POINT_SMOOTH);
					  gl.glEnable (GL.GL_BLEND);
					  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					  gl.glHint (GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
					}
				else 
						gl.glDisable(GL.GL_POINT_SMOOTH);
				// draw the points slightly in front of the rest (the faces)	
				// TODO pull this out and make it accessible to calibration		
				//gl.glDepthRange(0.0d, 0.9999d);
				if (pts !=null) gl.glPointSize((float) pts.getPointSize());
				if (pointSize == null) gl.glBegin(GL.GL_POINTS);
				for (int i = 0; i< sg.getNumPoints(); ++i)	{
					double vv = 1.0f;
					if (pointSize != null) {
						float ps = (float) pointSize.item(i).toDoubleArray().getValueAt(0);
						gl.glPointSize( ps);
						vv =  (ps < 1) ? ps : (1d - (Math.ceil(ps) - ps) * 0.25d);

					}
					if (pointSize != null)	gl.glBegin(GL.GL_POINTS);
					if (vertexColors != null)	{
						da = vertexColors.item(i).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor3d(vv * da.getValueAt(0), vv * da.getValueAt(1), vv * da.getValueAt(2));
						} else if (colorLength == 4) 	{
							gl.glColor4d(vv * da.getValueAt(0), vv * da.getValueAt(1), vv  * da.getValueAt(2), da.getValueAt(3));
						} 						
					}
					da = vertices.item(i).toDoubleArray();				
					if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
					else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));;
				}
				if (pointSize == null) gl.glEnd();
				//gl.glDepthRange(0d, 1d);		
				if (renderingHints.isLightingEnabled()) gl.glEnable(GL.GL_LIGHTING);
				if (renderingHints.isAntiAliasingEnabled())	{
					gl.glDisable (GL.GL_POINT_SMOOTH);
					gl.glDisable (GL.GL_BLEND);
				}

			}
		}
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
		if (renderingHints.isLightingEnabled()) gl.glEnable(GL.GL_LIGHTING);
		if (renderingHints.isAntiAliasingEnabled())	{
			gl.glDisable (GL.GL_LINE_SMOOTH);
			gl.glDisable (GL.GL_BLEND);
		}
	}
	/**
	 * @param sg
	 */
	public static void drawLines(IndexedLineSet sg, GLCanvas theCanvas, DefaultGeometryShader currentGeometryShader, 
			RenderingHintsShader renderingHints, boolean insidePointSet, boolean pickMode) {
		GL gl = theCanvas.getGL();
		int colorBind;
		DefaultLineShader dls = ((DefaultLineShader) currentGeometryShader.getLineShader());
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		DataList colors = sg.edgeAttributes.getList(Attribute.COLORS);

		int[][] indices = null;
		IntArrayArray foo = sg.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
		if (foo != null) indices = foo.toIntArrayArray(null);
		int lineMode = GL.GL_LINE_STRIP;

		if (indices == null)	{
			if (sg instanceof IndexedFaceSet)	indices = ((IndexedFaceSet) sg).getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
			lineMode = GL.GL_LINE_LOOP;
		} 
		
		if (indices == null) {
			//return;
		}
		// vertex color has priority over face color
		// should also check for override behavior
		//if (vc != null) 		colorBind = ElementBinding.PER_VERTEX;
		if (colors != null) 	colorBind = ElementBinding.PER_EDGE;
		else 				colorBind = ElementBinding.PER_PART;
		
		if (renderingHints.isAntiAliasingEnabled())	{
		  gl.glEnable (GL.GL_LINE_SMOOTH);
		  gl.glEnable (GL.GL_BLEND);
		  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		  gl.glHint (GL.GL_LINE_SMOOTH_HINT, GL.GL_NICEST);
		}
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDepthRange(0.0d, 0.9999d);
		//if (colorBind == ElementBinding.PER_PART)	{
			if (dls != null) gl.glColor4fv(dls.getDiffuseColorAsFloat());
		//} 	
		if (dls != null) gl.glLineWidth((float) dls.getLineWidth());
		if (((DefaultLineShader) currentGeometryShader.getLineShader()).isLineStipple()) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			if (dls != null) gl.glLineStipple(dls.getLineFactor(), (short) dls.getLineStipplePattern());
			} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);
		//System.out.println("Stippling is "+currentGeometryShader.lineShader.isLineStipple());
		DoubleArray da;
		// TODO support for colors per vertex?
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
		gl.glDepthRange(0d, 1d);
		if (renderingHints.isLightingEnabled()) gl.glEnable(GL.GL_LIGHTING);
		if (renderingHints.isAntiAliasingEnabled())	{
			gl.glDisable (GL.GL_LINE_SMOOTH);
			gl.glDisable (GL.GL_BLEND);
		}
	}

	/**
	 * @param sg
	 */
	public static void drawFaces(JOGLRenderer jr, IndexedFaceSet sg, GLCanvas theCanvas, DefaultGeometryShader currentGeometryShader, 
			RenderingHintsShader renderingHints, boolean insidePointSet, boolean insideLineSet, boolean pickMode) {

		if (pickMode && (insidePointSet || insideLineSet)) return;

		GL gl = theCanvas.getGL();
		int colorBind,normalBind, colorLength=3, coordLength=3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		DefaultPolygonShader dps = ((DefaultPolygonShader) currentGeometryShader.getPolygonShader());
		//double[][] vv = null, vn=null, fn=null, vc=null, fc=null, tc = null;
		// signal a geometry
		if (pickMode)	gl.glPushName(10000);
		
		if (texCoords != null && dps!=null && dps.isTextureEnabled())	{
			Texture2DLoaderJOGL tl = Texture2DLoaderJOGL.FactoryLoader;
			Object tex =  dps.getTexture2D();
			if (tex instanceof Texture2D)		{
				tl.bindTexture2D(theCanvas, (Texture2D) tex);
				int[] res = new int[1];
				gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
				//System.out.println("Texture is resident: "+res[0]);
				if (res[0] == 0)	{ jr.gri.texResident = false; }
			}
			else texCoords = null;
			gl.glEnable(GL.GL_TEXTURE_2D);			
		} else
			texCoords = null;
		// vertex color has priority over face color
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		boolean shadePerFace = false;
		if (!dps.isSmoothShading() || dps.isSmoothShading() && vertexNormals == null) 	{
			shadePerFace = true;
		}

		if (vertexColors != null && !(faceColors != null && shadePerFace)) 		{
			colorBind = ElementBinding.PER_VERTEX;
			colorLength = GeometryUtility.getVectorLength(vertexColors);
		} 
		else if (faceColors != null) 	{
			colorBind = ElementBinding.PER_FACE;
			colorLength = GeometryUtility.getVectorLength(faceColors);
		} 
		else 	colorBind = ElementBinding.PER_PART;
		
		
		if (renderingHints.isLightingEnabled())	{
			if (dps.isSmoothShading()  )	{
				normalBind = ElementBinding.PER_VERTEX;
				if (vertexNormals == null) {
					System.err.println("JOGL renderer: No vertex normals");
					normalBind = ElementBinding.PER_PART;
				}
			}
			/* TODO figure out this behavior
			 * The following code gives strange results; only one side of the surface appears lit
			 * in contrast to using the vertex normals (above).
			 */
			 else	{
				normalBind = ElementBinding.PER_FACE;
				if (faceNormals == null) {
					normalBind = ElementBinding.PER_PART;
				}
			}
		}
		else {
			normalBind = ElementBinding.PER_PART;
		} 
		if (renderingHints.isTransparencyEnabled())	{
		  gl.glEnable (GL.GL_BLEND);
		  gl.glDepthMask(false);
		  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		}
		if (colorBind == ElementBinding.PER_PART) {
			//float[] color = currentGeometryShader.polygonShader.getDiffuseColorAsFloat();
			DefaultPointShader pts = ((DefaultPointShader) currentGeometryShader.getPointShader());
			DefaultLineShader dls = ((DefaultLineShader) currentGeometryShader.getLineShader());
			if (pts != null && insidePointSet) gl.glColor4fv(pts.getDiffuseColorAsFloat());
			else if (dls != null && insideLineSet) gl.glColor4fv(dls.getDiffuseColorAsFloat());
			else if (dps != null) {
				gl.glColor4fv(((DefaultPolygonShader) currentGeometryShader.getPolygonShader()).getDiffuseColorAsFloat());
				//System.out.println("JOGLRH: Setting diffuse color to : "+currentGeometryShader.polygonShader.getDiffuseColor().toString());
			}
		}
		DoubleArray da;
		if (dps.isSmoothShading()) 	
			gl.glShadeModel(GL.GL_SMOOTH);
		else											
			gl.glShadeModel(GL.GL_FLAT);
		// Fast mode for picking doesn't make sense; it isn't possible to resolve finer than the deepest glBegin/glEnd block,
		// which in this case is a whole QUAD_STRIP.  Render the slow way instead and get actual polygon info.
		if (!pickMode && sg instanceof QuadMeshShape)	{
			QuadMeshShape qm = (QuadMeshShape) sg;
			RegularDomainQuadMesh rdqm = null;
			int type = Pn.EUCLIDEAN;
			int faceCount = 0;
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
									gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
								}
								else if (colorLength == 4) 	{
									gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
								}
							}
						} else
						if (colorBind == ElementBinding.PER_VERTEX) {
							da = vertexColors.item(vnn).toDoubleArray();
							if (colorLength == 3) 	{
								gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
							} else if (colorLength == 4) 	{
								gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
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
		for (int i = 0; i< sg.getNumFaces(); ++i)	{
			IntArray tf = sg.getFaceAttributes(Attribute.INDICES).item(i).toIntArray();
			if (colorBind == ElementBinding.PER_FACE) 		{					
				da = faceColors.item(i).toDoubleArray();
				if (colorLength == 3) 	{
					gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				}
				else if (colorLength == 4) 	{//gl.glColor4dv(fc[i]);
					gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
				}
			}
			if (pickMode) {
				//System.out.print("+G"+i+"\n");
				gl.glPushName( i);
		}
			gl.glBegin(GL.GL_POLYGON);
			if (normalBind == ElementBinding.PER_FACE) {
				da = faceNormals.item(i).toDoubleArray();
				gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
			} 
			for (int j = 0; j<tf.getLength(); ++j)	{
				int k = tf.getValueAt(j);
				if (normalBind == ElementBinding.PER_VERTEX) {
					da = vertexNormals.item(k).toDoubleArray();
					gl.glNormal3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				} 
				if (colorBind == ElementBinding.PER_VERTEX) {
					da = vertexColors.item(k).toDoubleArray();
						if (colorLength == 3) 	{
							gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
						} else if (colorLength == 4) 	{
							gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));
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
		// pop to balance the glPushName(10000) above
		if (pickMode) gl.glPopName();
		
		gl.glDisable(GL.GL_TEXTURE_2D);

		if (renderingHints.isTransparencyEnabled())	{
		  	gl.glDisable (GL.GL_BLEND);
			gl.glDepthMask(true);
		}
	}

}
