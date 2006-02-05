
package de.jreality.scene;


/**
 * Sphere in (0, 0, 0) and r=1.0.
 */
public class Sphere extends Geometry
{
  /**
   * Constructor for UnitSphere.
   */
  public Sphere()
  {
    super();
  }
	
  public void accept(SceneGraphVisitor v) {
    v.visit(this);
  }
  static void superAccept(Sphere u, SceneGraphVisitor v) {
    u.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }
}
