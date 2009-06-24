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
import de.jreality.scene.SceneGraphNode;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.tools.RotateTool;
import de.jreality.util.Input;



public class ModelExample  {
	
	private static final Color DEFAULT_COLOR = Color.green;
	private static final Color HIGHLIGHT_COLOR = Color.red;
	
	PointSetFactory controlPoints = new PointSetFactory();
	CachedAudioInputStreamSource audioSource;
	
	SceneGraphComponent baseCmp = new SceneGraphComponent();
	SceneGraphComponent controlCmp = new SceneGraphComponent();
	SceneGraphComponent splineCmp = new SceneGraphComponent();
	SceneGraphComponent audioCmp = new SceneGraphComponent();
	
	int n = 5;
	double[][] vertices = Primitives.regularPolygonVertices(n, 0);
	Color[] vertexColors = new Color[n];

	public ModelExample() {
		controlPoints.setVertexCount(n);
		for (int i=0; i<n; i++) vertexColors[i] = DEFAULT_COLOR; 
		try {
			audioSource = new CachedAudioInputStreamSource("hammond", Input.getInput("sound/churchbell_loop.wav"), true);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		audioCmp.setAudioSource(audioSource);
		
		updateControlPoints();
		updateSpline();

		DragEventTool tool = new DragEventTool();

		tool.addPointDragListener(new PointDragListener() {
			public void pointDragStart(PointDragEvent e) {
				updateFeedback(e.getIndex(), true);
				updateControlPoints();
			}
			public void pointDragged(PointDragEvent e) {
				double[] pos = new double[]{e.getX(), e.getY(), e.getZ()}; //e.getPosition();
				vertices[e.getIndex()] = pos;
				MatrixBuilder.euclidean().translate(pos).assignTo(audioCmp);
				updateControlPoints();		
				updateSpline();
			}
			public void pointDragEnd(PointDragEvent e) {
				updateFeedback(e.getIndex(), false);
				updateControlPoints();
			}
		});
		
		controlCmp.setGeometry(controlPoints.getGeometry());
		controlCmp.addTool(tool);
		
		baseCmp.addTool(new RotateTool());
		
		Appearance app = new Appearance();
		app.setAttribute(CommonAttributes.POINT_RADIUS, 0.05);
		controlCmp.setAppearance(app);
		
		baseCmp.addChild(controlCmp);
		baseCmp.addChild(splineCmp);
		baseCmp.addChild(audioCmp);
	}
	
	protected void updateFeedback(int index, boolean highlight) {
		if (highlight) {
			vertexColors[index] = HIGHLIGHT_COLOR;
			audioSource.start();
		}
		else {
			vertexColors[index] = DEFAULT_COLOR;
			audioSource.stop();
		}
		updateControlPoints();
	}

	private void updateControlPoints() {
		controlPoints.setVertexCoordinates(vertices);
		controlPoints.setVertexColors(vertexColors);
		controlPoints.update();
	}

	private void updateSpline() {
		double[][] spline = subdivide();
		splineCmp.setGeometry(createCurve(spline));
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

	private SceneGraphNode getComponent() {
		return baseCmp;
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
		v.setContent(example.getComponent());
		v.startup();
	}

}