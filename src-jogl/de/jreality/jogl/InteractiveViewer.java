/*
 * Created on Jun 16, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import de.jreality.jogl.tools.MotionManager;
import de.jreality.jogl.tools.MouseTool;
import de.jreality.jogl.tools.ToolManager;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InteractiveViewer extends de.jreality.jogl.Viewer implements  SelectionManager.Listener, ToolManager.Listener {
	protected SelectionManager selectionManager;
	protected ToolManager toolManager;
	protected MotionManager motionManager;
	protected MouseTool currentTool;
	HelpOverlay helpOverlay;

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
					//System.err.println("Button: "+e.getButton());
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

	/* (non-Javadoc)
	 * @see charlesgunn.gv2.SelectionManager.Listener#selectionChanged(charlesgunn.gv2.SelectionManager.Changed)
	 */
	SceneGraphComponent oldSel = null;
	public void selectionChanged(SelectionManager.Changed e) {
		SceneGraphComponent sel = selectionManager.representSelectionAsSceneGraph(this);
//		if (oldSel != null && sceneRoot.isDirectAncestor(oldSel)) sceneRoot.removeChild(oldSel);
//		sceneRoot.addChild(sel);
		//System.out.println("In selection changed");
		if (sel != oldSel)	{
			if (oldSel != null) removeAuxiliaryComponent(oldSel);
			addAuxiliaryComponent(sel);
			oldSel = sel;
			//System.out.println("Adding selection to viewer");			
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
	public void setCameraPath(SceneGraphPath pp) {
		SceneGraphPath p = pp;
		if (p == null || p.getElementAt(0) != sceneRoot || p.getLastComponent().getCamera() == null)	{
			System.err.println("Invalid camera path, adding new camera.");
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
			System.err.println("Invalid scene root, creating new root.");
			r = new SceneGraphComponent();
			r.setName("Default SceneRoot");
		}
		super.setSceneRoot(r);
	}

	public HelpOverlay getHelpOverlay() {
		return helpOverlay;
	}
}
