package de.jreality.tutorial.tool;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.scene.SceneShrinkPanel;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.tools.RotateTool;
import de.jreality.ui.JSliderVR;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;



public class ModelExample implements PointDragListener {
	
	PointSetFactory controlPoints = new PointSetFactory();
	
	SceneGraphComponent base = new SceneGraphComponent();
	SceneGraphComponent controlComponent = new SceneGraphComponent();
	SceneGraphComponent curveComponent = new SceneGraphComponent();
	
	double[][] vertices;
	Color[] vertexColors;

	private int subdivisionLevel=2;
	
	public ModelExample(int n) {
		vertices = Primitives.regularPolygonVertices(n, 0);
		vertexColors = new Color[n];
		for (int i=0; i<n; i++) vertexColors[i] = Color.green; 
		controlPoints.setVertexCount(n);
		updateControlPoints();
		updateCurve();

		DragEventTool tool = new DragEventTool();
		tool.addPointDragListener(this);
		
		controlComponent.setGeometry(controlPoints.getGeometry());
		controlComponent.addTool(tool);
		
		base.addTool(new RotateTool());
		
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.POINT_RADIUS, 0.05);
		controlComponent.setAppearance(app);
		
		base.addChild(controlComponent);
		base.addChild(curveComponent);
	}
	
	private void updateControlPoints() {
		controlPoints.setVertexCoordinates(vertices);
		controlPoints.setVertexColors(vertexColors);
		controlPoints.update();
	}

	// highlight the selected point
	public void pointDragStart(PointDragEvent e) {
		vertexColors[e.getIndex()] = Color.red;
		updateControlPoints();
	}
	
	// drag the point of the geometry
	public void pointDragged(PointDragEvent e) {
		vertices[e.getIndex()] = new double[]{e.getX(), e.getY(), e.getZ()};
		updateControlPoints();		
		updateCurve();
	}
	
	// remove highlight
	public void pointDragEnd(PointDragEvent e) {
		vertexColors[e.getIndex()] = Color.green;
		updateControlPoints();
	}

	private void updateCurve() {
		double[][] cur = vertices;
		for (int i=0; i<subdivisionLevel; i++) {
			double[][] sub = new double[2*cur.length][];
			int n = cur.length;
			for (int j=0; j<n; j++) {
				sub[2*j] = cur[j];
				sub[2*j+1] = fourPointSubdivision(cur[(j-1+n)%n], cur[j], cur[(j+1)%n], cur[(j+2)%n]);
			}
			cur = sub;
		}
		curveComponent.setGeometry(IndexedLineSetUtility.createCurveFromPoints(cur, true));
	}
	
	SceneShrinkPanel createPanel() {
		return new SceneShrinkPanel() {
			public PluginInfo getPluginInfo() {
				return new PluginInfo("Subdivision Control");
			}
			@Override
			public void install(Controller c) throws Exception {
				super.install(c);
				setTriggerComponent(base);
				final JSliderVR slider = new JSliderVR(0, 6, subdivisionLevel);
				slider.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						subdivisionLevel = slider.getValue();
						updateCurve();
					}
				});
				getShrinkPanel().setLayout(new GridLayout());
				getShrinkPanel().add(slider);
			}
		};
		
	}
	
	private static double[] fourPointSubdivision(double[] v1, double[] v2, double[] v3, double[] v4) {
		double[] ret = new double[3];
    	for (int j=0; j<3; j++) ret[j] = (9.0*(v2[j]+v3[j])-v1[j]-v4[j])/16.0;
    	return ret;
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		ModelExample example = new ModelExample(5);
		v.setContent(example.base);
		v.registerPlugin(example.createPanel());
		v.startup();
	}
}
