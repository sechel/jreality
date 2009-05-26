package de.jreality.plugin.content;

import java.awt.event.ActionEvent;

import de.jreality.plugin.PluginUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AxisTranslationTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.EncompassTool;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.actions.AbstractJrToggleAction;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ContentTools extends Plugin {

	private static final boolean DEFAULT_PICK_FACES = true;
	private static final boolean DEFAULT_PICK_EDGES = true;
	private static final boolean DEFAULT_PICK_VERTICES = true;

	private RotateTool rotateTool;
	private DraggingTool draggingTool;
	private AxisTranslationTool snapDragTool;
	private EncompassTool encompassTool;
	
	private AbstractJrToggleAction 
		rotate,
		drag,
		snapToGrid,
		pickFaces,
		pickEdges,
		pickVertices,
		encompass;
	
	private Scene scene = null;
	private Content content = null;
	
	@SuppressWarnings("serial")
	public ContentTools() {
		
		rotate = new AbstractJrToggleAction("rotate") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setRotationEnabled(isSelected());
			}
		};
		rotate.setIcon(ImageHook.getIcon("arrow_rotate_clockwise.png"));
		
		drag = new AbstractJrToggleAction("drag") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDragEnabled(isSelected());
			}	
		};
		drag.setIcon(ImageHook.getIcon("arrow_inout.png"));
		
		snapToGrid = new AbstractJrToggleAction("snap to grid") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSnapToGrid(isSelected());
			}	
		};
		snapToGrid.setIcon(ImageHook.getIcon("brick.png"));
		
		pickFaces = new AbstractJrToggleAction("pick faces") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPickFaces(isSelected());
			}
		};
		pickFaces.setIcon(ImageHook.getIcon("shape_square.png"));
		
		pickEdges = new AbstractJrToggleAction("pick edges") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPickEdges(isSelected());
			}
		};
		pickEdges.setIcon(ImageHook.getIcon("shape_edges.png"));
		
		pickVertices = new AbstractJrToggleAction("pick vertices") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setPickVertices(isSelected());
			}
		};
		pickVertices.setIcon(ImageHook.getIcon("shape_handles.png"));
		
		encompass = new AbstractJrToggleAction("encompass") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEncompassEnabled(isSelected());
			}
		};
		encompass.setIcon(ImageHook.getIcon("arrow_out.png"));
		
		encompassTool = new EncompassTool();
		
		rotateTool = new RotateTool();
		rotateTool.setFixOrigin(false);
		rotateTool.setMoveChildren(false);
		rotateTool.setUpdateCenter(false);
		rotateTool.setAnimTimeMin(250.0);
		rotateTool.setAnimTimeMax(750.0);

		draggingTool = new DraggingTool();
		draggingTool.setMoveChildren(false);

		snapDragTool = new AxisTranslationTool();

		setRotationEnabled(true);
		setDragEnabled(true);
		setPickEdges(DEFAULT_PICK_EDGES);
		setPickFaces(DEFAULT_PICK_FACES);
		setPickVertices(DEFAULT_PICK_VERTICES);

//		setInitialPosition(SHRINKER_RIGHT);
	}

	public void install() {
		setRotationEnabled(isRotationEnabled());
		setDragEnabled(isDragEnabled());
		setPickEdges(isPickEdges());
		setPickFaces(isPickFaces());
		setPickVertices(isPickVertices());
	}

	public void setSnapToGrid(boolean b) {
		snapToGrid.setSelected(b);
		setToolEnabled(draggingTool, drag.isSelected() && !snapToGrid.isSelected());
		setToolEnabled(snapDragTool, drag.isSelected() && snapToGrid.isSelected());
	}

	public boolean isSnapToGrid() {
		return snapToGrid.isSelected();
	}

	public void setPickVertices(boolean b) {
		pickVertices.setSelected(b);
		setPickable(CommonAttributes.POINT_SHADER, b);
	}

	public void setPickEdges(boolean b) {
		pickEdges.setSelected(b);
		setPickable(CommonAttributes.LINE_SHADER, b);
	}

	public void setPickFaces(boolean b) {
		pickFaces.setSelected(b);
		setPickable(CommonAttributes.POLYGON_SHADER, b);
	}

	private void setPickable(String shader, boolean b) {
		if (scene != null) {
			Appearance contentAppearance = scene.getContentAppearance();
			if (contentAppearance != null) {
				contentAppearance.setAttribute(
						shader+"."+CommonAttributes.PICKABLE,
						b
				);
			}
		}
	}

	public boolean isDragEnabled() {
		return drag.isSelected();
	}

	public void setDragEnabled(boolean b) {
		drag.setSelected(b);
		setToolEnabled(draggingTool, drag.isSelected() && !snapToGrid.isSelected());
		setToolEnabled(snapDragTool, drag.isSelected() &&  snapToGrid.isSelected());
	}

	public boolean isRotationEnabled() {
		return rotate.isSelected();
	}

	public void setRotationEnabled(boolean b) {
		rotate.setSelected(b);
		setToolEnabled(rotateTool, b);
	}

	private void setToolEnabled(Tool tool, boolean b) {
		if (content != null) {
			if (b) content.addContentTool(tool);
			else content.removeContentTool(tool);
		}
	}

	public boolean isPickFaces() {
		return pickFaces.isSelected();
	}

	public boolean isPickEdges() {
		return pickEdges.isSelected();
	}

	public boolean isPickVertices() {
		return pickVertices.isSelected();
	}

	protected void setEncompassEnabled(boolean b) {
		encompass.setSelected(b);
		setToolEnabled(encompassTool, b);
	}

	@Override
	public void install(Controller c) throws Exception {
		scene = c.getPlugin(Scene.class);
		content = PluginUtility.getPlugin(c, Content.class);
		install();
		ViewMenuBar viewMenuBar = c.getPlugin(ViewMenuBar.class);
		installMenu(viewMenuBar);

		ViewToolBar tb = c.getPlugin(ViewToolBar.class);
		//installToolbox(this);
		installToolbox(tb);
//		tb.addToggle(getClass(), getRotateToggle(), "Content", "Tools");
//		tb.addToggle(getClass(), getPickEdgesToggle(), "Content", "Pick");
//		shrinkPanel.setHeaderColor(new Color(0.5f, 0.5f, 0.2f));
		super.install(c);
	}

	private void installToolbox(ToolBarAggregator viewToolbar) {
		viewToolbar.addTool(getClass(), 1.1, getDragToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.2, getRotateToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.3, getSnapToGridToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addSeparator(getClass(), 1.4);//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.5, getPickFacesToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.6, getPickEdgesToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.7, getPickVerticesToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addSeparator(getClass(), 1.8);//, "Tools", "Content");
		viewToolbar.addTool(getClass(), 1.9, getEncompassToggle().createToolboxItem());//, "Tools", "Content");
		viewToolbar.addSeparator(getClass(), 2.0);//, "Tools", "Content");
	}

	private void installMenu(ViewMenuBar viewMenuBar) {
		viewMenuBar.addMenuItem(getClass(), 1.1, getDragToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.2, getRotateToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.3, getSnapToGridToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuSeparator(getClass(), 1.4, "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.5, getPickFacesToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.6, getPickEdgesToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.7, getPickVerticesToggle().createMenuItem(), "Content", "Tools");
		viewMenuBar.addMenuSeparator(getClass(), 1.8, "Content", "Tools");
		viewMenuBar.addMenuItem(getClass(), 1.9, getEncompassToggle().createMenuItem(), "Content", "Tools");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		setToolEnabled(draggingTool, false);
		setToolEnabled(rotateTool, false);
		setToolEnabled(snapDragTool, false);
		super.uninstall(c);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Tools";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("toolsblau.png");
		return info; 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setRotationEnabled(c.getProperty(getClass(), "rotationEnabled", isRotationEnabled()));
		setDragEnabled(c.getProperty(getClass(), "dragEnabled", isDragEnabled()));
		setSnapToGrid(c.getProperty(getClass(), "snapTogrid", isSnapToGrid()));
		setPickVertices(c.getProperty(getClass(), "pickVertices", isPickVertices()));
		setPickEdges(c.getProperty(getClass(), "pickEdges", isPickEdges()));
		setPickFaces(c.getProperty(getClass(), "pickFaces", isPickFaces()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "rotationEnabled", isRotationEnabled());
		c.storeProperty(getClass(), "dragEnabled", isDragEnabled());
		c.storeProperty(getClass(), "snapTogrid", isSnapToGrid());
		c.storeProperty(getClass(), "pickVertices", isPickVertices());
		c.storeProperty(getClass(), "pickEdges", isPickEdges());
		c.storeProperty(getClass(), "pickFaces", isPickFaces());
		super.storeStates(c);
	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	public AbstractJrToggleAction getRotateToggle() {
		return rotate;
	}

	public AbstractJrToggleAction getDragToggle() {
		return drag;
	}

	public AbstractJrToggleAction getSnapToGridToggle() {
		return snapToGrid;
	}

	public AbstractJrToggleAction getPickFacesToggle() {
		return pickFaces;
	}

	public AbstractJrToggleAction getPickEdgesToggle() {
		return pickEdges;
	}

	public AbstractJrToggleAction getPickVerticesToggle() {
		return pickVertices;
	}
	
	public AbstractJrToggleAction getEncompassToggle() {
		return encompass;
	}
	
}

