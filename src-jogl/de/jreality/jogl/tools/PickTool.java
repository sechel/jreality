/*
 * Created on Nov 9, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;
import java.util.List;

import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.pick.PickPoint;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PickTool extends AbstractMouseTool {

	protected PickPoint newPickPoint;
	protected JOGLPickAction pickAction;
	PickPoint[] hits;
	static boolean debug = true;


	public boolean attachToViewer(InteractiveViewer v) {
		if (!super.attachToViewer(v)) return false;
		pickAction = new JOGLPickAction(theViewer);
		return true;
	}
	
	public boolean endTracking(MouseEvent e) {
		return super.endTracking(e);
	}
	
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;;

		newPickPoint = null;
		pickAction.setPickPoint(current);
		List picks = (List) pickAction.visit();			
		if (picks!= null && (picks.size() > 0))	{
			newPickPoint = (PickPoint) picks.get(0);
			return true;
		} 
		return false;
		
	}
		
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;

		newPickPoint = null;
		pickAction.setPickPoint(current);
		List picks = (List) pickAction.visit();			
		if (picks!= null && (picks.size() > 0))	{
			newPickPoint = (PickPoint) picks.get(0);
			return true;
		} else return false;

	}
}
