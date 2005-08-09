/*
 * Created on Jun 16, 2004
 *
 */
package de.jreality.jogl;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import de.jreality.jogl.tools.MotionManager;
import de.jreality.jogl.tools.MouseTool;
import de.jreality.jogl.tools.ToolManager;
import de.jreality.scene.*;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 */
public class InteractiveViewer extends de.jreality.jogl.Viewer implements  SelectionManager.SelectionListener, ToolManager.Listener, InfoOverlay.InfoProvider {
	protected SelectionManager selectionManager;
	protected ToolManager toolManager;
	protected MotionManager motionManager;
	protected MouseTool currentTool;
	HelpOverlay helpOverlay;
	InfoOverlay infoOverlay;

	/**
	 * 
	 */
	public InteractiveViewer() {
		this(null, null);
	}

	/**
	 * @param object
	 * @param root
	 * @param fullscreen
	 */
	public InteractiveViewer(SceneGraphPath p, SceneGraphComponent r) {
		super(p, r);

		helpOverlay = new HelpOverlay(this);
		infoOverlay = new InfoOverlay(this);
		infoOverlay.setPosition(InfoOverlay.LOWER_RIGHT);
		infoOverlay.setInfoProvider(this);

		Component vc = getViewingComponent();
		selectionManager = new SelectionManager();
		selectionManager.addSelectionListener(this);

		toolManager = new ToolManager(vc, this);
		toolManager.addToolListener(this);
		currentTool = toolManager.getCurrentTool();
		
		motionManager = new MotionManager();
		
		vc.addKeyListener(new ViewerKeyListener(this));

		vc.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e)	{
					JOGLConfiguration.getLogger().log(Level.FINER,"Button: "+e.getButton());
					currentTool.startTrackingAt(e);
				}
		        
				public void mouseReleased(MouseEvent e)	{
					currentTool.endTracking(e);
			}
		});
		
		vc.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseDragged(MouseEvent e)	{
					currentTool.track(e);	        	    
			}
		});
	
		
	}

	List infoStrings = new Vector();
	public void updateInfoStrings(InfoOverlay io)	{
		//JOGLConfiguration.theLog.log(Level.INFO,"Providing info strings");
		infoStrings.clear();
		infoStrings.add("Real FPS: "+getRenderer().getFramerate());
		infoStrings.add("Clock FPS: "+getRenderer().getClockrate());
		infoStrings.add(getRenderer().getMemoryUsage());
		io.setInfoStrings(infoStrings);
	}
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SelectionManager.Listener#selectionChanged(charlesgunn.gv2.SelectionManager.Changed)
	 */
	SceneGraphComponent oldSel = null;
	public void selectionChanged(SelectionManager.SelectionEvent e) {
		SceneGraphComponent sel = selectionManager.representSelectionAsSceneGraph(this);
//		if (oldSel != null && sceneRoot.isDirectAncestor(oldSel)) sceneRoot.removeChild(oldSel);
//		sceneRoot.addChild(sel);
		//JOGLConfiguration.theLog.log(Level.FINE,"In selection changed");
		if (sel != oldSel)	{
			if (oldSel != null) removeAuxiliaryComponent(oldSel);
			addAuxiliaryComponent(sel);
			oldSel = sel;
			//JOGLConfiguration.theLog.log(Level.FINE,"Adding selection to viewer");			
		}
		render(); 
	}
	
	/* (non-Javadoc)
	 * @see charlesgunn.gv2.ToolManager.Listener#toolChanged(charlesgunn.gv2.ToolManager.Changed)
	 */
	public void toolChanged(ToolManager.Changed e) {
		currentTool = ((ToolManager) e.getSource()).getCurrentTool();
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.InteractiveViewer#getMotionManager()
	 */
	public MotionManager getMotionManager() {
		return motionManager;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.InteractiveViewer#getSelectionManager()
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/* (non-Javadoc)
	 * @see de.jreality.scene.InteractiveViewer#getToolManager()
	 */
	public ToolManager getToolManager() {
		return toolManager;
	}

	public void toggleBackPlane()	{
		if (hasBackPlane)	removeBackPlane();
		else addBackPlane();
	}
	
	boolean hasBackPlane = false;
	SceneGraphComponent backPlaneComponent = null;
	public void addBackPlane()	{
		Color[] corners = { new Color(.5f,.5f,1f), new Color(.5f,.5f,.5f),new Color(1f,.5f,.5f),new Color(.5f,1f,.5f) };
		if (sceneRoot.getAppearance() == null)	sceneRoot.setAppearance(new Appearance());
		sceneRoot.getAppearance().setAttribute("backgroundColors", corners);
		hasBackPlane = true;
	}
	
	public void removeBackPlane()	{
		sceneRoot.getAppearance().setAttribute("backgroundColors", Appearance.INHERITED);
		hasBackPlane = false;
	}

	public void setBackgroundColor(java.awt.Color c)	{
		Appearance ap = sceneRoot.getAppearance();
		if (ap == null)	{
			ap = new Appearance();
			sceneRoot.setAppearance(ap);
		}
		ap.setAttribute(CommonAttributes.BACKGROUND_COLOR, c);
	}
	
	public MouseTool getCurrentTool() {
		return currentTool;
	}
	public SceneGraphComponent getScalerNode() {
		return scalerNode;
	}
	SceneGraphComponent scalerNode = null;
	public void setCameraPath(SceneGraphPath pp) {
		SceneGraphPath p = pp;
		if (pp == null || !CameraUtility.isCameraPathValid(pp) || pp.getFirstElement() != sceneRoot) {
			JOGLConfiguration.theLog.log(Level.INFO,"Invalid camera path, adding new camera.");
//			scalerNode = new SceneGraphComponent();
//			sceneRoot.addChild(scalerNode);
			Camera c = new Camera();
			SceneGraphComponent sgc = new SceneGraphComponent();
			sgc.setTransformation(new Transformation());
			sgc.setName("Default Camera node");
			sgc.setCamera(c);
			sceneRoot.addChild(sgc);
			p = SceneGraphPath.getFirstPathBetween(sceneRoot, c);
		}
		super.setCameraPath(p);
	}
	public void setSceneRoot(SceneGraphComponent r) {
		if (r == null)	{
			JOGLConfiguration.getLogger().log(Level.INFO,"Invalid scene root, creating new root.");
			r = new SceneGraphComponent();
			r.setAppearance(new Appearance());
			r.setName("Default SceneRoot");
		}
		super.setSceneRoot(r);
	}

	public HelpOverlay getHelpOverlay() {
		return helpOverlay;
	}

	/**
	 * @return
	 */
	public InfoOverlay getInfoOverlay() {
		// TODO Auto-generated method stub
		return infoOverlay;
	}
}
