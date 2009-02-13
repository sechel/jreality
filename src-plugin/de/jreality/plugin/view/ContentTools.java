package de.jreality.plugin.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AxisTranslationTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class ContentTools extends ShrinkPanelPlugin {

	private static final boolean DEFAULT_PICK_FACES = true;
	private static final boolean DEFAULT_PICK_EDGES = true;
	private static final boolean DEFAULT_PICK_VERTICES = true;

	private RotateTool rotateTool;
	private DraggingTool draggingTool;
	private AxisTranslationTool snapDragTool;
	private JPanel panel;
	private JCheckBox rotate;
	private JCheckBox drag;
	private JCheckBox snapToGrid;
	private JCheckBox pickFaces;
	private JCheckBox pickEdges;
	private JCheckBox pickVertices;
	private AlignedContent alignedContent;

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

		setInitialPosition(SHRINKER_RIGHT);
	}

	public void install(AlignedContent alignedContent) {
		this.alignedContent  = alignedContent;
		setRotationEnabled(isRotationEnabled());
		setDragEnabled(isDragEnabled());
		setPickEdges(isPickEdges());
		setPickFaces(isPickFaces());
		setPickVertices(isPickVertices());
	}

	public JPanel getPanel() {
		return panel;
	}

	public void setSnapToGrid(boolean b) {
		snapToGrid.setSelected(b);
		if (alignedContent != null) {
			setToolEnabled(draggingTool, drag.isSelected() && !snapToGrid.isSelected());
			setToolEnabled(snapDragTool, drag.isSelected() && snapToGrid.isSelected());
		}
	}

	public boolean isSnapToGrid() {
		return snapToGrid.isSelected();
	}

	public void setPickVertices(boolean b) {
		pickVertices.setSelected(b);
		if (alignedContent != null) {
			Appearance contentAppearance = alignedContent.getTransformationComponent().getAppearance();
			if (contentAppearance == null) {
				contentAppearance = new Appearance();
				alignedContent.getTransformationComponent().setAppearance(contentAppearance);
			}
			contentAppearance.setAttribute(
					CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE,
					b
			);
		}
	}

	public void setPickEdges(boolean b) {
		pickEdges.setSelected(b);
		if (alignedContent != null) {
			Appearance contentAppearance = alignedContent.getTransformationComponent().getAppearance();
			if (contentAppearance == null) {
				contentAppearance = new Appearance();
				alignedContent.getTransformationComponent().setAppearance(contentAppearance);
			}
			contentAppearance.setAttribute(
					CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE,
					b
			);
		}
	}

	public void setPickFaces(boolean b) {
		pickFaces.setSelected(b);
		if (alignedContent != null) {
			Appearance contentAppearance = alignedContent.getTransformationComponent().getAppearance();
			if (contentAppearance == null) {
				contentAppearance = new Appearance();
				alignedContent.getTransformationComponent().setAppearance(contentAppearance);
			}
			contentAppearance.setAttribute(
					CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE,
					b
			);
		}
	}

	public boolean isDragEnabled() {
		return drag.isSelected();
	}

	public void setDragEnabled(boolean b) {
		drag.setSelected(b);
		if (alignedContent != null) {
			setToolEnabled(draggingTool, drag.isSelected() && !snapToGrid.isSelected());
			setToolEnabled(snapDragTool, drag.isSelected() &&  snapToGrid.isSelected());
		}
	}

	public boolean isRotationEnabled() {
		return rotate.isSelected();
	}

	public void setRotationEnabled(boolean b) {
		rotate.setSelected(b);
		if (alignedContent != null) {
			setToolEnabled(rotateTool, b);
		}
	}

	private void setToolEnabled(Tool tool, boolean b) {
		SceneGraphComponent contentComponent = alignedContent.getTransformationComponent();
		if (!b && contentComponent.getTools().contains(tool)) {
			contentComponent.removeTool(tool);
		}
		if (b && !contentComponent.getTools().contains(tool)) {
			contentComponent.addTool(tool);
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
		panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Box toolBox = new Box(BoxLayout.Y_AXIS);
		Box toolButtonBox = new Box(BoxLayout.X_AXIS);
		toolButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		rotate = new JCheckBox("rotate");
		rotate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setRotationEnabled(rotate.isSelected());
			}
		});
		toolButtonBox.add(rotate);
		drag = new JCheckBox("drag");
		drag.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setDragEnabled(drag.isSelected());
			}
		});
		toolButtonBox.add(drag);
		toolButtonBox.add(Box.createHorizontalGlue());
		snapToGrid = new JCheckBox("snap");
		snapToGrid.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSnapToGrid(snapToGrid.isSelected());
			}
		});
		toolButtonBox.add(snapToGrid);
		toolButtonBox.add(Box.createHorizontalGlue());
		toolBox.add(toolButtonBox);


		Box pickButtonBox = new Box(BoxLayout.X_AXIS);
		pickButtonBox.setBorder(new EmptyBorder(5, 5, 5, 5));
		pickButtonBox.add(new JLabel("pick: "));
		pickFaces = new JCheckBox("faces");
		pickFaces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickFaces(pickFaces.isSelected());
			}
		});
		pickButtonBox.add(pickFaces);

		pickEdges = new JCheckBox("edges");
		pickEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickEdges(pickEdges.isSelected());
			}
		});
		pickButtonBox.add(pickEdges);

		pickVertices = new JCheckBox("vertices");
		pickVertices.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickVertices(pickVertices.isSelected());
			}
		});
		pickButtonBox.add(pickVertices);
		pickButtonBox.add(Box.createHorizontalGlue());

		toolBox.add(pickButtonBox);

		panel.add(BorderLayout.CENTER, toolBox);
	}

	@Override
	public void install(Controller c) throws Exception {

		alignedContent = c.getPlugin(AlignedContent.class);
		install(alignedContent);

		panel.setPreferredSize(new Dimension(10, 80));
		panel.setMinimumSize(new Dimension(10, 80));

		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(panel);
		shrinkPanel.setHeaderColor(new Color(0.5f, 0.5f, 0.2f));
		super.install(c);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		setToolEnabled(draggingTool, false);
		setToolEnabled(rotateTool, false);
		setToolEnabled(snapDragTool, false);
		shrinkPanel.removeAll();
		super.uninstall(c);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Content Tools";
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
	public String getHelpDocument() {
		return "ContentTools.html";
	}
	
	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}
	
}

