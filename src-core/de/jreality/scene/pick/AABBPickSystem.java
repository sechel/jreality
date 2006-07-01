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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.scene.data.Attribute;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

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
    private Matrix mInv=new Matrix();
    
    private double tubeRadius=CommonAttributes.TUBE_RADIUS_DEFAULT;
    private double pointRadius=CommonAttributes.POINT_RADIUS_DEFAULT;
    private int signature=Pn.EUCLIDEAN;

    private boolean pickPoints=false;
    private boolean pickEdges=true;
    private boolean pickFaces=true;

    /* local ray */
    private double[] from4;
    private double[] dir4;
    
    public void visit(SceneGraphComponent c) {
      if (!c.isVisible()) return;
//      System.err.println("visiting "+c.getName());
     path.push(c);
      if (c.getAppearance()!=null) {
        EffectiveAppearance eapNew = eap.create(c.getAppearance());
        appStack.push(eap);
        eap=eapNew;
        readEApp();
      }
      path.getMatrix(m.getArray());
      path.getInverseMatrix(mInv.getArray());
      
      from4=mInv.multiplyVector(from);
      dir4=mInv.multiplyVector(to);

      
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
      Object o = g.getGeometryAttributes(CommonAttributes.PICKABLE);
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

        tree.intersect(from4, dir4, localHits);
        extractFaceTreeHits(ifs);
      }
    }
    
    public void visit(IndexedLineSet ils) {
      visit((PointSet)ils);
      if (!pickEdges || !isPickable(ils)) return;

      localHits.clear();

 //     System.err.println("Picking indexed line set "+ils.getName());
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
        double[] pointWorld = m.multiplyVector((double[])val[0]);
        int index = ((Integer)val[1]).intValue();
        int triIndex = ((Integer)val[2]).intValue(); //index of the first point of triangle in pt sequence of the polygon
        Hit h = new Hit(path.pushNew(ifs), pointWorld, Rn.euclideanDistance(fromEuclidean, pointWorld), 0, PickResult.PICK_TYPE_FACE, index,triIndex);
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

  }
  
}
