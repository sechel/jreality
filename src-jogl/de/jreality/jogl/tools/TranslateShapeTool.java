/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.logging.Level;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.math.*;
import de.jreality.scene.Transformation;

/**
 * @author Charles Gunn
 *
 */
public class TranslateShapeTool extends AbstractShapeTool {
	double[] zDirectionObject;
	/**
	 * 
	 */
	public TranslateShapeTool() {
		super();
		zDirectionObject = new double[4];
	}

	private double[] zDir = {0,0,1,0};
	
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;
		Rn.matrixTimesVector(zDirectionObject,theProjector.getCameraToObject(), zDir );
		return true;
	}
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!isTracking || !super.track(e)) return false;
		
		final double[] mat ;
		if (button == 1)	{
			mat = P3.makeTranslationMatrix(null, anchorV, currentV, theEditedTransform.getSignature());
		} else {
			double[] oV = new double[4];
			double[] zV = new double[4];
			double d= Pn.distanceBetween(anchorV, currentV, theEditedTransform.getSignature());
			if (current[1] < anchor[1]) d = -d;
			double ed = theProjector.getDistanceToPlane();
			theProjector.getObjectPosition(anchor, oV);
			theProjector.setDistanceToPlane(ed+d);
			theProjector.getObjectPosition(anchor, zV);
			theProjector.setDistanceToPlane(ed);
			mat = P3.makeTranslationMatrix(null, oV, zV, theEditedTransform.getSignature());
		}
		myTransform = new FactoredMatrix(mat);		
		double[] comp = Rn.times(null, origM, myTransform.getArray());
		theEditedTransform.setMatrix(comp);
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		if (!super.endTracking(e)) return false;
		
		isTracking = false;
		if (theEditedTransform == null) return false;
		//JOGLConfiguration.theLog.log(Level.FINE,"Translation is: "+Rn.toString(myTransform.getTranslation()));
		if (!keepsMoving) return true;
		long dt = currentTime - lastTime;
		if (dt == 0 || strength == 0.0) return true;
		if (button != 1)		{
			JOGLConfiguration.theLog.log(Level.FINE,"Continued motion not yet implemented here");
			return true;
		}
		theProjector.getObjectPosition(last, anchorV);
		theProjector.getObjectPosition(current, currentV);
		double size = .03* strength * (1000.0 /(currentTime-lastTime));
		//JOGLConfiguration.theLog.log(Level.FINE,"dt: "+dt+"Strength: "+strength+"size: "+size);
		Pn.linearInterpolation(currentV, anchorV, currentV, size, theEditedTransform.getSignature());
		final double[] mat = P3.makeTranslationMatrix(null, anchorV, currentV,theEditedTransform.getSignature());
		//SJOGLConfiguration.theLog.log(Level.FINE,"Translation: "+Rn.matrixToString(mat));
		continuedMotion = new javax.swing.Timer(20, new ActionListener()	{
			final Transformation tt = theEditedTransform;
			final double[] repeater = mat;
			public void actionPerformed(ActionEvent e) {updateRotation(); } 
			public void updateRotation()	{
				if (tt == null) return;
				tt.multiplyOnRight(repeater);
				if (theViewer != null) theViewer.render();
			}

		} );
		MotionManager mm = theViewer.getMotionManager();
		if (mm!= null) mm.addMotion(continuedMotion);
		return true;
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Translate tool", 
		"Translate currently selected scene graph component");
		overlay.registerInfoString("","a distance proportional to distance of mouse movement.");
		overlay.registerInfoString("Mouse button1 dragged", 
		"In direction of mouse motion");
		overlay.registerInfoString("Mouse button2/3 dragged", 
		"In direction perpendicular to plane of screen ");
	}
}
