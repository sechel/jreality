/*
 * Created on Jan 30, 2004
 *
 */
package de.jreality.jogl.pick;


import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.*;
/**
 * @author gunn
 *
 */
public class PickActionBad  {
	
	public static final int PICK_VERTICES	= 1;
	public static final int PICK_EDGES		= 2;
	public static final int PICK_FACES		= 4;
	public static final int PICK_ALL		= 7;
	public static final int PICK_CLOSEST	= 8;

	public static final double MIN_PICKZ =  -1.0;
	public static final double MAX_PICKZ =  1.0;
	
	static int debug = 0;
	
	SceneGraphNode theRoot;
	Graphics3D context3D;
	int pickType;
	double[] pickPoint = new double[3];
	Vector pickHits;
	PickVisitor pickVisitor;
	
	/**
	 * 
	 */
	public PickActionBad(SceneGraphNode root, Camera cam) {
		super();
		theRoot = root;
		context3D  = new Graphics3D(null);
		//context3D.setCamera(cam);
		pickType = PICK_ALL | PICK_CLOSEST;
		pickHits = new Vector();
		pickVisitor = new PickVisitor();
	}

	public PickActionBad(InteractiveViewer v) {
		this((SceneGraphNode) v.getSceneRoot(), null);
	}
	
	public void setPickPoint(double[] ndc)	{
		if (ndc == null) return;
		// we need homogeneous coordinates for our geometric tests
		pickPoint[0] = ndc[0];
		pickPoint[1] = ndc[1];
		pickPoint[2] = 1.0;
	}
	
	public Object visit()	{
		return pickVisitor.visit();
	}
	
	// these fields really belong to the PickVisitor but we keep them outside
	// its scope, so we don't have to copy them in pushContext()s
	private SceneGraphPath thePath;
	private double[][] pointsInNDC;
	
	private class PickVisitor extends SceneGraphVisitor	{
		
		protected double[] currentMatrix = Rn.identityMatrix(4);
		private boolean possibleHit = true;
		 
		public PickVisitor (PickVisitor p) {
			super();
			currentMatrix = ((double[]) p.currentMatrix.clone());
		}	
	
		public PickVisitor()	{
		 	super();
		 }
		 
		public PickVisitor pushContext()	{
			PickVisitor copy = new PickVisitor(this);
			return copy;
		}
		
		public Object visit() {
			//System.err.println("Initializing Visiting");
			thePath = new SceneGraphPath();
			currentMatrix = Rn.identityMatrix(4);
			pickHits.clear();
			//thePath.push(theRoot);
			if (debug >= 1) JOGLConfiguration.theLog.log(Level.FINE,Rn.toString(pickPoint));
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
				//JOGLConfiguration.theLog.log(Level.FINE,pp.getPointNDC()[2]+pp.getPickPath().toString());
			}
			return pickHits;
		}
	
		/* (non-Javadoc)
		 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.Camera)
		 */
		public void visit(PointSet sg) {
			if ((debug & 4) != 0) System.err.println("Visiting PointSet");
			//DataGrid vv = sg.getVertices();
			double[][] verts = sg.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			pointsInNDC = Rn.matrixTimesVector(null, context3D.getObjectToNDC(), verts);
			if (verts[0].length == 4) Pn.dehomogenize(pointsInNDC, pointsInNDC);
			Rectangle3D box = GeometryUtility.calculateBoundingBox(pointsInNDC);
			double[][] bnds = box.getBounds();
			if ((debug & 2) != 0) 	{
				JOGLConfiguration.theLog.log(Level.FINE,"NDC bound: "+Rn.toString(bnds));
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
		}
	
		/* (non-Javadoc)
		 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.IndexedLineSet)
		 */
		public void visit(IndexedLineSet sg) {
			if ((debug & 4) != 0) System.err.println("Visiting ILS");
			visit((PointSet) sg);
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
			int[][] indices = sg.getFaceAttributes(Attribute.INDICES).toIntArrayArray().toIntArrayArray(null);
			double[][] onePolygon;
			for (int i = 0; i< indices.length; ++i)	{
				onePolygon = new double[indices[i].length][];
				for (int j = 0; j<indices[i].length; ++j)	{
					onePolygon[j] = pointsInNDC[indices[i][j]];
				}
				double[][] bds = new double[2][3];
				if (pointsInNDC[0].length == 3)	bds = Rn.calculateBounds(bds, onePolygon);
				else if (pointsInNDC[0].length == 4)	bds = Pn.calculateBounds(bds, onePolygon);
				if (bds[0][2] < -2.0)	continue;
				if (P2.polygonContainsPoint(onePolygon, pickPoint))	{
					if ((debug & 2) != 0) JOGLConfiguration.theLog.log(Level.INFO,"Hit Polygon "+i+Rn.toString(onePolygon));
					//JOGLConfiguration.theLog.log(Level.FINER,"Polygon hit "+i);
					//JOGLConfiguration.theLog.log(Level.FINER"Path: "+thePath.toString());
					// find the exact point of intersection
					double[] plane = P3.planeFromPoints(null, onePolygon[0], onePolygon[1], onePolygon[2]);
					double[] p1 = (double[]) pickPoint.clone();
					p1[2] = 0.0;
					double[] intersect = P3.lineIntersectPlane(null, pickPoint, p1, plane);
					if (intersect[2] < MIN_PICKZ || intersect[2] > MAX_PICKZ) continue;
					//JOGLConfiguration.theLog.log(Level.FINER"Intersect = "+Rn.toString(intersect));
					double[] NDCToObject = Rn.inverse(null, context3D.getObjectToNDC());
					double[] objectPt = Rn.matrixTimesVector(null, NDCToObject, intersect);
					Pn.dehomogenize(objectPt, objectPt);
					//JOGLConfiguration.theLog.log(Level.FINER"Object coords: "+Rn.toString(objectPt));
					PickPoint pp = PickPoint.PickPointFactory(thePath, null, intersect);
					//pp.setPickPath( (SceneGraphPath) thePath.clone());
					//pp.setPointNDC(intersect);
					//pp.setPointObject(objectPt);
					pp.setFaceNum(i);
					pp.setPickType(PickPoint.HIT_FACE);
					if (pickHits == null)	pickHits = new Vector();
					pickHits.add(pp);
				}
			}
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
				Rn.times(currentMatrix, currentMatrix, t.getMatrix());
				context3D.setObjectToWorld(currentMatrix);
			}
			
			thePath.push(sg);
			// TODO figure out how to get the non-Components onto the path 
			sg.childrenAccept(pushContext());
			thePath.pop();
		}


		/* (non-Javadoc)
		 * @see charlesgunn.gv2.SceneGraphVisitor#visit(charlesgunn.gv2.SceneGraphNode)
		 */
		public void visit(SceneGraphNode sg) {
			if ((debug & 4) != 0) System.err.println("Visiting Node");
		}
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
