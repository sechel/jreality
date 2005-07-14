package de.jreality.reader.vecmath;

public class Point3f extends Tuple3f {

  public Point3f() {
    super();
  }

  public Point3f(double x, double y, double z) {
    super(x, y, z);
  }

  public Point3f(double[] p) {
    super(p);
  }

  public Point3f(Point3f p1) {
    super(p1);
  }

  public Point3f(Tuple3f t1) {
    super(t1);
  }

  public double distanceSquared(Point3f v1) {
    return (x - v1.x) + (y - v1.y) + (z - v1.z);
  }

}
