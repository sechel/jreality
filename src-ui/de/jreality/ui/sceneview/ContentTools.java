package de.jreality.ui.sceneview;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.AxisTranslationTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;

public class ContentTools {
	
	private static final boolean DEFAULT_PICK_FACES = true;
	private static final boolean DEFAULT_PICK_EDGES = true;
	private static final boolean DEFAULT_PICK_VERTICES = true;
	
	private SceneGraphComponent contentParent;
	private RotateTool rotateTool;
	private DraggingTool draggingTool;
	private AxisTranslationTool snapDragTool;
	private JPanel toolPanel;
	private JCheckBox rotate;
	private JCheckBox drag;
	private JCheckBox snapToGrid;
	private JCheckBox pickFaces;
	private JCheckBox pickEdges;
	private JCheckBox pickVertices;
	private Appearance contentAppearance;
	
	public ContentTools(Content content) {
		contentParent  = content.getContentParent();
		contentAppearance = content.getContentAppearance();
		
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
	}
	
	public JPanel getPanel() {
		return toolPanel;
	}
	
	public void setSnapToGrid(boolean b) {
		snapToGrid.setSelected(b);
		setToolEnabled(draggingTool, drag.isSelected() && !snapToGrid.isSelected());
		setToolEnabled(snapDragTool, drag.isSelected() && snapToGrid.isSelected());
	}

	public boolean isSnapTogrid() {
		return snapToGrid.isSelected();
	}
	
	public void setPickVertices(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickVertices.setSelected(b);
	}

	public void setPickEdges(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickEdges.setSelected(b);
	}

	public void setPickFaces(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickFaces.setSelected(b);
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
		setToolEnabled(rotateTool, b);
		rotate.setSelected(b);
	}

	private void setToolEnabled(Tool tool, boolean b) {
		if (!b && contentParent.getTools().contains(tool)) {
			contentParent.removeTool(rotateTool);
		}
		if (b && !contentParent.getTools().contains(tool)) {
			contentParent.addTool(tool);
		}
	}
	
	public boolean isPickFaces() {
		Object v = contentAppearance.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_FACES;
	}

	public boolean isPickEdges() {
		Object v = contentAppearance.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_EDGES;
	}

	public boolean isPickVertices() {
		Object v = contentAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_VERTICES;
	}
	
	private void makePanel() {
		toolPanel = new JPanel(new BorderLayout());
		toolPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
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
		
		toolPanel.add(BorderLayout.CENTER, toolBox);
		
//		JPanel buttonPanel = new JPanel(new FlowLayout());
//		JButton resetButton = new JButton("reset");
//		resetButton.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent e) {
//					MatrixBuilder.euclidean().assignTo(contentParent);
//					content.alignContent();
//			}
//		});
//		buttonPanel.add(resetButton);
//		toolPanel.add(BorderLayout.SOUTH, buttonPanel);
	}
}
