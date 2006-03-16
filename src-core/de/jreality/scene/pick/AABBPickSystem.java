package de.jreality.scene.pick;

import java.util.*;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.scene.data.*;
import de.jreality.scene.pick.bounding.AABB;
import de.jreality.scene.pick.bounding.AABBTree;
import de.jreality.util.PickUtility;

public class AABBPickSystem implements PickSystem {
  
  private Impl impl;
  private SceneGraphComponent root;
  
  private double[] fromEuclidean;
  private double[] dirEuclidean;
  private double maxDist;
  
  private AABB tmpAABB=new AABB();
  
  private ArrayList hits = new ArrayList();
  
  private Comparator cmp = new HitComparator();
  
  final boolean defaultBuildTree=true;
  private double[] from;
  private double[] to;
  
  public void setSceneRoot(SceneGraphComponent root) {
    impl= new Impl();
    this.root=root;
  }
  
  public List computePick(double[] from, double[] to) {
    if (to.length < 4 || to[3] == 0) return computePickImpl(from, to, 1000);
    double[] dir = new double[3];
    if (from.length > 3) P3.dehomogenize(from, from);
    P3.dehomogenize(to, to);
    dir[0] = to[0]-from[0];
    dir[1] = to[1]-from[1];
    dir[2] = to[2]-from[2];
    this.from=from;
    this.to=to;
    return computePickImpl(from, dir, Rn.euclideanNorm(dir));
  }
  
  private List computePickImpl(double[] from, double[] dir, double maxDist) {
    this.fromEuclidean=new double[]{from[0], from[1], from[2]};
    this.dirEuclidean=new double[]{dir[0], dir[1], dir[2]};
    this.maxDist=maxDist;
    impl.visit();
    if (hits.isEmpty()) return Collections.EMPTY_LIST;
    List tmp = hits;
    hits = new ArrayList();
    Collections.sort(tmp, cmp);
    if (hits.size()>1) System.out.println("hits="+tmp);
    return tmp;
  }

  private class Impl extends SceneGraphVisitor {

    private SceneGraphPath path=new SceneGraphPath();
    private ArrayList localHits=new ArrayList();

    private double tubeRadius=0.01;
    private double pointRadius=0.015;
    private int signature;
    
    public void visit(SceneGraphComponent c) {
      if (!c.isVisible()) return;
      path.push(c);
      
      Geometry g = c.getGeometry();
      if(defaultBuildTree && g != null && g instanceof IndexedFaceSet && !checkHasTree((IndexedFaceSet) g))
          c.childrenWriteAccept(this,false,false,false,false,true,false);
      else
          c.childrenAccept(this);
      path.pop();
    }
    
    private boolean checkHasTree(IndexedFaceSet ifs) {
        AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
        return tree!=null;
    }
    
    public void visit() {
      visit(root);
    }

    public void visit(IndexedFaceSet ifs) {
      Object o = ifs.getGeometryAttributes("pickable");
      boolean pickable = !(o != null && o.equals(Boolean.FALSE));
      if (!pickable) return;
      visit((IndexedLineSet)ifs);
      AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
      if (tree==null) { 
          if (defaultBuildTree) {
              System.out.println("make tree ....");
              PickUtility.assignFaceAABBTree(ifs);
              tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
              System.out.println("made tree ...."+tree);
          } 
      }
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();
      if (tree != null) {
        tree.intersect(m, fromEuclidean, dirEuclidean, localHits);
      } else {
        BruteForcePicking.intersectPolygons(Pn.EUCLIDEAN, m, from, to, localHits);
      }
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        Object[] val = (Object[]) i.next();
        double[] pointWorld = (double[])val[0];
        int index = ((Integer)val[1]).intValue();
        int triIndex = ((Integer)val[2]).intValue(); //index of the first point of triangle in pt sequence of the polygon
        Hit h = new Hit(path.pushNew(ifs), pointWorld, Rn.euclideanDistance(fromEuclidean, pointWorld), 0, PickResult.PICK_TYPE_FACE, index,triIndex);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }
    
    public void visit(IndexedLineSet ils) {
      visit((PointSet)ils);
      Object o = ils.getGeometryAttributes("edges.pickable");
      boolean pickable = (o != null && o.equals(Boolean.TRUE));
      AABBTree tree = (AABBTree) ils.getGeometryAttributes(Attribute.attributeForName("AABBTreeEdge"));
      if (!pickable && tree==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();

      if (tree == null) {
        BruteForcePicking.intersectEdges(signature, m, from, to, tubeRadius, localHits);
        // add localHits to AABBPickSystem.this.hits!
      } else {
        tree.intersect(m, fromEuclidean, dirEuclidean, localHits);
        for (Iterator i = localHits.iterator(); i.hasNext(); ) {
          DoubleArrayArray vertices = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
          int[] indices = (int[]) i.next();
          int index = indices[0];
          int subIndex = indices[1];
          IntArray line = ils.getEdgeAttributes(Attribute.INDICES).toIntArrayArray().getValueAt(index);
          DoubleArray point1 = vertices.getValueAt(line.getValueAt(subIndex));
          DoubleArray point2 = vertices.getValueAt(line.getValueAt(subIndex+1));
          // for the distance we choose the farest point of the lineSegment
          // so that a conflict with this vertex is resolved by sorting the vertex before this edge
          double[] p1 = point1.toDoubleArray(new double[4]);
          double[] p2 = point2.toDoubleArray(new double[4]);
          
          double[] p;
          if (distFromRay(p1) < distFromRay(p2))
            p=p1;
          else
            p=p2;
          
          if (point1.getLength() == 4) {
            p1 = P3.dehomogenize(p1, p1);
            p2 = P3.dehomogenize(p2, p2);
          }
          else p[3] = 1;
          if (p[3] == 0) throw new RuntimeException("pick at infinity");
          p = m.multiplyVector(p);
          double[] pointWorld = new double[]{p[0], p[1], p[2]};
          double[] center = Rn.times(null, 0.5, Rn.add(p1, p1, p2));
          Hit h = new Hit(path.pushNew(ils), center, Rn.euclideanDistance(fromEuclidean, pointWorld), 0, PickResult.PICK_TYPE_LINE, index,-1);
          if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
        }
      }
    }

    public void visit(PointSet ps) {
      Object o = ps.getGeometryAttributes("vertices.pickable");
      boolean pickable = (o != null && o.equals(Boolean.TRUE));
      AABBTree tree = (AABBTree) ps.getGeometryAttributes(Attribute.attributeForName("AABBTreeVertex"));
      if (!pickable && tree==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();
      if (tree == null) {
        BruteForcePicking.intersectPoints(signature, m, from, to, pointRadius, localHits);
        // add localHits to AABBPickSystem.this.hits!
        
      } else {
        tree.intersect(m, fromEuclidean, dirEuclidean, localHits);
        for (Iterator i = localHits.iterator(); i.hasNext(); ) {
          int index = ((Integer)i.next()).intValue();
          DoubleArray point = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index);
          double[] p = point.toDoubleArray(new double[4]);
          if (point.getLength() == 4) p = P3.dehomogenize(p, p);
          else p[3] = 1;
          if (p[3] == 0) throw new RuntimeException("pick at infinity");
          p = m.multiplyVector(p);
          double[] pointWorld = new double[]{p[0], p[1], p[2]};
          Hit h = new Hit(path.pushNew(ps), pointWorld, Rn.euclideanDistance(fromEuclidean, pointWorld), 0, PickResult.PICK_TYPE_POINT, index,-1);
          if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
        }
      }
    }

    private double distFromRay(double[] pickPoint) {
      double[] tf = Rn.subtract(null, dirEuclidean, fromEuclidean);
      double[] pf = Rn.subtract(null, pickPoint, fromEuclidean);
      double k = Rn.innerProduct(pf, tf) / Rn.euclideanNorm(tf);
      double[] pp = Rn.subtract(null, pf, Rn.times(null, k, tf));
      return Rn.euclideanNorm(pp);
    }
  }
  
  public static class Hit implements PickResult {
    final SceneGraphPath path;
    final double[] pointWorld;
    final double[] pointObject;
    double[] texCoords = null;
    final int pickType;
    final int index;
    final int triIndex;
    final double dist;
    final double distRay;

    public Hit(SceneGraphPath path, double[] pointWorld, double dist, double distRay, int pickType, int index,int triIndex) {
      this.path = (SceneGraphPath) path;
      Matrix m = new Matrix();
      path.getInverseMatrix(m.getArray());
      this.pointWorld= pointWorld;
      this.pointObject=m.multiplyVector(pointWorld);
      this.dist = dist;
      this.distRay = distRay;
      this.pickType=pickType;
      this.index=index;
      this.triIndex=triIndex;
    }

    public SceneGraphPath getPickPath() {
        return path;
    }
    public double[] getWorldCoordinates() {
        return pointWorld;
    }
    public double[] getObjectCoordinates() {
        return pointObject;
    }
    public double getDist() {
      return dist;
    }

    public int getIndex() {
      return index;
    }

    public String toString() {
      StringBuffer sb = new StringBuffer();
      sb.append("AABB-Pick: ");
      sb.append(" distRay=").append(distRay);
      sb.append(" dist=").append(dist);
      sb.append(" world=").append(Rn.toString(pointWorld));
      sb.append(" path=").append(path.toString());
      return sb.toString();
    }

    public int getPickType() {
      return pickType;
    }

    public double getDistRay() {
      return distRay;
    }

    private int hasTextureCoordinates() {
        if(texCoords== null) {
            if(triIndex > -1) {
            SceneGraphNode end = path.getLastElement();
            if (end instanceof IndexedFaceSet) {
                IndexedFaceSet ifs = (IndexedFaceSet) end;
                DataList txc = ifs.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
                if(txc != null) {
                    DataList indices=ifs.getFaceAttributes(Attribute.INDICES);
                    if(indices != null){
                        DoubleArrayArray points
                        =ifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray();
                        IntArray faceIndices = indices.item(index).toIntArray();
                        int l = faceIndices.size();
                        int ptIndex0 = faceIndices.getValueAt(0);
                        int ptIndex1 = faceIndices.getValueAt((triIndex+1)%l);
                        int ptIndex2 = faceIndices.getValueAt((triIndex+2)%l);
                        DoubleArray tc0 = txc.item(ptIndex0).toDoubleArray();
                        DoubleArray tc1 = txc.item(ptIndex1).toDoubleArray();
                        DoubleArray tc2 = txc.item(ptIndex2).toDoubleArray();
                        final int textureLength = tc0.getLength();
                        texCoords = new double[textureLength];
                        
                        // two methods the first one uses a smart algorithm
                        // grabed from Markus code in numerical methods
                        // the second is straight and dumb
if(false) {
                        // get the points
                        double[] a = new double[3];
                        DoubleArray     pt = points.item(ptIndex0).toDoubleArray();
                        a[0] = pt.getValueAt(0);
                        a[1] = pt.getValueAt(1);
                        a[2] = pt.getValueAt(2);
                        if(pt.size()==4) {
                            double w = pt.getValueAt(3);
                            a[0] /= w;
                            a[1] /= w;
                            a[2] /= w;
                        }
                        double[] b = new double[3];
                        pt = points.item(ptIndex1).toDoubleArray();
                        b[0] = pt.getValueAt(0);
                        b[1] = pt.getValueAt(1);
                        b[2] = pt.getValueAt(2);
                        if(pt.size()==4) {
                            double w = pt.getValueAt(3);
                            b[0] /= w;
                            b[1] /= w;
                            b[2] /= w;
                        }
                        double[] c = new double[3];
                        pt = points.item(ptIndex2).toDoubleArray();
                        c[0] = pt.getValueAt(0);
                        c[1] = pt.getValueAt(1);
                        c[2] = pt.getValueAt(2);
                        if(pt.size()==4) {
                            double w = pt.getValueAt(3);
                            c[0] /= w;
                            c[1] /= w;
                            c[2] /= w;
                        }
 
                        double bc[] = new double[3];
                        convertToBary(bc,a,b,c,pointObject);
                        for(int j = 0 ; j<textureLength;j++) {
                            texCoords[j] = bc[0]*tc0.getValueAt(j) + bc[1] * tc1.getValueAt(j) + bc[2] * tc2.getValueAt(j);
                        }
} else {                           
                        DoubleArray     pt = points.item(ptIndex2).toDoubleArray();
                        final double cx = pt.getValueAt(0);
                        final double cy = pt.getValueAt(1);
                        final double cz = pt.getValueAt(2);
                        
                        final double c = cx-pointObject[0];
                        final double f = cy-pointObject[1];
                        final double i = cz-pointObject[2];
                    
                        pt = points.item(ptIndex0).toDoubleArray();
                        final double a = pt.getValueAt(0)-cx;
                        final double d = pt.getValueAt(1)-cy;
                        final double g = pt.getValueAt(2)-cz;
                        pt = points.item(ptIndex1).toDoubleArray();
                        final double b = pt.getValueAt(0)-cx;
                        final double e = pt.getValueAt(1)-cy;
                        final double h = pt.getValueAt(2)-cz;
                        
                      // p = u ptIndex0 + v ptIndex1 + w ptIndex2 :
                        
                        final double u = (b*(f+i)-c*(e+h))/(a*(e+h)-b*(d+g));
                        final double v = (a*(f+i)-c*(d+g))/(b*(d+g)-a*(e+h));
                        final double w = 1-u-v;
                        if(u<0 || u>1 || v<0 ||v>1)
                            System.err.println("bad uv interpolate "+u+ " "+v);
//                        System.err.println("tc "+tc0.getValueAt(0)+" "+tc0.getValueAt(1));
//                        System.err.println("tc "+tc1.getValueAt(0)+" "+tc1.getValueAt(1));
//                        System.err.println("tc "+tc2.getValueAt(0)+" "+tc2.getValueAt(1));
                        for(int j = 0 ; j<textureLength;j++) {
                            texCoords[j] = u*tc0.getValueAt(j) + v * tc1.getValueAt(j) + + w * tc2.getValueAt(j);
                        }
//                        System.err.println("tc "+texCoords[0]+" "+texCoords[1]);

}
                        return texCoords.length;
                    }
                }
            }
        }
        texCoords = new double[0];
        }
        return texCoords.length;
    }
    
    private static final double EPS = 0.00001; 
    /*
     * calc barycentric coordinates bary for point x in anElement el, not
     * necessarily 0 <= b[i] <= 1
     */
    private boolean  convertToBary(
        double[] bary,
        double[] x0,
        double[] x1,
        double[] x2,
        double[] x) {
        //TODO: use exception handling for degenerate situations
        //TODO: the return type should be void or double which should give
        // distance from plane

        int i0 = 0, i1 = 1, i2 = 2;

        double det;

        /* find two linear independent rows */
        for (;;) {
            det =
                x1[i0] * x2[i1]
                    - x1[i1] * x2[i0]
                    - (x0[i0] * x2[i1] - x0[i1] * x2[i0])
                    + x0[i0] * x1[i1]
                    - x0[i1] * x1[i0];

            if (Math.abs(det) > EPS)
                break;

            if (i1 == 1) {
                i1 = 2;
                i2 = 1;
            } else if (i0 == 0) {
                i0 = 1;
                i2 = 0;
            } else {

                System.out.println(
                    "mBaryInElementConvertFromVec3: triangle degenerated?");
                //              
                //              fprintf(stderr, "gmBaryInElementConvertFromVec3: triangle
                // degenerated?\n");
                //              fprintf(stderr, "\tcan't compute barycentric
                // coordinates.\n");
                //              fprintf(stderr, "vertex0: (%f, %f, %f)\n", x0[0], x0[1],
                // x0[2]);
                //              fprintf(stderr, "vertex1: (%f, %f, %f)\n", x1[0], x1[1],
                // x1[2]);
                //              fprintf(stderr, "vertex2: (%f, %f, %f)\n", x2[0], x2[1],
                // x2[2]);
                //              fprintf(stderr, "point : (%f, %f, %f)\n", x[0], x[1], x[2]);
                //              fflush(stderr);

                return false;
            }
        }

        /* calculate barycentric coordinates */
        bary[0] =
            (x1[i0] * x2[i1]
                - x1[i1] * x2[i0]
                - (x[i0] * x2[i1] - x[i1] * x2[i0])
                + x[i0] * x1[i1]
                - x[i1] * x1[i0])
                / det;
        bary[1] =
            (x[i0] * x2[i1]
                - x[i1] * x2[i0]
                - (x0[i0] * x2[i1] - x0[i1] * x2[i0])
                + x0[i0] * x[i1]
                - x0[i1] * x[i0])
                / det;
        bary[2] = 1.0 - bary[0] - bary[1];

        /* test third row */
        if (Math
            .abs(x0[i2] * bary[0] + x1[i2] * bary[1] + x2[i2] * bary[2] - x[i2])
            > 1.e-3) {
            System.out.println(
                "gmBaryInElementConvertFromVec3: test for third row failed."
                    +Math.abs(x0[i2] * bary[0] + x1[i2] * bary[1] + x2[i2] * bary[2] - x[i2])
                    );
            //          fprintf(stderr, "gmBaryInElementConvertFromVec3: test for third
            // row failed.\n");
            //          fprintf(stderr, "\tpoint not in triangle plane?\n");
            //          fflush(stderr);

            return false;
        }

        correct(bary);

        return true;
    }
    final  void correct(double[] bary) {

        double sum = 0;
        int j, k, i = 0;

        for (i = 0; i < 3; i++) {
            if (Math.abs(bary[i]) < EPS)
                bary[i] = 0;
            sum += bary[i];
        }

        for (i = 0; bary[i] == 0. && i < 3; i++);

        bary[(j = (i + 1) % 3)] /= sum;
        bary[(k = (i + 2) % 3)] /= sum;
        bary[i] = 1. - bary[j] - bary[k];
    }
    
    
    
    
    

    public double[] getTextureCoordinates() {
        hasTextureCoordinates();
            return texCoords;
    }
  }
  
  // TODO: how to do that right??
      public static class HitComparator implements Comparator {
        public int compare(Object o1, Object o2) {
          // distance from ray
          Hit hit1 = (Hit) o1;
          Hit hit2 = (Hit) o2;
          double a = hit1.getDistRay();
          double b = hit2.getDistRay();
          if (a>b) return 1;
          if (b>a) return -1;
          // distance from "from"
          a = hit1.getDist();
          b = hit2.getDist();
          if (a>b) return 1;
          if (b>a) return -1;
          // point before edge before face before object
          a = hit1.getPickType();
          b = hit2.getPickType();
          return (a>b) ? 1 : (b>a? -1:0);
        }
      }
}
