/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import de.jreality.math.Rn;
import de.jreality.scene.Camera;
import de.jreality.scene.Viewer;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 */
public class Projector {
	Viewer	theViewer;
	double[] objectToCam, camToObject;
	double[] anchor, 		// beginning point for interaction
		current,		// latest point of interaction
		last;			// next-to-latest point of interaction
	double[] anchorV, currentV, lastV;
	boolean isReflection = false;

	/**
	 * 
	 */
	public Projector() {
		super();
		objectToCam = Rn.identityMatrix(4);
		camToObject =  Rn.identityMatrix(4);
		anchor = new double[2];
		current = new double[2];
		last = new double[2];
		anchorV = new double[4];
		currentV = new double[4];
		lastV = new double[4];
	}

	public Projector(Viewer v) {
		this();
		theViewer = v;
	}

	/**
	 * @return
	 */
	public double[] getAnchor() {
		return anchor;
	}

	/**
	 * @return
	 */
	public double[] getCameraToObject() {
		return camToObject;
	}

	/**
	 * @return
	 */
	public double[] getObjectToCamera() {
		return objectToCam;
	}

	/**
	 * @param ds
	 */
	public void setAnchor(double[] ds) {
		anchor = ds;
	}

	/**
	 * @param ds
	 */
	public void setCameraToObject(double[] ds) {
		camToObject = ds;
		Rn.inverse(objectToCam, camToObject);
		if (Rn.determinant(camToObject) < 0.0) isReflection = true;
		else isReflection = false;
		//JOGLConfiguration.theLog.log(Level.FINE,"Reflection is "+isReflection);
	}

	/**
	 * @param ds
	 */
	public void setObjectToCamera(double[] ds) {
		objectToCam = ds;
		Rn.inverse(camToObject, objectToCam);
		if (Rn.determinant(camToObject) < 0.0) isReflection = true;
		else isReflection = false;
		//JOGLConfiguration.theLog.log(Level.FINE,"Reflection is "+isReflection);
	}
	
//	this translates a vector in camera coordinates to object coordinates
//	since it's a vector with w=0, dehomogenizing is dangerous
	protected void convertCameraToObject(double[] inv, double[] outV)	{
		if (inv.length != 4 || outV.length != 4)	{
			throw new IllegalArgumentException("Arguments must be 4-vectors");
		}
		Rn.matrixTimesVector(outV, camToObject, inv);
	}

//	protected void NDCToObject(double[] object, double[] ndc)	{
//		if (ndc == null || object != null && object.length != 4)	{
//			throw new IllegalArgumentException("Null arguments, or arguments must be 4-vectors");
//		}
//		double[] ndc4 = new double[4];
//		Rn.setToValue(ndc4, ndc[0], ndc[1], 0d, 1d);
//		if (object == null)	object = new double[4];
//		Rn.matrixTimesVector(object, camera.getNDCToCamera(), ndc4);
//		Rn.matrixTimesVector(object, camToObject, object);
//		Pn.dehomogenize(object, object);
//	}
	
//	this translates the NDC position (considered as a tangent vector
//	projected on near clipping plane) into a tangent vector in object coords
//	based at the origin of object space
	public void getDirection(double[] ndcLoc, double[] result)	{
		if (ndcLoc.length != 3 || result.length != 4)	{
			throw new IllegalArgumentException("Bad vector length");
		}
		Projector.projectToDirection(theViewer, ndcLoc, result);
		result[3] = 0.0;
		Rn.matrixTimesVector(result, camToObject, result);	
	}

	//	* This computes the tangent vector centered at the camera (that is, the point $(0,0,0)$),
	//	* corresponding to the point in NDC coordinates specified by
	//	* \IT{inV} (for example a mouse position).  
	//	* The vector is normalized not to have unit length, but
	//	* so its tip lies on the $ z = -1 $ plane. 
	//	* Project the NDC point \IT{inV} to the point in camera coordinates which lies on the
	//	* plane $ z  = -aDistance $.
		public static void projectToDirection(Viewer v, double[] inV, double[] outV)	{
			if (inV.length != 2 || outV.length != 3)	{
				throw new IllegalArgumentException("Invalid dimensions");
			}
			double[] tmp = new double[3];
			Rn.setToValue(tmp, inV[0], inV[1], -1.0);
			Rn.matrixTimesVector(outV, CameraUtility.getNDCToCamera(v ), outV);
			double sc = Math.abs(outV[2]);
			if (sc != 0) sc = 1.0/sc;
			Rn.times(outV, sc, outV);
		}

		//	* Project the NDC point \IT{inV} to the point in camera coordinates which lies on the
	//	* plane $ z  = -aDistance $.
		public static double[] projectToPlane(Viewer v, double distance, double[] ndc)	{
			double[] outVector = new double[3];
			Rn.setToValue(outVector, ndc[0], ndc[1], -1d);
			Rn.matrixTimesVector(outVector, CameraUtility.getNDCToCamera(v), outVector);
			if (CameraUtility.getCamera(v).isPerspective())	{
				double scale = distance/outVector[2];
				Rn.times(outVector, scale, outVector);
			} else outVector[2] = distance;
			return outVector;
		}
}
