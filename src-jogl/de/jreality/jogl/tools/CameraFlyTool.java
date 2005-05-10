/*
 * Created on May 27, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.*;
import java.util.logging.Level;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.P3;
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 */
public class CameraFlyTool extends AbstractMouseTool {
	SceneGraphComponent cameraNode;
	Transformation cameraTrans;
	CameraDirectionKeyListener cdkl;
	double camFactor = 0.5;
	double[] transM;
	double[] xyTransM;
	double[] origCM;
	double[] rotM;
	int signature;
	double[] toVec = {0.0, 0.0, 0.0, 1d};
	double speed = 0.1;
	int pathNumber = 0;
	boolean isZTrans = true;
	boolean flyBackwards = false;
	boolean coMo = true;
	/**
	 * 
	 */
	public CameraFlyTool() {
		super();
		transM = Rn.identityMatrix(4);
		xyTransM = Rn.identityMatrix(4);
		origCM = Rn.identityMatrix(4);
		rotM = Rn.identityMatrix(4);
		cdkl = new CameraDirectionKeyListener();
	}

	public boolean attachToViewer(InteractiveViewer v) {
		if (!super.attachToViewer(v)) return false;
		cameraNode = CameraUtility.getCameraNode(v);
		cameraTrans = cameraNode.getTransformation();
		Camera cam = CameraUtility.getCamera(v);
		signature = cam.getSignature();
		if ( cameraTrans == null)	{
			JOGLConfiguration.theLog.log(Level.FINE,"No transform in CameraDirectionTool");
			return false;
		}
		theViewer.getViewingComponent().addKeyListener(cdkl);
		//JOGLConfiguration.theLog.log(Level.FINE,"Start tracking: Mouse event"+e.toString());
		return true;
	}

	public boolean detachFromViewer() {
		theViewer.getViewingComponent().removeKeyListener(cdkl);
		return super.detachFromViewer();
	}
	
	/* (non-Javadoc)
	 * @see de.jreality.jogl.tools.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;
		//MotionManager mm = theViewer.getMotionManager();
		//if (mm!= null) mm.addMotion(continuedMotion);
		cameraNode = CameraUtility.getCameraNode(theViewer);
		cameraTrans = cameraNode.getTransformation();
		Camera cam = CameraUtility.getCamera(theViewer);
		signature = cam.getSignature();
		cameraTrans.getMatrix(origCM);
		Rn.setIdentityMatrix(transM);
		Rn.setIdentityMatrix(rotM);
		if (continuedMotion != null)	{
			continuedMotion.stop();
		}
		continuedMotion = new javax.swing.Timer(20, new ActionListener()	{
			final Transformation tt = cameraTrans;
			final double[] repeater1 = transM;
			final double[] repeater2 = rotM;
			public void actionPerformed(ActionEvent e) {updateRotation(); } 
			public void updateRotation()	{
				if (tt == null || theViewer == null) return;
				tt.multiplyOnRight(repeater1);
				//tt.multiplyOnRight(repeater2);				
				theViewer.render();
			}

		} );
		if (coMo &&  (button == 1) ) continuedMotion.start();
		return true;
	}

	static final double[] zaxis = {0,0,1};
	private double[] deltas;
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;
		//JOGLConfiguration.theLog.log(Level.FINE,"Mouse event"+e.toString());
		
		deltas = Rn.subtract(null, current, last);
		double s = Rn.euclideanNorm(deltas);
		double[] axis = new double[3];
		axis[0] = deltas[1];
		axis[1] = -deltas[0];
		axis[2] = 0.0;
		if (button == 1) {
			isZTrans = true;
			setTranslation();
			cameraTrans.multiplyOnRight(P3.makeRotationMatrix(null, axis, s * camFactor));
			Rn.setIdentityMatrix(rotM);
		}	
		else if (button == 2) {
			double[] absChange = new double[4];
			absChange[0] = current[0] - anchor[0];
			absChange[1] = current[1] - anchor[1];
			double[] tvec = Rn.times(null, speed, absChange);
			tvec[3] = 1.0;
			P3.makeTranslationMatrix(xyTransM, tvec, signature);
			cameraTrans.setMatrix(Rn.times(null, origCM, xyTransM));
			
		}
		else if (button == 3)		{		// right button: roll
			double[] zrot = P3.makeRotationMatrix(null, zaxis, -10 * deltas[1] * s );
			cameraNode.getTransformation().multiplyOnRight(zrot);
		} 
		// always supply the translation
		//cameraTrans.multiplyOnRight(transM);	
		theViewer.render();
		return true;
	}

	/* (non-Javadoc)
	 * @see de.jreality.jogl.tools.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		super.endTracking(e);
		continuedMotion.stop();
		return true;
		
	}

	private void setTranslation()	{
		toVec[0] = toVec[1] = 0.0; toVec[2] = flyBackwards ? (speed*.01) : (-speed * .01);
		synchronized(transM)	{
			P3.makeTranslationMatrix(transM, toVec, signature);					
		}		
	}

	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Mouse button1 dragged", 
		"Fly forward/backward and turn in direction of mouse motion.");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Fly in (x,y) plane (no forward velocity).");
		//overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON2_DOWN_MASK + MouseEvent.SHIFT_DOWN_MASK, 
		//"Fly ");
		overlay.registerInfoString("Mouse button3 dragged", 
		"Roll (rotate in (x,y) plane) based on mouse y-displacement");
		overlay.registerInfoString("Left arrow", "Fly backwards");
		overlay.registerInfoString("Right arrow", "Fly forwards");
		overlay.registerInfoString("Up/down arrows", "Increase/decrease speed");
	}
/**
		 * 
		 */
	private void toggleMotion() {
		//if (isTracking)	{
		if (!coMo)	{
			if (isTracking) continuedMotion.stop();
			JOGLConfiguration.theLog.log(Level.INFO,"Pointing mode");
		}
		else			{
			if (isTracking) continuedMotion.start();							
			JOGLConfiguration.theLog.log(Level.INFO,"Moving mode");
		}
	}
/**
	 * @author Charles Gunn
	 *
	 */
	public class CameraDirectionKeyListener extends KeyAdapter {
		private boolean showHelp = false;
		
		HelpOverlay helpOverlay;	
		int value = 0;
		long beginCurveTime = 0;
		/**
		 * 
		 */
		public CameraDirectionKeyListener() {
			super();
			
			helpOverlay = new HelpOverlay(theViewer);

		}
		double scaleFactor = 1.1;
		public void keyPressed(KeyEvent e)	{
				switch(e.getKeyCode())	{
					
					case KeyEvent.VK_DOWN:
						if (speed == 0.0) speed = .1;
						speed /= scaleFactor;
						setTranslation();
						JOGLConfiguration.theLog.log(Level.FINE,"Speed is "+speed);
				        break;
						
					case KeyEvent.VK_UP:
						if (speed == 0.0) speed = .1;
						speed *= scaleFactor;
						setTranslation();
						JOGLConfiguration.theLog.log(Level.FINE,"Speed is "+speed);
						break;
						
					case KeyEvent.VK_LEFT:
						flyBackwards = true;
						setTranslation();
						break;
						
					case KeyEvent.VK_RIGHT:
						flyBackwards = false;
						setTranslation();
						break;
						
					case KeyEvent.VK_K:
						continuedMotion.stop();
						break;
						
					case KeyEvent.VK_COMMA:
						coMo = !coMo;
						toggleMotion();
						break;
					
				}
			}
		

	}
	
}

