package de.jreality.scene.pick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
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
	  System.out.println("BruteForcePicking.intersectPoints()");
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
   * @param dir in local coordinates !!!!!MUST BE NORMALIZED !!!!
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
		  if(t>=0){
			  double[] hitPointOb3=new double[3];
			  Rn.times(hitPointOb3,t,dir);
			  Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir
			  double[] hitPoint=new double[4];
			  Pn.homogenize(hitPoint,hitPointOb3);
			  hitPoint=m.multiplyVector(hitPoint);
			  hits.add(hitPoint);	
		  }
		  t=t+dis; //t=(-b+dis)/2;
		  if(t>=0){
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
  
  private static final List CYLINDER_HIT_LIST=new LinkedList();
  public static void intersectCylinder(int signature, SceneGraphPath path, double[] from, double[] to, ArrayList localHits) {
	  System.out.println("BruteForcePicking.intersectCylinder()");
	  
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
	  
	  intersectCylinder(CYLINDER_HIT_LIST, fromOb3, dirOb3, new double[] {0,0,1} , new double[] {0,0,-1}, 1);
	  
	  for (Iterator i = CYLINDER_HIT_LIST.iterator(); i.hasNext(); ) {
		  double[] hitPoint = (double[]) i.next();
		  i.remove();
    	  double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
		  Hit h = new Hit(SceneGraphPath.fromList(path.toList()), hitPoint, dist ,0 , PickResult.PICK_TYPE_OBJECT, -1,-1);
		  localHits.add(h);
	  }	 
  }

  private static void intersectCylinder(List hits, final double[] f, final double[] d, final double[] v1, final double[] v2, int r) {

	  double[] from;
	  double[] dir;
	  double[] top={0,0,1};
	  double[] bottom={0,0,-1};
	  boolean objTransformed;
	  Matrix objTrans=new Matrix();
	  if(!((v1[0]==top[0]&&v1[1]==top[1]&&v1[2]==top[2] && v2[0]==bottom[0]&&v2[1]==bottom[1]&&v2[2]==bottom[2]) || (v1[0]==bottom[0]&&v1[1]==bottom[1]&&v1[2]==bottom[2] && v2[0]==top[0]&&v2[1]==top[1]&&v2[2]==top[2]))){
		  double[] cNeg=Rn.times(null,-0.5,Rn.add(null,v1,v2));
		  double[] aCentered=Rn.normalize(null,Rn.add(null,v1,cNeg));
		  MatrixBuilder.euclidian().scale(1/r,1/r,2/Rn.euclideanDistance(v1,v2)).rotateFromTo(aCentered, new double[] {0,0,1}).translate(cNeg).assignTo(objTrans);
		  from=objTrans.multiplyVector(f);
		  dir=objTrans.multiplyVector(d);
		  objTransformed=true;
		  System.out.println("object transformed");
	  }else{
		  from=f;
		  dir=d;
		  objTransformed=false;
	  }
	  
	  double a=Math.pow(dir[0],2)+Math.pow(dir[1],2);
	  double b=2*(from[0]*dir[0]+from[1]*dir[1]);
	  double c=Math.pow(from[0],2)+Math.pow(from[1],2)-1;
	  
	  double dis=Math.pow(b,2)-4*a*c;
	  if(dis>=0){
		  double t=(-b-Math.sqrt(dis))/(2*a);
		  if(t>=0){
			  double[] hitPointOb3=new double[3];
			  Rn.times(hitPointOb3,t,dir);
			  Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir			  
			  if(hitPointOb3[2]<top[2]&&hitPointOb3[2]>bottom[2]){
				  
				  System.out.println("cylinder matched_1");
				  System.out.println("hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
				  
				  double[] hitPoint=new double[4];
				  Pn.homogenize(hitPoint,hitPointOb3);
				  hitPoint=m.multiplyVector(hitPoint);
				  if(objTransformed){
					  Matrix objTransInv=objTrans.getInverse();
					  hitPoint=objTransInv.multiplyVector(hitPoint);
				  }
				  hits.add(hitPoint);	
			  }
		  }
		  t=(-b+Math.sqrt(dis))/(2*a);
		  if(t>=0){
			  double[] hitPointOb3=new double[3];
			  Rn.times(hitPointOb3,t,dir);
			  Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir
			  if(hitPointOb3[2]<top[2]&&hitPointOb3[2]>bottom[2]){ 
				  
				  System.out.println("cylinder matched_2");
				  System.out.println("hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
				  
				  double[] hitPoint=new double[4];
				  Pn.homogenize(hitPoint,hitPointOb3);
				  hitPoint=m.multiplyVector(hitPoint);
				  if(objTransformed){
					  Matrix objTransInv=objTrans.getInverse();
					  hitPoint=objTransInv.multiplyVector(hitPoint);
				  }
				  hits.add(hitPoint);	
			  }			  
		  }
		  System.out.println("");		  
	  }	
  }

}
