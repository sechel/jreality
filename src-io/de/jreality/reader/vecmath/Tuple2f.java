package de.jreality.reader.vecmath;

public class Tuple2f {

  public double x;

  public double y;

  public Tuple2f() {
    this.x = (double) 0.0;
    this.y = (double) 0.0;
  }

  public Tuple2f(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public Tuple2f(double[] t) {
    this.x = t[0];
    this.y = t[1];
  }

  public Tuple2f(Tuple2f t1) {
    this.x = t1.x;
    this.y = t1.y;
  }

  public final void set(Tuple2f t1) {
    this.x = t1.x;
    this.y = t1.y;
  }

  public final void add(Tuple2f t1) {
    this.x += t1.x;
    this.y += t1.y;
  }

  public final void scale(double s) {
    this.x *= s;
    this.y *= s;
  }

}
