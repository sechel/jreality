/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.Camera;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class StereoCameraTool extends AbstractMouseTool {
	Camera cam;
	double 
		eyeSeparationOrig,
		focusOrig;
	/**
	 * 
	 */
	public StereoCameraTool() {
		super();
	}

	public boolean attachToViewer(InteractiveViewer v) {
		if (!super.attachToViewer(v)) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;
		double es = eyeSeparationOrig * Math.pow(5.0, diff[0]);
		cam.setEyeSeparation(es);
		
		double f = focusOrig * Math.pow(5.0, diff[1]);
		cam.setFocus(f);
		
		System.out.println("Sep: "+es+"Focus: "+f);
		return true;
	}
	/* (non-Javadoc)
	 * @see de.jreality.jogl.tools.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		// TODO Auto-generated method stub
		if (!super.startTrackingAt(e)) return false;
		cam = CameraUtility.getCamera(theViewer);
		eyeSeparationOrig = cam.getEyeSeparation();
		focusOrig = cam.getFocus();
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		if (!super.endTracking(e)) return false;
		
		return true;
	}
	
}
