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
      c.childrenAccept(this);
      path.pop();
      if (c.getAppearance()!=null) {
        eap=(EffectiveAppearance) appStack.pop();
        readEApp();
      }
    }

    private void readEApp() {
      pickPoints=eap.getAttribute(CommonAttributes.VERTEX_DRAW, false)
        && eap.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, true);
      pickEdges=eap.getAttribute(CommonAttributes.EDGE_DRAW, true)
        && eap.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE, true);
      pickFaces=eap.getAttribute(CommonAttributes.FACE_DRAW, true)
      && eap.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE, true);
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
      if (!pickFaces || !isPickable(s)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectSphere(s, signature, path, from, to, localHits);
      AABBPickSystem.this.hits.addAll(localHits);
    };
    
    public void visit(Cylinder c) {
      if (!pickFaces || !isPickable(c)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectCylinder(c, signature, path, from, to, localHits);
      AABBPickSystem.this.hits.addAll(localHits);
    };
    
    public void visit(IndexedFaceSet ifs) {
      
      visit((IndexedLineSet)ifs);

      if (!pickFaces || !isPickable(ifs)) return;      

      AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
      
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

  }
  
}
