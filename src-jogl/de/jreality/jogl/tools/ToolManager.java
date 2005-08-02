/*
 * Created on May 25, 2004
 *
 */
package de.jreality.jogl.tools;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.Timer;
import java.util.logging.Level;

import javax.swing.*;

import de.jreality.jogl.InteractiveViewer;
import de.jreality.jogl.JOGLConfiguration;


/**
 * @author Charles Gunn
 *
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
	
	final public static int SELECTION_TOOL = 1;
	final public static int ROTATION_TOOL = 10;
	final public static int STRETCH_TOOL = 11;
	final public static int TRANSLATION_TOOL = 12;
	final public static int CAMERA_ZOOM_TOOL = 20;
	final public static int CAMERA_FLY_TOOL = 21;
	final public static int CAMERA_STEREO_TOOL = 22;
	final public static int USER_TOOL = 30;
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

	public void activateTool(int which)	{
		MouseTool whichTool = null;
		switch(which)	{
		case SELECTION_TOOL:	whichTool = selTool; break;
		case ROTATION_TOOL:	whichTool = rotTool; break;
		case STRETCH_TOOL:	whichTool = scaleTool; break;
		case TRANSLATION_TOOL:	whichTool = transTool; break;
		case CAMERA_ZOOM_TOOL:	whichTool = zoomTool; break;
		case CAMERA_FLY_TOOL: 	whichTool = pointTool; break;
		case CAMERA_STEREO_TOOL:	whichTool = stereoTool; break;
		default:					whichTool = selTool; break;
		}
		activateTool(whichTool);
	}
	
	int userToolPosition = 1;
	JToolBar tb;
	ToolAction[] actions = null;
	public JToolBar getToolbar()	{
		if (tb == null) tb = new JToolBar();
		else tb.removeAll();
		if (actions == null) actions = new ToolAction[7];
		tb.add(actions[0] = new ToolAction("X",selTool,"Selection tool"));
		//tb.add(new ToolAction("U",userTool,"User tool"));
		// new user tools go here
		tb.addSeparator();
		tb.addSeparator();
		tb.add(actions[1] = new ToolAction("R",rotTool,"Rotate tool"));
		tb.add(actions[2] = new ToolAction("S",scaleTool,"Scale tool"));
		tb.add(actions[3] = new ToolAction("T",transTool,"Translate tool"));
		tb.addSeparator();
		tb.addSeparator();
		tb.add(actions[4] = new ToolAction("Z",zoomTool,"Camera zoom tool"));
		tb.add(actions[5] = new ToolAction("F",pointTool,"Camera fly tool"));
		tb.add(actions[6] = new ToolAction("2",stereoTool,"Stereo camera tool"));
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
			putValue(Action.SHORT_DESCRIPTION,tooltip);
		}
		public void actionPerformed(ActionEvent e)	{
			if (currentTool == tool) return;
			activateTool(tool);
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
	
	/**
	 * 
	 */
	private void activateTool(MouseTool tool) {
		if (currentTool != null) currentTool.detachFromViewer();
		currentTool = tool;
		currentTool.attachToViewer(viewer);
		broadcastChange();
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
	HashMap usertools = null; 
	public void addUserTool(final UserTool tool, final String symbol, final String name)	{
		// just adding this at start-up is causing problems allocating the GLCanvas (!!)
		// see TriangleGroupDemo
		final ToolAction ta = new ToolAction(symbol, tool, name);
		if (usertools == null) usertools = new HashMap();
		if (tb == null) getToolbar();
		TimerTask addToolTask = new TimerTask()	{
			public void run()	{
				JButton jb = tb.add(new ToolAction(symbol, tool, name));
				usertools.put(tool, jb);
			}
		};
		Timer doIt = new Timer();
		doIt.schedule(addToolTask, 10);
		
	}
	
	public void removeUserTool(final UserTool tool)	{
		if (usertools == null)	{
			JOGLConfiguration.theLog.log(Level.WARNING, "Removing usertool before any have been added");
			return;
		}
		Object obj = usertools.get(tool);
		if (obj != null && obj instanceof JButton)	{
			tb.remove(((JButton) obj));
			usertools.remove(tool);
		}
	}
	
	/**
	 * @return
	 */
	public MouseTool getCurrentTool() {
		return currentTool;
	}


}
