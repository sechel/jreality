package de.jreality.scene.pick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Cylinder;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;

// TODO: currently 3vectors (from, to) are NOT supported

class BruteForcePicking {

  private static Matrix m=new Matrix();
  private static Matrix mInv=new Matrix();
  
  //private static double[] bary = new double[4], p1=new double[4], p2=new double[4], p3=new double[4], plane=new double[4], pobj=new double[4];
  public static void intersectPolygons(IndexedFaceSet ifs, int signature, SceneGraphPath path, double[] from, double[] to, ArrayList hits) {
    path.getMatrix(m.getArray());
    path.getInverseMatrix(mInv.getArray()); 
    
    double[] from4=mInv.multiplyVector(from);
    double[] dir4=mInv.multiplyVector(to);
    double[] bary = new double[4], p1=new double[4], p2=new double[4], p3=new double[4], plane=new double[4], pobj=new double[4];
    p1[3]=p2[3]=p3[3]=1;
    IntArrayArray faces=getFaces(ifs);
    DoubleArrayArray points = getPoints(ifs);
    if (faces == null || points == null || faces.getLength() == 0 || points.getLength() == 0 ) return;
        
    for (int i = 0, n=faces.getLength(); i<n; i++) {
      IntArray face = faces.getValueAt(i);
      // simple triangulation:
      tris: for (int j=0, c=face.getLength()-2; j<c; j++) {
        p1=points.getValueAt(face.getValueAt(0)).toDoubleArray(p1);
        p2=points.getValueAt(face.getValueAt(1+j)).toDoubleArray(p2);
        p3=points.getValueAt(face.getValueAt(2+j)).toDoubleArray(p3);
        
        plane = P3.planeFromPoints(plane, p1, p2, p3);
        pobj = P3.lineIntersectPlane(pobj, from4, dir4, plane);
        if(pobj[3]*pobj[3]<Rn.TOLERANCE) continue; // parallel

        Pn.dehomogenize(p1, p1);
        Pn.dehomogenize(p2, p2);
        Pn.dehomogenize(p3, p3);
        
        if (!Hit.convertToBary(bary, p1, p2, p3, pobj)) continue;
        if (
            (     (bary[0]<0||bary[0]>1)
                ||(bary[1]<0||bary[1]>1)
                ||(bary[2]<0||bary[2]>1)
            )||((bary[0]+bary[1]+bary[2]-1)*(bary[0]+bary[1]+bary[2]-1)> Rn.TOLERANCE)) {
          continue tris;          
        }
        // TODO: the barycentric coordinates should be used in the PickResult for tex coordindates
        // check if for pobj = from4 + lambda * dir4: lambda >= 0
        if (Rn.innerProduct(from4, dir4) > Rn.innerProduct(pobj, dir4)) continue tris;
        double[] pw = m.multiplyVector(pobj);
        hits.add(new Hit(path.pushNew(ifs), pw, Rn.euclideanDistance(from, pw), 0, PickResult.PICK_TYPE_FACE, i,j));
      }
    }

  }

  public static void intersectEdges(IndexedLineSet ils, int signature, SceneGraphPath path, double[] from, double[] to, double tubeRadius, ArrayList localHits) {
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
    
    IntArrayArray edges=getEdges(ils);
    DoubleArrayArray points = getPoints(ils);
    if (points == null || edges == null || edges.getLength() == 0 || points.getLength() == 0 ) return;
    
    IntArray edge = edges.getValueAt(0);
    
    DoubleArray point = points.getValueAt(0);
    boolean vec3 = point.getLength() == 3;
    double[] vertex1 = new double[3];
    double[] vertex2 = new double[3];
    double[] vecRaw=vec3?null:new double[4];
//    if(edge.getLength()>=2){
//      points.getValueAt(edge.getValueAt(0)).toDoubleArray(vertex1);
//      points.getValueAt(edge.getValueAt(1)).toDoubleArray(vertex2);
//    }
    
    LinkedList MY_HITS = new LinkedList();
    
    for(int i=0, m=edges.getLength();i<m;i++){
      edge = edges.getValueAt(i);
      for(int j=0, n=edge.getLength()-1;j<n;j++){
        if (vec3) {
          points.getValueAt(edge.getValueAt(j)).toDoubleArray(vertex1);
          points.getValueAt(edge.getValueAt(j+1)).toDoubleArray(vertex2);
        } else {
          points.getValueAt(edge.getValueAt(j)).toDoubleArray(vecRaw);
          Pn.dehomogenize(vertex1, vecRaw);
          points.getValueAt(edge.getValueAt(j+1)).toDoubleArray(vecRaw);
          Pn.dehomogenize(vertex2, vecRaw);
        }
        intersectCylinder(MY_HITS,fromOb3,dirOb3,vertex1,vertex2,tubeRadius);
        for (Iterator it = MY_HITS.iterator(); it.hasNext(); ) {
          double[] hitPoint = (double[]) it.next();
          it.remove();
          double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
          // TODO: pass index j (index in the edge) to the Hit?
          Hit h = new Hit(path.pushNew(ils), hitPoint, dist ,0 , PickResult.PICK_TYPE_LINE, i, -1);
          localHits.add(h);
        }       
      } 
    }
  }

  private static DoubleArrayArray getPoints(PointSet ils) {
    DataList dl = ils.getVertexAttributes(Attribute.COORDINATES);
    if (dl == null) return null;
    return dl.toDoubleArrayArray();
  }
  
  private static IntArrayArray getEdges(IndexedLineSet ils) {
    DataList dl = ils.getEdgeAttributes(Attribute.INDICES);
    if (dl == null) return null;
    return dl.toIntArrayArray();
  }

  private static IntArrayArray getFaces(IndexedFaceSet ils) {
    DataList dl = ils.getFaceAttributes(Attribute.INDICES);
    if (dl == null) return null;
    return dl.toIntArrayArray();
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

    DoubleArrayArray points = getPoints(ps);
    if (points == null || points.getLength() == 0) return;
    DoubleArray point = points.getValueAt(0);
    boolean vec3 = point.getLength() == 3;
    double[] vertexRaw = vec3 ? new double[3] : new double[4];
    double[] vertex = new double[3];
    
    LinkedList MY_HITS = new LinkedList();
    
    for (int j = 0, n=points.getLength(); j<n; j++) {
      if (!vec3) {
        points.getValueAt(j).toDoubleArray(vertexRaw);  
        if (vertexRaw[3]==0) continue;
        Pn.dehomogenize(vertex, vertexRaw);
      } else {
        points.getValueAt(j).toDoubleArray(vertex);  
      }
      intersectSphere(MY_HITS, vertex, fromOb3, dirOb3, pointRadius);
      for (Iterator i = MY_HITS.iterator(); i.hasNext(); ) {
        double[] hitPoint = (double[]) i.next();
        hitPoint = m.multiplyVector(hitPoint);
        i.remove();
        double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
        Hit h = new Hit(path.pushNew(ps), hitPoint, dist ,0 , PickResult.PICK_TYPE_POINT, j,-1);
        localHits.add(h);
      }       
    }
    
  }


  private static final List SPHERE_HIT_LIST=new LinkedList();
  public static void intersectSphere(Sphere sphere, int signature, SceneGraphPath path, double[] from, double[] to, ArrayList localHits) {
    
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
    
    intersectSphere(SPHERE_HIT_LIST, null, fromOb3, dirOb3, 1);
    
    for (Iterator i = SPHERE_HIT_LIST.iterator(); i.hasNext(); ) {
      double[] hitPoint = (double[]) i.next();
      hitPoint = m.multiplyVector(hitPoint);
      i.remove();
        double dist=Rn.euclideanNorm(Rn.subtract(null,hitPoint,from));
      Hit h = new Hit(path.pushNew(sphere), hitPoint, dist ,0 , PickResult.PICK_TYPE_OBJECT, -1,-1);
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
    Rn.normalize(dir,dir);
    if(vertex!=null) from=Rn.subtract(null,from,vertex);
    
    double b=2*Rn.innerProduct(dir,from);
    double c=Rn.euclideanNormSquared(from)-r*r;   
    double dis=Math.pow(b,2)-4*c;
    if(dis>=0){
      dis=Math.sqrt(dis);
      double t=(-b-dis)/2;
      for (int i=0; i<2; i++) { // avoid duplicate code
        t+=i*dis;
        if(t>=0){
          double[] hitPointOb3=new double[3];
          Rn.times(hitPointOb3,t,dir);
          Rn.add(hitPointOb3,hitPointOb3,from); //from+t*dir
          if (vertex != null) Rn.add(hitPointOb3, hitPointOb3, vertex);
          double[] hitPoint=new double[4];
          Pn.homogenize(hitPoint,hitPointOb3);
          //hitPoint=m.multiplyVector(hitPoint);
          hits.add(hitPoint); 
        }
      }
    }
  }
  
  private static final List CYLINDER_HIT_LIST=new LinkedList();
  public static void intersectCylinder(Cylinder cylinder, int signature, SceneGraphPath path, double[] from, double[] to, ArrayList localHits) {
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
      Hit h = new Hit(path.pushNew(cylinder), hitPoint, dist ,0 , PickResult.PICK_TYPE_OBJECT, -1,-1);
      localHits.add(h);
    }  
  }

  /**
   * 
   * @param hits
   * @param f=from in local coordinates
   * @param d=dir in local coordinates !!!!!MUST BE NORMALIZED !!!!
   * @param v1 upper point of cylinder-axis
   * @param v2 lower point of cylinder-axis
   * @param r in local coordinates, radius
   */
  private static void intersectCylinder(List hits, final double[] f, final double[] d, final double[] v1, final double[] v2, double r) {

    
    boolean debug=false;
    long time=System.currentTimeMillis();
    /*
    //Methode1:
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
      //System.out.println("object transformed");
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
          
          if(debug){
            System.out.println("Methode1: cylinder matched_1");
            System.out.println("Methode1: hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
          }
            
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
          
          if(debug){
            System.out.println("Methode1: cylinder matched_2");
            System.out.println("Methode1: hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
          }
            
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
    } 
    
    if(debug){
      System.out.println("Methode1: "+(System.currentTimeMillis()-time)+" ms");
    }
    
    
    time=System.currentTimeMillis();
    
    */
    
    
    //Methode2:
    
    double[] from=f;
    double[] dir=d;
    //from=f;
    //dir=d;
    Rn.normalize(dir,dir);
    double[] dir_cyl=Rn.subtract(null,v2,v1);
    Rn.normalize(dir_cyl,dir_cyl);
    
    double[] dir_cyl_x_dir=Rn.crossProduct(null,dir_cyl,dir);
    double[] from_min_v1=Rn.subtract(null,from,v1); 
    double lambda=Rn.innerProduct(dir_cyl_x_dir,Rn.crossProduct(null,from_min_v1,dir_cyl))/Rn.euclideanNormSquared(dir_cyl_x_dir);
    double[] nearest=Rn.add(null,from,Rn.times(null,lambda,dir));
    double dist=Math.abs(Rn.innerProduct(dir_cyl_x_dir,from_min_v1))/Rn.euclideanNorm(dir_cyl_x_dir);
    
    if(dist<=r && (nearest[0]-from[0])/dir[0]>=0){        
      double angle=Math.abs(Rn.euclideanAngle(dir,dir_cyl));
      if(Math.cos(angle)!=0){  //sonst Sehstrahl parallel zu Zylinder-Achse
        if(angle>Math.PI) angle=2*Math.PI-angle;
        if(angle>Math.PI/2) angle=Math.PI-angle;        
        double factor=Math.sqrt(Math.pow(r,2)-Math.pow(dist,2))/Math.cos(Math.PI/2-angle);      
        double maxDist=Math.sqrt(Math.pow(Rn.euclideanDistance(v1,v2),2)+Math.pow(r,2));  //mx Abstand hit von v1,v2: hitPoint zwischen v1 und v2 auf Cylinder?
        
        double[] hitPointOb3=new double[3];
        Rn.times(hitPointOb3,-factor,dir);
        Rn.add(hitPointOb3,hitPointOb3,nearest); //nearest-factor*dir       
        if((hitPointOb3[0]-from[0])/dir[0]>=0 && (Rn.euclideanDistance(hitPointOb3,v1)<maxDist && Rn.euclideanDistance(hitPointOb3,v2)<maxDist)){ //vor oder hinter from && hitPoint zwischen v1 und v2 auf Cylinder    
          
          if(debug){
            System.out.println("Methode2: cylinder matched_1");
            System.out.println("Methode2: hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
          }
          
          double[] hitPoint=new double[4];
          Pn.homogenize(hitPoint,hitPointOb3);
          hitPoint=m.multiplyVector(hitPoint);
          hits.add(hitPoint); 
        }       

        Rn.times(hitPointOb3,factor,dir);
        Rn.add(hitPointOb3,hitPointOb3,nearest); //nearest+factor*dir
        if((hitPointOb3[0]-from[0])/dir[0]>=0 && (Rn.euclideanDistance(hitPointOb3,v1)<maxDist && Rn.euclideanDistance(hitPointOb3,v2)<maxDist)){ //vor oder hinter from && hitPoint zwischen v1 und v2 auf Cylinder  
          
          if(debug){
            System.out.println("Methode2: cylinder matched_2");
            System.out.println("Methode2: hitPointOb: "+hitPointOb3[0]+", "+hitPointOb3[1]+", "+hitPointOb3[2]);
          }
            
          double[] hitPoint=new double[4];
          Pn.homogenize(hitPoint,hitPointOb3);
          hitPoint=m.multiplyVector(hitPoint);
          hits.add(hitPoint); 
        }         
      }   
    }
    
    if(debug){
      System.out.println("Methode2: "+(System.currentTimeMillis()-time)+" ms");
      System.out.println("");
    }
    
  }
}
