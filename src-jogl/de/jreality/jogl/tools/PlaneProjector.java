/*
 * Created on Mar 29, 2004
 *
 */
package de.jreality.jogl.tools;

import de.jreality.util.CameraUtility;
import de.jreality.util.math.Pn;
import de.jreality.util.math.Rn;

/**
 * @author Charles Gunn
 *
 */
public class PlaneProjector extends Projector {
	double  distanceToPlane;
	/**
	 * 
	 */
	public PlaneProjector() {
		super();
		distanceToPlane = -1;
	}

	public void setDefaultPlane()	{
		double[] imageOfOrigin = new double[4];
		Rn.matrixTimesVector(imageOfOrigin, objectToCam, Pn.originP3);
		// project onto z-axis
		imageOfOrigin[0] = imageOfOrigin[1] = 0d;
		distanceToPlane = Pn.distanceBetween(imageOfOrigin, Pn.originP3, camera.getSignature());
		if (imageOfOrigin[2] < 0.0) distanceToPlane *= -1;
	}
	
	public double getDistanceToPlane()	{
		return distanceToPlane;
	}
	
	public void setDistanceToPlane(double d)	{
		distanceToPlane = d;
	}
	
	public double[] getObjectPosition(double[] ndc, double[] result)	{
		double[] position;
		if (result == null) position = new double[4];
		else position = result;
		double[] p3 = CameraUtility.projectToPlane(camera, distanceToPlane, ndc);
		System.arraycopy(p3, 0, position, 0, 3);
		position[3] = 1.0;
		Rn.matrixTimesVector(position, camToObject, position);
		Pn.dehomogenize(position, position);
		return position;		
	}
	
	public double[] getObjectVector(double[] ndc1, double[] ndc2, double[] result)	{
		anchor[0] = ndc1[0];  anchor[1] = ndc1[1];
		return getObjectVector(ndc2, result);
	}
	
	public double[] getObjectVector(double[] ndc, double[] result)	{
		double[] vector;
		if (result == null) vector = new double[4];
		else vector = result;
		Rn.setToValue(last, current[0], current[1]);
		Rn.setToValue(current, ndc[0], ndc[1]);
		getObjectPosition(anchor, anchorV);
		getObjectPosition(current, currentV);
		Rn.subtract(vector, currentV, anchorV);
		//TODO clean up the tangent vector for non-euclidean case
		return vector;
	}
}
 
    
