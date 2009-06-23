package de.jreality.tutorial.tool;

import java.awt.Color;

import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.tools.RotateTool;



public class ModelExample implements PointDragListener {
	
	PointSetFactory geom = new PointSetFactory();
	
	SceneGraphComponent base = new SceneGraphComponent();
	SceneGraphComponent comp = new SceneGraphComponent();
	SceneGraphComponent curveComponent = new SceneGraphComponent();
	
	double[][] vertices;
	Color[] vertexColors;
	
	public ModelExample(int n) {
		vertices = Primitives.regularPolygonVertices(n, 0);
		vertexColors = new Color[n];
		for (int i=0; i<n; i++) vertexColors[i] = Color.blue; 
		geom.setVertexCount(n);
		updateGeometry();

		DragEventTool tool = new DragEventTool();
		tool.addPointDragListener(this);
		
		comp.setGeometry(geom.getGeometry());
		comp.addTool(tool);
		
		base.addTool(new RotateTool());
		base.addTool(new DraggingTool());
		
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS, 0.05);
		app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		comp.setAppearance(app);
		
		base.addChild(comp);
		base.addChild(curveComponent);
	}
	
	private void updateGeometry() {
		geom.setVertexCoordinates(vertices);
		geom.setVertexColors(vertexColors);
		geom.update();
	}

	// highlight the selected point
	public void pointDragStart(PointDragEvent e) {
		vertexColors[e.getIndex()] = Color.red;
		updateGeometry();
	}
	
	// drag the point of the geometry
	public void pointDragged(PointDragEvent e) {
		vertices[e.getIndex()] = new double[]{e.getX(), e.getY(), e.getZ()};
		updateGeometry();		
		updateCurve();
	}
	
	// remove highlight
	public void pointDragEnd(PointDragEvent e) {
		vertexColors[e.getIndex()] = Color.blue;
		updateGeometry();
	}

	private void updateCurve() {
		double[][] cur = vertices;
		for (int i=0; i<3; i++) {
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
		
		v.startup();
	}
}
