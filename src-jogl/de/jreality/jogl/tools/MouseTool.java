/*
 * Created on Mar 22, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;


/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface MouseTool {
	public boolean attachToViewer(InteractiveViewer v);
	public boolean detachFromViewer();
	public InteractiveViewer getViewer();
	public boolean startTrackingAt(MouseEvent e);
	public boolean track(MouseEvent e);
	public boolean endTracking(MouseEvent e);
	public void registerHelp(HelpOverlay overlay);
}
