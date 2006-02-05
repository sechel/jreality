/*
 * Created on 12.01.2004
 */
package de.jreality.scene;

import de.jreality.geometry.QuadMeshShape;


/**
 * A visitor for performing type-specific operations and/or
 * traversing the scene graph. All visit method are implemented
 * as a call to the visit() method with the supertype parameter.
 * @author Holger
 * 
 * TODO: adapt to refactoring
 */
public class SceneGraphVisitor {

  protected SceneGraphVisitor() {}

  public void visit(SceneGraphNode m) {}

  public void visit(SceneGraphComponent c) {
    SceneGraphComponent.superAccept(c, this);
  }

  public void visit(Appearance a) {
    Appearance.superAccept(a, this);
  }

  public void visit(Transformation t) {
    Transformation.superAccept(t, this);
  }

  public void visit(Light l) {
    Light.superAccept(l, this);
  }

  public void visit(DirectionalLight l) {
    DirectionalLight.superAccept(l, this);
  }
  
  public void visit(PointLight l) {
      PointLight.superAccept(l, this);
    }

  public void visit(SpotLight l) {
    SpotLight.superAccept(l, this);
  }

  public void visit(Geometry g) {
    Geometry.superAccept(g, this);
  }

  public void visit(Sphere s) {
    Sphere.superAccept(s, this);
  }
  
  public void visit(Cylinder c) {
      Cylinder.superAccept(c, this);
  }

  public void visit(PointSet p) {
    PointSet.superAccept(p, this);
  }

  public void visit(IndexedLineSet g) {
    IndexedLineSet.superAccept(g, this);
  }

  public void visit(IndexedFaceSet i) {
    IndexedFaceSet.superAccept(i, this);
  }

  public void visit(QuadMeshShape q) {
    QuadMeshShape.superAccept(q, this);
  }

  public void visit(Camera c) {
    Camera.superAccept(c, this);
  }
  
  public void visit(ClippingPlane c) {
      ClippingPlane.superAccept(c, this);
  }
}
