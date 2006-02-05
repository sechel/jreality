package de.jreality.scene.pick;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.scene.data.*;
import de.jreality.scene.pick.bounding.AABB;
import de.jreality.scene.pick.bounding.AABBTree;

public class AABBPickSystem implements PickSystem {
  
  private Impl impl;
  private SceneGraphComponent root;
  
  private double[] from;
  private double[] to;
  private double maxDist;
  
  private AABB tmpAABB=new AABB();
  
  private ArrayList hits = new ArrayList();
  
  private Comparator cmp = new HitComparator();
  
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
    return computePickImpl(from, dir, Rn.euclideanNorm(dir));
  }
  
  private List computePickImpl(double[] from, double[] to, double maxDist) {
    this.from=new double[]{from[0], from[1], from[2]};
    this.to=new double[]{to[0], to[1], to[2]};
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
    
    public void visit(SceneGraphComponent c) {
      if (!c.isVisible()) return;
      path.push(c);
      c.childrenAccept(this);
      path.pop();
    }
    
    public void visit() {
      visit(root);
    }

    public void visit(IndexedFaceSet ifs) {
      visit((IndexedLineSet)ifs);
      AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
      if (tree==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();
      tree.intersect(m, from, to, localHits);
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        Object[] val = (Object[]) i.next();
        double[] pointWorld = (double[])val[0];
        int index = ((Integer)val[1]).intValue();
        Hit h = new Hit(path.pushNew(ifs), pointWorld, Rn.euclideanDistance(from, pointWorld), 0, PickResult.PICK_TYPE_FACE, index);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }
    
    public void visit(IndexedLineSet ils) {
      visit((PointSet)ils);
      AABBTree tree = (AABBTree) ils.getGeometryAttributes(Attribute.attributeForName("AABBTreeEdge"));
      if (tree==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();
      tree.intersect(m, from, to, localHits);
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
        Hit h = new Hit(path.pushNew(ils), center, Rn.euclideanDistance(from, pointWorld), 0, PickResult.PICK_TYPE_LINE, index);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

    public void visit(PointSet ps) {
      visit((Geometry)ps);
      AABBTree tree = (AABBTree) ps.getGeometryAttributes(Attribute.attributeForName("AABBTreeVertex"));
      if (tree==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      localHits.clear();
      tree.intersect(m, from, to, localHits);
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        int index = ((Integer)i.next()).intValue();
        DoubleArray point = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray().getValueAt(index);
        double[] p = point.toDoubleArray(new double[4]);
        if (point.getLength() == 4) p = P3.dehomogenize(p, p);
        else p[3] = 1;
        if (p[3] == 0) throw new RuntimeException("pick at infinity");
        p = m.multiplyVector(p);
        double[] pointWorld = new double[]{p[0], p[1], p[2]};
        Hit h = new Hit(path.pushNew(ps), pointWorld, Rn.euclideanDistance(from, pointWorld), 0, PickResult.PICK_TYPE_POINT, index);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

    public void visit(Geometry ifs) {
      AABB aabb = (AABB) ifs.getGeometryAttributes(Attribute.attributeForName("AABB"));
      if (aabb==null) return;
      Matrix m = new Matrix();
      path.getMatrix(m.getArray());
      aabb.transform(m, tmpAABB);
      if (tmpAABB.intersects(from, to)) {
        double[] p = tmpAABB.getExtent(null);
        double[] pointWorld = new double[]{p[0], p[1], p[2]};
        Hit h = new Hit((SceneGraphPath) path.clone(), pointWorld, Rn.euclideanDistance(from, pointWorld), distFromRay(pointWorld), PickResult.PICK_TYPE_OBJECT, -1);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

    private double distFromRay(double[] pickPoint) {
      double[] tf = Rn.subtract(null, to, from);
      double[] pf = Rn.subtract(null, pickPoint, from);
      double k = Rn.innerProduct(pf, tf) / Rn.euclideanNorm(tf);
      double[] pp = Rn.subtract(null, pf, Rn.times(null, k, tf));
      return Rn.euclideanNorm(pp);
    }
  }
  
  public static class Hit implements PickResult {
    final SceneGraphPath path;
    final double[] pointWorld;
    final double[] pointObject;
    final int pickType;
    final int index;
    final double dist;
    final double distRay;

    public Hit(SceneGraphPath path, double[] pointWorld, double dist, double distRay, int pickType, int index) {
      this.path = (SceneGraphPath) path;
      Matrix m = new Matrix();
      path.getInverseMatrix(m.getArray());
      this.pointWorld= pointWorld;
      this.pointObject=m.multiplyVector(pointWorld);
      this.dist = dist;
      this.distRay = distRay;
      this.pickType=pickType;
      this.index=index;
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
