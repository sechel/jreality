/*
 * Created on Aug 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import java.awt.Color;
import java.util.List;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
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

	static float[] backgroundColor = {0f, 0f, 0f, 1f};
	static float [] bg = {0f, 0f, 0f, 1f};
	static int[] sphereDLists = null;
	static PolygonShader dps = null;
	static boolean useQuadMesh = true;
	public static void setupSphereDLists(JOGLRenderer jr)	{
		GLCanvas theCanvas = jr.theCanvas;
		GL gl = theCanvas.getGL();
		if (sphereDLists != null) return;
		//dps = new DefaultPolygonShader();
		EffectiveAppearance eap = EffectiveAppearance.create();
		eap = eap.create(SphereHelper.pointAsSphereAp);
		dps =ShaderLookup.getPolygonShaderAttr(eap, "", CommonAttributes.POLYGON_SHADER);		
		int n = SphereHelper.spheres.length;
		sphereDLists= new int[n];
		for (int i = 0; i<n; ++i)	{
			sphereDLists[i] = gl.glGenLists(1);
			gl.glNewList(sphereDLists[i], GL.GL_COMPILE);
			if (useQuadMesh) {
				QuadMeshShape qms = SphereHelper.cubePanels[i];
				for (int j = 0; j<SphereHelper.cubeSyms.length; ++j)	{
					gl.glPushMatrix();
					gl.glMultTransposeMatrixd(SphereHelper.cubeSyms[j].getMatrix());
					drawFaces(qms, theCanvas, false, true);
					gl.glPopMatrix();
				}				
			} else {
				drawFaces(SphereHelper.spheres[i], theCanvas, false, true);
			}
			gl.glEndList();
		}
	}
	
	/**
	 * @param i
	 * @return
	 */
	public static int getSphereDLists(int i, JOGLRenderer jr) {
		if (sphereDLists == null) setupSphereDLists(jr);
		return sphereDLists[i];
	}

	public static void handleBackground(GLCanvas theCanvas, Appearance topAp)	{
			GL gl = theCanvas.getGL();
			Object bgo = null;
			
			for (int i = 0; i<8; ++i)	{
				gl.glDisable(i+GL.GL_CLIP_PLANE0);
			}
			//TODO replace BackPlane class with simple quad drawn here, keyed to "backgroundColors" in topAp
			if (topAp != null)	bgo = topAp.getAttribute(CommonAttributes.BACKGROUND_COLOR);
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
	public static void drawVertices( PointSet sg, JOGLRenderer jr, boolean drawSpheres, double pointRadius) {
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
			if (sphereDLists == null) setupSphereDLists(jr);
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
						gl.glColor3d( da.getValueAt(0), da.getValueAt(1),  da.getValueAt(2));
					} else if (colorLength == 4) 	{
						gl.glColor4d(da.getValueAt(0),  da.getValueAt(1),  da.getValueAt(2), da.getValueAt(3));
					} 						
				}
				gl.glCallList(sphereDLists[1]);
				gl.glPopMatrix();
			}
		} else {
			gl.glBegin(GL.GL_POINTS);
			for (int i = 0; i< sg.getNumPoints(); ++i)	{
				double vv = 1.0f;
				if (pointSize != null) {
					float ps = (float) pointSize.item(i).toDoubleArray().getValueAt(0);
					gl.glPointSize( ps);
					vv =  (ps < 1) ? ps : (1d - (Math.ceil(ps) - ps) * 0.25d);

				}
				//if (pointSize != null)	gl.glBegin(GL.GL_POINTS);
				if (vertexColors != null)	{
					da = vertexColors.item(i).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor3d( da.getValueAt(0), da.getValueAt(1),  da.getValueAt(2));
					} else if (colorLength == 4) 	{
						gl.glColor4d( da.getValueAt(0), da.getValueAt(1),da.getValueAt(2), da.getValueAt(3));
					} 						
				}
				da = vertices.item(i).toDoubleArray();				
				if (vertexLength == 3) gl.glVertex3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
				else if (vertexLength == 4) gl.glVertex4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), da.getValueAt(3));;
			}
			gl.glEnd();
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
	public static void drawLines(IndexedLineSet sg, GLCanvas theCanvas,JOGLRenderer.JOGLPeerComponent jpc, boolean pickMode) {
		GL gl = theCanvas.getGL();
//		DefaultGeometryShader currentGeometryShader = jpc.geometryShader;
//		RenderingHintsShader renderingHints = jpc.renderingHints;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		int vertexLength = GeometryUtility.getVectorLength(vertices);
		DataList colors = sg.edgeAttributes.getList(Attribute.COLORS);

		int[][] indices = null;
		IntArrayArray foo = sg.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
		if (foo != null) indices = foo.toIntArrayArray(null);
		if (indices == null)	{
			if (sg instanceof IndexedFaceSet)	indices = ((IndexedFaceSet) sg).getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		} 
		
		if (indices == null) {
			//return;
		}
		// vertex color has priority over face color
		// should also check for override behavior
		//if (vc != null) 		colorBind = ElementBinding.PER_VERTEX;
		int colorBind = 0;
		if (colors != null) 	colorBind = ElementBinding.PER_EDGE;
		else 				colorBind = ElementBinding.PER_PART;
		
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
	}

	/**
	 * @param sg
	 */
	public static void drawFaces( IndexedFaceSet sg, GLCanvas theCanvas, boolean pickMode, boolean smooth) {

		//if (jr.pickMode && (insidePointSet || insideLineSet)) return;

		//DefaultGeometryShader currentGeometryShader = jpc.geometryShader;
		//RenderingHintsShader renderingHints = jpc.renderingHints;
		GL gl = theCanvas.getGL();
		int colorBind,normalBind, colorLength=3;
		DataList vertices = sg.getVertexAttributes(Attribute.COORDINATES);
		DataList vertexNormals = sg.getVertexAttributes(Attribute.NORMALS);
		DataList faceNormals = sg.getFaceAttributes(Attribute.NORMALS);
		DataList vertexColors = sg.getVertexAttributes(Attribute.COLORS);
		DataList faceColors = sg.getFaceAttributes(Attribute.COLORS);
		DataList texCoords = sg.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
		//System.out.println("Vertex normals are: "+((vertexNormals != null) ? vertexNormals.size() : 0));
		//System.out.println("Face normals are: "+((faceNormals != null) ? faceNormals.size() : 0));
		
		
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

}
