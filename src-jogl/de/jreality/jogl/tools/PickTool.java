/*
 * Created on Nov 9, 2004
 *
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
	
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;

		newPickPoint = null;
		pickAction.setPickPoint(current[0], current[1]);
		List picks = (List) pickAction.visit();			
		if (picks!= null && (picks.size() > 0))	{
			newPickPoint = (PickPoint) picks.get(0);
		} 
		return true;
		
	}
		
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;

		newPickPoint = null;
		pickAction.setPickPoint(current[0], current[1]);
		//long begin = System.currentTimeMillis();
		List picks = (List) pickAction.visit();			
		//long end = System.currentTimeMillis();
		//JOGLConfiguration.theLog.log(Level.FINE,"Pick took "+(end-begin)+" ms");
		if (picks!= null && (picks.size() > 0))	{
			newPickPoint = (PickPoint) picks.get(0);
		} 
		return true;

	}
	public boolean endTracking(MouseEvent e) {
		return super.endTracking(e);
	}
	
}
