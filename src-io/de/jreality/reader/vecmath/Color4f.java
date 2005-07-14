package de.jreality.reader.vecmath;

import java.awt.Color;

public class Color4f extends Tuple4f {

  public Color4f() {
    super();
  }

  public Color4f(double x, double y, double z, double w) {
    super(x, y, z, w);
  }

  public Color4f(double[] c) {
    super(c);
  }

  public Color4f(Color4f c1) {
    super(c1);
  }

  public Color4f(Tuple4f t1) {
    super(t1);
  }

  public Color4f(Color color) {
    super((double) color.getRed() / 255.0f, (double) color.getGreen() / 255.0f,
        (double) color.getBlue() / 255.0f, (double) color.getAlpha() / 255.0f);
  }

}
