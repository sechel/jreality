/*
 * Created on Mar 22, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.event.MouseEvent;

import de.jreality.jogl.HelpOverlay;
import de.jreality.jogl.InteractiveViewer;


/**
 * @author Charles Gunn
 *
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
