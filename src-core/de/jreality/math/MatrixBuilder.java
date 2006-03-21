/*
 * Created on Apr 18, 2005
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.math;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;

/**
 *
 * This class wraps a Matrix instance.
 * <br> All the static methods are factory methods that
 * create an instance for a selected geometry/metric.
 * <br> <b>Note:</b> the factory methods with Transformation as a
 * parameter copy the underlying double[] and wrap the copy 
 * into a new Matrix instance - the factory methods that
 * take a Matrix as a parameter simply wor on the given Matrix.
 * Finally, the factorymethods without parameters create a
 * new identity matrix to act on. 
 * <p>
 * The instance methods are always applyed from rhs, so
 * they are operations "in the local coordinate system". All
 * these methods return a this reference, so that one can do
 * many calls in a row.
 * 
 * <code>
 * Matrix m = MatrixBuilder.euclidean()
 *            .translate(2,2,2)
 *            .rotate(Math.PI/2, 1, 1, 1)
 *            .scale(3,1,2)
 *            .getMatrix();
 * </code>
 * 
 * <code>
 * SceneGraphComponent camCom = new SceneGraphComponent();
 * MatrixBuilder.euclidean().translate(0,2,3)
 *              .rotateFromTo(new double[]{0,0,-1}, new double[]{0,-2,-3})
 *              .scale(2)
 *              .assignTo(camComp); // Transformation gets set and assigned
 * </code>
 * 
 * @author weissman
 */
public final class MatrixBuilder {
  
  private final Matrix matrix;
  private final int signature;

  private final double[] tmp = new double[16];
  
  /**
   * @deprecated Use {@link #euclidean(Transformation)}
   * Britannican on-line finds 159 matches for "euclidean" and 3 for "euclidian",
   * making clear that the preferred spelling is "euclidean" -- which is also
   * consistent with the spelling used in the class P3.
   */
  public static MatrixBuilder euclidian(Transformation m) {
	Matrix mat=(m!=null) ? new Matrix(m) : new Matrix();
    return new MatrixBuilder(mat, Pn.EUCLIDEAN);
  }

  /**
   * @deprecated Use {@link #euclidean(Matrix)}
   */
  public static MatrixBuilder euclidian(Matrix m) {
    return new MatrixBuilder(m, Pn.EUCLIDEAN);
  }
  
  /**
   * @deprecated Use {@link #euclidean()}
   */
  public static MatrixBuilder euclidian() {
    return euclidian(new Matrix());
  }

  public static MatrixBuilder euclidean(Transformation m) {
		Matrix mat=(m!=null) ? new Matrix(m) : new Matrix();
	    return new MatrixBuilder(mat, Pn.EUCLIDEAN);
	  }

  public static MatrixBuilder euclidean(Matrix m) {
	    return new MatrixBuilder(m, Pn.EUCLIDEAN);
	  }
	  
  public static MatrixBuilder euclidean() {
	    return euclidean(new Matrix());
	  }

  public static MatrixBuilder hyperbolic(Transformation m) {
	Matrix mat=(m!=null) ? new Matrix(m) : new Matrix();
    return new MatrixBuilder(mat, Pn.HYPERBOLIC);
  }
  
  public static MatrixBuilder hyperbolic(Matrix m) {
    return new MatrixBuilder(m, Pn.HYPERBOLIC);
  }
  
  public static MatrixBuilder hyperbolic() {
    return hyperbolic(new Matrix());
  }

  public static MatrixBuilder elliptic(Transformation m) {
	Matrix mat=(m!=null) ? new Matrix(m) : new Matrix();
    return new MatrixBuilder(mat, Pn.ELLIPTIC);
  }
  
  public static MatrixBuilder elliptic(Matrix m) {
    return new MatrixBuilder(m, Pn.ELLIPTIC);
  }
  
  public static MatrixBuilder elliptic() {
    return elliptic(new Matrix());
  }

  public static MatrixBuilder projective(Transformation m) {
	Matrix mat=(m!=null) ? new Matrix(m) : new Matrix();
    return new MatrixBuilder(mat, Pn.PROJECTIVE);
  }
  
  public static MatrixBuilder projective(Matrix m) {
    return new MatrixBuilder(m, Pn.PROJECTIVE);
  }
  
  public static MatrixBuilder projective() {
    return projective(new Matrix());
  }

  // It's often convenient to be able to specify the signature via integer rather than
  // searching for the specific signature-specific method [gunn]
  public static MatrixBuilder init(Matrix m, int signature)	{
  	return new MatrixBuilder(m==null ? new Matrix() : m, signature);
  }
  
  protected MatrixBuilder(Matrix m, int signature) {
    matrix = m;
    this.signature = signature;
  }

  public MatrixBuilder rotate(double angle, double axisX, double axisY, double axisZ) {
    return rotate(angle, new double[]{axisX, axisY, axisZ});
  }
  
  public MatrixBuilder rotate(double angle, double[] axis) {
    P3.makeRotationMatrix(tmp, axis, angle);
    matrix.multiplyOnRight(tmp);
    return this;
  }

  /**
   * rotate about the axis through the points p1 and p2
   * @param p1 first point on axis
   * @param p2 second point on axis
   * @param angle the angle to rotate
   * @return a MatrixBuilder...
   */
  public MatrixBuilder rotate(double[] p1, double[] p2, double angle) {
    P3.makeRotationMatrix(tmp, p1, p2, angle, signature);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder rotateX(double angle) {
    P3.makeRotationMatrixX(tmp, angle);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder rotateY(double angle) {
    P3.makeRotationMatrixY(tmp, angle);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder rotateZ(double angle) {
    P3.makeRotationMatrixZ(tmp, angle);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder rotateFromTo(double[] v1, double[] v2) {
    P3.makeRotationMatrix(tmp, v1, v2);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder scale(double scale) {
	    matrix.multiplyOnRight(P3.makeStretchMatrix(tmp, scale));
	    return this;
	  }

  public MatrixBuilder scale(double[] scale) {
	    scale(scale[0], scale[1], scale[2]);
		return this;
	  }
  
  public MatrixBuilder scale(double scaleX, double scaleY, double scaleZ) {
    // TODO: is this right for non-euclidean geoms?
    P3.makeStretchMatrix(tmp, new double[]{scaleX, scaleY, scaleZ, 1});
    matrix.multiplyOnRight(tmp);
    return this;
  }

  public MatrixBuilder translate(double[] vector) {
    P3.makeTranslationMatrix(tmp, vector, signature);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  public MatrixBuilder translate(double dx, double dy, double dz) {
    return translate(new double[]{dx, dy, dz});
  }
  
  /**
   * 
   * @return this
   */
  public MatrixBuilder translateFromTo(double[] p1, double[] p2) {
    P3.makeTranslationMatrix(tmp, p1, p2, signature);
    matrix.multiplyOnRight(tmp);
    return this;
  }

  /**
   * reflects the wrapped Matrix at the plane
   * determined by the the given 3 points
   * 
   * @param v1 first point on reflection plane
   * @param v2 second point on reflection plane
   * @param v3 third point on reflection plane
   * 
   * @return this
   */
  public MatrixBuilder reflect(double[] v1, double[] v2, double[] v3) {
    return reflect(P3.planeFromPoints(null, v1, v2, v3));
  }
  
  /**
   * reflects the wrapped Matrix at the plane
   * 
   * @param plane
   * 
   * @return this
   */
  public MatrixBuilder reflect(double[] plane) {
    P3.makeReflectionMatrix(tmp, plane, signature);
    matrix.multiplyOnRight(tmp);
    return this;
  }
  
  /**
   * multiplies the given Matrix on the right hand side
   * @param matrix
   * @return this
   */
  public MatrixBuilder times(Matrix matrix) {
    return times(matrix.getArray());
  }
  
  /**
   * multiplies the given array (length==16) on the right hand side
   * @param array
   * @return this
   */
  public MatrixBuilder times(double[] array) {
    matrix.multiplyOnRight(array);
    return this;
  }
  
  /**
   * assigns ID to the underlying matrix 
   * @return
   */
  public MatrixBuilder reset() {
    matrix.assignIdentity();
    return this;
  }
  
  public Matrix getMatrix() {
    return matrix;
  }
  public void assignTo(SceneGraphComponent comp) {
    matrix.assignTo(comp);
  }
  public void assignTo(Transformation trafo) {
    matrix.assignTo(trafo);
  }
  public void assignTo(double[] array) {
    matrix.assignTo(array);
  }
  public void assignTo(Matrix m) {
    matrix.assignTo(m);
  }

public double[] getArray() {
	return matrix.getArray();
}
}
