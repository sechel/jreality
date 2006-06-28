/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.scene;

import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

import de.jreality.math.Pn;
import de.jreality.scene.event.CameraEvent;
import de.jreality.scene.event.CameraEventMulticaster;
import de.jreality.scene.event.CameraListener;
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
 * The camera also supports stereo viewing @link #setStereo(boolean).  For CAVE-like environments where there are not traditional on-axis camera, one 
 * can also set  an orientation 
 * matrix (@link #setOrientationMatrix(double[]))  to specify the orientation of the eye axis in camera coordinates.  
 * Default eye positions are (-eyeSeparation/2,0,0) and (eyeSeparation, 0,0) in the coordinate system of the
 * mono-optic camera.  Use @link de.jreality.util.CameraUtility#getNDCToCamera(Camera, double, int) to generate 
 * the appropriate projection matrices.
 * 
 * Due to refactoring, the camera no longer has enough state to provide the projective viewing transformation
 * from/to camera to/from Normalized Device Coordinates (NDC). It basically lacks the aspect ratio of the
 * output device.
 * 
 * @author Charles Gunn
 * 
 * TODO: IMPORTANT: remove setEye stuff +setViewPort +setFocus (everything that depends on screen etc.)
 * 
 *  Refactoring ideas:
 *  - eye separation - ?
 *  - stereo - ?
 *  - the orientation matrix should be removed, also viewer-dependent config
 *  - focus / field of view: i guess these should be removed also
 *  - aspect ratio isnt written at all...
 *  - isOnAxis might be implicit, if the frustum matrix is calculated, the viewer/renderer needs to decide whether onaxis or not ?
 *  		-- weissmann
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
	
	double eyeSeparation = 0.1;
	double[] orientationMatrix;		
	
  private CameraListener cameraListener;


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

	public double getNear() {
		return near;
	}

	public void setNear(double d) {
		near = d;
		fireCameraChanged();
	}

	public double getFar() {
		return far;
	}

	public void setFar(double d) {
		far = d;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	public double getFieldOfView() {
		return (180.0/Math.PI) * fieldOfView;
	}

	public void setFieldOfView(double d) {
		double f = (Math.PI/180.0)*d;
		if (f == fieldOfView)  return;
		fieldOfView = f;
    fireCameraChanged();
	}

	public double getFocus() {
		return focus;
	}

	public void setFocus(double d) {
		focus = d;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	public Rectangle2D getViewPort() {
		return viewPort;
	}

	public void setViewPort(Rectangle2D rectangle2D) {
		if (isOnAxis)	
			throw new IllegalStateException("Can't set viewport for an on-axis camera");
		viewPort = rectangle2D;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	public boolean isOnAxis() {
		return isOnAxis;
	}

	public void setOnAxis(boolean b) {
		isOnAxis = b;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	public boolean isPerspective() {
		return isPerspective;
	}

	public void setPerspective(boolean b) {
		isPerspective = b;
    // TODO: compare with old value?
    fireCameraChanged();
	}

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

	public void setOrientationMatrix(double[] orientationMatrix) {
		this.orientationMatrix = orientationMatrix;
    // TODO: compare with old value?
    fireCameraChanged();
	}

	public boolean isStereo() {
		return isStereo;
	}


	public void setStereo(boolean isStereo) {
		this.isStereo = isStereo;
		if (!isPerspective)	{
			LoggingSystem.getLogger(this).log(Level.WARNING,"Stereo camera must be perspective, setting it so.");
			isPerspective = true;
		}
    // TODO: compare with old value?
    fireCameraChanged();
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
