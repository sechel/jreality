/*
 * Created on May 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JToolBar;

import de.jreality.jogl.InteractiveViewer;


/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ToolManager {
	Component comp;
	InteractiveViewer viewer;
	protected MouseTool currentTool;
	protected SelectionTool selTool;
	protected UserTool userTool;
	protected PointManipulationTool pmTool;
	protected CameraZoomTool zoomTool;
	protected CameraFlyTool pointTool;
//	protected OldCameraFlyTool flyTool;
	protected RotateShapeTool rotTool;
	protected ScaleShapeTool scaleTool;
	protected TranslateShapeTool transTool;
	protected StereoCameraTool stereoTool;
	/**
	 * 
	 */
	public ToolManager(Component c, InteractiveViewer v) {
		super();
		comp = c;
		viewer = v;
		selTool = new SelectionTool();
		userTool = new UserTool();
		pmTool = new PointManipulationTool();
		rotTool = new RotateShapeTool();
		scaleTool = new ScaleShapeTool();
		transTool = new TranslateShapeTool();
		zoomTool = new CameraZoomTool();
		pointTool = new CameraFlyTool();
//		flyTool = new OldCameraFlyTool();
		stereoTool = new StereoCameraTool();
		currentTool = rotTool;
		currentTool.attachToViewer(viewer);
	}

	int userToolPosition = 1;
	JToolBar tb;
	public JToolBar getToolbar()	{
		if (tb == null) tb = new JToolBar();
		else tb.removeAll();
		tb.add(new ToolAction("X",selTool,"Selection tool"));
		//tb.add(new ToolAction("U",userTool,"User tool"));
		// new user tools go here
		tb.addSeparator();
		tb.addSeparator();
		tb.add(new ToolAction("R",rotTool,"Rotate tool"));
		tb.add(new ToolAction("S",scaleTool,"Scale tool"));
		tb.add(new ToolAction("T",transTool,"Translate tool"));
		tb.addSeparator();
		tb.addSeparator();
		tb.add(new ToolAction("Z",zoomTool,"Camera zoom tool"));
		tb.add(new ToolAction("F",pointTool,"Camera fly tool"));
		tb.add(new ToolAction("2",stereoTool,"Stereo camera tool"));
		//tb.add(new ToolAction("F",flyTool,"Camera fly tool"));
		tb.addSeparator();
		tb.addSeparator();
		return tb;
	}
	
	class ToolAction extends AbstractAction {
		MouseTool tool;
		public ToolAction(String name, MouseTool t, String tooltip)	{
			super(name);
			tool = t;
			putValue(AbstractAction.SHORT_DESCRIPTION,tooltip);
		}
		public void actionPerformed(ActionEvent e)	{
			if (currentTool == tool) return;
			if (currentTool != null) currentTool.detachFromViewer();
			currentTool = tool;
			currentTool.attachToViewer(viewer);
			broadcastChange();
		}
	}
	public static class Changed extends java.util.EventObject	{

		/**
		 * @param source
		 */
		public Changed(Object source) {
			super(source);
		}
	}
	
	Vector listeners;
	
	public interface Listener extends java.util.EventListener	{
		public void toolChanged(ToolManager.Changed e);
	}

	public void addToolListener(ToolManager.Listener l)	{
		if (listeners == null)	listeners = new Vector();
		if (listeners.contains(l)) return;
		listeners.add(l);
		//System.err.println("ToolManager: Adding geometry listener"+l+"to this:"+this);
	}
	
	public void removeToolListener(ToolManager.Listener l)	{
		if (listeners == null)	return;
		listeners.remove(l);
	}

	public void broadcastChange()	{
		if (listeners == null) return;
		//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
		if (!listeners.isEmpty())	{
			ToolManager.Changed e = new ToolManager.Changed(this);
			//System.err.println("ToolManager: broadcasting"+listeners.size()+" listeners");
			for (int i = 0; i<listeners.size(); ++i)	{
				ToolManager.Listener l = (ToolManager.Listener) listeners.get(i);
				l.toolChanged(e);
			}
		}
	}
	
	public void addUserTool(final UserTool tool, final String symbol, final String name)	{
		// just adding this at start-up is causing problems allocating the GLCanvas (!!)
		// see TriangleGroupDemo
		TimerTask addToolTask = new TimerTask()	{
			public void run()	{
				tb.add(new ToolAction(symbol, tool, name));
			}
		};
		Timer doIt = new Timer();
		doIt.schedule(addToolTask, 10);
		
	}
	
	/**
	 * @return
	 */
	public MouseTool getCurrentTool() {
		return currentTool;
	}

}
