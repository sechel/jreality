/*
 * Created on Mar 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.SelectionManager;
import de.jreality.scene.SceneGraphPath;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SelectionTool extends PickTool  {
	int			depth; 
	SelectionManager		selectionManager;
	SceneGraphPath	previousFullSelection, truncatedSelection, newSelection;
	boolean mouseMoved;
	/**
	 * 
	 */
	public SelectionTool() {
		super();
	}


/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#attachToViewer(charlesgunn.gv2.Viewer)
	 */
	public boolean attachToViewer(InteractiveViewer v) {
		// TODO Auto-generated method stub
		if (!super.attachToViewer(v)) return false;
		selectionManager = v.getSelectionManager();
		return true;
	}


	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#startTrackingAt(java.awt.event.MouseEvent)
	 */
	public boolean startTrackingAt(MouseEvent e) {
		newSelection = null;
		if (!super.startTrackingAt(e)) 	return false;
		newSelection = null;
		mouseMoved=false;		

		if (newPickPoint != null)	{
			newSelection = newPickPoint.getPickPath();
			selectionManager.setPickPoint(newPickPoint);
			theViewer.render();
		}
		return true;
	} 
		
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#track(java.awt.event.MouseEvent)
	 */
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;
	
		mouseMoved=true;
		if (newPickPoint != null)	{
			newSelection = newPickPoint.getPickPath();
			selectionManager.setPickPoint(newPickPoint);
			theViewer.render();			
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.tool.MouseTool#endTracking(java.awt.event.MouseEvent)
	 */
	public boolean endTracking(MouseEvent e) {
		if (!super.endTracking(e)) return false;
		if (newSelection == null)		{		// nothing under the cursor
			previousFullSelection = null;
			truncatedSelection = null;
			selectionManager.setPickPoint(null);
			selectionManager.setSelection(null);
			//System.err.println("1");
			return true;
		} // else {
		// the selection tool does nothing if the user moves the mouse.
		// He/she has to hold and click to have an effect.
		System.out.println("Pick point: "+newPickPoint.toString());
		if (mouseMoved) return false;
		// perform the pick
		track(e);

		// cycle through the path as long as the full selection path remains the same
		if (previousFullSelection != null && truncatedSelection != null && truncatedSelection.getLength() > 1  && previousFullSelection.isEqual(newSelection) ) {
			truncatedSelection.pop();
			selectionManager.setPickPoint(newPickPoint);
			selectionManager.setSelection(truncatedSelection);
			//System.err.println("3");
			return true;	
		} // else {    // renew selection
		// To be here means we have selected a different full path than the previous one
		previousFullSelection = newSelection;
		// notify the selection manager
		selectionManager.setPickPoint(newPickPoint);
		selectionManager.setSelection(newSelection);
		newPickPoint = null;
		newSelection = null;
		truncatedSelection = (SceneGraphPath) previousFullSelection.clone();
		theViewer.render();
		return true;
	}

}
