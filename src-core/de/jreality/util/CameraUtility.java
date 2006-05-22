/*
 * Created on May 28, 2004
 *
 */
package de.jreality.util;

import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.*;
import de.jreality.scene.*;

/**
 * @author Charles Gunn
 *
 */
public class CameraUtility {

	static boolean debug = false;
	// constants for support of stereo viewing
	public static final int MIDDLE_EYE = 0;
	public static final int LEFT_EYE = 1;
	public static final int RIGHT_EYE = 2;

	private CameraUtility() {
		super();
	}

	public static Camera getCamera(Viewer v)	{
		if (v == null || v.getCameraPath() == null || !(v.getCameraPath().getLastElement() instanceof Camera)) 
			throw new IllegalStateException("Viewer has no camera!");
		return ((Camera) v.getCameraPath().getLastElement());
	}

	public static SceneGraphComponent getCameraNode(Viewer v)	{
		return  v.getCameraPath().getLastComponent();
	}

	static double nearFarFactor = 0.9;
	/**
	 * @param camera
	 * @param viewer
	 */
	public static void encompass( de.jreality.scene.Viewer viewer) {
		// remove camera from the sceneRoot and encompass the result
		SceneGraphPath cp = viewer.getCameraPath();
		if (cp == null) throw new IllegalStateException("camerapath == null");
		if (cp.getLength() < 3)	{
		  throw new IllegalStateException("can't encompass: possibly Camera attached to root");
		}
    boolean removedCamera = false;
    SceneGraphComponent cameraBranch = (SceneGraphComponent) cp.iterator(1).next();
    SceneGraphComponent root = viewer.getSceneRoot();
    try {
      // TODO this is always true if camerapath starts at root
  		if(root.isDirectAncestor(cameraBranch)) {
  			root.removeChild(cameraBranch);
  			removedCamera = true;
  		} 
  		encompass(viewer, root, true);
    } finally {
      // if we miss that the camera path is invalid!
		  if (removedCamera) root.addChild(cameraBranch);
    }
	}
	
	public static void encompass(Viewer viewer, SceneGraphComponent sgc, boolean setStereoParameters)	{
		Rectangle3D worldBox = GeometryUtility.calculateBoundingBox(sgc);//. bbv.getBoundingBox();
		
		if (worldBox == null || worldBox.isEmpty())	{
			LoggingSystem.getLogger(CameraUtility.class).log(Level.WARNING,"encompass: empty bounding box");
			return;	
		}
		
		if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"BBox: "+worldBox.toString());
		
		Camera cam = getCamera(viewer);
		// the extent in camera coordinates
		double[] extent = worldBox.getExtent();
		
		double ww = (extent[1] > extent[0]) ? extent[1] : extent[0];
		double focus =  .5 * ww / Math.tan(Math.PI*(cam.getFieldOfView())/360.0);
    
		double[] to = worldBox.getCenter();
		to[2] += extent[2]*.5;
		double[] tofrom = {0,0,focus}; 
		double[] from = Rn.add(null, to, tofrom);
		if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"translate: "+Rn.toString(from));
		double[] newCamToWorld = P3.makeTranslationMatrix(null, from, viewer.getSignature());
		double[] newWorldToCam = Rn.inverse(null, newCamToWorld);
		getCameraNode(viewer).getTransformation().setMatrix(newCamToWorld); //Translation(from);
		double[] centerWorld = Rn.matrixTimesVector(null, newWorldToCam, worldBox.getCenter() );
		if (setStereoParameters)	{
			cam.setFocus(Math.abs(centerWorld[2]) ); 		//focus);
			cam.setEyeSeparation(cam.getFocus()/12.0);		// estimate a reasonable separation based on the focal length	
		}
		//TODO figure out why setting the near/far clipping planes sometimes doesn't work
		Rectangle3D cameraBox = worldBox.transformByMatrix(null, newWorldToCam);
		if (debug) LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"Bbox: "+cameraBox.toString());
		double zmin = cameraBox.getMinZ();
		double zmax = cameraBox.getMaxZ();
		
		if ( cam.getFar() > 0.0 && zmax < 0.0 && -zmax > .1*cam.getFar() )  cam.setFar(-10*zmax);
		if ( zmin < 0.0 && -zmin < 10*cam.getNear() )  cam.setNear(-.1*zmin);
		//cam.update();
		
	}

	private static double getAspectRatio(java.awt.Component vc)		{
		return ((double) vc.getWidth())/(vc.getHeight());
	}
	
	public static double getAspectRatio(Viewer v)		{
		if (!v.hasViewingComponent()) return 1.0;
		Component c = v.getViewingComponent();
		return ((double) c.getWidth())/ c.getHeight();
	}
	
	public static double[] getCameraToNDC(Viewer v)			{
		Camera cam = getCamera(v);
		double aspectRatio = (v.hasViewingComponent() ? getAspectRatio(v.getViewingComponent()) : 1.0);
		// TODO figure out a better way to do this:  This is only correct in crosseyed stereo mode.
//		if (cam.isStereo()) aspectRatio *= .5;
		return getCameraToNDC(cam, aspectRatio);
	}
	
	public static double[] getCameraToNDC(Camera cam, double aspectRatio)		{
		return getCameraToNDC(cam, aspectRatio, CameraUtility.MIDDLE_EYE);
	}
	
	/**
	 * Calculate a 4x4 projection matrix for this camera.  If <i>which</i> is <code>MIDDLE_EYE</code>, calculate a
	 * normal "monocular" camera.  If <i>which</i> is <code>LEFT_EYE</code> or <code>RIGHT_EYE,</code>, calculate the
	 * projection matrix corresponding to the given eye of a stere-ocular camera.  The stereo case can be derived 
	 * from the monocular case as follows.  
	 * 
	 * Define V to be the intersection of the viewing frustum with the plane <i>z = focus</i> (See {@link #setFocus(double)}).
	 * Second, define the positions <i>Pl = (d,0,0,0)</i> and <i>Pr = (-d,0,0,0)</i> where <i>d = eyeSeparation/2.0</i>  (See
	 * {@link #setEyeSeparation(double)}). Then the position of the left eye in
	 * camera coordinates is O.Pl (where O is the camera's orientation matrix (See {@link #setOrientationMatrix(double[])}), or the identity
	 * matrix if none has been set) and similarly for the right eye. Then the viewing frustum for the left eye is the unique viewing frustum determined by 
	 * the position at the left (right) eye and the rectangle V; similarly for the right eye.
	 * 
	 * In plain English, the monocular, left, and right views all show the same picture if the world lies in the <i>z = focus</i> plane.
	 * This plane is in fact the focal plane in this sense.
	 * 
	 * Note that the <i>orientationMatrix</i> is only non-trivial in the case of virtual environments such as the PORTAL or CAVE. 	 * @deprecated
	 * @param which
	 * @return
	 */
	public static double[] getCameraToNDC(Camera cam, double aspectRatio, int which)		{
			/** 
			* If the projectoin is orthogonal, scales the viewPort by the \IT{focus}.
			* This method won't be called if the value of \IT{isOnAxis} is FALSE;
			* instead the view port should be directly set using \mlink{setViewPort:}.
			*/
		LoggingSystem.getLogger(CameraUtility.class).log(Level.FINER,"Aspect ratio is "+aspectRatio);
//		System.out.println("Aspect ratio is "+aspectRatio);
		Rectangle2D viewPort = getViewport(cam, aspectRatio);
		if (which == CameraUtility.MIDDLE_EYE)		{
			double[] cameraToNDC = null;
			if (cam.isPerspective())
				cameraToNDC = P3.makePerspectiveProjectionMatrix(null, viewPort, cam.getNear(), cam.getFar());
			else	
				cameraToNDC = P3.makeOrthographicProjectionMatrix(null, viewPort,cam.getNear(), cam.getFar());
			return cameraToNDC;			
		}  // else we're in a stereo mode
		double factor;
		factor = (which == CameraUtility.LEFT_EYE) ? -1 : 1;
		//if (eyeSeparation == 0.0) eyeSeparation = focus/6.0;
		
		double[] eyePosition = new double[] {factor * cam.getEyeSeparation()/2.0, 0d, 0d, 0d};
		if (cam.getOrientationMatrix() != null)	{
			Rn.matrixTimesVector(eyePosition, cam.getOrientationMatrix(), eyePosition);
			Pn.dehomogenize(eyePosition, eyePosition);
			//LoggingSystem.getLogger().log(Level.FINER,((which == RIGHT_EYE)?"right":"left")+" eye position: "+Rn.toString(eyePosition));
		}
		if (eyePosition[3] == 0.0) eyePosition[3] = 1.0;
		// TODO make this work also for non-euclidean cameras
		double[] moveToEye = P3.makeTranslationMatrix(null, eyePosition, Pn.EUCLIDEAN );
		double x = eyePosition[0];
		double y = eyePosition[1];
		double z = eyePosition[2];
		Rectangle2D newVP = new Rectangle2D.Double();
		double focus = cam.getFocus();
		double newFocus = focus + z;
		double fscale = 1.0/newFocus;
		// We want the slice of the non-stereo frustum at z = focus to be also a slice 
		// of the new, stereo frustum.  Make that happen:
		// Scale the camera viewport to lie in the z=focus plane, 
		// translate it into the coordinates of the eye position (left or right),
		// then project it onto the z=1 plane in this coordinate system.
		//if (isOnAxis && needsViewport) updateViewport();
		//Rectangle2D viewPort = cam.getViewPort();
		newVP.setFrameFromDiagonal(fscale*(viewPort.getMinX()*focus-x), 
								 fscale*(viewPort.getMinY()*focus-y), 
								 fscale*(viewPort.getMaxX()*focus-x), 
								 fscale*(viewPort.getMaxY()*focus-y));
		// TODO should we adjust near and far ?
		double[] c2ndc = P3.makePerspectiveProjectionMatrix(null, newVP, cam.getNear(), cam.getFar());		
		double[] iMoveToEye = Rn.inverse(null, moveToEye);
		//LoggingSystem.getLogger().log(Level.FINER,"iMoveToEye is \n"+Rn.matrixToString(iMoveToEye));
		double[] ret = Rn.times(null, c2ndc, iMoveToEye);
		return ret;
	}

	/**
	 * @param cam
	 * @param aspectRatio
	 * @return
	 */
	public static Rectangle2D getViewport(Camera cam, double aspectRatio) {
		Rectangle2D viewPort = null;
		if (cam.isOnAxis())	{
			viewPort = new Rectangle2D.Double();
			double hwidth = Math.tan((Math.PI/180.0)*cam.getFieldOfView()/2.0);
			if (!cam.isPerspective())	hwidth *= cam.getFocus();
			if (aspectRatio > 1.0)	{
				viewPort.setFrameFromDiagonal(-hwidth*aspectRatio, -hwidth,hwidth*aspectRatio,  hwidth);
			} else {
				viewPort.setFrameFromDiagonal(-hwidth,-hwidth/aspectRatio, hwidth,  hwidth/aspectRatio);
			}	
		} else viewPort = cam.getViewPort();
		return viewPort;
	}
	

	public static double[] getNDCToCamera(Viewer v)	{
		return Rn.inverse(null, getCameraToNDC(v));
	}
	
	public static double[] getNDCToCamera(Camera cam, double aspectRatio)	{
		return Rn.inverse(null, getCameraToNDC(cam, aspectRatio));
	}
	
    
//  static double xDimPORTAL = 4.068;   // half PORTAL screen x-dim in feet
//  static double yDimPORTAL = 6.561;   // full PORTAL screen y-dim in feet
  static double xDimPORTAL = 4.068*0.3048;   // half PORTAL screen x-dim in METER
  static double yDimPORTAL = 6.561*0.3048;   // full PORTAL screen y-dim in METER
  static double yOffsetPORTAL = 0.4;
  /*
	 * comment from arMotionstarDriverPORTAL.cpp:
	 *   x -= 4.068; // move x=0 to the middle
	 * 	 y -= 1.36;  // move y=0 to the bottom of the visible screen (instead of the bottom)
	 * 	 z -= 4.068; // move z=0 to the center of the floor
	 */
	//TODO read values and correction from ConfigurationAttributes
	//TODO change to multiplication with correction matrix
	//TODO think about moving this to a different class (PORTALUtilities)
	public static void setPORTALViewport(Matrix world2cam, FactoredMatrix portalMatrix, Camera cam) {
    
		double xmin=0, xmax=0, ymin=0, ymax=0;
		double x0 = -xDimPORTAL;
		double x1 = xDimPORTAL;
		double y0 = yOffsetPORTAL;
		double y1 = yDimPORTAL+yOffsetPORTAL;
		//double[] world2cam = v.getCameraPath().getInverseMatrix(null);
		//double[] pos1 = Rn.matrixTimesVector(null, cam2world, P3.originP3);
		double[] portalOriginInCamCoordinates = world2cam.multiplyVector(portalMatrix.getTranslation());
		//double[] pos1 = Rn.matrixTimesVector(null, world2cam, Pn.originP3);
		Pn.dehomogenize(portalOriginInCamCoordinates, portalOriginInCamCoordinates);
		
    // TODO: 
    // The world coordinate system and the one we want differ only
		// by moving the origin forward so it lies on front wall.
		double x = -portalOriginInCamCoordinates[0];
		double y = -portalOriginInCamCoordinates[1];
		double z = -portalOriginInCamCoordinates[2] + xDimPORTAL;  // make wall z=0
		cam.setFocus(z);
		xmin = (x - x0)/z;
		xmax = ((x1 - x0) - (x - x0))/z;
		ymin = (y - y0)/z;
		ymax = (( y1 - y0) - (y - y0))/z;
		cam.setViewPort(new Rectangle2D.Double(-xmin, -ymin, xmin+xmax, ymin+ymax));
//		LoggingSystem.getLogger(CameraUtility.class).info("Setting camera viewport to "+cam.getViewPort().toString());
	}

  public static void encompass(SceneGraphPath avatarPath, SceneGraphPath scene, SceneGraphPath cameraPath) {
    encompass(avatarPath, scene, cameraPath, 0, Pn.EUCLIDEAN);
  }
  
  public static void encompass(SceneGraphPath avatarPath, SceneGraphPath scene, SceneGraphPath cameraPath, double margin, int signature) {
    Rectangle3D bounds = GeometryUtility.calculateBoundingBox(scene.getLastComponent());
    if (bounds.isEmpty()) return;
    Matrix rootToScene = new Matrix();
    scene.getMatrix(rootToScene.getArray(), 0, scene.getLength()-2);
    Rectangle3D worldBounds = bounds.transformByMatrix(new Rectangle3D(), rootToScene.getArray());
    Rectangle3D avatarBounds = worldBounds.transformByMatrix(new Rectangle3D(), avatarPath.getInverseMatrix(null));
    double [] e = avatarBounds.getExtent();
    double radius = Math.sqrt(e[0]*e[0] + e[2]*e[2] + e[1]*e[1]);
    double [] c = avatarBounds.getCenter();
    c[2] += radius;
    Rn.times(c, margin, c);
    // add head height to c[1]
    Matrix camMatrix = new Matrix();
    cameraPath.getInverseMatrix(camMatrix.getArray(), avatarPath.getLength());
    
    ((Camera)cameraPath.getLastElement()).setFar(margin*5*radius);
    ((Camera)cameraPath.getLastElement()).setNear(.002*radius);
    SceneGraphComponent avatar = avatarPath.getLastComponent();
    Matrix m = new Matrix(avatar.getTransformation());
    MatrixBuilder.init(m, signature).translate(c).translate(camMatrix.getColumn(3)).assignTo(avatar);
  }

}
