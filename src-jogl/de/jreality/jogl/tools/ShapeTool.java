/*
 * Created on Mar 23, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.util.math.FactoredMatrix;
import de.jreality.util.math.Rn;

/**
 * @author Charles Gunn
 *
 */
public class ShapeTool extends AbstractMouseTool {
	double[] 	origM; 		// original object transformation
	SceneGraphPath 	selection, alternateSelection;	// two selections: altSel is the "center"
			// of the motion; sel is the object moved
	Transformation theEditedTransform;	// the transform that gets edited
	 FactoredMatrix   myTransform;
	double[] 	theAxis;
	double[]	worldToCamera;			// get this from the camera in startTrackingAt()
	protected double[] anchorV = new double[4];
	protected double[] currentV = new double[4];
	int signature;
	protected PlaneProjector theProjector;
	protected double distance;


	/**
	 * 
	 */
	public ShapeTool() {
		super();
		duration = 400.0;			// make it quasi-infinite
		origM = new double[16];
		theAxis = new double[3];
		theProjector = new PlaneProjector();
	}


	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		Object	nodeContents;
		// the shape tools need a selection path and a shape to
		// act upon
		if (!super.startTrackingAt(e)) {isTracking = false; return false; }
		isTracking =false;
		selection = theViewer.getSelectionManager().getSelection();
		if (selection == null) selection = theViewer.getSelectionManager().getDefaultSelection();
		if (selection == null) return false;
		nodeContents = selection.getLastElement();
		if (nodeContents == null) return false;
		if (!(nodeContents instanceof SceneGraphComponent)) return false;
		theEditedNode = (SceneGraphComponent) nodeContents;
		theEditedTransform = ((SceneGraphComponent) theEditedNode).getTransformation();
		if (theEditedTransform == null || !theEditedTransform.getIsEditable()) return false;

		//theEditedTransform.setDoFactor(false);
		Rn.copy(origM,theEditedTransform.getMatrix());
		signature = theEditedTransform.getSignature();
		myTransform = new FactoredMatrix();
		worldToCamera = theViewer.getCameraPath().getInverseMatrix(worldToCamera);
		
		isTracking = true;
		theProjector.setCamera(theCamera);
		theProjector.setAnchor(anchor);
		double[] objectToWorld = selection.getMatrix(null);
		theProjector.setObjectToCamera(Rn.times(null, worldToCamera, objectToWorld));
		theProjector.setDefaultPlane();
		distance = theProjector.getDistanceToPlane();
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!isTracking || !super.track(e)) return false;		
		theProjector.getObjectPosition(anchor, anchorV);
		theProjector.getObjectPosition(current, currentV);
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		if (!super.endTracking(e)) return false;
		//if (theEditedTransform != null) theEditedTransform.setDoFactor(true);
		
		return true;
	}



}


