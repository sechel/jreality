/*
 * Created on Mar 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class AbstractMouseTool implements MouseTool {
	public void registerHelp(HelpOverlay overlay) {
		System.out.println("The "+this.getClass().getName()+" toolclass should override this abstract method");
	}
	
	int button = 1;
		InteractiveViewer theViewer;		// view to which this tool is attached
		Camera	theCamera;
		Component theCanvas;
		SceneGraphNode theEditedNode; // depends on the tool
		double 	scale,		// general scaling amount for action
			motionCutoff,		// motion smaller than this? terminate
			strength,
			duration ;		// how strong an effect?
		double[]	anchor, current, last, diff; // various mouse positions
		long	currentTime, lastTime;
		boolean isTracking = false,
			keepsMoving = true;
		Timer continuedMotion;

	/**
	 * 
	 */
	public AbstractMouseTool() {
		super();
		motionCutoff = .01;
		duration = 4.0;
		anchor = new double[2];
		current = new double[2];
		last = new double[2];
		diff = new double[2];
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#attachToViewer(charlesgunn.gv2.Viewer)
	 */
	public boolean attachToViewer(InteractiveViewer v) {
		if (v == null) return false;
		theViewer = v;
		theCamera = theViewer.getCameraPath().getLastComponent().getCamera();
		theCanvas = theViewer.getViewingComponent();
		if (theCamera == null || theCanvas == null) return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#detachFromViewer()
	 */
	public boolean detachFromViewer() {
		if (continuedMotion!= null) theViewer.getMotionManager().removeMotion(continuedMotion);
		theViewer = null;
		theCamera = null;
		theCanvas = null;
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#getViewer()
	 */
	public InteractiveViewer getViewer() {
		return theViewer;
	}

	private void getButton(MouseEvent e)	{
		button = e.getButton();
		isTracking = true;
		if (button == 0)	{		// Linux!
			int mods = e.getModifiersEx();
			//System.out.println("Mods are: "+mods);
			if ((mods & MouseEvent.BUTTON1_DOWN_MASK) != 0)		button = 1;
			else if ((mods & MouseEvent.BUTTON2_DOWN_MASK) != 0)  button = 2;
			else button = 3;
		} else {					// Mac OS X Laptop (no 3-mouse button)!!
			int mods = e.getModifiers();
			if (e.isAltDown() && ((mods & MouseEvent.BUTTON2_MASK) != 0) ) button = 2;
			else if (button == 1 &&  ((mods & MouseEvent.BUTTON3_MASK) != 0) ) button = 3;
		}
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		// assert good pointers
		if (theViewer == null || e == null) return false;
		getButton(e);
		MotionManager mm = theViewer.getMotionManager();
		mm.pauseMotions();
		convertScreenToNDC(anchor, e.getX(), e.getY(), theCanvas.getWidth(), theCanvas.getHeight());
		Rn.copy(current, anchor);
		Rn.subtract(diff, current, anchor);
		currentTime = e.getWhen();
		//System.out.println("Mouse event: "+e.toString());
		//System.out.println("Button: "+e.getButton());
		// want to have a clean slate in case there are no motion events
		Rn.copy(last, anchor);
		lastTime = currentTime;
		theViewer.render();
		return true;
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (theViewer == null || e == null) return false;
		getButton(e);
		//System.out.println("Mouse event: "+e.toString());
		lastTime = currentTime;
		currentTime = e.getWhen();
		Rn.copy(last, current);
		current = convertScreenToNDC(e.getX(), e.getY(), theCanvas.getWidth(), theCanvas.getHeight());
		Rn.subtract(diff, current, anchor);
		strength = Rn.euclideanNorm(diff);
		theViewer.render();
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		isTracking = false;
		theViewer.render();
		if (theViewer == null || e == null) return false;
		getButton(e);
		if ( (e.getWhen() - currentTime) > 3 * ( currentTime - lastTime ))	strength = 0;
		else 																strength = Rn.euclideanNorm(diff);
		return true;
	}
	
	protected static double[] convertScreenToNDC(int x, int y, int w, int h)	{
		return convertScreenToNDC(null, x, y, w, h);
	}
	
	protected static double[] convertScreenToNDC(double[] ndc, int x, int y, int w, int h)	{
		double[] NDC;
		if (ndc == null)	NDC = new double[2];
		else NDC = ndc;
		double tx = w/2.0;
		double ty = h/2.0;
		NDC[0] = (x-tx)/tx;
		NDC[1] = -(y-ty)/ty;
		return NDC;
	}

		/**
		 * @return
		 */
		public boolean isKeepsMoving() {
			return keepsMoving;
		}

		/**
		 * @param b
		 */
		public void setKeepsMoving(boolean b) {
			keepsMoving = b;
		}

}
