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


import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.P2;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.Graphics3D;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
/**
 * This class performs software picking of jReality scene graphs. An instance
 * is obtained by the constructor  {@link #PickAction(SceneGraphComponent)}, where the parameter identifies
 * the scene graph to be picked.  To carry out a
 * pick, there are two options: 
 * 
 * 1) first invoke {@link #setPickPoint(double[])} with the NDC coordinates of
 * the cursor, or
 * 2) first invoke {@link #setPickSegment(double[], double[]) with the 3D coordinates of the beginning and end points
 * of a line segment in world coordinates (as 3-vectors or homoegeneous 4-vectors): the pick then finds all intersections of
 * the scene graph with this segment, sorted by distance to the beginning point.
 * 
 * In both cases, then {@link #visit()} to perform the pick itself, which returns a List of hits 
 * (each an instance of {@link de.jreality.jogl.pick.PickPoint}. The caller needs to cast this
 * to type <code> List</code> since the interface {@link de.jreality.scene.SceneGraphVisitor}
 * specifies a return of type Object.  If there are no hits, the list is non-null of 
 * size 0.
 * 
 * The same instance can be used repeatedly
 * for different picks, calling the two methods as outlined above.
 * 
 * There is no longer any dependence on viewers in any shape, way, or form.
 * 
 * Current limitations:
 * 	 Only picks faces, not edges or vertices
 *   Doesn't pick Cylinder class; does pick Sphere [30.05.05]
 *   Is not reliable in elliptic space, due to difficulty in clipping
 * 		geometry reliably.
 * 	 Does not completely "fill out" the information for a "hit":
 * 		cannot interpolate vertex data  except for triangles 
 * 		(see notes in source code).
 *    Does not consider the effects of appearances when picking 
 *  		(so always "showFaces" attribute is true.)
  * @see {@link de.jreality.jogl.pick.PickPoint}
 * @see {@link charlesgunn.jreality.jogl.pick.JOGLPickAction}
 * @author gunn
 *
 */
public class PickAction extends SceneGraphVisitor{
	
	// specifies which types of geometry elements should be picked
	// settable with setPickType(). Currently only faces are supported.
	public static final int PICK_VERTICES	= 1;
	public static final int PICK_EDGES		= 2;
	public static final int PICK_FACES		= 4;
	public static final int PICK_ALL		= 7;
	// only interested in closest hit? currently ignored
	public static final int PICK_CLOSEST	= 8;		
	
	// a pick action can be defined in terms of NDC coordinates (like a mouse press)
	// or in world coordinates as a line segment
	public static final int PICK_NDC = 1;
	public static final int PICK_WORLD = 2;

	public static final double MIN_PICKZ =  -1.0;
	public static final double MAX_PICKZ =  1.0;
	
	static int debug = 0;
	
	//protected Viewer theViewer;
	protected SceneGraphComponent theRoot;
	protected SceneGraphPath cameraPath;
	protected int metric = Pn.EUCLIDEAN;
	protected Graphics3D context3D;
	protected int pickType, pickCoordinateSystem;
	protected double[] pickPointNDC = new double[3];
	protected SceneGraphPath thePath;
	protected Vector pickHits;		// is returned
	protected double minZ, maxZ;

	// shared by the different geometry visit() methods
	protected boolean possibleHit = true;
	protected double[][] pointsInNDC;
	
	// if coordinate system is world, then we convert to NDC and use a second pick action 
	protected PickAction worldToNDC = null;
	
	// Handle the transformation stack by hand
	protected double[][] tstack = new double[32][16];
	protected int tstackPtr = 0;


	/**
	 * @param v
	 */public PickAction(Viewer v) {
		this(v.getSceneRoot());
		setCameraPath(v.getCameraPath());
		theViewer = v;	
	 }
	 
	public PickAction(SceneGraphComponent root)	{
		//theViewer = v;
		theRoot = root; //v.getSceneRoot();
		thePath = new SceneGraphPath();
		pickType = PICK_ALL | PICK_CLOSEST;
		pickCoordinateSystem = PICK_NDC;
		setPickPoint(0.0, 0.0);
		pickHits = new Vector();
		Rn.setIdentityMatrix(tstack[0]);
	}
	
	/**
	 * Specify the pick action by the (x,y) normalized device coordinates.
	 * That is, -1 <= x <= 1, -1 <= y <= 1. 
	 * @param xNDC
	 * @param yNDC
	 */
	public void setPickPoint(double xNDC, double yNDC)	{
		setPickPoint(Rn.setToValue(null, xNDC, yNDC, 1.0));
	}
	
	/**
	 * 
	 * @param ndc	double[3]
	 * @deprecated	Use {@link #setPickPoint(double, double)
	 */
	public void setPickPoint(double[] ndc)	{
		if (ndc == null) return;
		// we need homogeneous coordinates for our geometric tests
		pickPointNDC[0] = ndc[0];
		pickPointNDC[1] = ndc[1];
		pickPointNDC[2] = 1.0;
		pickCoordinateSystem = PICK_NDC;
		minZ = MIN_PICKZ;
		maxZ = MAX_PICKZ;
	}
	
	 /**
	 * @param cameraPath
	 */
	protected void setCameraPath(SceneGraphPath cp) {
		cameraPath = cp;
	}

	/**
	 * In the case of a world-coordinate pick, have to remove the extra camera which was added to the scene graph.
	 *
	 */
	public void dispose()		{
		if (worldToNDC != null)	{
			if (theRoot.isDirectAncestor(camNode)) theRoot.removeChild(camNode);
		}
	}
	
	SceneGraphComponent camNode = null;
	Camera cam = null;
	protected Viewer theViewer = null;
	public void setPickSegment(double[] p0, double[] p1)	{
		pickCoordinateSystem = PICK_WORLD;
		if (worldToNDC == null)	{
			SceneGraphPath camPath = new SceneGraphPath();
			camPath.push(theRoot);
			camNode = new SceneGraphComponent();
			camNode.setTransformation(new Transformation());
			camPath.push(camNode);
			cam = new Camera();
			camPath.push(cam);
			theRoot.addChild(camNode);
			camNode.setCamera(cam);
			worldToNDC = new PickAction(theRoot);	
			worldToNDC.setMetric(getMetric());
			worldToNDC.setCameraPath(camPath);
			//cam.setFieldOfView(1.0);
		}
		double[] p04, p14;
		if (p0.length == 3) p04 = Pn.homogenize(null,p0);
		else if (p0.length == 4) p04 = p0;
		else throw new IllegalArgumentException("p0 has invalid length");
		if (p1.length == 3) p14 = Pn.homogenize(null,p1);
		else if (p1.length == 4) p14 = p1;
		else throw new IllegalArgumentException("p1 has invalid length");
		// create a transformation so that p0 and p1 lie along the negative z-axis, with p0 "close" to the camera.
		double[] origin = Rn.linearCombination(null, 1.0,p04, -.1, p14);
		Pn.dehomogenize(origin, origin);
		camNode.getTransformation().setMatrix(Rn.inverse(null,P3.makeLookatMatrix(null, origin, p04, 0.0, metric)));
		// calculate min/max z-bounds
		Graphics3D cg = new Graphics3D(worldToNDC.cameraPath, null, CameraUtility.getAspectRatio(theViewer));
		double[] worldToNDCMatrix = cg.getWorldToNDC();
		double[] p04NDC = Rn.matrixTimesVector(null, worldToNDCMatrix, p04);
		Pn.dehomogenize(p04NDC, p04NDC);
		double[] p14NDC = Rn.matrixTimesVector(null, worldToNDCMatrix, p14);
		Pn.dehomogenize(p14NDC, p14NDC);
//		System.out.println("Image of p0 is: "+Rn.toString(p04NDC));
//		System.out.println("Image of p1 is: "+Rn.toString(p14NDC));
		// we only allow hits between p0 and p1
		minZ = p04NDC[2];
		maxZ = p14NDC[2];
		
	}
	
	public int getMetric() {
		return metric;
	}
	
	public void setMetric(int metric) {
		this.metric = metric;
	}
	
	public PickPoint getFirstHit()	{
		if (pickHits != null && pickHits.size() != 0)	return ((PickPoint) pickHits.get(0));
		return null;
	}
	
	protected void pushMatrix(double[] m)	{
		tstackPtr++;
		if (tstackPtr>=tstack.length) {
			return;
		}
		//System.arraycopy(tstack[tstackPtr-1],0,tstack[tstackPtr],0,16);
		Rn.times(tstack[tstackPtr], tstack[tstackPtr-1], m);
		context3D.setObjectToWorld(tstack[tstackPtr]);
	}

	protected void popMatrix()	{
		tstackPtr--;
	}
	
	/* (non-Javadoc)
	 */
	
	public Object visit() {
		if (pickCoordinateSystem == PICK_WORLD)	return worldToNDC.visit();
		context3D  = new Graphics3D( cameraPath, null, CameraUtility.getAspectRatio(theViewer));
		pickHits.clear();
		tstackPtr = 0;
		Rn.setIdentityMatrix(tstack[0]);
		if (debug >= 1) System.out.println(Rn.toString(pickPointNDC));
		thePath.clear();
		theRoot.accept(this);
		if (debug != 0) System.out.println("Returning "+pickHits.size()+" hits");
		if (pickHits.size() == 0) return pickHits;
		
		Comparator sorter = new Comparator()	{
			public int compare(Object o1, Object o2)	{
				PickPoint p1 = (PickPoint) o1;
				PickPoint p2 = (PickPoint) o2;
				double z1 = p1.getPointNDC()[2];
				double z2 = p2.getPointNDC()[2];
				if (z1-z2 < 0) return -1;
				if (z1-z2 == 0) return 0;
				return 1;
			}
		};
		if (debug != 0)
			for (int i = 0; i<pickHits.size(); ++i)	{
				PickPoint pp = ((PickPoint) pickHits.get(i));
				LoggingSystem.getLogger(this).log(Level.INFO, pp.getPointNDC()[2]+pp.getPickPath().toString());
			}
		Collections.sort(pickHits, sorter);
		return pickHits;
	}
	/* (non-Javadoc)
	 * The current version does not pick vertices, so this method only does a rough bounds check
	 * which subclasses (such as IndexedFaceSet) can use to decide if there might be a pick hit.
	 * The boolean value possibleHit records this info.
	 */
	public void visit(PointSet sg) {
		if ((debug & 4) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Visiting PointSet");
		if (sg.getClass() == PointSet.class) thePath.push(sg);
		double[][] verts = sg.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		if (verts.length>0) {	// are there any vertices?
			pointsInNDC = Rn.matrixTimesVector(null, context3D.getObjectToNDC(), verts);
			if (verts[0].length == 4) Pn.dehomogenize(pointsInNDC, pointsInNDC);
			Rectangle3D box = BoundingBoxUtility.calculateBoundingBox(pointsInNDC);
			double[][] bnds = box.getBounds();
			if ((debug & 2) != 0) 	{
				System.out.println("NDC bound: "+Rn.toString(bnds));
			}
			if (bnds[0][0] < pickPointNDC[0] &&
				bnds[1][0] > pickPointNDC[0] && 
				bnds[0][1] < pickPointNDC[1] && 
				bnds[1][1] > pickPointNDC[1] && 
				bnds[0][2] <= maxZ && 
				bnds[1][2] >= minZ ){
					if ((debug & 1) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Possible hit");
					possibleHit = true;
			} else {
					possibleHit = false;
			}
		}
		else
			possibleHit=false;	// move along, nothing to hit here...
		if (sg.getClass() == PointSet.class) thePath.pop();
	}
	
	/* (non-Javadoc)
	 * Current version does not pick edges, so this method is very short
	 */
	public void visit(IndexedLineSet sg) {
		if ((debug & 4) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Visiting ILS");
		if (sg.getClass() == IndexedLineSet.class) thePath.push(sg);
		visit((PointSet) sg);
		if (sg.getClass() == IndexedLineSet.class) thePath.pop();
	}
	/* (non-Javadoc)
	 * This method goes through the list of faces in an IndexedFaceSet and for each
	 * face, check whether the given pick point "lies over" the face (in NDC coordinates).
	 * if so, it calculates the intersection point and stores off all necessary information.
	 * It is not complete -- see note at end of source code.
	 * in a PickPoint instance.
	 */
	public void visit(IndexedFaceSet sg) {
		if ((debug & 4) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Visiting IFS");
		//if (!sg.isPickable) return;
		
		possibleHit = true;
		visit((IndexedLineSet) sg);
		// The superclass PointSet does a rough bound check which we now check
		if (!possibleHit) return;
		
		if ((pickType & PICK_FACES) == 0)	return;
		
		//int[][] indices = sg.getIndices();
		thePath.push(sg);
		int[][] indices = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] onePolygon;
		// Loop through the faces
		for (int i = 0; i< indices.length; ++i)	{
			onePolygon = new double[indices[i].length][];
			// put together the vertices for this face into one double[][] array
			for (int j = 0; j<indices[i].length; ++j)	{
				onePolygon[j] = pointsInNDC[indices[i][j]];
			}
			double[][] bds = new double[2][3];
			// Use library methods to calculate a bounding box
			if (pointsInNDC[0].length == 3)	bds = Rn.calculateBounds(bds, onePolygon);
			else if (pointsInNDC[0].length == 4)	bds = Pn.calculateBounds(bds, onePolygon);
			// The following check was an effort to work around problems in non-euclidean space
			// where bounding boxes are of questionable value anyway
			//if (bds[0][2] < -2.0)	continue;
			// Only valid polygons are convex.  The important work of this method is done
			// in the call to polygonContainsPoint()
			if (P2.isConvex(onePolygon) && P2.polygonContainsPoint(onePolygon, pickPointNDC ))	{
				if ((debug & 2) != 0) System.out.println("Hit Polygon "+i+Rn.toString(onePolygon));
				//System.out.println("Polygon hit "+i);
				//System.out.println("Path: "+thePath.toString());

				// find the exact point of intersection
				// we assume the polygon lies in the plane spanned by the first three vertices.
				double[] planeNDC = P3.planeFromPoints(null, onePolygon[0], onePolygon[1], onePolygon[2]);
				// here we create a second point which together with pickPoint span the line
				// perpendicular to the z-direction through the given NDC pick point
				double[] pickPoint2 = (double[]) pickPointNDC.clone();
				pickPoint2[2] = 0.0;
				// find the point of intersection in NDC coordinates
				// This returns a homogeneous 4-vector
				double[] intersectNDC = P3.lineIntersectPlane(null, pickPointNDC, pickPoint2, planeNDC);
				//System.out.println("Intersect = "+Rn.toString(intersectNDC));
				if (intersectNDC[2] < minZ || intersectNDC[2] > maxZ) continue;
				double[] NDCToObject = Rn.inverse(null, context3D.getObjectToNDC());
				double[] objectPt = Rn.matrixTimesVector(null, NDCToObject, intersectNDC);
				Pn.dehomogenize(objectPt, objectPt);
				//System.out.println("Object coords: "+Rn.toString(objectPt));
				// Allocate and initialize a PickPoint instance to store this pick hit.
				PickPoint pp = PickPoint.PickPointFactory(thePath, cameraPath, intersectNDC);
				if ((debug & 2) != 0) System.out.println("Path is "+pp.getPickPath().toString());
				//pp.setPickPath( (SceneGraphPath) thePath.clone());
				//pp.setPointNDC(intersectNDC);
				//pp.setPointObject(objectPt);
				pp.setFaceNum(i);
				pp.setPickType(PickPoint.HIT_FACE);
				// To be complete, this method should go further and determine 3 vertices of
				// this polygon, such that the pick point lies in or on the boundary of the triangle
				// determined by this triangle.  From this information, barycentric coordinates of
				// pick point can be calculated and vertex data such as normal vector and texture 
				// coordinates can then be reliably calculated.
				pickHits.add(pp);
			}
		}
		thePath.pop();
	}

	public void visit(Sphere sph)	{
		double[] pndc1 = new double[4], pndc2 = new double[4];
		pndc1[0] = pndc2[0] = pickPointNDC[0];
		pndc1[1] = pndc2[1] = pickPointNDC[1];
		pndc1[2] = 0.5;
		pndc2[2] = -.5;
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
//		boolean fliproots = false;
//		if (s == 1.0)	{  //unlikely, but the one case not handled by our p0+tp1 ansatz.
//			fliproots = true;
//			s = q;
//			q = 1.0;
//			double[] tmp = hp0;
//			hp0 = hp1;
//			hp1 = tmp;
//		}
		// use quadratic formula to 
		// solve for intersection of line (1-t)p0 + tp1 with unit sphere.
		double a = s - 2 * r + q; //s; //
		double b = 2 * (r - q);//2 * r; //
		double c = q - 1.0;
		double d = b*b - 4 * a * c;
		if (d < 0) {
			LoggingSystem.getLogger(this).log(Level.FINEST,"Missed sphere");
			return;
		}
		d = Math.sqrt(d);
		double x[] = new double[2];
		x[0] = (-b + d)/(2 * a);
		x[1] = (-b - d)/(2 * a);

		double[][] opt = new double[2][];
		double[][] ndcpt = new double[2][];
		double[] o2ndc = Rn.inverse(null, ndc2o);
		thePath.push(sph);
		for (int k = 0; k<2; ++k)	{
			opt[k] = Rn.linearCombination(null, 1.0-x[k], hp0, x[k], hp1);
			ndcpt[k] = Rn.matrixTimesVector(null, o2ndc, opt[k]);
			//if (ndcpt[k][2] < minZ || ndcpt[k][2] > maxZ) continue;
			PickPoint oneHit = PickPoint.PickPointFactory(thePath, cameraPath,  Pn.homogenize(null, ndcpt[k]));
			//oneHit.setPointObject(Pn.homogenize(null,opt[k]));				
			//oneHit.setPointNDC(ndcpt[k]);
			//oneHit.setPickPath( (SceneGraphPath) sgp.clone());
			//oneHit.setContext(context3D.copy());
			oneHit.setPickType(PickPoint.HIT_FACE);  // TODO not really a face;  HIT_PRIMITIVE ?
			pickHits.add(oneHit);
			//realHits++;
		}
		thePath.pop();
		
	}
	/* Push transform and scene graph paths, and pop again after children have been called.
	 */
	public void visit(SceneGraphComponent sg) {
		if (! sg.isVisible()) return;
		if ((debug & 4) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Visiting Component");
		Transformation t = sg.getTransformation();
		if (t != null)	pushMatrix(t.getMatrix());
			
		thePath.push(sg);
		sg.childrenAccept(this);
		thePath.pop();
		if (t != null)	popMatrix();
	}

	

	/* (non-Javadoc)
	 */
	public void visit(SceneGraphNode sg) {
		if ((debug & 4) != 0) LoggingSystem.getLogger(this).log(Level.FINER,"Visiting Node");
	}
	/**
	 * @return
	 */
	public Vector getPickHits() {
		return pickHits;
	}

	/**
	 * @return
	 */
	public int getPickType() {
		return pickType;
	}

	/**
	 * @param i
	 */
	public void setPickType(int i) {
		pickType = i;
	}


}
