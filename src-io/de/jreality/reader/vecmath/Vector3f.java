package de.jreality.reader.vecmath;

public class Vector3f extends Tuple3f {

  public Vector3f() {
    super();
  }

  public Vector3f(double x, double y, double z) {
    super(x, y, z);
  }

  public Vector3f(double[] v) {
    super(v);
  }

  public Vector3f(Vector3f v1) {
    super(v1);
  }

  public Vector3f(Tuple3f t1) {
    super(t1);
  }

  public double dot(Vector3f pos) {
    return x*pos.x+y*pos.y+z*pos.z;
  }

}
