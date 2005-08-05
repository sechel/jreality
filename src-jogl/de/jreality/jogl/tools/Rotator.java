/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import de.jreality.math.Quaternion;
import de.jreality.math.Rn;

/**
 * A Rotator is a class which interprets mouse motions to produce rotations
 * in object coordinates.
 *  
 * 
 *  @author Charles Gunn
 *
  */
public class Rotator extends Projector {
	double 	scaleFactor;

	/**
	 * 
	 */
	public Rotator() {
		super();
		scaleFactor = 1.0;
	}

	/**
	 * @return
	 */
	public double getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * @param d
	 */
	public void setScaleFactor(double d) {
		scaleFactor = d;
	}

	public Quaternion getRotationXY(double[] ndcLoc1, double[] ndcLoc2) {
		Rn.copy(anchor, ndcLoc1);
		return getRotationXY(ndcLoc2);
	}
	
	public Quaternion getRotationZ(double[] ndcLoc1, double[] ndcLoc2) {
		Rn.copy(anchor, ndcLoc1);
		return getRotationZ(ndcLoc2);
	}
	
	public Quaternion getRotationXY(double[] ndcLoc) {
		if (ndcLoc == null || ndcLoc.length != 2)	{
			throw new IllegalArgumentException("Bad NDC argument");
		}
		Quaternion theQ = new Quaternion();
		Rn.copy(current, ndcLoc);
		double[] diff = Rn.subtract(null, current, anchor);
		double theAngle = scaleFactor * Rn.euclideanNorm(diff);
		if (isReflection) theAngle *= -1.0;
		// very important this is a 4-vector!
		double[] theAxis = Rn.setToValue(null, -diff[1], diff[0], 0d,0d);
		Rn.matrixTimesVector(theAxis, camToObject, theAxis);
		Rn.normalize(theAxis, theAxis);
		Quaternion.makeRotationQuaternionAngle(theQ, theAngle, theAxis);
		return theQ;
	}
	public Quaternion getRotationZ(double[] ndcLoc) {
		if (ndcLoc == null || ndcLoc.length != 2)	{
			throw new IllegalArgumentException("Bad NDC argument");
		}
		Quaternion theQ = new Quaternion();
		Rn.copy(current, ndcLoc);
		double[] diff = Rn.subtract(null, current, anchor);
		double theAngle = scaleFactor * Rn.euclideanNorm(diff);
		// very important this is a 4-vector!
		double[] theAxis = Rn.setToValue(null, 0d, 0d, diff[1],0d);
		Rn.matrixTimesVector(theAxis, camToObject, theAxis);
		Rn.normalize(theAxis, theAxis);
		Quaternion.makeRotationQuaternionAngle(theQ, theAngle, theAxis);
		return theQ;
	}
}