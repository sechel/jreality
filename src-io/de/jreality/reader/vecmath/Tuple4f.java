package de.jreality.reader.vecmath;

public class Tuple4f {

  public double x;

  public double y;

  public double z;

  public double w;

  public Tuple4f() {
    this.x = 0.0f;
    this.y = 0.0f;
    this.z = 0.0f;
    this.w = 0.0f;
  }

  public Tuple4f(double x, double y, double z, double w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Tuple4f(double[] t) {
    this.x = t[0];
    this.y = t[1];
    this.z = t[2];
    this.w = t[3];
  }

  public Tuple4f(Tuple4f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
    this.w = t1.w;
  }

  public final void set(Tuple4f t1) {
    this.x = t1.x;
    this.y = t1.y;
    this.z = t1.z;
    this.w = t1.w;
  }

  public final void add(Tuple4f t1) {
    this.x += t1.x;
    this.y += t1.y;
    this.z += t1.z;
    this.w += t1.w;
  }

  public final void scale(double s) {
    this.x *= s;
    this.y *= s;
    this.z *= s;
    this.w *= s;
  }

}
