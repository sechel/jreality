/*
 * Created on Apr 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CameraZoomTool extends AbstractMouseTool {
	double initialFOV;
	double scale = 1.5;
	/**
	 * 
	 */
	public CameraZoomTool() {
		super();
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		// TODO Auto-generated method stub
		return super.endTracking(e);
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		super.startTrackingAt(e);
		initialFOV = theCamera.getFieldOfView();
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		super.track(e);
		double fov = Math.exp(-scale * diff[1]) * initialFOV;
		theCamera.setFieldOfView(fov > 179.0 ? 179.0 : fov);
		return true;
	}

}
