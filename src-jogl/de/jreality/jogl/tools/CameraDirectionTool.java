/*
 * Created on May 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import de.jreality.jogl.FramedCurve;
import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.P3;
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CameraDirectionTool extends AbstractMouseTool {
	SceneGraphComponent cameraNode;
	Transformation cameraTrans;
	CameraDirectionKeyListener cdkl;
	double camFactor = 0.5;
	double[] transM;
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
	public CameraDirectionTool() {
		super();
		transM = Rn.identityMatrix(4);
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
			System.out.println("No transform in CameraDirectionTool");
			return false;
		}
		theViewer.getViewingComponent().addKeyListener(cdkl);
		//System.out.println("Start tracking: Mouse event"+e.toString());
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
		Rn.setIdentityMatrix(transM);
		Rn.setIdentityMatrix(rotM);
		continuedMotion = new javax.swing.Timer(20, new ActionListener()	{
			final Transformation tt = cameraTrans;
			final double[] repeater1 = transM;
			final double[] repeater2 = rotM;
			public void actionPerformed(ActionEvent e) {updateRotation(); } 
			public void updateRotation()	{
				if (tt == null || theViewer == null) return;
				tt.multiplyOnRight(repeater1);
				tt.multiplyOnRight(repeater2);
				theViewer.render();
			}

		} );
		if (!(flyBackwards ^ !e.isShiftDown()))  {
			flyBackwards = !flyBackwards;
			setTranslation();
		}
		if (coMo &&  (button == 1 ||  button == 2 || (e.isShiftDown() && button == 3)) ) continuedMotion.start();
		return true;
	}

	static final double[] zaxis = {0,0,1};
	private double[] deltas;
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;
		//System.out.println("Mouse event"+e.toString());
		deltas = Rn.subtract(null, current, last);
		double s = Rn.euclideanNorm(deltas);
		//if (s > 10E-8) {
			double[] axis = new double[3];
			axis[0] = deltas[1];
			axis[1] = -deltas[0];
			axis[2] = 0.0;
			
			if (button == 1) {
				isZTrans = true;
				if (!(flyBackwards ^ !e.isShiftDown()))  {
					flyBackwards = !flyBackwards;
				}
				setTranslation();
				
				cameraTrans.multiplyOnRight(P3.makeRotationMatrix(null, axis, s * camFactor));
				Rn.setIdentityMatrix(rotM);
			}	
			else if (button == 2) {
				if (e.isShiftDown()) 
					synchronized(rotM)	{
						P3.makeRotationMatrix(rotM, axis, s * camFactor);
					}
				else	{
					isZTrans = false;
					setTranslation();
				}
			}
			else if (button == 3)		{		// right button: roll
				double[] zrot = P3.makeRotationMatrix(null, zaxis, -10 * deltas[1] * s );
				cameraNode.getTransformation().multiplyOnRight(zrot);
			} 
		//}
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
		//System.out.println("End tracking: Mouse event"+e.toString());
		//MotionManager mm = theViewer.getMotionManager();
		//if (mm!= null) mm.removeMotion(continuedMotion);
		//if (coMo) 
		continuedMotion.stop();
		return true;
		
	}

	private void setTranslation()	{
		if (isZTrans) { toVec[0] = toVec[1] = 0.0; toVec[2] = flyBackwards ? (speed*.01) : (-speed * .01);}
		else { toVec[0] = speed* deltas[0]; toVec[1] = speed* deltas[1]; toVec[2] = 0.0; }
		synchronized(transM)	{
			P3.makeTranslationMatrix(transM, toVec, signature);					
		}		
	}

	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON1_DOWN_MASK, 
		"Fly forward and turn in direction of mouse motion; (speed: up/down arrows)");
		overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON1_DOWN_MASK + MouseEvent.SHIFT_DOWN_MASK, 
		"Fly backwards ...");
		overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON2_DOWN_MASK, 
		"Fly in (x,y) plane (no forward velocity); (speed: up/down arrows)");
		//overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON2_DOWN_MASK + MouseEvent.SHIFT_DOWN_MASK, 
		//"Fly ");
		overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON3_DOWN_MASK, 
		"Resting roll (rotate in (x,y) plane) based on y-displacement");
		overlay.registerMouseEvent(MouseEvent.MOUSE_DRAGGED, MouseEvent.BUTTON3_DOWN_MASK, 
		"Moving roll (rotate in (x,y) plane) based on y-displacement");
	}
/**
		 * 
		 */
	private void toggleMotion() {
		//if (isTracking)	{
		if (!coMo)	{
			if (isTracking) continuedMotion.stop();
			System.out.println("Pointing mode");
		}
		else			{
			if (isTracking) continuedMotion.start();							
			System.out.println("Moving mode");
		}
	}
/**
	 * @author Charles Gunn
	 *
	 * To change the template for this generated type comment go to
	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
//			helpOverlay.registerKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_1,0), "Encompass");
//			if ((theViewer.getViewingComponent() instanceof GLCanvas))
//				((GLDrawable) theViewer.getViewingComponent()).addGLEventListener(helpOverlay);

		}
		double scaleFactor = 1.1;
		public void keyPressed(KeyEvent e)	{
				switch(e.getKeyCode())	{
					
					case KeyEvent.VK_DOWN:
						if (speed == 0.0) speed = .1;
						speed /= scaleFactor;
						setTranslation();
				        break;
						
					case KeyEvent.VK_UP:
						if (speed == 0.0) speed = .1;
						speed *= scaleFactor;
						setTranslation();
						break;
						
					case KeyEvent.VK_COMMA:
						coMo = !coMo;
						toggleMotion();
						//}
						break;
					
//					case KeyEvent.VK_PERIOD:
//						System.out.println("time is: "+e.getWhen());
//						System.out.println("Camera node is: "+Rn.matrixToString(cameraNode.getTransformation().getMatrix()));
//						if (cameraKeys == null) {
//							cameraKeys = new FramedCurve();
//							beginCurveTime = e.getWhen();
//						}
//						Transformation tt = new Transformation(signature);
//						tt.setMatrix(cameraNode.getTransformation().getMatrix());
//						double t = (e.getWhen() - beginCurveTime)/1000.0;
//						cameraKeys.addControlPoint(new FramedCurve.ControlPoint(tt,t));
//						break;
				}
				System.out.println("Speed is "+speed);
			}
		

	}
	
}

