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


package de.jreality.jogl.pick;


import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLViewer;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.pick.Graphics3D;
/**
 * @author gunn
 *
 */
public class JOGLPickAction extends PickAction  {
	public static int SGCOMP_BASE	= 20010;
	public static int GEOMETRY_BASE	= 20000;
	static public int GEOMETRY_POINT = GEOMETRY_BASE;
	static public int GEOMETRY_LINE = GEOMETRY_BASE+1;
	static public int GEOMETRY_FACE = GEOMETRY_BASE+2;
	static public int PROXY_GEOMETRY_POINT = GEOMETRY_BASE+3;
	static public int PROXY_GEOMETRY_LINE = GEOMETRY_BASE+4;
	static public int PROXY_GEOMETRY_FACE = GEOMETRY_BASE+5;
	static boolean useOpenGL = true;
	static boolean debug = true;
	public JOGLPickAction(JOGLViewer v) {
		super(v);
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#init()
	 */
	
	public Object visit() {
		if (useOpenGL && theViewer instanceof de.jreality.jogl.JOGLViewer)	{
			PickPoint[] hits =  ((de.jreality.jogl.JOGLViewer)theViewer).getRenderer().performPick(pickPointNDC);	
			int n = 0;
			if (hits != null)	n = hits.length;
			pickHits = new Vector();
			for (int i =0; i<n; ++i)	pickHits.add(hits[i]);
			return pickHits;			
		} 
		return super.visit();
	}
	/**
	 * @param numberHits
	 * @param selectBuffer
	 * @return
	 */
	public static PickPoint[] processOpenGLSelectionBuffer(int numberHits, IntBuffer selectBuffer, double[] pickPoint, JOGLViewer v) {
		double factor = 1.0/(0x7fffffff);
		ArrayList al = new ArrayList();
		PickPoint oneHit = null;
		int realHits = 0;
		Graphics3D context3D = new Graphics3D(v);
		SceneGraphComponent theRoot = v.getSceneRoot();
		if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Processing gl selection buffer");
		for (int i =0, count = 0; i<numberHits; ++i)	{
			int names = selectBuffer.get(count++);
			int[] path = new int[names];
			double[] pndc = new double[3];
			pndc[0] = pickPoint[0];
			pndc[1] = pickPoint[1];
			SceneGraphPath sgp = new SceneGraphPath();
			SceneGraphComponent sgc = theRoot;
			sgp.push(theRoot);
			double z1 = selectBuffer.get(count++) * factor;
			double z2 = selectBuffer.get(count++) * factor;
			// TODO figure out why I have to add 1 to get agreement with my transformation
			pndc[2] = z1+1.0;
			if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Hit "+i+": "+z1+" - "+z2+" ");
			//boolean geometryFound = false;
			int geometryFound = -1;
			int[] geomID = {-1, -1};
			for (int j = 0; j<names; ++j)	{
				path[j] = selectBuffer.get(count);
				if (debug) JOGLConfiguration.theLog.log(Level.FINE,": "+path[j]);
				if (j>0) {
					// apparently the first thing on the selection stack is bogus
					// we look for identifiers corresponding to geomteries
					if (path[j] >= SGCOMP_BASE)	{					
						// otherwise we assume it's a scene graph component
						// and store it off
						int which = path[j] - SGCOMP_BASE;
						if (debug) JOGLConfiguration.theLog.log(Level.FINE,"?");
						if (sgc.getChildComponentCount() > which && sgc.getChildComponent(which) != null) {
							SceneGraphComponent tmpc = sgc.getChildComponent(which);
							sgp.push(tmpc); 
							sgc = tmpc;
							if (debug) JOGLConfiguration.theLog.log(Level.FINE,"("+sgc.getName()+")");
						}
					}
					else if (path[j] >= GEOMETRY_BASE)	{
						if (geometryFound == -1) {
							geometryFound = path[j];
							sgp.push(sgc.getGeometry());
						}
						else {
							JOGLConfiguration.theLog.log(Level.WARNING,"Whoa: too many geometries in the path");
						}
					} 	
					else if (geomID[0] == -1) geomID[0] = path[j];	
					else if (geomID[1] == -1) geomID[1]= path[j];
				}
				count++;
			}
			if (debug) JOGLConfiguration.theLog.log(Level.FINE,"\n");
			SceneGraphNode sgn = sgp.getLastElement();
			if (geometryFound != -1)	{
//				JOGLConfiguration.theLog.log(Level.FINE,"\ngeometry found "+geometryFound);
//				JOGLConfiguration.theLog.log(Level.FINE,"geoemtry is"+sgn.getName());	
			}
			else continue;
			if (!(sgn instanceof Geometry))	continue;
			Geometry geom = (Geometry) sgn;
			context3D.setObjectToWorld(sgp.getMatrix(null));
			if (geometryFound == GEOMETRY_FACE && (geom instanceof IndexedFaceSet))	{
				if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Picked face "+ geomID[0]);
				IndexedFaceSet sg = (IndexedFaceSet) sgn;
				oneHit = calculatePickPointForFace(oneHit, pndc, context3D,sgp, sg, geomID);
				if (oneHit == null) continue;
				al.add(oneHit);
				realHits++;
			} 
			else if (geometryFound == GEOMETRY_LINE  && (geom instanceof IndexedLineSet))	{
				if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Picked edge "+ geomID[0]+" "+geomID[1]);
				IndexedLineSet sg = (IndexedLineSet) geom;
				oneHit = calculatePickPointForEdge(oneHit, pndc, context3D, sgp, sg,geomID);
				if (oneHit == null) continue;
				al.add(oneHit);
				realHits++;					
			} else if (geometryFound == GEOMETRY_POINT && (geom instanceof PointSet))	{
				PointSet sg = (PointSet) geom;
				if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Picked vertex "+geomID[0]);
				oneHit = calculatePickPointForVertex(oneHit, pndc, context3D,sgp, sg, geomID);
				if (oneHit == null) continue;
				al.add(oneHit);
				realHits++;
			} else if (geom instanceof Sphere)	{
				// first transform the pick line into object coordinates
				double[] pndc1 = new double[4], pndc2 = new double[4];
				pndc1[0] = pndc2[0] = pndc[0];
				pndc1[1] = pndc2[1] = pndc[1];
				pndc1[2] = 0.0;
				pndc2[2] = 1.0;
				pndc1[3] = pndc2[3] = 1.0;
				double[] ndc2o = context3D.getNDCToObject();
				double[] pt0 = Rn.matrixTimesVector(null, ndc2o, pndc1);
				double[] pt1 = Rn.matrixTimesVector(null, ndc2o, pndc2);
				double[] hp0 = new double[3];
				double[] hp1 = new double[3];
				Pn.dehomogenize(hp0, pt0);
				Pn.dehomogenize(hp1, pt1);
				double q = Rn.innerProduct(hp0, hp0);
				double r = Rn.innerProduct(hp0, hp1);
				double s = Rn.innerProduct(hp1, hp1);
				double a = s - 2 * r + q;
				double b = 2 * (r - q);
				double c = q - 1.0;
				double d = b*b - 4 * a * c;
				if (d < 0) {
					JOGLConfiguration.theLog.log(Level.WARNING,"Missed sphere");
					continue;
				}
				d = Math.sqrt(d);
				double x[] = new double[2];
				x[0] = (-b + d)/(2 * a);
				x[1] = (-b - d)/(2 * a);
	
				double[][] opt = new double[2][];
				double[][] ndcpt = new double[2][];
				double[] o2ndc = Rn.inverse(null, ndc2o);
				for (int k = 0; k<2; ++k)	{
					opt[k] = Rn.linearCombination(null, 1.0 - x[k], hp0, x[k], hp1);
					ndcpt[k] = Rn.matrixTimesVector(null, o2ndc, opt[k]);
					//Pn.dehomogenize(ndcpt[k], ndcpt[k]);
					oneHit = PickPoint.PickPointFactory(sgp, v.getCameraPath(), ndcpt[k]);
					//oneHit.setPointObject(opt[k]);				
					//oneHit.setPointNDC(ndcpt[k]);
					//oneHit.setPickPath( (SceneGraphPath) sgp.clone());
					//oneHit.setContext(context3D.copy());
					oneHit.setPickType(PickPoint.HIT_FACE);  // TODO not really a face;  HIT_PRIMITIVE ?
					al.add(oneHit);
					realHits++;
				}
			} else {
				JOGLConfiguration.theLog.log(Level.WARNING,"Invalid geometry type");
				continue; 			
			}
		}
		PickPoint[] hits = new PickPoint[realHits];
		hits =  (PickPoint[]) al.toArray(hits);
		Comparator cc = PickPointComparator.sharedInstance;
		Arrays.sort(hits, cc);
		return hits;
	}
	protected static PickPoint calculatePickPointForVertex(PickPoint dst, double[] pndc, Graphics3D gc, SceneGraphPath sgp, PointSet sg, int[] geomID)	{
		DataList verts = sg.getVertexAttributes(Attribute.COORDINATES);
		if (geomID[0] >= verts.size())	{
			JOGLConfiguration.theLog.log(Level.WARNING,"Invalid vertex number in calculatePickPointFor()");
			return null;
		}
		double[] realNDC = Rn.matrixTimesVector(null,gc.getObjectToNDC(), verts.item(geomID[0]).toDoubleArray(null));
		if (realNDC.length == 4) Pn.dehomogenize(realNDC, realNDC);
		realNDC[2] = pndc[2];
		if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Real and theoretical z-value: "+pndc[2]+"  "+realNDC[2]);
		PickPoint pp = PickPoint.PickPointFactory(sgp, gc.getCameraPath(),  realNDC);
		pp.setVertexNum(geomID[0]);
		pp.setPickType(PickPoint.HIT_VERTEX);
		return pp;
	}
	protected static PickPoint calculatePickPointForEdge(PickPoint dst, double[] pndc, Graphics3D gc, SceneGraphPath sgp, IndexedLineSet sg, int[] geomID)	{
		int[][] indices = sg.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		DataList verts = sg.getVertexAttributes(Attribute.COORDINATES);
		if (geomID[0] >= indices.length)	{
			JOGLConfiguration.theLog.log(Level.WARNING,"Invalid edge number in calculatePickPointFor()");
			return null;
		}
		int n = indices[geomID[0]].length;
		//pndc[2] = 1.0;
//		double[][] oneEdge = new double[n][], oneEdgeP2 = new double[n][3];
//		for (int j = 0; j<indices[geomID[0]].length; ++j)	{
//				oneEdge[j] = verts.item(indices[geomID[0]][j]).toDoubleArray(null);
//		}
//		Rn.matrixTimesVector(oneEdge,gc.getObjectToNDC(), oneEdge);
//		if (oneEdge[0].length == 4) Pn.dehomogenize(oneEdge, oneEdge);
//		for (int j = 0; j<indices[geomID[0]].length; ++j)	{
//			oneEdgeP2[j][0] = oneEdge[j][0]; 
//			oneEdgeP2[j][1] = oneEdge[j][1]; 
//			oneEdgeP2[j][2] = 1.0; 
//		}
//		double[][] lines = new double[n-1][3];
//		double min = 10E20;
//		double[] distances = new double[n-1];
//		int index = 0;
//		for (int i = 0; i<n-1; ++i)	{
//			P2.lineFromPoints(lines[i], oneEdgeP2[i], oneEdgeP2[i+1]);
//			distances[i] = Math.abs(Rn.innerProduct(pndc, lines[i]));
//			if (distances[i] < min) { min = distances[i]; index = i; }
//		}
//		double d1 = 10^20, d2=10^20;
//		double[] pickndc1 = null, pickndc2 = null;
//		double tt = 0.0, zval1 = 0, zval2=0;
//		double[] pndcp2 = {pndc[0], pndc[1], 1.0};
//		if (index > 0)	{
//			pickndc1 = P2.closestPointOnLine(null, lines[index-1],  pndcp2, Pn.EUCLIDEAN);
//			d1 = Rn.innerProduct( pndcp2, pickndc1);
//			double dx = (oneEdge[index+1][0] - oneEdge[index][0]);
//			if (dx != 0.0)	
//				tt = (pickndc1[0] - oneEdge[index][0])/dx;				
//			else 
//				tt = (pickndc1[1] - oneEdge[index][1])/(oneEdge[index+1][1] - oneEdge[index][1]);
//
//			zval1 = tt * oneEdge[index+1][2] + (1-tt)*oneEdge[index][2];
//			//JOGLConfiguration.theLog.log(Level.INFO,"d1 is "+d1);
//		}
//		if (index < n-2)	{
//			pickndc2 = P2.closestPointOnLine(null, lines[index],  pndcp2, Pn.EUCLIDEAN);
//			d2 = Rn.innerProduct( pndcp2, pickndc2);
//			double dx = (oneEdge[index+1][0] - oneEdge[index][0]);
//			if (dx != 0.0) tt = (pickndc2[0] - oneEdge[index][0])/dx;
//			else tt = (pickndc2[1] - oneEdge[index][1])/(oneEdge[index+1][1] - oneEdge[index][1]);
//			zval2 = tt * oneEdge[index+1][2] + (1-tt)*oneEdge[index][2];
//			//JOGLConfiguration.theLog.log(Level.INFO,"d2 is "+d2);
//		}
//		double zval = 0;
//		if (d1 < d2)	zval = zval1;
//		else zval = zval2;
//		if (debug) JOGLConfiguration.theLog.log(Level.FINE,"Real and theoretical z-value: "+pndc[2]+"  "+zval);
		double[] realNDC = new double[4];
		realNDC[0] = pndc[0]; realNDC[1] = pndc[1];  		realNDC[2] = pndc[2]; realNDC[3] = 1.0;
		dst = PickPoint.PickPointFactory(sgp, gc.getCameraPath(),realNDC);
		dst.setEdgeNum(geomID);
		dst.setPickType(PickPoint.HIT_EDGE);
		return dst;
	}

	
	protected static PickPoint calculatePickPointForFace(PickPoint dst, double[] pndc, Graphics3D gc, SceneGraphPath sgp, IndexedFaceSet sg, int[] geomID)	{

		int[][] indices = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		DataList verts = sg.getVertexAttributes(Attribute.COORDINATES);
		int faceNum = geomID[0];
		if (faceNum >= indices.length || faceNum < 0)	{
			JOGLConfiguration.theLog.log(Level.WARNING,"Invalid face number in calculatePickPointFor()");
			return null;
		}
		pndc[2] = 1.0;
		double[][] onePolygon = new double[indices[faceNum].length][];
		for (int j = 0; j<indices[faceNum].length; ++j)	{
				onePolygon[j] = verts.item(indices[faceNum][j]).toDoubleArray(null);
		}
		Rn.matrixTimesVector(onePolygon,gc.getObjectToNDC(), onePolygon);
		if (onePolygon[0].length == 4) Pn.dehomogenize(onePolygon, onePolygon);
		//if (P2.isConvex(onePolygon) && P2.polygonContainsPoint(onePolygon,pndc))	{
		if (P2.polygonContainsPoint(onePolygon,pndc))	{
			double[] plane = P3.planeFromPoints(null, onePolygon[0], onePolygon[1], onePolygon[2]);
			double[] p1 = (double[]) pndc.clone();
			p1[2] = 0.0;
			double[] intersect = P3.lineIntersectPlane(null, pndc, p1, plane);
			if (intersect[2] < MIN_PICKZ || intersect[2] > MAX_PICKZ) {
				JOGLConfiguration.theLog.log(Level.WARNING,"calculatePickPointFor: bad z-coordinate");
				return null;
			}
			//JOGLConfiguration.theLog.log(Level.FINE,"Intersect = "+Rn.toString(intersect));
			double[] NDCToObject = Rn.inverse(null, gc.getObjectToNDC());
			double[] objectPt = Rn.matrixTimesVector(null, NDCToObject, intersect);
			Pn.dehomogenize(objectPt, objectPt);
			dst = PickPoint.PickPointFactory(sgp,  gc.getCameraPath(),intersect);
			//dst.setPickPath( (SceneGraphPath) sgp.clone());
			//dst.setContext(gc.copy());
			//dst.setPointNDC(intersect);
			//dst.setPointObject(objectPt);
			dst.setFaceNum(faceNum);
			dst.setPickType(PickPoint.HIT_FACE);
		} else{
			//JOGLConfiguration.theLog.log(Level.FINE,"calculatePickPointFor: no hit");
			return null;
		}
		
		return dst;
	}

	// In case I want to be able to use the superclass functionality, I have to be
	// able to turn off the JOGL 
	public static boolean isUseOpenGL() {
		return useOpenGL;
	}
	public static void setUseOpenGL(boolean useOpenGL) {
		JOGLPickAction.useOpenGL = useOpenGL;
	}
}
