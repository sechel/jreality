package de.jreality.plugin.basic.content;

import static de.jreality.geometry.BoundingBoxUtility.calculateBoundingBox;
import static de.jreality.geometry.BoundingBoxUtility.removeZeroExtends;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.Tool;
import de.jreality.ui.JSliderVR;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class TerrainAlignedContent extends ContentPanel implements Content {

	SceneGraphComponent transformationComponent;
	SceneGraphComponent scalingComponent = new SceneGraphComponent("scaling");
	
	private double contentSize=20;
	private double verticalOffset=10;

	private Rectangle3D bounds;

	public boolean addContentTool(Tool tool) {
		if (!scalingComponent.getTools().contains(tool)) scalingComponent.addTool(tool);
		return true;
	}
	
	public boolean removeContentTool(Tool tool) {
		return scalingComponent.removeTool(tool);
	}
	
	private SceneGraphNode content;
	private Matrix lastMatrix=new Matrix();
	
	final JSliderVR sizeSlider = new JSliderVR(1, 5001);
	final JSliderVR offsetSlider = new JSliderVR(-250, 5000-250);
	private JPanel guiPanel;

	public void alignContent() {
		try {
			bounds = calculateBoundingBox(wrap(content));
		} catch (Exception e) {
			return;
		}
		removeZeroExtends(bounds);
		double scale = 1;
		double[] e = bounds.getExtent();
		double[] center = bounds.getCenter();
		double objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
		scale = contentSize/objectSize;
		center[0] *= -scale;
		center[1] *= -scale;
		center[2] *= -scale;
		Matrix matrix = MatrixBuilder.euclidean().scale(
				scale
		).translate(
				center
		).getMatrix();
		
		/*
		Matrix toolModification = new Matrix(lastMatrix);
		toolModification.invert();
		toolModification.multiplyOnRight(scalingComponent.getTransformation().getMatrix());

		lastMatrix.assignFrom(matrix);
		
		matrix.multiplyOnRight(toolModification);
		*/
		
		matrix.assignTo(scalingComponent);
		
		// translate contentComponent
		bounds = bounds.transformByMatrix(
				bounds,
				matrix.getArray()
		);
		center = bounds.getCenter();
				
		Matrix m = MatrixBuilder.euclidean().translate(
				-center[0], 
				-bounds.getMinY() + verticalOffset,
				-center[2]
		).getMatrix();
		m.assignTo(transformationComponent);
		bounds = bounds.transformByMatrix(
				bounds,
				m.getArray()
		);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		Scene scene = c.getPlugin(Scene.class);
		setContentSize(contentSize);
		setVerticalOffset(verticalOffset);
		transformationComponent = scene.getContentComponent();
		scalingComponent.setTransformation(new Transformation("scaling trafo"));
		transformationComponent.addChild(scalingComponent);
		SceneGraphPath newEmptyPick = scene.getContentPath().pushNew(scalingComponent);
		scene.setEmptyPickPath(newEmptyPick);		
		createGUI();
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		transformationComponent.removeChild(scalingComponent);
		super.uninstall(c);
	}
	
	private SceneGraphComponent wrap(SceneGraphNode node) {
		if (node instanceof SceneGraphComponent) return (SceneGraphComponent) node;
		SceneGraphComponent wrap = new SceneGraphComponent("wrapper");
		SceneGraphUtility.addChildNode(wrap, node);
		return wrap;
	}

	public void contentChanged() {
		alignContent();
	}

	public void setContent(SceneGraphNode newContent) {
		if (content != newContent) {
			if (content != null) SceneGraphUtility.removeChildNode(scalingComponent, content);
		}
		this.content = newContent;
		alignContent();
		if (newContent != null) SceneGraphUtility.addChildNode(scalingComponent, content);
	}

	public double getContentSize() {
		return contentSize;
	}

	public void setContentSize(double contentSize) {
		this.contentSize = contentSize;
		sizeSlider.setValue((int) (contentSize * 100));
		alignContent();
	}

	public double getVerticalOffset() {
		return verticalOffset;
	}

	public void setVerticalOffset(double verticalOffset) {
		this.verticalOffset = verticalOffset;
		offsetSlider.setValue((int) (verticalOffset * 100));
		alignContent();
	}
	
	private void createGUI() {		
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setContentSize(sizeSlider.getValue()/100.);
			}
		});
		offsetSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setVerticalOffset(offsetSlider.getValue()/100.);
			}
		});
		shrinkPanel.setLayout(new GridLayout(2,1));
		sizeSlider.setMinimumSize(new Dimension(250,35));
		sizeSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "size"));
		offsetSlider.setMinimumSize(new Dimension(250,35));
		offsetSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "offset"));
		shrinkPanel.add(sizeSlider);
		shrinkPanel.add(offsetSlider);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("terrain aligned content");
	}
}
