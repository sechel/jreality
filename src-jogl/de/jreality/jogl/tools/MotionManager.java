/*
 * Created on Mar 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Timer;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MotionManager {
	List motions;			// list of Timer instances
	boolean allowMotion;
	boolean oneAtATime;
	Timer currentMotion;
	/**
	 * 
	 */
	public MotionManager() {
		super();
		motions = new Vector();
		allowMotion = true;
		oneAtATime = true;
		currentMotion = null;
	}

	/**
	 * @return
	 */
	public List getMotions() {
		return motions;
	}

	public void addMotion(Timer m)	{
		if (m == null || !(m instanceof Timer)) return;
		if (oneAtATime) {
			motions.clear();
			if (currentMotion != null && currentMotion.isRunning()) currentMotion.stop();
		}
		motions.add(m);
		if (allowMotion)	m.start();
		currentMotion = m;
	}
	
	public void removeMotion(Timer m)	{
		if (m.isRunning()) m.stop();
		motions.remove(m);
		if (currentMotion == m) currentMotion = null;
	}
	
	public void pauseMotions()	{
		stopMotions();
	}
	
	public void stopMotions()	{
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			Timer m = (Timer) it.next();
			if (m.isRunning()) m.stop();
		}	
	}
	
	public void resumeMotions()	{
		Iterator it = motions.iterator();
		while(it.hasNext())	{
			Timer m = (Timer) it.next();
			if (!m.isRunning()) m.restart();
		}
	}
}
