
/*
 * Created on Feb 24, 2004
 *
  */
package de.jreality.scene;

import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.event.*;
import de.jreality.util.CameraUtility;
import de.jreality.util.LoggingSystem;
/**
 *
 * The camera represents essentially a projection from three dimensions into two, that is
 * a specification of a viewing frustrum.
 * <p>
 * The viewing frustrum includes control over perspective/orthographic projection, field of view,
 * and focal length. 
 * The default camera is an on-axis camera.  
 * 
 * There is also support for off-axis cameras.  Use the {@link #setViewPort(Rectangle2D)} method to
 * specify the desired viewport.  In this case the field of view is ignored.
 * 
 * The camera also supports stereo viewing @link #setStereo(boolean).  Additionally one sets at which eye the camera is 
 * located (@link #setEye(int)).  Finally, for CAVE-like environments where there are not traditional on-axis camera, one 
 * can also set  an orientation 
 * matrix (@link #setOrientationMatrix(double[]))  to specify the orientation of the eye axis in camera coordinates.  
 * Default eye positions are (-eyeSeparation/2,0,0) and (eyeSeparation, 0,0) in the coordinate system of the
 * mono-optic camera.  Use @link de.jreality.util.CameraUtility#getNDCToCamera(Camera, double) to generate 
 * the appropriate projection matrices.
 * 
 * Due to refactoring, the camera no longer has enough state to provide the projective viewing transformation
 * from/to camera to/from Normalized Device Coordinates (NDC). It basically lacks the aspect ratio of the
 * output device. @see de.jreality.util.CameraUtility#getNDCToCamera(Camera, double).
 * 
 * @author Charles Gunn
 * 
 * TODO: clean up remove deprecated
 * TODO: IMPORTANT: remove setEye stuff +setViewPort +setFocus (everything that depends on screen etc.)
 * 
 *  Refactoring ideas:
 *  - eye should be removed, the renderers know the current eye and pass it to the calculate method in camera utility
 *  - eye separation - ?
 *  - stereo - ?
 *  - the orientation matrix should be removed, also viewer-dependent config
 *  - focus / field of view: i guess these should be removed also
 *  - aspect ratio isnt written at all...
 *  - isOnAxis might be implicit, if the frustum matrix is calculated, the viewer/renderer needs to decide whether onaxis or not ?
 *  
 */
public class Camera extends SceneGraphNode {
	double near, 
			far, 
			fieldOfView,
			aspectRatio,
			focus;
	Rectangle2D viewPort;
	private boolean 	isOnAxis = true,
			isPerspective = true,
			isStereo = false;
	
	//int whichEye = CameraUtility.MIDDLE_EYE;
	double eyeSeparation = 0.1;
	double[] orientationMatrix;		
	
  private CameraListener cameraListener;


	/**
	 * 
	 */
	public Camera(int sig) {
		super();
		switch (sig)	{
			  case Pn.EUCLIDEAN:
				  near = .5;
				  far = 50.0;
				  fieldOfView = Math.toRadians(60.0);
				  focus = 3.0;
				  break;
			  case Pn.HYPERBOLIC:
				  near = .05;
				  far = 100.0;
				  fieldOfView = Math.toRadians(90.0);
				  focus = 2.5;
				  break;
			  case Pn.ELLIPTIC:
				  near = .05;
				  far = -.05;
				  fieldOfView = Math.toRadians(90.0);
				  focus = 0.5;
				  break;
			  }
	}
	
	public Camera()	{
		this(Pn.EUCLIDEAN);
	}

	/**
	 * @return
	 */
	public double getNear() {
		return near;
	}

	/**
	 * @param d
	 */
	public void setNear(double d) {
		near = d;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * @return
	 */
	public double getFar() {
		return far;
	}

	/**
	 * @param d
	 */
	public void setFar(double d) {
		far = d;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * @return Field of view in degrees.
	 */
	public double getFieldOfView() {
		return (180.0/Math.PI) * fieldOfView;
	}

	/**
	 * @param d	Field of view in degrees.
	 */
	public void setFieldOfView(double d) {
		double f = (Math.PI/180.0)*d;
		if (f == fieldOfView)  return;
		fieldOfView = f;
    fireCameraChanged();
	}

	/**
	 * @return
	 */
	public double getFocus() {
		return focus;
	}

	/**
	 * @param d
	 */
	public void setFocus(double d) {
		focus = d;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * 
	 * @return
	 */
	public Rectangle2D getViewPort() {
		return viewPort;
	}

	/**
	 * 
	 * @param rectangle2D
	 */
	public void setViewPort(Rectangle2D rectangle2D) {
		if (isOnAxis)	
			throw new IllegalStateException("Can't set viewport for an on-axis camera");
		viewPort = rectangle2D;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * @return
	 */
	public boolean isOnAxis() {
		return isOnAxis;
	}

	/**
	 * @param b
	 */
	public void setOnAxis(boolean b) {
		isOnAxis = b;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * @return
	 */
	public boolean isPerspective() {
		return isPerspective;
	}

	/**
	 * @param b
	 */
	public void setPerspective(boolean b) {
		isPerspective = b;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	/**
	 * @return Returns the eyeSeparation.
	 */
	public double getEyeSeparation() {
		return eyeSeparation;
	}
	/**
	 * @param eyeSeparation The eyeSeparation to set.
	 */
	public void setEyeSeparation(double eyeSeparation) {
		this.eyeSeparation = eyeSeparation;
    // TODO: compare with old value?
    fireCameraChanged();
	}
	/**
	 * The orientation matrix describes the transformation in
	 * camera coordinate system which describes the orientation of
	 * the head; the "standard" position is that the eyes are on the
	 * x-axis, up is the y-axis, and z is the direction of projection
	 * The orientation matrix is used for cameras such as those in the
	 * PORTAL.
	 * @return Returns the orientationMatrix.
	 */
	public double[] getOrientationMatrix() {
		return orientationMatrix;
	}
	/**
	 * @param orientationMatrix The orientationMatrix to set.
	 */
	public void setOrientationMatrix(double[] orientationMatrix) {
		this.orientationMatrix = orientationMatrix;
    // TODO: compare with old value?
    fireCameraChanged();
	}
	/**
	 * @return Returns the isStereo.
	 */
	public boolean isStereo() {
		return isStereo;
	}
	/**
	 * @param isStereo The isStereo to set.
	 */
	public void setStereo(boolean isStereo) {
		this.isStereo = isStereo;
		if (!isPerspective)	{
			LoggingSystem.getLogger(this).log(Level.WARNING,"Stereo camera must be perspective, setting it so.");
			isPerspective = true;
		}
    // TODO: compare with old value?
    fireCameraChanged();
	}
	
//	public int getEye()	{
//		return whichEye;
//	}
//	
//	// TODO: backends should not set this!!!!
//	public void setEye(int which)	{
//		// TODO figure out proper behavior if not stereo already
//		if (!isStereo) { whichEye = CameraUtility.MIDDLE_EYE; return; }
//		whichEye = which;
//    // TODO: compare with old value?
////    fireCameraChanged();
//	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public double[] getCameraToNDC() {
		double[] m1 = CameraUtility.getCameraToNDC(this, aspectRatio, CameraUtility.MIDDLE_EYE); //whichEye);
		return m1;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public double[] getNDCToCamera() {
		return Rn.inverse(null, getCameraToNDC());
	}

  public void addCameraListener(CameraListener listener) {
    cameraListener=
      CameraEventMulticaster.add(cameraListener, listener);
  }

  public void removeCameraListener(CameraListener listener) {
    cameraListener=
      CameraEventMulticaster.remove(cameraListener, listener);
  }
  
  protected void fireCameraChanged() {
    final CameraListener cl = cameraListener;
    if (cl != null) cl.cameraChanged(new CameraEvent(this));
  }

	public void accept(SceneGraphVisitor v) {
	  v.visit(this);
	}
	
	static void superAccept(Camera c, SceneGraphVisitor v) {
	  c.superAccept(v);
	}
	private void superAccept(SceneGraphVisitor v) {
	  super.accept(v);
	}


 }
