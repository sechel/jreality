/*
 * Author	gunn
 * Created on Aug 8, 2005
 *
 */
package de.jreality.jogl.pick;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.PickAction;
import de.jreality.scene.pick.PickPoint;
import de.jreality.util.CameraUtility;

public class JOGLPickUtility {

	private JOGLPickUtility() {
	}

	/**
	 * A convenience method
	 * @return
	 */
	public static double[] getPointObject(PickPoint pp, Viewer v) {
		double[] pointObject = null;
		Graphics3D context = new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
		if (pp.pickCoordinateSystem == PickAction.PICK_NDC)  pointObject = Rn.matrixTimesVector(null, context.getNDCToObject(), pp.getPointNDC() );
		else pointObject = Rn.matrixTimesVector(null, context.getWorldToObject(), pp.getPointWorld());
		if (pointObject.length == 4) Pn.dehomogenize(pointObject, pointObject);
		return pointObject;
	}

	/**
	 * A convenience method
	 * @return
	 */
	public double[] getPointWorld(PickPoint pp, Viewer v) {
		if (pp.pickCoordinateSystem == PickAction.PICK_WORLD) return pp.getPointWorld();
		if (pp.getPointNDC() == null)	throw new IllegalStateException("PickPoint should have non-null NDC point");
		Graphics3D context = new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
		double[] pointWorld = Rn.matrixTimesVector(null, context.getNDCToWorld(), pp.getPointNDC() );
		if (pointWorld.length == 4) Pn.dehomogenize(pointWorld, pointWorld);
		return (double[]) pointWorld.clone();
	}

	public static Graphics3D getContext(PickPoint pp, de.jreality.jogl.Viewer v) {
		return new Graphics3D(pp.getCameraPath(), pp.getPickPath(), CameraUtility.getAspectRatio(v));
	}

}
