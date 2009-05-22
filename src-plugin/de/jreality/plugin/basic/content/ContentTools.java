package de.jreality.plugin.basic.content;

import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.PluginUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AxisTranslationTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ContentTools extends ToolBarAggregator {

	private static final boolean DEFAULT_PICK_FACES = true;
	private static final boolean DEFAULT_PICK_EDGES = true;
	private static final boolean DEFAULT_PICK_VERTICES = true;

	private RotateTool rotateTool;
	private DraggingTool draggingTool;
	private AxisTranslationTool snapDragTool;
	private JToggleButton 
		rotate = new JToggleButton(ImageHook.getIcon("arrow_rotate_clockwise.png")),
		drag = new JToggleButton(ImageHook.getIcon("arrow_out.png")),
		snapToGrid = new JToggleButton(ImageHook.getIcon("brick.png")),
		pickFaces = new JToggleButton(ImageHook.getIcon("shape_square.png")),
		pickEdges = new JToggleButton(ImageHook.getIcon("shape_edges.png")),
		pickVertices = new JToggleButton(ImageHook.getIcon("shape_handles.png"));
	
	private Scene scene = null;
	private Content content = null;

	public ContentTools() {
		rotateTool = new RotateTool();
		rotateTool.setFixOrigin(false);
		rotateTool.setMoveChildren(false);
		rotateTool.setUpdateCenter(false);
		rotateTool.setAnimTimeMin(250.0);
		rotateTool.setAnimTimeMax(750.0);

		draggingTool = new DraggingTool();
		draggingTool.setMoveChildren(false);

		snapDragTool = new AxisTranslationTool();

		makePanel();
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

	private void makePanel() {
		rotate.setToolTipText("Rotate Tool");
		rotate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setRotationEnabled(rotate.isSelected());
			}
		});
;		drag.setToolTipText("Drag Tool");
		drag.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setDragEnabled(drag.isSelected());
			}
		});
		snapToGrid.setToolTipText("Snap to Grid");
		snapToGrid.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSnapToGrid(snapToGrid.isSelected());
			}
		});

		pickFaces.setToolTipText("Pick Faces");
		pickFaces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickFaces(pickFaces.isSelected());
			}
		});

		pickEdges.setToolTipText("Pick Edges");
		pickEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickEdges(pickEdges.isSelected());
			}
		});

		pickVertices.setToolTipText("Pick Vertices");
		pickVertices.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickVertices(pickVertices.isSelected());
			}
		});
		
		addTool(getClass(), 1, rotate);
		addTool(getClass(), 2, drag);
		addSeparator(getClass(), 3);
		addTool(getClass(), 4, snapToGrid);
		addSeparator(getClass(), 5);
		addTool(getClass(), 6, pickFaces);
		addTool(getClass(), 7, pickEdges);
		addTool(getClass(), 8, pickVertices);
	}

	@Override
	public void install(Controller c) throws Exception {
		scene = c.getPlugin(Scene.class);
		content = PluginUtility.getPlugin(c, Content.class);
		install();
//		shrinkPanel.setHeaderColor(new Color(0.5f, 0.5f, 0.2f));
		super.install(c);
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
	
	@Override
	public double getToolBarPriority() {
		return 0.0;
	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}
	
}

