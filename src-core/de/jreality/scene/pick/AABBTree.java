/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.scene.pick;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.CommonAttributes;

/**
 * An AABB tree for IndexedFaceSets.
 * <p>
 * <b>TODO:</b> the pick algorithm assumes that polygons are konvex...
 * this is easy to change, steal code from tims triangulate non convex poly...
 * </p>
 * @author Steffen Weissmann
 *
 */
public class AABBTree {

    /** The max number of triangles in a leaf. */
    private final int maxPerLeaf;

    /** Left tree. */
    private AABBTree left;

    /** Right tree. */
    private AABBTree right;

    /** Untransformed bounds of this tree. */
    private AABB bounds;

    /** Array of triangles this tree is indexing. */
    private TreePolygon[] tris;

    /** Start and end triangle indexes that this node contains. */
    private int myStart, myEnd;

    private boolean debug;
    
    private AABBTree(TreePolygon[] polygons, int maxPolysPerLeaf, int start, int end, boolean debug) {
      this.debug = debug;
      this.maxPerLeaf = maxPolysPerLeaf;
      this.tris = polygons;
      createTree(start, end);
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
        AABBTree ret = new AABBTree(tris, maxPolysPerLeaf, 0, tris.length-1, debug);
        for (int i = 0; i < tris.length; i++) {
            tris[i].centroid = null;
        }
        return ret;
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
        bounds.computeFromTris(tris, start, end);
        if (end - start < maxPerLeaf) return;
        else {
          splitTris(start, end);
					int half = (start + end) / 2;
          this.left = new AABBTree(tris, maxPerLeaf, start, half, debug);
					if (half < end) this.right = new AABBTree(tris, maxPerLeaf, half + 1, end, debug);
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
    void intersect(double[] from, double[] dir, ArrayList hits) {
      if (!bounds.intersects(from, dir)) {
        return;
      }
      if (left != null) {
        left.intersect(from, dir, hits);
      }
  
      if (right != null) {
        right.intersect(from, dir, hits);
      } else if (left == null) { // left == right == null
        boolean hit = false;
        TreePolygon tempt;
        double[] tempVa, tempVb, tempVc;
        for (int i = myStart; i <= myEnd; i++) {
          tempt = tris[i];
          poly: for (int j = 0; j < tempt.getNumTriangles(); j++) {
            double[][] tri = tempt.getTriangle(j);
            tempVa = tri[0];
            tempVb = tri[1];
            tempVc = tri[2];
            if (intersect(from, dir, tempVa, tempVb, tempVc)) {
              double[] plane = P3.planeFromPoints(null, tempVa, tempVb, tempVc);
              double[] pointObject = P3.lineIntersectPlane(null, from,
                  new double[] { dir[0], dir[1], dir[2], 0 }, plane);
              if (sign(from, dir, pointObject) == 1) {
                hits.add(new Object[]{pointObject, new Integer(tempt.getIndex()),new Integer(j)});
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
      sort(start, end, 2);
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
      sort(start, end, 1);
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
      sort(start, end, 0);
    }

    /**
    *
    * <code>sort</code> sorts the bounds of the tree.
    *
    * @param start
    *            the start index of the triangle list.
    * @param end
    *            the end index of the triangle list.
    */
   private void sort(int start, int end, int index) {
     double[] tmp=null;
     for (int i = start; i < end; i++) {
         tmp = Rn.subtract(tmp, tris[i].centroid, bounds.center);
         tris[i].projection = tmp[index];
     }
     Arrays.sort(tris, start, end, treeCompare);
       Arrays.sort(tris, start, end, treeCompare);
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
          // handle 4-vectors
          if (verts[0].length == 4)	
        	  this.verts = Pn.dehomogenize(new double[verts.length][3], verts);
        
           this.index=index;
        }
        TreePolygon(double[][] verts, int index, int subIndex) {
          this.verts = verts;
          if (verts[0].length == 4)	
        	  this.verts = Pn.dehomogenize(new double[verts.length][3], verts);
       
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
    private Comparator treeCompare = new Comparator() {

        public int compare(Object o1, Object o2) {
            TreePolygon a = (TreePolygon) o1;
            TreePolygon b = (TreePolygon) o2;
            if (a.projection < b.projection) { return -1; }
            if (a.projection > b.projection) { return 1; }
            return 0;
        }
    };
    
    /**
     * this is only for debugging and might be removed in future.
     * @return A component that contains the AABBs of the tree as
     * IndexedLineSets.
     */
    public SceneGraphComponent display() {
      SceneGraphComponent cmp = new SceneGraphComponent();
      Appearance app = new Appearance();
      app.setAttribute("showPoints", false);
      app.setAttribute("showLines", true);
      app.setAttribute("showFaces", false);
      cmp.setAppearance(app);
      display(cmp, Color.BLUE, Color.RED, true, 0.0001, 0.99);
      return cmp;
    }
    
    void display(SceneGraphComponent parent, Color leftColor, Color rightColor, boolean isLeft, double radius, double factor) {
      if (left != null) left.display(parent, leftColor.brighter(), rightColor.brighter(), true, radius*factor, factor);
      if (right != null) right.display(parent, leftColor.darker(), rightColor.darker(), false, radius*factor, factor);
      else if (left == null) { // leaf
        SceneGraphComponent myComp = new SceneGraphComponent();
        double[] t = bounds.center;
        double[] s = bounds.extent;
        double[] z=new double[]{0,0,1};
        Matrix m = MatrixBuilder.euclidean().translate(t)./*rotateFromTo(z, bounds.zAxis).*/scale(s[0], s[1], s[2]).getMatrix();
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
