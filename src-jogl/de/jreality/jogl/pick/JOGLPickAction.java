
/*
 * Created on Jan 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.pick;


import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import de.jreality.scene.Graphics3D;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.pick.PickAction;
import de.jreality.scene.pick.PickPoint;
import de.jreality.scene.pick.PickPointComparator;
import de.jreality.util.P2;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
/**
 * @author gunn
 *
 */
public class JOGLPickAction extends PickAction  {
	
	static boolean useOpenGL = true;
	
	public JOGLPickAction(Viewer v) {
		super(v);
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SceneGraphVisitor#init()
	 */
	
	public Object visit() {
		if (useOpenGL && theViewer instanceof de.jreality.jogl.Viewer)	{
			PickPoint[] hits = ((de.jreality.jogl.Viewer) theViewer).getRenderer().performPick(pickPointNDC);	
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
	public static PickPoint[] processOpenGLSelectionBuffer(int numberHits, IntBuffer selectBuffer, double[] pickPoint, Viewer v) {
		double factor = 1.0/(0x7fffffff);
		ArrayList al = new ArrayList();
		PickPoint oneHit = null;
		int realHits = 0;
		Graphics3D context3D = new Graphics3D(v);
		SceneGraphComponent theRoot = v.getSceneRoot();
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
			pndc[2] = z1;
			//System.out.print("Hit "+i+": "+z1+" - "+z2+" ");
			boolean geometryFound = false;
			int geomID = 0;
			for (int j = 0; j<names; ++j)	{
				path[j] = selectBuffer.get(count);
				if (j>0) {
					if (!geometryFound)	{
						if (path[j] >= 0 && sgc.getChildComponentCount() > path[j] && sgc.getChildComponent(path[j]) != null) {
							SceneGraphComponent tmpc = sgc.getChildComponent(path[j]);
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
					oneHit = new PickPoint(v, sgp, ndcpt[k]);
					oneHit.setPointObject(opt[k]);				
					//oneHit.setPointNDC(ndcpt[k]);
					//oneHit.setPickPath( (SceneGraphPath) sgp.clone());
					//oneHit.setContext(context3D.copy());
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
	
	protected static PickPoint calculatePickPointFor(PickPoint dst, double[] pndc, Graphics3D gc, SceneGraphPath sgp, IndexedFaceSet sg, int faceNum)	{
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
			dst = new PickPoint(gc.getViewer(), sgp, intersect);
			//dst.setPickPath( (SceneGraphPath) sgp.clone());
			//dst.setContext(gc.copy());
			//dst.setPointNDC(intersect);
			dst.setPointObject(objectPt);
			dst.setFaceNum(faceNum);
			dst.setPickType(PickPoint.HIT_FACE);
		} else{
			//System.out.println("calculatePickPointFor: no hit");
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
