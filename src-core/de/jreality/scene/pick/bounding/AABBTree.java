package de.jreality.scene.pick.bounding;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.*;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryListener;
import de.jreality.shader.CommonAttributes;

/**
 * TODO: the pick algorithm assumes that polygons are konvex...
 * this is easy to change, steal code from tims triangulate non convex poly...
 * 
 * @author gollwas
 *
 */
public class AABBTree {

    /** The max number of triangles in a leaf. */
    public final int maxPerLeaf;

    /** Left tree. */
    private AABBTree left;

    /** Right tree. */
    private AABBTree right;

    /** Untransformed bounds of this tree. */
    public AABB bounds;

    /** Array of triangles this tree is indexing. */
    private TreePolygon[] tris;

    /** Start and end triangle indexes that this node contains. */
    private int myStart, myEnd;

    private boolean debug;
    
    private final double eps;
    
    private final boolean pickFaces;
    private final boolean pickEdges;

    private AABBTree(TreePolygon[] polygons, int maxPolysPerLeaf, int start, int end, boolean pickFaces, boolean pickEdges, double eps, boolean debug) {
      this.debug = debug;
      this.maxPerLeaf = maxPolysPerLeaf;
      this.tris = polygons;
      this.pickFaces=pickFaces;
      this.pickEdges=pickEdges;
      this.eps = eps;
      createTree(start, end);
    }
    
    public static AABBTree constructVertexAABB(PointSet pointSet, double eps) {
      return constructVertexAABB(pointSet, eps, false);
    }    
    
    public static AABBTree constructVertexAABB(PointSet pointSet, double eps, boolean debug) {
      double[][][] polygons = getPointsAsPolygons(pointSet, eps);
      TreePolygon[] tris = new TreePolygon[polygons.length];
      for (int i = 0; i < tris.length; i++) {
        tris[i] = new TreePolygon(polygons[i], i);
        tris[i].putCentriod();
        tris[i].index = i;
      }
      // eps is hacked for line picking - should be used for vertices as well...
      AABBTree ret = new AABBTree(tris, 1, 0, tris.length-1, false, false, 0, debug);
      for (int i = 0; i < tris.length; i++) {
          tris[i].centroid = null;
      }
      return ret;
  }

    public static AABBTree constructEdgeAABB(IndexedLineSet lineSet, double eps) {
      return constructEdgeAABB(lineSet, eps, false);
    }    
    
    public static AABBTree constructEdgeAABB(IndexedLineSet lineSet, double eps, boolean debug) {
      TreePolygon[] tris = getEdgesAsPolygons(lineSet, eps);
      AABBTree ret = new AABBTree(tris, 1, 0, tris.length-1, false, true, eps, debug);
      for (int i = 0; i < tris.length; i++) {
          tris[i].centroid = null;
      }
      return ret;
  }

    public static AABBTree construct(IndexedFaceSet faceSet, int maxPolysPerLeaf) {
      return construct(faceSet, maxPolysPerLeaf, false);
    }
    /**
     * Recreates this OBBTree's information for the given TriMesh.
     *
     * @param parent
     *            The trimesh that this OBBTree should represent.
     */
    public static AABBTree construct(IndexedFaceSet faceSet, int maxPolysPerLeaf, boolean debug) {
        double[][][] polygons = getMeshAsPolygons(faceSet);
        TreePolygon[] tris = new TreePolygon[polygons.length];
        for (int i = 0; i < tris.length; i++) {
        	tris[i] = new TreePolygon(polygons[i], i);
          tris[i].putCentriod();
          tris[i].index = i;
        }
        AABBTree ret = new AABBTree(tris, maxPolysPerLeaf, 0, tris.length-1, true, false, 0, debug);
        for (int i = 0; i < tris.length; i++) {
            tris[i].centroid = null;
        }
        return ret;
    }
    
    public static void constructAndRegister(final IndexedFaceSet ifs, final SceneGraphComponent displayComponent, final int maxPolys) {
      constructAndRegister(ifs, displayComponent, maxPolys, false);
    }

    /**
     * 
     * @param ifs
     * @param maxPolys
     * @deprecated don't use this for dynamic geometries..
     */
    public static void constructAndRegister(final IndexedFaceSet ifs, final SceneGraphComponent displayComponent, final int maxPolys, final boolean debug) {
      final AABBTree first = construct(ifs, maxPolys, debug);
      ifs.setGeometryAttributes("AABBTree", first);
      ifs.addGeometryListener(new GeometryListener() {
        SceneGraphComponent displ;
        {
          if (displayComponent != null) {
            displ = first.display();
            displayComponent.addChild(displ);
          }
        }
        public void geometryChanged(GeometryEvent ev) {
          if (ev.getChangedVertexAttributes().contains(Attribute.COORDINATES)
              || ev.getChangedFaceAttributes().contains(Attribute.INDICES)) {
            final AABBTree newAABB = construct(ifs, maxPolys, debug);
            if (displayComponent != null) {
              if (displ != null) displayComponent.removeChild(displ);
              displ = newAABB.display();
              displayComponent.addChild(displ);
            }
            ev.enqueueWriter(new Runnable() {
              public void run() {
                ifs.setGeometryAttributes("AABBTree", newAABB);
              }
            });
          }
        }
      });
    }

    /**
     * 
     * @param ifs
     * @param maxPolys
     */
    public static void constructAndRegisterVertexAABB(final PointSet ifs, final double eps) {
      final AABBTree first = constructVertexAABB(ifs, eps);
      ifs.setGeometryAttributes("AABBTreeVertex", first);
      ifs.addGeometryListener(new GeometryListener() {
        public void geometryChanged(GeometryEvent ev) {
          if (ev.getChangedVertexAttributes().contains(Attribute.COORDINATES)) {
            AABBTree newAABB = constructVertexAABB(ifs, eps);
            ifs.setGeometryAttributes("AABBTreeVertex", newAABB);
          }
        }
      });
    }

    /**
     * 
     * @param ifs
     * @param maxPolys
     */
    public static void constructAndRegisterEdgeAABB(final IndexedLineSet ifs, final double eps) {
      final AABBTree first = constructEdgeAABB(ifs, eps);
      ifs.setGeometryAttributes("AABBTreeEdge", first);
      ifs.addGeometryListener(new GeometryListener() {
        public void geometryChanged(GeometryEvent ev) {
          if (ev.getChangedVertexAttributes().contains(Attribute.COORDINATES) || ev.getChangedEdgeAttributes().contains(Attribute.INDICES)) {
            AABBTree newAABB = constructEdgeAABB(ifs, eps);
            ifs.setGeometryAttributes("AABBTreeEdge", newAABB);
          }
        }
      });
    }

    private static double[][][] getMeshAsPolygons(IndexedFaceSet faceSet) {
      int numFaces = faceSet.getNumFaces();
      double[][][] ret = new double[numFaces][][];
      IntArrayArray faces = faceSet.getFaceAttributes(Attribute.INDICES).toIntArrayArray();
      DoubleArrayArray verts = faceSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
      for (int i = 0; i < numFaces; i++) {
        IntArray face = faces.getValueAt(i);
        int faceLength = face.getLength();
        ret[i] = new double[faceLength][];
        for (int j = 0; j < faceLength; j++) {
          DoubleArray vertex = verts.getValueAt(face.getValueAt(j));
          ret[i][j] = vertex.toDoubleArray(null);
        }
      }
      return ret;
    }

    private static double[][][] getPointsAsPolygons(PointSet pointSet, double eps) {
      int numPoints = pointSet.getNumPoints();
      double[][][] ret = new double[numPoints][][];
      DoubleArrayArray verts = pointSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
      double[] epsVec = new double[verts.getLengthAt(0)];
      for (int i = 0; i < epsVec.length; i++) epsVec[i]=eps;
      double[] tmp = new double[verts.getLengthAt(0)];
      for (int i = 0; i < numPoints; i++) {
        ret[i] = new double[2][];
        DoubleArray vertex = verts.getValueAt(i);
        tmp = vertex.toDoubleArray(tmp);
        ret[i][0]=Rn.subtract(null, tmp, epsVec);
        ret[i][1]=Rn.add(null, tmp, epsVec);
      }
      return ret;
    }

    private static TreePolygon[] getEdgesAsPolygons(IndexedLineSet lineSet, double eps) {
      int numEdges = lineSet.getNumEdges();
      LinkedList ret = new LinkedList();
      IntArrayArray edges = lineSet.getEdgeAttributes(Attribute.INDICES).toIntArrayArray();
      DoubleArrayArray verts = lineSet.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
      double[][] tmp = new double[2][]; // tmp storage for line segments
      for (int i = 0; i < numEdges; i++) {
        IntArray edge = edges.getValueAt(i);
        int faceLength = edge.getLength();
        DoubleArray startVertex = verts.getValueAt(edge.getValueAt(0));
        tmp[1]=startVertex.toDoubleArray(null);
        for (int j = 1; j < faceLength; j++) {
          DoubleArray endVertex = verts.getValueAt(edge.getValueAt(j));
          tmp[0] = tmp[1];
          tmp[1] = endVertex.toDoubleArray(null);
          TreePolygon tp = new TreePolygon((double[][]) tmp.clone(), i, j-1);
          tp.putCentriod();
          ret.add(tp);
        }
      }
      TreePolygon[] tris = new TreePolygon[ret.size()];
      tris = (TreePolygon[]) ret.toArray(tris);
      return tris;
    }

    /**
     * Creates an OBB tree recursivly from the tris's array of triangles.
     *
     * @param start
     *            The start index of the tris array, inclusive.
     * @param end
     *            The end index of the tris array, exclusive.
     */
    private void createTree(int start, int end) {
        myStart = start;
        myEnd = end;
  			bounds = new AABB();
        bounds.computeFromTris(tris, start, end, eps);
        if (end - start < maxPerLeaf) {
          if (pickEdges) bounds.computeFromEdge(tris[start].verts, eps);
          return;
        } else {
            splitTris(start, end);
						int half = (start + end) / 2;
            this.left = new AABBTree(tris, maxPerLeaf, start, half, pickFaces, pickEdges, eps, debug);
						if (half < end) this.right = new AABBTree(tris, maxPerLeaf, half + 1, end, pickFaces, pickEdges, eps, debug);
        }
    }

    /**
     * Stores in the given array list all indexes of triangle intersection
     * between this tree and a given ray.
     *
     * @param ray
     *            The ray to test this tree against.
     * @param hits
     *            The arraylist to hold indexes of this OBBTree's triangle
     *            intersections.
     */
    public void intersect(Matrix mat, double[] from, double[] dir, ArrayList hits) {
      if (debug) System.out.println("intersect");
      intersectImpl(mat, from, dir, hits, new AABB());
    }
    
    private void intersectImpl(Matrix mat, double[] from, double[] dir, ArrayList hits, AABB worldBounds) {
      bounds.transform(mat, worldBounds);
      if (!worldBounds.intersects(from, dir)) {
        boolean leaf = (left == null && right == null);
        if (debug) System.out.println("Missed world bounds ["+(leaf?"leaf]":"no leaf]"));
        if (debug && leaf) {
          System.out.println("************");
          System.out.println("Matrix:\n"+mat);
          System.out.println("************");
          System.out.println("Bounds:\n"+bounds);
          System.out.println("************");
          System.out.println("WorldBounds:\n"+worldBounds);
          System.out.println("************");
          System.out.println("************");
        }
        return;
      }
      if (left != null) {
        left.intersectImpl(mat, from, dir, hits, worldBounds);
      }
  
      if (right != null) {
        right.intersectImpl(mat, from, dir, hits, worldBounds);
      } else if (left == null) { // left == right == null
        if (pickFaces) {
          boolean hit = false;
          TreePolygon tempt;
          double[] tempVa, tempVb, tempVc;
          for (int i = myStart; i <= myEnd; i++) {
            tempt = tris[i];
            poly: for (int j = 0; j < tempt.getNumTriangles(); j++) {
              double[][] tri = tempt.getTriangle(j);
              tempVa = mat.multiplyVector(tri[0]);
              tempVb = mat.multiplyVector(tri[1]);
              tempVc = mat.multiplyVector(tri[2]);
              if (intersect(from, dir, tempVa, tempVb, tempVc)) {
                double[] plane = P3.planeFromPoints(null, tempVa, tempVb, tempVc);
                double[] pointWorld = P3.lineIntersectPlane(null, from,
                    new double[] { dir[0], dir[1], dir[2], 0 }, plane);
                if (sign(from, dir, pointWorld) == 1) {
                  hits.add(new Object[]{pointWorld, new Integer(tempt.getIndex()),new Integer(j)});
                  hit = true;
                  break poly;
                } else {
                  if (debug) 
                    System.out.println("negative hit!");
                }
              }
            }
          }
          if (debug) {
            System.out.println("scanned polys: "+((hit)?"hit":"no hit"));
          }
        } else {
          TreePolygon tp = tris[myStart];
          if (pickEdges) {
            // pick edges
            hits.add(new int[]{tp.index, tp.subIndex});
          } else {
            // pick vertex
            hits.add(new Integer(tp.getIndex()));
          }
        }
      }
    }

    private int sign(double[] camPos, double[] dir, double[] pointWorld) {
      double dx = pointWorld[0]-camPos[0];
      double dy = pointWorld[1]-camPos[1];
      double dz = pointWorld[2]-camPos[2];
      double rx = dir[0];
      double ry = dir[1];
      double rz = dir[2];
      int sign = 1;
      if (rx != 0) sign = (int) ( (dx/rx) / Math.abs(dx/rx) ); 
      if (ry != 0) sign = (int) ( (dy/ry) / Math.abs(dy/ry) ); 
      if (rz != 0) sign = (int) ( (dz/rz) / Math.abs(dz/rz) ); 
      return sign;
    }

    /**
     * tests if the triangle intersects the the given ray 
     * @param from ray start
     * @param dir ray dir
     * @param v0 triag p0
     * @param v1 triag p1
     * @param v2 triag p2
     * @return true if intersects
     */
    private boolean intersect(double[]from, double[] dir, double[] v0,double[] v1,double[] v2){
      double[] edge1=Rn.subtract(null, v1, v0);
      double[] edge2=Rn.subtract(null, v2, v0);
      double[] pvec=Rn.crossProduct(null, dir, edge2);
      double det=Rn.innerProduct(edge1, pvec);
      if (det > -Rn.TOLERANCE && det < Rn.TOLERANCE)
          return false;
      det=1/det;
      double[] tvec=Rn.subtract(null, from, v0);
      double u=Rn.innerProduct(tvec, pvec)*det;
      if (u <0.0 || u>1.0)
          return false;
      double[] qvec=Rn.crossProduct(null, tvec, edge1);
      double v=Rn.innerProduct(dir, qvec) * det;
      if (v <0.0 || v + u >1.0)
          return false;
      return true;
    }

    
    /**
     * Splits the root obb acording to the largest bounds extent.
     *
     * @param start
     *            Start index in the tris array, inclusive, that is the OBB to
     *            split.
     * @param end
     *            End index in the tris array, exclusive, that is the OBB to
     *            split.
     */
    private void splitTris(int start, int end) {
        if (bounds.extent[0] > bounds.extent[1]) {
            if (bounds.extent[0] > bounds.extent[2])
                sortX(start, end);
            else
                sortZ(start, end);
        } else {
            if (bounds.extent[1] > bounds.extent[2])
                sortY(start, end);
            else
                sortZ(start, end);
        }
    }

    /**
     *
     * <code>sortZ</code> sorts the z bounds of the tree.
     *
     * @param start
     *            the start index of the triangle list.
     * @param end
     *            the end index of the triangle list.
     */
    private void sortZ(int start, int end) {
      double[] tmp=null;
        for (int i = start; i < end; i++) {
            tmp = Rn.subtract(tmp, tris[i].centroid, bounds.center);
            tris[i].projection = Rn.innerProduct(bounds.zAxis, tmp);
        }
        Arrays.sort(tris, start, end, new TreeCompare());
    }

    /**
     *
     * <code>sortY</code> sorts the y bounds of the tree.
     *
     * @param start
     *            the start index of the triangle list.
     * @param end
     *            the end index of the triangle list.
     */
    private void sortY(int start, int end) {
      double[] tmp=null;
      for (int i = start; i < end; i++) {
          tmp = Rn.subtract(tmp, tris[i].centroid, bounds.center);
          tris[i].projection = Rn.innerProduct(bounds.yAxis, tmp);
      }
      Arrays.sort(tris, start, end, new TreeCompare());
        Arrays.sort(tris, start, end, new TreeCompare());
    }

    /**
     *
     * <code>sortX</code> sorts the x bounds of the tree.
     *
     * @param start
     *            the start index of the triangle list.
     * @param end
     *            the end index of the triangle list.
     */
    private void sortX(int start, int end) {
      double[] tmp=null;
      for (int i = start; i < end; i++) {
          tmp = Rn.subtract(tmp, tris[i].centroid, bounds.center);
          tris[i].projection = Rn.innerProduct(bounds.xAxis, tmp);
      }
      Arrays.sort(tris, start, end, new TreeCompare());
        Arrays.sort(tris, start, end, new TreeCompare());
    }

    /**
     * This class is simply a container for a triangle.
     */
    static class TreePolygon {

        private double[][] verts;

        private double projection;

        private int index;
        private int subIndex; // for lines

        double[] centroid;

        TreePolygon(double[][] verts, int index) {
          this.verts = verts;
          this.index=index;
        }
        TreePolygon(double[][] verts, int index, int subIndex) {
          this.verts = verts;
          this.index=index;
          this.subIndex=subIndex;
        }

        void putCentriod() {
            int count = verts.length;
            centroid = Rn.copy(null, verts[0]);
            for (int i = 1; i < count; i++) Rn.add(centroid, centroid, verts[i]);
            Rn.times(centroid, 1./count, centroid);
        }
        
        int getNumTriangles() {
          return verts.length-2;
        }
        
        double[][] getTriangle(int i) {
          return new double[][]{verts[0], verts[i+1], verts[i+2]};
        }
        
        double[][] getVertices() {
          return verts;
        }

        public int getIndex() {
          return index;
        }
        
    }

    /**
     * Class to sort TreeTriangle acording to projection.
     */
    static class TreeCompare implements Comparator {

        public int compare(Object o1, Object o2) {
            TreePolygon a = (TreePolygon) o1;
            TreePolygon b = (TreePolygon) o2;
            if (a.projection < b.projection) { return -1; }
            if (a.projection > b.projection) { return 1; }
            return 0;
        }
    }
    
    public SceneGraphComponent display() {
      SceneGraphComponent ret = new SceneGraphComponent();
      Appearance a = new Appearance();
      a.setAttribute(CommonAttributes.FACE_DRAW, false);
      a.setAttribute(CommonAttributes.EDGE_DRAW, true);
      a.setAttribute("lineShader."+CommonAttributes.TUBES_DRAW, true);
      ret.setAppearance(a);
      display(ret, Color.green, Color.green, true, 0.005, 1);
      return ret;
    }
    
    public void display(SceneGraphComponent parent, Color leftColor, Color rightColor, boolean isLeft, double radius, double factor) {
      if (left != null) left.display(parent, leftColor.brighter(), rightColor.brighter(), true, radius*factor, factor);
      if (right != null) right.display(parent, leftColor.darker(), rightColor.darker(), false, radius*factor, factor);
      else if (left == null) { // leaf
        SceneGraphComponent myComp = new SceneGraphComponent();
        double[] t = bounds.center;
        double[] s = bounds.extent;
        double[] z=new double[]{0,0,1};
        Matrix m = MatrixBuilder.euclidian().translate(t).rotateFromTo(z, bounds.zAxis).scale(s[0], s[1], s[2]).getMatrix();
        boolean printed = false;
        IndexedFaceSet box = new IndexedFaceSet();
        double[][] verts = new double[8][3];
        box.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(verts);
        for (int i = 0; i < 8; i++) {
          verts[i] = m.multiplyVector(verts[i]);
          if (Rn.euclideanNorm(verts[i]) < 0.0001) {
            if (!printed) {
              System.out.println("trans: "+Rn.toString(t));
              System.out.println("scale: "+Rn.toString(s));
              System.out.println("Matrix: \n"+m);
              printed = true;
            }
            System.out.println(i+">>"+Rn.toString(verts[i]));
          }
        }
        box.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(verts));
        myComp.setGeometry(box);
        IndexedFaceSetUtility.calculateAndSetEdgesFromFaces(box);
        GeometryUtility.calculateAndSetNormals(box);
        Appearance a = new Appearance();
        a.setAttribute("lineShader.diffuseColor", isLeft ? leftColor : rightColor);
        a.setAttribute("lineShader."+CommonAttributes.TUBE_RADIUS, radius);
        myComp.setAppearance(a);
        parent.addChild(myComp);
      }
    }
}
