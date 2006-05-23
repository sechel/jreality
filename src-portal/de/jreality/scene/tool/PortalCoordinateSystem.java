package de.jreality.scene.tool;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Camera;

public class PortalCoordinateSystem {

	/*
	 * comment from arMotionstarDriverPORTAL.cpp:
	 *   x -= 4.068; // move x=0 to the middle
	 * 	 y -= 1.36;  // move y=0 to the bottom of the visible screen (instead of the bottom)
	 * 	 z -= 4.068; // move z=0 to the center of the floor
	 */
	//  static double xDimPORTAL = 4.068;   // half PORTAL screen x-dim in feet
	//  static double yDimPORTAL = 6.561;   // full PORTAL screen y-dim in feet
	public static double xDimPORTAL = 2*4.068*0.3048;   // full PORTAL screen x-dim in METER
	public static double yDimPORTAL = 6.561*0.3048;   // full PORTAL screen y-dim in METER
	public static double zDimPORTAL = xDimPORTAL;
	public static double yOffsetPORTAL = 0.4;

	static double portalScale = 1.0;
	static double[] portalCenter = {0,0,0,1};
	static double[] metersToPortal = Rn.identityMatrix(4);
	static double[] portalToMeters = Rn.identityMatrix(4);

	private static boolean changed = true;
	public static double[] getPortalCenter() {
		return portalCenter;
	}
	public static void setPortalCenter(double[] portalCenter) {
		PortalCoordinateSystem.portalCenter = portalCenter;
		changed = true; update();
	}
	public static double getPortalScale() {
		return portalScale;
	}
	public static void setPortalScale(double portalScale) {
		PortalCoordinateSystem.portalScale = portalScale;
		changed = true; update();
	}
	private static void update()	{
		if (!changed) return;
		MatrixBuilder.euclidean().scale(portalScale).translate(portalCenter).assignTo(metersToPortal);
		Rn.inverse(portalToMeters, metersToPortal);
		changed = false;
	}
	public static double[] getMetersToPortal() {
		return metersToPortal;
	}
	public static double[] getPortalToMeters() {
		return portalToMeters;
	}
	public static Rectangle2D getWallPort() {
		Rectangle2D wp = new Rectangle2D.Double();
		wp.setFrame(
				portalScale*(-xDimPORTAL/2-portalCenter[0]),
				portalScale*(yOffsetPORTAL-portalCenter[1]),
				portalScale*xDimPORTAL,
				portalScale*yDimPORTAL);
		return wp;
	}
	//TODO read values and correction from ConfigurationAttributes
		//TODO change to multiplication with correction matrix
		//TODO think about moving this to a different class (PORTALUtilities)
		public static void setPORTALViewport(Matrix world2cam, FactoredMatrix portalMatrix, Camera cam) {
	    
			double xmin=0, xmax=0, ymin=0, ymax=0;
	//		double x0 = -PortalCoordinateSystem.xDimPORTAL/2;
	//		double x1 = PortalCoordinateSystem.xDimPORTAL/2;
	//		double y0 = PortalCoordinateSystem.yOffsetPORTAL;
	//		double y1 = PortalCoordinateSystem.yDimPORTAL+PortalCoordinateSystem.yOffsetPORTAL;
			Rectangle2D wallport = getWallPort();
			double x0 = wallport.getMinX();
			double y0 = wallport.getMinY();
			double x1 = wallport.getMaxX();
			double y1 = wallport.getMaxY();
			double[] portalOriginInCamCoordinates = world2cam.multiplyVector(portalMatrix.getTranslation());
			Pn.dehomogenize(portalOriginInCamCoordinates, portalOriginInCamCoordinates);
			
			double x = -portalOriginInCamCoordinates[0];
			double y = -portalOriginInCamCoordinates[1];
			double z = -portalOriginInCamCoordinates[2] + zDimPORTAL/2;  // make wall z=0
			cam.setFocus(z);
			xmin = (x - x0)/z;
			xmax = ((x1 - x0) - (x - x0))/z;
			ymin = (y - y0)/z;
			ymax = (( y1 - y0) - (y - y0))/z;
			cam.setViewPort(new Rectangle2D.Double(-xmin, -ymin, xmin+xmax, ymin+ymax));
	//		LoggingSystem.getLogger(CameraUtility.class).info("Setting camera viewport to "+cam.getViewPort().toString());
		}
}
