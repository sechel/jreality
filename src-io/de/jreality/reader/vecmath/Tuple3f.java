package de.jreality.reader.vecmath;

public class Tuple3f {

  public double x;

  public double y;

  public double z;

  public Tuple3f() {
    this.x = 0.0f;
    this.y = 0.0f;
    this.z = 0.0f;
  }

  public Tuple3f(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Tuple3f(double[] t) {
    this.x = t[0];
    this.y = t[1];
    this.z = t[2];
  }

  public Tuple3f(Tuple3f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
  }

  public final void set(Tuple3f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
  }

  public final void add(Tuple3f t1) {
    this.x += t1.x;
    this.y += t1.y;
    this.z += t1.z;
  }

  public final void scale(double s) {
    this.x *= s;
    this.y *= s;
    this.z *= s;
  }

  public final void interpolate(Tuple3f t1, Tuple3f t2, double alpha) {
    this.x = (1 - alpha) * t1.x + alpha * t2.x;
    this.y = (1 - alpha) * t1.y + alpha * t2.y;
    this.z = (1 - alpha) * t1.z + alpha * t2.z;

  }

}
