package de.jreality.scene.pick;

import java.util.ArrayList;

import de.jreality.math.Matrix;

class BruteForcePicking {

  /**
   * used from AABBPickSystem, from, to see PickSystem. hits are added to the List, as Object[], with the
   * following content:
   * {double[3/4] worldCoords, int faceIndex, int triangleIndex}
   */
  public static void intersectPolygons(int euclidean, Matrix m, double[] from, double[] to, ArrayList hits) {
    System.out.println("BruteForcePicking.intersectPolygons()");
  }

  public static void intersectEdges(int euclidean, Matrix m, double[] from, double[] to, double tubeRadius, ArrayList localHits) {
    System.out.println("BruteForcePicking.intersectEdges()");
  }

  public static void intersectPoints(int signature, Matrix m, double[] from, double[] to, double pointRadius, ArrayList localHits) {
    System.out.println("BruteForcePicking.intersectPoints()");
  }

}
