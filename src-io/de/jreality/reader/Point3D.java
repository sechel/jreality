package de.jreality.reader;

/**
 * helper class to identify equal points i.e. in Mathematica files
 * 
 * use in a HashMap ;-)
 * 
 * @author weissman
 */
class Point3D {

  private double x;
  private double y;
  private double z;

  Point3D(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public int hashCode() {
    long xbits = Double.doubleToLongBits(x);
    long ybits = Double.doubleToLongBits(y);
    long zbits = Double.doubleToLongBits(z);
    return (int) (xbits ^ (xbits >> 32) ^ ybits ^ (ybits >> 32) ^ zbits ^ (zbits >> 32));
  }

  public boolean equals(Object o) {
    if (o==null || !(o instanceof Point3D)) return false;
    Point3D t1 = (Point3D) o;
    return x == t1.x && y == t1.y && z == t1.z;
  }

}
