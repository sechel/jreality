/*
 * Created on Apr 21, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.JOGLConfiguration;

/**
 * @author Charles Gunn
 *
 */
public class CameraZoomTool extends AbstractMouseTool {
	double initialFOV;
	double scale = 1.5;
	Rectangle2D viewport, initialViewport;
	/**
	 * 
	 */
	public CameraZoomTool() {
		super();
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		super.startTrackingAt(e);
		initialFOV = theCamera.getFieldOfView();
		if (button == 2)	{
			initialViewport = theCamera.getViewPort();
			viewport = new Rectangle2D.Double();
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		super.track(e);
		if (button == 2) return true;
		double fov = Math.exp(-scale * diff[1]) * initialFOV;
		theCamera.setFieldOfView(fov > 179.0 ? 179.0 : fov);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		super.endTracking(e);
		if (button == 2)	{
			viewport.setFrameFromDiagonal(anchor[0], anchor[1], current[0], current[1]);
			double cx = viewport.getCenterX();
			double cy = viewport.getCenterY();
			double w = viewport.getWidth();
			double iw = initialViewport.getWidth();
			double ih = initialViewport.getHeight();
			double iminx = initialViewport.getMinX();
			double iminy = initialViewport.getMinY();
			double icx = iminx + iw * cx;
			double icy = iminy + ih * cy;
			double newminx = iminx + iw * (cx - w/2.0);
			double newminy = iminy + ih * (cy - w/2.0);
			viewport.setFrameFromCenter(icx, icy, newminx, newminy);
			JOGLConfiguration.theLog.log(Level.FINE,"old viewport is "+initialViewport.toString());
			JOGLConfiguration.theLog.log(Level.FINE,"new viewport is "+viewport.toString());
			theCamera.setOnAxis(false);
			theCamera.setViewPort(viewport);
			theViewer.render();
		}
		return true;
	}

	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Camera Zoom tool", "Change the field of view of the camera");
		overlay.registerInfoString("Mouse button1/2/3 dragged", "Zoom (change the field of view) proportional to y-component of mouse motion.");
	}


}
