package de.jreality.tutorial.tool;

import java.awt.Color;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.jreality.audio.javasound.CachedAudioInputStreamSource;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.PointSetFactory;
import de.jreality.geometry.Primitives;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.tools.RotateTool;
import de.jreality.util.Input;



public class ModelExample implements PointDragListener {
	
	PointSetFactory controlPoints = new PointSetFactory();
	CachedAudioInputStreamSource audioSource;
	
	SceneGraphComponent base = new SceneGraphComponent();
	SceneGraphComponent controlComponent = new SceneGraphComponent();
	SceneGraphComponent curveComponent = new SceneGraphComponent();
	SceneGraphComponent audioComponent = new SceneGraphComponent();
	
	int n = 5;
	double[][] vertices = Primitives.regularPolygonVertices(n, 0);
	Color[] vertexColors = new Color[n];

	public ModelExample() {
		try {
			audioSource = new CachedAudioInputStreamSource("hammond", Input.getInput("sound/churchbell_loop.wav"), true);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		audioComponent.setAudioSource(audioSource);
		
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
		base.addChild(audioComponent);
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
		audioSource.start();
	}
	
	// drag the point of the geometry
	public void pointDragged(PointDragEvent e) {
		double[] pos = new double[]{e.getX(), e.getY(), e.getZ()};
		vertices[e.getIndex()] = pos;
		MatrixBuilder.euclidean().translate(pos).assignTo(audioComponent);
		updateControlPoints();		
		updateCurve();
	}
	
	// remove highlight
	public void pointDragEnd(PointDragEvent e) {
		vertexColors[e.getIndex()] = Color.green;
		updateControlPoints();
		audioSource.stop();
	}

	private void updateCurve() {
		double[][] newCurve = subdivide();
		curveComponent.setGeometry(createCurve(newCurve));
	}

	private IndexedLineSet createCurve(double[][] newCurve) {
		return IndexedLineSetUtility.createCurveFromPoints(newCurve, true);
	}

	private double[][] subdivide() {
		double[][] cur = vertices;
		for (int i=0; i<3; i++) {
			double[][] sub = new double[2*cur.length][];
			int n = cur.length;
			for (int j=0; j<n; j++) {
				sub[2*j] = cur[j];
				sub[2*j+1] = subdivide(
								cur[(j-1+n)%n],
								cur[j],
								cur[(j+1)%n],
								cur[(j+2)%n]
							 );
			}
			cur = sub;
		}
		return cur;
	}
	
	private static double[] subdivide(double[] v1, double[] v2, double[] v3, double[] v4) {
		double[] ret = new double[3];
    	for (int j=0; j<3; j++) ret[j] = (9.0*(v2[j]+v3[j])-v1[j]-v4[j])/16.0;
    	return ret;
	}
	
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addAudioSupport();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		ModelExample example = new ModelExample();
		v.setContent(example.base);
		v.startup();
	}
}