/*
 * Created on Mar 5, 2004
 *
 */
package de.jreality.scene.pick;

import de.jreality.math.Rn;
import de.jreality.scene.*;
import de.jreality.util.CameraUtility;

/**
 * @author gunn
 * @deprecated
 */
public class Graphics3D {
	//Viewer viewer;
	SceneGraphComponent theRoot;
	SceneGraphPath cameraPath;
	Camera camera;
	double[] objectToWorld;
	boolean fastAndDirty = false;
	SceneGraphPath currentPath;

	/**
	 * @deprecated
	 * @param v
	 */
	public Graphics3D(Viewer v)	{
		this(v, null);
	}
	
	/**
	 * @deprecated
	 * @param v
	 * @param sgp
	 */public Graphics3D(Viewer v, SceneGraphPath sgp)	{
	 	this( v.getCameraPath(), sgp);
	 }
	 
	 public Graphics3D(SceneGraphPath cp, SceneGraphPath sgp)	{
		super();
		//objectToWorld = Rn.identityMatrix(4);
		if (sgp != null && sgp.getLength() > 0) setRoot((SceneGraphComponent) sgp.getFirstElement());
		setCameraPath(cp);
		setCurrentPath(sgp);
	}
	
	/**
	 * @param cameraPath2
	 */
	private void setCameraPath(SceneGraphPath cameraPath2) {
		cameraPath = cameraPath2;
		if (cameraPath == null) return;
		Object obj = cameraPath.getLastElement();
		if ( obj != null && obj instanceof Camera)	camera = (Camera) cameraPath.getLastElement();
		else throw new IllegalArgumentException("Not a camera path");
	}

	/**
	 * @param sceneRoot
	 */
	private void setRoot(SceneGraphComponent sceneRoot) {
		if (sceneRoot == null) throw new IllegalArgumentException("Root can't be null");
		theRoot = sceneRoot;
	}

	public Graphics3D Graphics3DFactory( SceneGraphPath cp, SceneGraphPath sgp)	{
		if (sgp == null) {
			sgp = new SceneGraphPath();
		}
		Graphics3D gc = new Graphics3D( cp, sgp);
		return gc;
	}
//	public Object clone() throws CloneNotSupportedException {
//		try {
//			Graphics3D copy = (Graphics3D) super.clone();
//			if (objectToWorld !=null) copy.objectToWorld = (double[]) objectToWorld.clone();
//			if (currentPath !=null) copy.currentPath = (SceneGraphPath) currentPath.clone();
//			return copy;
//		} catch (CloneNotSupportedException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	public Graphics3D copy()	{
		Graphics3D copy = new Graphics3D(cameraPath, currentPath);
		copy.fastAndDirty = fastAndDirty;
		copy.objectToWorld = objectToWorld;  // already a copy, no danger using it
		return copy;
	}

	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}
	public SceneGraphComponent getRoot() {
		return theRoot;
	}
	/**
	 * @return
	 */
	public double[] getCameraToNDC() {
		if (cameraPath == null) throw new IllegalStateException("No camera path set for this context");
		return CameraUtility.getCameraToNDC(camera, 1.0, CameraUtility.MIDDLE_EYE); //camera.aspectRatio, CameraUtility.MIDDLE_EYE);
	}

	/**
	 * If the path is set, then it overrides the object2World matrix value
	 * @return
	 */
	public double[] getObjectToWorld() {
		if (objectToWorld != null)	return objectToWorld;
		else if (currentPath != null) return currentPath.getMatrix(null);
		return Rn.identityMatrix(4);
	}

	/**
	 * @return
	 */
	public double[] getWorldToObject() {
		return Rn.inverse(null,getObjectToWorld());
	}

	/**
	 * @return
	 */
	public double[] getWorldToCamera() {
		if (cameraPath == null) 
			throw new IllegalStateException("No camera path");
		return cameraPath.getInverseMatrix(null);
	}

	/**
	 * @return
	 */
	public double[] getCameraToWorld() {
		if (cameraPath == null) 			
			throw new IllegalStateException("No camera path");
		return cameraPath.getMatrix(null);
	}
	/**
	 * @return
	 */
	public double[] getWorldToNDC() {
		return Rn.times(null, getCameraToNDC(), getWorldToCamera());
	}

	/**
	 * @param ds
	 */
	public void setObjectToWorld(double[] ds) {
		objectToWorld = (double[]) ds.clone();
	}


	 /* @param ds
	 */
	public double[] getObjectToCamera() {
		return Rn.times(null, getWorldToCamera(), getObjectToWorld());
	}

	/**
	 * @return
	 */
	public double[] getCameraToObject() {
		return Rn.inverse(null, getObjectToCamera());
	}

	/**
	 * @param ds
	 */
	public double[] getObjectToNDC() {
		if (camera == null) 
			throw new IllegalStateException("No camera for this context");
		return Rn.times(null,CameraUtility.getCameraToNDC(camera, 1.0, CameraUtility.MIDDLE_EYE), //camera.aspectRatio, CameraUtility.MIDDLE_EYE), 
			getObjectToCamera());
	}

	/**
	 * @param ds
	 */
	public double[] getNDCToObject() {
		if (camera == null) 
			throw new IllegalStateException("No camera for this context");
		return Rn.inverse(null, Rn.times(null, CameraUtility.getCameraToNDC(camera, 1.0, CameraUtility.MIDDLE_EYE), //camera.aspectRatio, CameraUtility.MIDDLE_EYE), 
			getObjectToCamera()));
	}
	
	/**
	 * @return
	 */
	public SceneGraphPath getCurrentPath() {
		return currentPath;
	}

	/**
	 * @param path
	 */
	public void setCurrentPath(SceneGraphPath path) {
		//if (path != null) currentPath = ((SceneGraphPath) path.clone());
		if (path != null && path.getLength() > 0)currentPath = path;
	}

	/**
	 * @return
	 */
	public double[] getNDCToWorld() {
		return Rn.inverse(null, getWorldToNDC());
	}


}
