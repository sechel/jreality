
package de.jreality.scene;

/**
 * Cylinder with axis from (0,0,-1) to (0,0,1) and  radius 1.
 */
public class Cylinder extends Geometry
{
  public void accept(SceneGraphVisitor v) {
    v.visit(this);
  }
  static void superAccept(Cylinder u, SceneGraphVisitor v) {
    u.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }
}
