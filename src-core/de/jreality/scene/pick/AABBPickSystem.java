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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Cylinder;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Sphere;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.PickUtility;

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
  
  private ArrayList<Hit> hits = new ArrayList<Hit>();
  private HashMap<IndexedFaceSet, AABBTree> aabbTreeExists = new HashMap<IndexedFaceSet, AABBTree>();
  private HashMap<Geometry, Boolean> isPickableMap = new HashMap<Geometry, Boolean>();
  private Comparator<Hit> cmp = new Hit.HitComparator();
  private double[] from;
  private double[] to;
//  private double[] dir;
  
  public void setSceneRoot(SceneGraphComponent root) {
    impl= new Impl();
    this.root=root;
  }
  
  public List<PickResult> computePick(double[] f, double[] t) {
    if (f.length == 4) Pn.dehomogenize(f, f);
    if (t.length == 4) Pn.dehomogenize(t, t);
    // XXX: somehow calculate maxDist depending on the far clipping plane
    double maxDist = 1E9;
    if (t.length == 3 || t[3] == 0) return new ArrayList<PickResult>(computePickImpl(f, t, t, maxDist));
    double[] dir = new double[3];
    dir[0] = t[0]-f[0];
    dir[1] = t[1]-f[1];
    dir[2] = t[2]-f[2];
    return new ArrayList<PickResult>(computePickImpl(f, t, dir, Rn.euclideanNorm(dir)));
  }
  
  private List<Hit> computePickImpl(double[] from, double[] to, double[] dir, double maxDist) {
    this.from=(double[]) from.clone();
    this.to=(double[]) to.clone();
//    this.dir=(double[]) dir.clone();
    this.maxDist=maxDist;
    impl.visit();
    if (hits.isEmpty()) return Collections.emptyList();
    List<Hit> tmp = hits;
    hits = new ArrayList<Hit>();
    if (tmp.size()>1) {
      sort(tmp);
//      System.out.println("hits="+tmp);
    }
    return tmp;
  }

  private void sort(List<Hit> tmp) {
    Collections.sort(tmp, cmp);
    double dist=0;
    for (Hit h : tmp) {
      if (h.getDist() < dist) throw new Error("unsorted!");
      dist=h.getDist();
    }
  }

  /**
   * TODO: optimize access to appearances to avoid use of effective appearance objects.
   *
   */
  private class Impl extends SceneGraphVisitor {

    private Stack<EffectiveAppearance> appStack = new Stack<EffectiveAppearance>();
    
    private EffectiveAppearance eap = EffectiveAppearance.create();
    
    private SceneGraphPath path=new SceneGraphPath();
    private ArrayList<Hit> localHits=new ArrayList<Hit>();

    private Matrix m=new Matrix();
    private Matrix mInv=new Matrix();
    
    private double tubeRadius=CommonAttributes.TUBE_RADIUS_DEFAULT;
    private double pointRadius=CommonAttributes.POINT_RADIUS_DEFAULT;
    private int signature=Pn.EUCLIDEAN;
    private Matrix[] matrixStack = new Matrix[256];
    int stackCounter = 0;
    private boolean pickPoints=true, drawVertices = false;
    private boolean pickEdges=true, drawEdges = true;
    private boolean pickFaces=true, drawFaces = true;

//    /* local ray */
//    private double[] fromLocal;
//    private double[] dirLocal;
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
      if (c.getTransformation() != null)	{
    	  if (matrixStack[stackCounter+1] == null) matrixStack[stackCounter+1] = new Matrix();
    	  Rn.times(matrixStack[stackCounter+1].getArray(), matrixStack[stackCounter].getArray(), c.getTransformation().getMatrix());
    	  stackCounter++;
    	  m = matrixStack[stackCounter];
    	  mInv = m.getInverse();
       }
//      path.getMatrix(m.getArray());
//      path.getInverseMatrix(mInv.getArray());
      
//      fromLocal=mInv.multiplyVector(from);
//      toLocal=mInv.multiplyVector(to);
//      dirLocal=mInv.multiplyVector(dir);

      c.childrenAccept(this);
      path.pop();
      if (c.getTransformation() != null) 	{
    	  stackCounter--;
    	  m = matrixStack[stackCounter];
    	  mInv = m.getInverse();
     }
      if (c.getAppearance()!=null) {
        eap=(EffectiveAppearance) appStack.pop();
        readEApp();
      }
    }
    
    // TODO simplify this to only read appearances
    private void readEApp() {
    	// test to see how fast picking is without having to access effective appearance
//    	pickPoints = pickEdges = false;
//    	pickFaces = true;
//    	pointRadius = .02;
//    	tubeRadius = .02;
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
    	Boolean boo = isPickableMap.get(g);
    	if (boo == null) {
    	    Object o = g.getGeometryAttributes(CommonAttributes.PICKABLE);
    	    boo =  !(o != null && o.equals(Boolean.FALSE));
    		isPickableMap.put(g, boo);
    	}
      return boo.booleanValue();
    }

    public void visit() {
    	stackCounter = 0;
    	matrixStack[0] = new Matrix();
    	aabbTreeExists.clear();
    	isPickableMap.clear();
    	visit(root);
    }

    public void visit(Sphere s) {
      if (!pickFaces || !isPickable(s)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectSphere(s, signature, path, m, mInv, from, to, localHits);
      
      extractHits(localHits);
    }
    
    public void visit(Cylinder c) {
       if (!pickFaces || !isPickable(c)) return;
      
      localHits.clear();
      
      BruteForcePicking.intersectCylinder(c, signature, path, m, mInv, from, to, localHits);

      extractHits(localHits);
    }
    
    public void visit(IndexedFaceSet ifs) {
      if (!isPickable(ifs)) return;
      visit((IndexedLineSet)ifs);

      if (!pickFaces) return;      
      
      AABBTree tree = aabbTreeExists.get(ifs);
      if (tree == null) {
    	  // not yet processed
    	  tree = (AABBTree) ifs.getGeometryAttributes(PickUtility.AABB_TREE);
    	  if (tree == null) tree = AABBTree.nullTree;
    	  aabbTreeExists.put(ifs, tree);
      }
      
      localHits.clear();
      
        if (tree == AABBTree.nullTree) {
          BruteForcePicking.intersectPolygons(ifs, signature, path, m, mInv, from, to, localHits);
        } else {
          tree.intersect(ifs, signature, path, from, to, localHits);
        }
        extractHits(localHits);
    }
    
    public void visit(IndexedLineSet ils) {
      if (!isPickable(ils)) return;
      visit((PointSet)ils);
      if (!pickEdges) return;

      localHits.clear();

 //     System.err.println("Picking indexed line set "+ils.getName());
       BruteForcePicking.intersectEdges(ils, signature, path, m, mInv, from, to, tubeRadius, localHits);
       extractHits(localHits);
    }

    public void visit(PointSet ps) {
     
      if (!pickPoints || !isPickable(ps)) return;

      localHits.clear();

      BruteForcePicking.intersectPoints(ps, signature, path, m, mInv, from, to, pointRadius, localHits);
      extractHits(localHits);        
    }

    private void extractHits(List l) {
      for (Iterator i = l.iterator(); i.hasNext(); ) {
        Hit h = (Hit) i.next();
        if (h.getDist() <= maxDist) AABBPickSystem.this.hits.add(h);
      }
    }

  }
  
}
