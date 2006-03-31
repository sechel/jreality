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
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.PickUtility;

public class AABBPickSystem implements PickSystem {
  
  private Impl impl;
  private SceneGraphComponent root;
  
  private double[] fromEuclidean;
  private double[] dirEuclidean;
  private double maxDist;
  
  private ArrayList hits = new ArrayList();
  
  private Comparator cmp = new Hit.HitComparator();
  
  static boolean defaultBuildTree=false;
  
  private double[] from;
  private double[] to;
  
  public void setSceneRoot(SceneGraphComponent root) {
    impl= new Impl();
    this.root=root;
  }
  
  public List computePick(double[] from, double[] to) {
    this.from=from;
    this.to=to;
    if (to.length < 4 || to[3] == 0) return computePickImpl(from, to, 1000);
    double[] dir = new double[3];
    if (from.length > 3) P3.dehomogenize(from, from);
    P3.dehomogenize(to, to);
    dir[0] = to[0]-from[0];
    dir[1] = to[1]-from[1];
    dir[2] = to[2]-from[2];
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

    private Stack appStack = new Stack();
    
    private EffectiveAppearance eap = EffectiveAppearance.create();
    
    private SceneGraphPath path=new SceneGraphPath();
    private ArrayList localHits=new ArrayList();

    private Matrix m=new Matrix();
    
    private double tubeRadius=CommonAttributes.TUBE_RADIUS_DEFAULT;
    private double pointRadius=CommonAttributes.POINT_RADIUS_DEFAULT;
    private int signature=Pn.EUCLIDEAN;

    private boolean pickPoints=false;
    private boolean pickEdges=true;
    private boolean pickFaces=true;
    
    public void visit(SceneGraphComponent c) {
      if (!c.isVisible()) return;
      path.push(c);
      if (c.getAppearance()!=null) {
        EffectiveAppearance eapNew = eap.create(c.getAppearance());
        appStack.push(eap);
        eap=eapNew;
        readEApp();
      }
      path.getMatrix(m.getArray());
      Geometry g = c.getGeometry();
      if(defaultBuildTree && g != null && g instanceof IndexedFaceSet && !checkHasTree((IndexedFaceSet) g))
          c.childrenWriteAccept(this,false,false,false,false,true,false);
      else
          c.childrenAccept(this);
      path.pop();
      if (c.getAppearance()!=null) {
        eap=(EffectiveAppearance) appStack.pop();
        readEApp();
      }
    }

    private void readEApp() {
      pickPoints=eap.getAttribute(CommonAttributes.VERTEX_DRAW, false)
        && eap.getAttribute(CommonAttributes.POINT_SHADER+".pickable", true);
      pickEdges=eap.getAttribute(CommonAttributes.EDGE_DRAW, true)
        && eap.getAttribute(CommonAttributes.LINE_SHADER+".pickable", true);
      pickFaces=eap.getAttribute(CommonAttributes.FACE_DRAW, true)
      && eap.getAttribute(CommonAttributes.POLYGON_SHADER+".pickable", true);
      pointRadius=eap.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, CommonAttributes.POINT_RADIUS_DEFAULT);
      tubeRadius=eap.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, CommonAttributes.TUBE_RADIUS_DEFAULT);
    }
    
    private boolean checkHasTree(IndexedFaceSet ifs) {
        AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
        return tree!=null;
    }
    
    private boolean isPickable(Geometry g) {
      Object o = g.getGeometryAttributes("pickable");
      return !(o != null && o.equals(Boolean.FALSE));
    }

    public void visit() {
      visit(root);
    }

    public void visit(Sphere s) {
      if (!pickFaces || defaultBuildTree || !isPickable(s)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectSphere(signature, path, from, to, localHits);
      AABBPickSystem.this.hits.addAll(localHits);
    };
    
    public void visit(Cylinder c) {
      if (!pickFaces || defaultBuildTree || !isPickable(c)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectCylinder(signature, path, from, to, localHits);
      AABBPickSystem.this.hits.addAll(localHits);
    };
    
    public void visit(IndexedFaceSet ifs) {
      
      visit((IndexedLineSet)ifs);

      if (!pickFaces || !isPickable(ifs)) return;      

      if (!eap.getAttribute(CommonAttributes.FACE_DRAW, true)) return;
      
      AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
      if (tree==null) { 
          if (defaultBuildTree) {
              System.out.println("make tree ....");
              PickUtility.assignFaceAABBTree(ifs);
              tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
              System.out.println("made tree ...."+tree);
          } 
      }
      
      localHits.clear();
      
      if (tree == null) {
        BruteForcePicking.intersectPolygons(ifs, signature, path, from, to, localHits);
        AABBPickSystem.this.hits.addAll(localHits);
      } else {
        tree.intersect(m, fromEuclidean, dirEuclidean, localHits);
        extractFaceTreeHits(ifs);
      }
    }
    
    public void visit(IndexedLineSet ils) {
      visit((PointSet)ils);
      
      if (!pickEdges || !isPickable(ils)) return;

      localHits.clear();

      BruteForcePicking.intersectEdges(ils, signature, path, from, to, tubeRadius, localHits);
      AABBPickSystem.this.hits.addAll(localHits);

    }

    public void visit(PointSet ps) {
      
      if (!pickPoints || !isPickable(ps)) return;

      localHits.clear();

      BruteForcePicking.intersectPoints(ps, signature, path, from, to, pointRadius, localHits);
      AABBPickSystem.this.hits.addAll(localHits);        
    }

    private void extractFaceTreeHits(IndexedFaceSet ifs) {
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        Object[] val = (Object[]) i.next();
        double[] pointWorld = (double[])val[0];
        int index = ((Integer)val[1]).intValue();
        int triIndex = ((Integer)val[2]).intValue(); //index of the first point of triangle in pt sequence of the polygon
        Hit h = new Hit(path.pushNew(ifs), pointWorld, Rn.euclideanDistance(fromEuclidean, pointWorld), 0, PickResult.PICK_TYPE_FACE, index,triIndex);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

    /******************** ugly stuff start *******************/
    
    private void extractEdgeTreeHits(IndexedLineSet ils, Matrix m) {
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

    private void extractVertexTreeHits(PointSet ps, Matrix m) {
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

    private double distFromRay(double[] pickPoint) {
      double[] tf = Rn.subtract(null, dirEuclidean, fromEuclidean);
      double[] pf = Rn.subtract(null, pickPoint, fromEuclidean);
      double k = Rn.innerProduct(pf, tf) / Rn.euclideanNorm(tf);
      double[] pp = Rn.subtract(null, pf, Rn.times(null, k, tf));
      return Rn.euclideanNorm(pp);
    }
    /******************** ugly stuff end *******************/
  }
  
}
