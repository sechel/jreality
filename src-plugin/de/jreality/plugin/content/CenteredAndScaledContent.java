package de.jreality.plugin.content;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.tool.Tool;
import de.jreality.ui.JSliderVR;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class CenteredAndScaledContent extends ContentPanel implements Content {

	private double size = 5;
	
	// things to remember for changing size
	private double[] center=new double[3];
	private double objectSize=1;
	private Matrix lastMatrix=new Matrix();
	
	JSliderVR sizeSlider = new JSliderVR(1, 5001);

	protected SceneGraphComponent contentCmp;
	protected SceneGraphNode oldContent;

	public void setContent(SceneGraphNode content) {
		
		if (oldContent != null) SceneGraphUtility.removeChildNode(contentCmp, oldContent);
		oldContent = content;
		if (content != null) SceneGraphUtility.addChildNode(contentCmp, content);
		
		if (content != null) {
			SceneGraphComponent cmp;
			if (content instanceof SceneGraphComponent) cmp = (SceneGraphComponent) content;
			else {
				cmp = new SceneGraphComponent("wrapper");
				SceneGraphUtility.addChildNode(cmp, content);
			}
	
			Rectangle3D bds = BoundingBoxUtility.calculateBoundingBox(cmp);
			
			double[] ext = bds.getExtent();
			objectSize = Math.max(Math.max(ext[0], ext[1]), ext[2]);
			center = bds.getCenter();
		} else {
			center[0]=0; center[1]=0; center[2]=0;
			objectSize=1;
		}
		updateMatrix();
	}
	
	private void updateMatrix() {
		
		Matrix newMatrix = MatrixBuilder.euclidean().scale(size/objectSize).translate(-center[0], -center[1], -center[2]).getMatrix();

		/*
		Matrix toolModification = new Matrix(lastMatrix);
		toolModification.invert();
		toolModification.multiplyOnRight(new Matrix(contentCmp.getTransformation()));
		newMatrix.multiplyOnRight(toolModification);
		 */
		lastMatrix.assignFrom(newMatrix);
		
		if (contentCmp != null) newMatrix.assignTo(contentCmp);
	}

	public boolean addContentTool(Tool tool) {
		if (!contentCmp.getTools().contains(tool)) contentCmp.addTool(tool);
		return true;
	}
	
	public boolean removeContentTool(Tool tool) {
		return contentCmp.removeTool(tool);
	}

	public void contentChanged() {
		setContent(oldContent);
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size=size;
		sizeSlider.setValue((int)(size * 100));
		updateMatrix();
	}
	
	void createGUI() {		
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSize(sizeSlider.getValue()/100.);
			}
		});
		shrinkPanel.setLayout(new GridLayout(1,1));
		sizeSlider.setMinimumSize(new Dimension(250,35));
		sizeSlider.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "size"));
		shrinkPanel.add(sizeSlider);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Aligned Content", "jReality Group");
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		contentCmp = c.getPlugin(Scene.class).getContentComponent();
		createGUI();
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		if (oldContent != null) SceneGraphUtility.removeChildNode(contentCmp, oldContent);
		super.uninstall(c);
	}
}
