package de.jreality.reader.vecmath;

import java.awt.Color;

public class Color3f extends Tuple3f {

  public Color3f() {
    super();
  }

  public Color3f(double x, double y, double z) {
    super(x, y, z);
  }

  public Color3f(double[] v) {
    super(v);
  }

  public Color3f(Color3f v1) {
    super(v1);
  }

  public Color3f(Tuple3f t1) {
    super(t1);
  }

  public Color3f(Color color) {
    super((double) color.getRed() / 255.0f, (double) color.getGreen() / 255.0f,
        (double) color.getBlue() / 255.0f);
  }
}
