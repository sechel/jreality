package de.jreality.scene.pick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;

class BruteForcePicking {

	private static Matrix m=new Matrix();
	private static Matrix mInv=new Matrix();
	
  public static void intersectPolygons(IndexedFaceSet ifs, int signature, SceneGraphPath path, double[] from, double[] to, ArrayList hits) {
	path.getMatrix(m.getArray());
    System.out.println("BruteForcePicking.intersectPolygons()");
  }


  public static void intersectEdges(IndexedLineSet ils, int signature, SceneGraphPath path, double[] from, double[] to, double tubeRadius, ArrayList localHits) {
		path.getMatrix(m.getArray());

    System.out.println("BruteForcePicking.intersectEdges()");
  }

  public static void intersectPoints(PointSet ps, int signature, SceneGraphPath path, double[] from, double[] to, double pointRadius, ArrayList localHits) {
	  path.getMatrix(m.getArray());
	  path.getInverseMatrix(mInv.getArray());	  
	  double[] fromOb=mInv.multiplyVector(from);
	  double[] toOb=mInv.multiplyVector(to);
	  
	  double[] fromOb3=new double[3];
	  double[] toOb3=new double[3];
	  double[] dirOb3=new double[3];
	  if(from.length > 3){
		  P3.dehomogenize(fromOb3, fromOb);
		  P3.dehomogenize(toOb3, toOb);
		  if(toOb[3]==0){
			  dirOb3=toOb3;
		  }else{
			  Rn.subtract(dirOb3,toOb3,fromOb3);
		  } 
	  }else{
		  Rn.subtract(dirOb3,toOb3,fromOb3);
	  } 
	  
	  Rn.normalize(dirOb3,dirOb3);

	  DoubleArrayArray points = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
	  if (points.getLength() == 0) return;
	  DoubleArray point = points.getValueAt(0);
	  boolean vec3 = point.getLength() == 3;
	  double[] vertex = vec3 ? new double[3] : new double[4];
	  
	  LinkedList MY_HITS = new LinkedList();
	  for (int j = 0, n=points.getLength(); j<n; j++) {
		  points.getValueAt(j).toDoubleArray(vertex);
		  if (!vec3) {
			  Pn.dehomogenize(vertex, vertex);
			  if (vertex[4]==0) continue;
		  }
		  intersectSphere(MY_HITS, vertex, fromOb3, dirOb3, pointRadius);
		  for (Iterator i = MY_HITS.iterator(); i.hasNext(); ) {
			  double[] hitPoint = (double[]) i.next();
			  i.remove();
	    	  double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
			  Hit h = new Hit(SceneGraphPath.fromList(path.toList()), hitPoint, dist ,0 , PickResult.PICK_TYPE_POINT, j,-1);
			  localHits.add(h);
		  }	  	  
	  }
	  System.out.println("BruteForcePicking.intersectPoints()");
  }


  private static final List SPHERE_HIT_LIST=new LinkedList();
  public static void intersectSphere(int signature, SceneGraphPath path, double[] from, double[] to, ArrayList localHits) {
	  
	  path.getMatrix(m.getArray());
	  path.getInverseMatrix(mInv.getArray());	  
	  double[] fromOb=mInv.multiplyVector(from);
	  double[] toOb=mInv.multiplyVector(to);
	  
	  double[] fromOb3=new double[3];
	  double[] toOb3=new double[3];
	  double[] dirOb3=new double[3];
	  if(from.length > 3){
		  P3.dehomogenize(fromOb3, fromOb);
		  P3.dehomogenize(toOb3, toOb);
		  if(toOb[3]==0){
			  dirOb3=toOb3;
		  }else{
			  Rn.subtract(dirOb3,toOb3,fromOb3);
		  } 
	  }else{
		  Rn.subtract(dirOb3,toOb3,fromOb3);
	  } 
	  
	  Rn.normalize(dirOb3,dirOb3);
	  
	  intersectSphere(SPHERE_HIT_LIST, null, fromOb3, dirOb3, 1);

	  switch (SPHERE_HIT_LIST.size()) {
		case 2:
			System.out.println("2 Hits");
			break;
		case 1:
			System.out.println("1 Hits");
			break;
		default:
			break;
	  }
	  
	  for (Iterator i = SPHERE_HIT_LIST.iterator(); i.hasNext(); ) {
		  double[] hitPoint = (double[]) i.next();
		  i.remove();
    	  double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
		  Hit h = new Hit(SceneGraphPath.fromList(path.toList()), hitPoint, dist ,0 , PickResult.PICK_TYPE_OBJECT, -1,-1);
		  localHits.add(h);
	  }	  
  }
  
  /**
   * 
   * @param hits
   * @param vertex null or translation in object coordinates
   * @param from in local coordinates
   * @param dir in local coordinates
   * @param r in local coordinates
   */
  private static void intersectSphere(List hits, final double[] vertex, final double[] f, final double[] dir, double r) {
	  double[] from=f;
	  if(vertex!=null){ 
		  from=Rn.subtract(null,from,vertex);
	  }
	  double b=2*Rn.innerProduct(dir,from);
	  double c=Rn.euclideanNormSquared(from)-r;	  
	  double dis=Math.pow(b,2)-4*c;
	  if(dis>=0){
		  dis=Math.sqrt(dis);
		  double t=(-b-dis)/2;
		  if(t>0){
			  double[] hitPointOb3=new double[3];
			  Rn.times(hitPointOb3,t,dir);
			  Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir
			  double[] hitPoint=new double[4];
			  Pn.homogenize(hitPoint,hitPointOb3);
			  hitPoint=m.multiplyVector(hitPoint);
			  hits.add(hitPoint);	
		  }
		  t=t+dis; //t=(-b+dis)/2;
		  if(t>0){
			  double[] hitPointOb3=new double[3];
			  Rn.times(hitPointOb3,t,dir);
			  Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir
			  double[] hitPoint=new double[4];
			  Pn.homogenize(hitPoint,hitPointOb3);
			  hitPoint=m.multiplyVector(hitPoint);
			  hits.add(hitPoint);
		  }
	  }
  }
  
  public static void intersectCylinder(int signature, SceneGraphPath path, double[] from, double[] to, ArrayList hits) {
		path.getMatrix(m.getArray());
		path.getInverseMatrix(mInv.getArray());
		
		
		
		

    System.out.println("BruteForcePicking.intersectCylinder()");
  }

}
