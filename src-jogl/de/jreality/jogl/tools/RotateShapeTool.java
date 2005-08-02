/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.HelpOverlay;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.math.Pn;
import de.jreality.util.math.Quaternion;
import de.jreality.util.math.Rn;

/**
 * @author Charles Gunn
 *
 */
public class RotateShapeTool extends AbstractShapeTool {
	Rotator	theRotator;
	double	theAngle;

	/**
	 * 
	 */
	public RotateShapeTool() {
		super();
		theRotator = new Rotator();
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;

		theRotator.setCamera(CameraUtility.getCamera(theViewer));
		theRotator.setAnchor(anchor);
		double[] objectToWorld = selection.getMatrix(null);
		theRotator.setObjectToCamera(Rn.times(null, worldToCamera, objectToWorld));
		isTracking = true;
		// TODO figure out a better strategy for setting the center of the rotation:
		// current version makes an effort to determine if it's sensible or not
		// but you want to have finer control over this.  E.g., you may want to use
		// the current pick point as the center, etc.
		if (button == 1 && theEditedTransform.getSignature() != Pn.ELLIPTIC)	{
			Rectangle3D bbox = GeometryUtility.calculateChildrenBoundingBox((SceneGraphComponent) theEditedNode);
			myTransform.setCenter(bbox.getCenter()); 
		}  else myTransform.setCenter(theEditedTransform.getCenter());
		return true;
	}
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!isTracking || !super.track(e)) return false;
		
		Quaternion q = null;
		//JOGLConfiguration.theLog.log(Level.FINE,"Mouse is "+e.toString());
		if (button != 3) q = theRotator.getRotationXY(current);
		else q = theRotator.getRotationZ(current);
		myTransform.setRotation(q);
		double[]composite = Rn.times(null, origM, myTransform.getMatrix());
		theEditedTransform.setMatrix(composite);
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		if (!super.endTracking(e)) return false;
		
		isTracking = false;
		if (theEditedTransform == null) return false;
		if (!keepsMoving) return true;
		long dt = currentTime - lastTime;
		if (dt == 0 || strength == 0.0) return true;

		Quaternion q = null;
		if (button != 3) q = theRotator.getRotationXY(last, current);
		else //if (e.getButton() == MouseEvent.BUTTON3) 
				q = theRotator.getRotationZ(last, current);
		theAngle = 2* Math.acos(q.re);
		if (theAngle > Math.PI) theAngle -= Math.PI * 2;
		Quaternion.IJK(theAxis, q);
		Rn.normalize(theAxis, theAxis);
		double angle = .3* strength * theAngle * (1000.0 /(currentTime-lastTime));
		//JOGLConfiguration.theLog.log(Level.FINE,"dt: "+dt+"Strength: "+strength+"angle: "+theAngle);
		double[] axis = (double[] ) theAxis.clone();
		//final double[] repeater = P3.makeRotationMatrix(null, axis, angle);
		final Transformation repeater = new Transformation();
		repeater.setCenter(myTransform.getCenter());
		repeater.setRotation(angle, axis);
		continuedMotion = new javax.swing.Timer(20, new ActionListener()	{
			final Transformation tt = theEditedTransform;
			final Transformation mt = myTransform;
			//final double[] repeater = mat;
			final double[] OM = origM; //theEditedTransform.getMatrix();
			final double[] acc = Rn.identityMatrix(4);
			public void actionPerformed(ActionEvent e) {updateRotation(); } 
			public void updateRotation()	{
				if (tt == null) return;
				//tt.multiplyOnRight(repeater);
				//Rn.times(acc, acc, repeater);
				mt.multiplyOnRight(repeater);
				tt.setMatrix(Rn.times(null, OM,mt.getMatrix()));
				//theEditedTransform.setRotation(tt.getRotationQuaternion());

				theViewer.render();
			}

		} );
		MotionManager mm = theViewer.getMotionManager();
		if (mm!= null) mm.addMotion(continuedMotion);
		return true;
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		overlay.registerInfoString("Rotate tool", 
		"Rotate currently selected scene graph component");
		overlay.registerInfoString("","through an angle proportional to distance of mouse movement.");
		overlay.registerInfoString("Mouse button1 dragged", 
		"Rotate around axis perpendicular to mouse motion passing through center of bounding box in object space.");
		overlay.registerInfoString("Mouse button2 dragged", 
		"Rotate around axis perpendicular to mouse motion passing through origin of object space.");
		overlay.registerInfoString("Mouse button3 dragged", 
		"Rotate around axis perpendicular to plane of screen ");
	}

}
