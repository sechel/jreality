package de.jreality.scene.pick;

import java.util.ArrayList;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;

class BruteForcePicking {

	private static Matrix m=new Matrix();
	
  public static void intersectPolygons(IndexedFaceSet ifs, int signature, SceneGraphPath path, double[] from, double[] to, ArrayList hits) {
	path.getMatrix(m.getArray());
    System.out.println("BruteForcePicking.intersectPolygons()");
  }


  public static void intersectEdges(IndexedLineSet ils, int signature, SceneGraphPath path, double[] from, double[] to, double tubeRadius, ArrayList localHits) {
		path.getMatrix(m.getArray());

    System.out.println("BruteForcePicking.intersectEdges()");
  }

  public static void intersectPoints(PointSet ps, int signature, SceneGraphPath path, double[] from, double[] to, double pointRadius, ArrayList hits) {
		path.getMatrix(m.getArray());
    System.out.println("BruteForcePicking.intersectPoints()");
  }


  public static void intersectSphere(int signature, SceneGraphPath path, double[] from, double[] to, ArrayList localHits) {
	
	path.getMatrix(m.getArray());

    double[] center=m.multiplyVector(new double[] {0,0,0,1});
    
    
    double[] from3=new double[3];
    double[] to3=new double[3];
    double[] dir3=new double[3];
    
    if (from.length > 3){
    	P3.dehomogenize(from3, from);
    	P3.dehomogenize(to3, to);
    	
    	if(to[3]==0){
    		dir3=to3;
    	}else{
    		Rn.subtract(dir3,to3,from3);
    	}  	
    	
    	P3.dehomogenize(center, center);
    	center=new double[]{center[0], center[1], center[2]}; 
    }   
    
    //Naechster Punkt und Abstand Zentrum-Strahl in Weltkoordinaten
    double t=Rn.innerProduct(Rn.subtract(null,center,from3),dir3)/Rn.euclideanNormSquared(dir3);    
    double[] nearestPoint=Rn.add(null,from3,Rn.times(null,t,dir3));    
    double distRay=Rn.euclideanNorm(Rn.subtract(null,nearestPoint,center));
    
    //Hit-Punkt
    to=m.multiplyVector(new double[] {1,0,0,1});  //temp fuer Radiusberechnung
    double[] hit=new double[3];  //temp fuer Radiusberechnung
    P3.dehomogenize(hit,to);
    double radius=Rn.euclideanNorm(Rn.subtract(null,hit,center));
    if(radius>=distRay){
    	double s=Math.sqrt(Math.pow(radius,2)-Math.pow(distRay,2));
    	Rn.add(hit,nearestPoint,Rn.times(null,-s/Rn.euclideanNorm(dir3),dir3));
    	
       //Hit h = new Hit(path.pushNew(ps), pointWorld, dist ,distRay , PickResult.PICK_TYPE_POINT, 0,-1);
       //localHits.add(h);
    	
    	
    }else{///////////TEST
    	hit[0]=Math.sqrt(-1); hit[1]=Math.sqrt(-1); hit[2]=Math.sqrt(-1);    	
    }
    
    
    
    System.out.println("Sphere: center: "+center[0]+", "+center[1]+", "+center[2]);
    System.out.println("Sphere: nearestPoint: "+nearestPoint[0]+", "+nearestPoint[1]+", "+nearestPoint[2]);
    System.out.println("Sphere: distRay: "+distRay);
    System.out.println("Sphere: radius: "+radius);
    System.out.println("Sphere: hitPoint: "+hit[0]+", "+hit[1]+", "+hit[2]);
    double[] testRad=Rn.subtract(null,hit,center);
    System.out.println("Sphere: Test: Abstand Hit-Center: "+Rn.euclideanNorm(testRad));   
    
    System.out.println("");
  }


  public static void intersectCylinder(int signature, SceneGraphPath path, double[] from, double[] to, ArrayList hits) {
		path.getMatrix(m.getArray());

    System.out.println("BruteForcePicking.intersectCylinder()");
  }

}
