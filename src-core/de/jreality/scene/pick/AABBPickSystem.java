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

/**
 * 
 * Our pick system implementation. Uses Brute-force as default
 * and AABBTrees if available.
 * 
 * @author Steffen Weissmann
 *
 */
public class AABBPickSystem implements PickSystem {
  
  private Impl impl;
  private SceneGraphComponent root;
  
  private double maxDist;
  
  private ArrayList hits = new ArrayList();
  
  private Comparator cmp = new Hit.HitComparator();
  
  private double[] from;
  private double[] to;
  private double[] dir;
  
  public void setSceneRoot(SceneGraphComponent root) {
    impl= new Impl();
    this.root=root;
  }
  
  public List computePick(double[] f, double[] t) {
    if (f.length == 4) Pn.dehomogenize(f, f);
    if (t.length == 4) Pn.dehomogenize(t, t);
    if (t.length == 3 || t[3] == 0) return computePickImpl(f, t, t, 1000);
    double[] dir = new double[3];
    dir[0] = t[0]-f[0];
    dir[1] = t[1]-f[1];
    dir[2] = t[2]-f[2];
    return computePickImpl(f, t, dir, Rn.euclideanNorm(dir));
  }
  
  private List computePickImpl(double[] from, double[] to, double[] dir, double maxDist) {
    this.from=(double[]) from.clone();
    this.to=(double[]) to.clone();
    this.dir=(double[]) dir.clone();
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

    private boolean pickPoints=true;
    private boolean pickEdges=true;
    private boolean pickFaces=true;

    /* local ray */
    private double[] fromLocal;
    private double[] dirLocal;

//    private double[] toLocal;
    
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
      
      fromLocal=mInv.multiplyVector(from);
//      toLocal=mInv.multiplyVector(to);
      dirLocal=mInv.multiplyVector(dir);

      c.childrenAccept(this);
      path.pop();
      if (c.getAppearance()!=null) {
        eap=(EffectiveAppearance) appStack.pop();
        readEApp();
      }
    }

    private void readEApp() {
      pickPoints=eap.getAttribute(CommonAttributes.VERTEX_DRAW, true)
        && eap.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, true);
      pickEdges=eap.getAttribute(CommonAttributes.EDGE_DRAW, true)
        && eap.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE, true);
      pickFaces=eap.getAttribute(CommonAttributes.FACE_DRAW, true)
      && eap.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE, true);
      pointRadius=eap.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, CommonAttributes.POINT_RADIUS_DEFAULT);
      tubeRadius=eap.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, CommonAttributes.TUBE_RADIUS_DEFAULT);
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
      
      extractHits();
    }
    
    public void visit(Cylinder c) {
      if (!pickFaces || !isPickable(c)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectCylinder(c, signature, path, from, to, localHits);

      extractHits();
    }
    
    public void visit(IndexedFaceSet ifs) {
      
      visit((IndexedLineSet)ifs);

      if (!pickFaces || !isPickable(ifs)) return;      

      AABBTree tree = (AABBTree) ifs.getGeometryAttributes(Attribute.attributeForName("AABBTree"));
      
      localHits.clear();
      
      if (tree == null) {
        BruteForcePicking.intersectPolygons(ifs, signature, path, from, to, localHits);
        extractHits();
      } else {
        tree.intersect(fromLocal, dirLocal, localHits); 
        extractFaceTreeHits(ifs, localHits);
      }
    }
    
    public void visit(IndexedLineSet ils) {
      visit((PointSet)ils);
      if (!pickEdges || !isPickable(ils)) return;

      localHits.clear();

 //     System.err.println("Picking indexed line set "+ils.getName());
       BruteForcePicking.intersectEdges(ils, signature, path, from, to, tubeRadius, localHits);
       extractHits();
    }

    public void visit(PointSet ps) {
      
      if (!pickPoints || !isPickable(ps)) return;

      localHits.clear();

      BruteForcePicking.intersectPoints(ps, signature, path, from, to, pointRadius, localHits);
      extractHits();        
    }

    private void extractHits() {
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        Hit h = (Hit) i.next();
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }
    
    private void extractFaceTreeHits(IndexedFaceSet ifs, List target) {
      int k = 0;
      for (Iterator i = localHits.iterator(); i.hasNext(); ) {
        Object[] val = (Object[]) i.next();
        double[] pointWorld = m.multiplyVector((double[])val[0]);
        int index = ((Integer)val[1]).intValue();
        int triIndex = ((Integer)val[2]).intValue(); //index of the first point of triangle in pt sequence of the polygon
        Hit h = new Hit(path.pushNew(ifs), pointWorld, Rn.euclideanDistance(from, pointWorld), 0, PickResult.PICK_TYPE_FACE, index,triIndex);
        if (h.getDist() <= maxDist) {
          AABBPickSystem.this.hits.add(h);
          if (target != null) target.add(h);
        }
      }
    }

  }
  
}
