/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import de.jreality.scene.Camera;
import de.jreality.util.CameraUtility;
import de.jreality.util.Pn;
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Projector {
	Camera	camera;
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

	/**
	 * @return
	 */
	public double[] getAnchor() {
		return anchor;
	}

	/**
	 * @return
	 */
	public Camera getCamera() {
		return camera;
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
	 * @param camera
	 */
	public void setCamera(Camera cam) {
		camera = cam;
	}

	/**
	 * @param ds
	 */
	public void setCameraToObject(double[] ds) {
		camToObject = ds;
		Rn.inverse(objectToCam, camToObject);
		if (Rn.determinant(camToObject) < 0.0) isReflection = true;
		else isReflection = false;
		//System.out.println("Reflection is "+isReflection);
	}

	/**
	 * @param ds
	 */
	public void setObjectToCamera(double[] ds) {
		objectToCam = ds;
		Rn.inverse(camToObject, objectToCam);
		if (Rn.determinant(camToObject) < 0.0) isReflection = true;
		else isReflection = false;
		//System.out.println("Reflection is "+isReflection);
	}
	
//	this translates a vector in camera coordinates to object coordinates
//	since it's a vector with w=0, dehomogenizing is dangerous
	protected void convertCameraToObject(double[] inv, double[] outV)	{
		if (inv.length != 4 || outV.length != 4)	{
			System.err.println("convertCameraToObject bad dimensions");
			return;
		}
		Rn.matrixTimesVector(outV, camToObject, inv);
	}

	protected void NDCToObject(double[] object, double[] ndc)	{
		if (ndc == null || object != null && object.length != 4)	{
			System.err.println("convertCameraToObject bad dimensions");
			return;
		}
		double[] ndc4 = new double[4];
		Rn.setToValue(ndc4, ndc[0], ndc[1], 0d, 1d);
		if (object == null)	object = new double[4];
		Rn.matrixTimesVector(object, camera.getNDCToCamera(), ndc4);
		Rn.matrixTimesVector(object, camToObject, object);
		Pn.dehomogenize(object, object);
	}
	
//	this translates the NDC position (considered as a tangent vector
//	projected on near clipping plane) into a tangent vector in object coords
//	based at the origin of object space
	public void getDirection(double[] ndcLoc, double[] result)	{
		if (ndcLoc.length != 3 || result.length != 4)	{
			System.err.println("projectToDirection bad dimensions");
			return;
		}
		CameraUtility.projectToDirection(camera, ndcLoc, result);
		result[3] = 0.0;
		Rn.matrixTimesVector(result, camToObject, result);	
	}
}
