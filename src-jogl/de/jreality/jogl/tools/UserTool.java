/*
 * Created on Nov 9, 2004
 *
  */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;
import de.jreality.scene.pick.PickPoint;

/**
 * @author gunn
 *
 */
public class UserTool extends PickTool {

	List listeners;
	double[] pointNDC = new double[3];
	
	public UserTool()	{
		super();
		listeners = new Vector();
	}
	public boolean attachToViewer(InteractiveViewer v) {
		return super.attachToViewer(v);
	}
	public boolean startTrackingAt(MouseEvent e) {
		if (!super.startTrackingAt(e)) return false;
		pointNDC = new double[] {current[0], current[1], 0.0, 1.0};
		//if (newPickPoint != null) newPickPoint.setPointNDC(pointNDC);
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.startTracking(this);
			}
		}
		return true;
	}
	
	public boolean track(MouseEvent e) {
		if (!super.track(e)) return false;
			
		pointNDC = new double[] {current[0], current[1], 0.0, 1.0};
		//JOGLConfiguration.theLog.log(Level.FINE,"Object coordinates: "+Rn.toString(newPickPoint.getPointObject()));
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.track(this);
			}
		}
		return true;
	}
	
	public boolean endTracking(MouseEvent e) {
		//if (!super.endTracking(e)) return false;
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.endTracking(this);
			}
		}
		return true;
	}
	
	public PickPoint getPickPoint()	{
		return newPickPoint;
	}
	
	public double[] getPointNDC()	{
		if (newPickPoint == null) return pointNDC;
		return newPickPoint.getPointNDC();
	}
	public int getButton() {
		return button;
	}
	
	public void addListener(UserToolInterface l)	{
		listeners.add(l);
	}
	
	public void removeListener(UserToolInterface l)	{
		listeners.remove(l);
	}
	
	public void registerHelp(HelpOverlay overlay) 	{	
		if (!listeners.isEmpty())	{
			for (int i = 0; i<listeners.size(); ++i)	{
				overlay.registerInfoString("User tool ", ""+i);
				UserToolInterface l = (UserToolInterface) listeners.get(i);
				l.registerHelp(overlay);
			}
		}
	}



}
