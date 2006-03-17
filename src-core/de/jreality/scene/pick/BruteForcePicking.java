package de.jreality.scene.pick;

import java.util.ArrayList;

import de.jreality.math.Matrix;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;

class BruteForcePicking {

  public static void intersectPolygons(IndexedFaceSet ifs, int signature, Matrix m, double[] from, double[] to, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectPolygons()");
  }

  public static void intersectEdges(IndexedLineSet ils, int signature, Matrix m, double[] from, double[] to, double tubeRadius, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectEdges()");
  }

  public static void intersectPoints(PointSet ps, int signature, Matrix m, double[] from, double[] to, double pointRadius, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectPoints()");
  }

  public static void intersectSphere(int signature, Matrix m, double[] from, double[] to, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectSphere()");
  }

  public static void intersectCylinder(int signature, Matrix m, double[] from, double[] to, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectCylinder()");
  }

}
