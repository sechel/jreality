
/*
 * Created on Jan 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.pick;


import java.nio.IntBuffer;
import java.util.*;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.util.*;
/**
 * @author gunn
 *
 */
public class PickAction extends SceneGraphVisitor{
	
	public static final int PICK_VERTICES	= 1;
	public static final int PICK_EDGES		= 2;
	public static final int PICK_FACES		= 4;
	public static final int PICK_ALL		= 7;
	public static final int PICK_CLOSEST	= 8;

	public static final double MIN_PICKZ =  -1.0;
	public static final double MAX_PICKZ =  1.0;
	
	static int debug = 0;
	
	static boolean useOpenGL = true;
	
	SceneGraphNode theRoot;
	Graphics3D context3D;
	int pickType;
	double[] pickPoint = new double[3];
	SceneGraphPath thePath;
	Vector pickHits;
	Viewer theViewer;

	private boolean possibleHit = true;
	private double[][] pointsInNDC;
	private double[][] tstack = new double[32][16];
	private int tstackPtr = 0;

	public PickAction(Viewer v) {
		super();
		theViewer = v;
		theRoot = v.getSceneRoot();
		context3D  = new Graphics3D(v);
		thePath = new SceneGraphPath();
		pickType = PICK_ALL | PICK_CLOSEST;
		pickHits = new Vector();
		Rn.setIdentityMatrix(tstack[0]);
	}
	
	public void setPickPoint(double[] ndc)	{
		if (ndc == null) return;
		// we need homogeneous coordinates for our geometric tests
		pickPoint[0] = ndc[0];
		pickPoint[1] = ndc[1];
		pickPoint[2] = 1.0;
	}
	
	protected void pushMatrix(double[] m)	{
		tstackPtr++;
		if (tstackPtr>=tstack.length) {
			return;
		}
		System.arraycopy(tstack[tstackPtr-1],0,tstack[tstackPtr],0,16);
		Rn.times(tstack[tstackPtr], tstack[tstackPtr], m);
		context3D.setObjectToWorld(tstack[tstackPtr]);
	}

	protected void popMatrix()	{
		tstackPtr--;
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#init()
	 */
	
	public Object visit() {
		pickHits.clear();
		//System.err.println("Initializing Visiting");
		if (useOpenGL && theViewer instanceof de.jreality.jogl.Viewer)	{
			PickPoint[] hits = ((de.jreality.jogl.Viewer) theViewer).getRenderer().performPick(pickPoint);	
			int n = 0;
			if (hits != null)	
				n = hits.length;
			pickHits = new Vector();
			for (int i =0; i<n; ++i)	pickHits.add(hits[i]);
			return pickHits;			
		}
		tstackPtr = 0;
		Rn.setIdentityMatrix(tstack[0]);
		//thePath.push(theRoot);
		if (debug >= 1) System.out.println(Rn.toString(pickPoint));
		thePath.clear();
		theRoot.accept(this);
		//thePath.pop();
		
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
		Collections.sort(pickHits, sorter);
		for (int i = 0; i<pickHits.size(); ++i)	{
			PickPoint pp = ((PickPoint) pickHits.get(i));
			//System.out.println(pp.getPointNDC()[2]+pp.getPickPath().toString());
		}
		return pickHits;
	}
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.Camera)
	 */
	public void visit(PointSet sg) {
		if ((debug & 4) != 0) System.err.println("Visiting PointSet");
		if (sg instanceof PointSet) thePath.push(sg);
		double[][] verts = sg.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		pointsInNDC = Rn.matrixTimesVector(null, context3D.getObjectToNDC(), verts);
		if (verts[0].length == 4) Pn.dehomogenize(pointsInNDC, pointsInNDC);
		Rectangle3D box = GeometryUtility.calculateBoundingBox(pointsInNDC);
		double[][] bnds = box.getBounds();
		if ((debug & 2) != 0) 	{
			System.out.println("NDC bound: "+Rn.toString(bnds));
		}
		if (bnds[0][0] < pickPoint[0] &&
			bnds[1][0] > pickPoint[0] && 
			bnds[0][1] < pickPoint[1] && 
			bnds[1][1] > pickPoint[1] && 
			bnds[0][2] < MAX_PICKZ && 
			bnds[1][2] > MIN_PICKZ){
				if ((debug & 1) != 0) System.err.println("Possible hit");
				possibleHit = true;
		} else {
				possibleHit = false;
		}
		if (sg instanceof PointSet) thePath.pop();
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.IndexedLineSet)
	 */
	public void visit(IndexedLineSet sg) {
		if ((debug & 4) != 0) System.err.println("Visiting ILS");
		if (sg instanceof IndexedLineSet) thePath.push(sg);
		visit((PointSet) sg);
		if (sg instanceof IndexedLineSet) thePath.pop();
	}
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.IndexedFaceSet)
	 */
	public void visit(IndexedFaceSet sg) {
		if ((debug & 4) != 0) System.err.println("Visiting IFS");
		//if (!sg.isPickable) return;
		
		possibleHit = true;
		visit((IndexedLineSet) sg);
		if (!possibleHit) return;
		if ((pickType & PICK_FACES) == 0)	return;
		
		//int[][] indices = sg.getIndices();
		thePath.push(sg);
		int[][] indices = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		double[][] onePolygon;
		for (int i = 0; i< indices.length; ++i)	{
			onePolygon = new double[indices[i].length][];
			for (int j = 0; j<indices[i].length; ++j)	{
				onePolygon[j] = pointsInNDC[indices[i][j]];
			}
			double[][] bds = new double[2][3];
			if (pointsInNDC[0].length == 3)	bds = Rn.calculateBounds(bds, onePolygon);
			else if (pointsInNDC[0].length == 4)	bds = Pn.calculateBounds(bds, onePolygon);
			//boolean toobig = false;
			//for (int k = 0; k<3; ++k)		{
				//if (bds[0][k] < -10E6 || bds[1][k] > 10E6) toobig = true;
			//}
			//if (toobig) continue;
			if (bds[0][2] < -2.0)	continue;
			if (P2.isConvex(onePolygon) && P2.polygonContainsPoint(onePolygon, pickPoint))	{
				if ((debug & 2) != 0) System.out.println("Hit Polygon "+i+Rn.toString(onePolygon));
				//System.out.println("Polygon hit "+i);
				//System.out.println("Path: "+thePath.toString());
				// find the exact point of intersection
				double[] plane = P3.planeFromPoints(null, onePolygon[0], onePolygon[1], onePolygon[2]);
				double[] p1 = (double[]) pickPoint.clone();
				p1[2] = 0.0;
				double[] intersect = P3.lineIntersectPlane(null, pickPoint, p1, plane);
				if (intersect[2] < MIN_PICKZ || intersect[2] > MAX_PICKZ) continue;
				//System.out.println("Intersect = "+Rn.toString(intersect));
				double[] NDCToObject = Rn.inverse(null, context3D.getObjectToNDC());
				double[] objectPt = Rn.matrixTimesVector(null, NDCToObject, intersect);
				Pn.dehomogenize(objectPt, objectPt);
				//System.out.println("Object coords: "+Rn.toString(objectPt));
				PickPoint pp = new PickPoint();
				pp.setPickPath( (SceneGraphPath) thePath.clone());
				pp.setPointNDC(intersect);
				pp.setPointObject(objectPt);
				pp.setFaceNum(i);
				pp.setPickType(PickPoint.HIT_FACE);
				
				if (pickHits == null)	pickHits = new Vector();
				pickHits.add(pp);
			}
		}
		thePath.pop();
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.SceneGraphComponent)
	 */
	public void visit(SceneGraphComponent sg) {
		if ((debug & 4) != 0) System.err.println("Visiting Kit");
		//if (!sg.isPickable) return;
		//sg.preRender(gc);
		
		Transformation t = sg.getTransformation();
		if (t != null)	{
			pushMatrix(t.getMatrix());
		}
			
		thePath.push(sg);
		// TODO figure out how to get the non-Components onto the path 
		sg.childrenAccept(this);
		thePath.pop();
		if (t != null)	popMatrix();
	}

	

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.SceneGraphNode)
	 */
	public void visit(SceneGraphNode sg) {
		if ((debug & 4) != 0) System.err.println("Visiting Node");
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

	private static PickPoint calculatePickPointFor(PickPoint dst, double[] pndc, Graphics3D gc, SceneGraphPath sgp, IndexedFaceSet sg, int faceNum)	{
		if (dst == null) dst = new PickPoint();
		int[][] indices = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray(null);
		DataList verts = sg.getVertexAttributes(Attribute.COORDINATES);
		if (faceNum >= indices.length)	{
			System.out.println("Invalid face number in calculatePickPointFor()");
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
				System.out.println("calculatePickPointFor: bad z-coordinate");
				return null;
			}
			//System.out.println("Intersect = "+Rn.toString(intersect));
			double[] NDCToObject = Rn.inverse(null, gc.getObjectToNDC());
			double[] objectPt = Rn.matrixTimesVector(null, NDCToObject, intersect);
			Pn.dehomogenize(objectPt, objectPt);
			dst.setPickPath( (SceneGraphPath) sgp.clone());
			dst.setContext(gc.copy());
			dst.setPointNDC(intersect);
			dst.setPointObject(objectPt);
			dst.setFaceNum(faceNum);
			dst.setPickType(PickPoint.HIT_FACE);
		} else{
			//System.out.println("calculatePickPointFor: no hit");
			return null;
		}
		
		return dst;
	}

	/**
	 * @param numberHits
	 * @param selectBuffer
	 * @return
	 */
	public static PickPoint[] processOpenGLSelectionBuffer(int numberHits, IntBuffer selectBuffer, double[] pickPoint, Viewer v) {
		double factor = 1.0/(0x7fffffff);
		ArrayList al = new ArrayList();
		PickPoint oneHit = new PickPoint();
		int realHits = 0;
		Graphics3D context3D = new Graphics3D(v);
		SceneGraphComponent theRoot = v.getSceneRoot();
		for (int i =0, count = 0; i<numberHits; ++i)	{
			int names = selectBuffer.get(count++);
			oneHit = new PickPoint();
			int[] path = new int[names];
			double[] pndc = new double[3];
			pndc[0] = pickPoint[0];
			pndc[1] = pickPoint[1];
			SceneGraphPath sgp = new SceneGraphPath();
			SceneGraphComponent sgc = theRoot;
			sgp.push(theRoot);
			double z1 = selectBuffer.get(count++) * factor;
			double z2 = selectBuffer.get(count++) * factor;
			pndc[2] = z1;
			//System.out.print("Hit "+i+": "+z1+" - "+z2+" ");
			boolean geometryFound = false;
			int geomID = 0;
			for (int j = 0; j<names; ++j)	{
				path[j] = selectBuffer.get(count);
				if (j>0) {
					if (!geometryFound)	{
						if (path[j] >= 0 && sgc.getChildComponentCount() > path[j] && sgc.getChildComponent(path[j]) != null) {
							SceneGraphComponent tmpc = (SceneGraphComponent) sgc.getChildComponent(path[j]);
							sgp.push(tmpc); 
							sgc = tmpc;
						}
					}
					else geomID = path[j];
					if (path[j] == 10000)	{	// geometry
						geometryFound = true;
						sgp.push(sgc.getGeometry());
					}
				}
				//System.out.print(": "+selectBuffer.get(count));
				count++;
			}
			if (!geometryFound) continue;
			SceneGraphNode sgn = sgp.getLastElement();
			context3D.setObjectToWorld(sgp.getMatrix(null));
			if ((sgn instanceof IndexedFaceSet))	{
				IndexedFaceSet sg = (IndexedFaceSet) sgn;
				oneHit = calculatePickPointFor(oneHit, pndc, context3D,sgp, sg, geomID);
				if (oneHit == null) continue;
				al.add(oneHit);
				realHits++;
			} else if (sgn instanceof Sphere)	{
				double[] p0 = new double[3], p1 = new double[3];
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
					System.out.println("Missed sphere");
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
					oneHit = new PickPoint();
					oneHit.setPointObject(opt[k]);				
					oneHit.setPointNDC(ndcpt[k]);
					oneHit.setPickPath( (SceneGraphPath) sgp.clone());
					oneHit.setContext(context3D.copy());
					oneHit.setPickType(PickPoint.HIT_FACE);  // TODO not really a face;  HIT_PRIMITIVE ?
					al.add(oneHit);
					realHits++;
				}
			} else {
				System.out.println("Invalid geometry type");
				continue; 			
			}
		}
		PickPoint[] hits = new PickPoint[realHits];
		hits =  (PickPoint[]) al.toArray(hits);
		Comparator cc = PickPointComparator.sharedInstance;
		Arrays.sort(hits, cc);
		return hits;
	}
	public static boolean isUseOpenGL() {
		return useOpenGL;
	}
	public static void setUseOpenGL(boolean useOpenGL) {
		PickAction.useOpenGL = useOpenGL;
	}
}
