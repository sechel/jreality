package de.jreality.tutorial.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import de.jreality.geometry.FrameFieldType;
import de.jreality.geometry.PolygonalTubeFactory;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.TubeUtility;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tutorial.geom.TubeFactory02;
import de.jreality.tutorial.util.TextSlider;
import de.jreality.ui.viewerapp.Navigator;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;

/**
 * This class shows how to add GUI elements to the {@link ViewerApp} class. In particular, it
 * <ul>
 * <li>adds an inspection panel to the {@link Navigator}, and</li>
 * <li>adds a key listeners to the viewing component of the ViewerApp instance, and </li>
 * </ul>
 * 
 * @see TubeFactory02 (same class with different name)
 * @author Charles Gunn
 *
 */
public class InspectorExample {

	static double R = 1, r = .25, tubeRadius = .04;
	static SceneGraphComponent torussgc;
	static boolean isSmooth = true;
	static boolean drawEdges = false;
	static DefaultGeometryShader dgs;
	static DefaultPolygonShader dps;
	static FrameFieldType currentFrameType = FrameFieldType.PARALLEL;
	static PolygonalTubeFactory polygonalTubeFactory;
	
	public static void main(String[] args) {
		torussgc = SceneGraphUtility
				.createFullSceneGraphComponent("torus knot");
		dgs = (DefaultGeometryShader) ShaderUtility
				.createDefaultGeometryShader(torussgc.getAppearance(), true);
		dgs.setShowLines(drawEdges);
		dgs.setShowPoints(false);
		dps = (DefaultPolygonShader) dgs.createPolygonShader("default");
		dps.setSmoothShading(isSmooth);
		updateGeometry();
		ViewerApp va = new ViewerApp(torussgc); // ViewerApp.display(torussgc);
		va.setAttachNavigator(true);
		va.setExternalNavigator(false);
		Component insp = getInspector();
		va.addAccessory(insp);
		va.setFirstAccessory(insp);
		va.update();
		va.display();
		CameraUtility.encompass(va.getCurrentViewer());
		Component comp = ((Component) va.getCurrentViewer()
				.getViewingComponent());
		comp.addKeyListener(new KeyAdapter() {
 				public void keyPressed(KeyEvent e)	{ 
					switch(e.getKeyCode())	{
						
					case KeyEvent.VK_H:
						System.err.println("	1: toggle smooth shading");
						System.out.println("	2: toggle edge drawing");
						break;
		
					case KeyEvent.VK_1:
						isSmooth = !isSmooth;
						dps.setSmoothShading(isSmooth);
						break;

					case KeyEvent.VK_2:
						drawEdges = !drawEdges;
						dgs.setShowLines(drawEdges);
						break;		
				}
		
				}
			});
  
  }

	private static void updateGeometry() {
		IndexedFaceSet tubedTorusKnot = tubedTorusKnot(R, r, tubeRadius);
		torussgc.setGeometry(tubedTorusKnot);
	}

	private static IndexedFaceSet tubedTorusKnot(
			double R, 
			double r,
			double tubeRadius) {
		IndexedLineSet torus1 = Primitives.discreteTorusKnot(R, r, 2, 9, 250);
		colorVertices(torus1, new double[] { 1, 0, 0 },new double[] { 0, 1, 0 });
		// create a non-circular cross section for the tube
		int size = 16;
		double scale = 1;
		double[][] mysection = new double[size][3];
		for (int i = 0; i < size; ++i) {
			double angle = (i / (size - 1.0)) * Math.PI * 2;
			mysection[i][0] = scale * Math.cos(angle)
					* (1.5 + Math.cos(4 * angle));
			mysection[i][1] = scale * Math.sin(angle)
					* (1.5 + Math.cos(4 * angle));
			mysection[i][2] = 0.0;
		}
		polygonalTubeFactory = new PolygonalTubeFactory(torus1, 0);
		polygonalTubeFactory.setClosed(true);
		polygonalTubeFactory.setVertexColorsEnabled(true);
		polygonalTubeFactory.setRadius(tubeRadius);
		polygonalTubeFactory.setCrossSection(mysection);
		polygonalTubeFactory.setTwists(6);
		double[][] vcolors = torus1.getVertexAttributes(Attribute.COLORS)
				.toDoubleArrayArray(null);
		polygonalTubeFactory.setVertexColors(vcolors);
		polygonalTubeFactory.setGenerateEdges(true);
		polygonalTubeFactory.setFrameFieldType(currentFrameType);
		polygonalTubeFactory.update();
		IndexedFaceSet torus1Tubes = polygonalTubeFactory.getTube();
		return torus1Tubes;
	}
  
	public static void colorVertices(IndexedLineSet ils, double[] color1,
			double[] color2) {
		int nPts = ils.getNumPoints();
		double[][] colors = new double[nPts][3];
		double[][] vertices = ils.getVertexAttributes(Attribute.COORDINATES)
				.toDoubleArrayArray(null);
		for (int i = 1; i < nPts - 1; ++i) {
			double[] v1 = Rn.subtract(null, vertices[i], vertices[i - 1]);
			double t = 10 * Math.sqrt(Math.abs(v1[0] * v1[0] + v1[1] * v1[1]));
			t = t - ((int) t);
			Rn.linearCombination(colors[i], t, color1, 1 - t, color2);
		}
		System.arraycopy(colors[1], 0, colors[0], 0, 3);
		System.arraycopy(colors[nPts - 2], 0, colors[nPts - 1], 0, 3);
		ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY
				.array(3).createReadOnly(colors));
	}

	private static Component getInspector() {
		Box container = Box.createVerticalBox();
		container.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				BorderFactory.createTitledBorder(BorderFactory
						.createEtchedBorder(), "Torus parameters")));

		final TextSlider.Double RSlider = new TextSlider.Double("R",
				SwingConstants.HORIZONTAL, 0.0, 2, R);
		RSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				R = RSlider.getValue();
				updateGeometry();
			}
		});
		container.add(RSlider);
		
		final TextSlider.Double rSlider = new TextSlider.Double("r",
				SwingConstants.HORIZONTAL, 0.0, 1, r);
		rSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				r = rSlider.getValue();
				updateGeometry();
			}
		});
		container.add(rSlider);
		
		final TextSlider.Double rtSlider = new TextSlider.Double("tube radius",
				SwingConstants.HORIZONTAL, 0.0, 1, tubeRadius);
		rtSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tubeRadius = rtSlider.getValue();
				updateGeometry();
			}
		});
		container.add(rtSlider);

		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		String[] frames = { "frenet", "parallel" };
		JComboBox frameCB = new JComboBox(frames);
		frameCB.setSelectedIndex(currentFrameType == FrameFieldType.PARALLEL ? 1 : 0);
		frameCB.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JComboBox jcb = (JComboBox) e.getSource();
				currentFrameType = jcb.getSelectedIndex() == 0 ? 
						FrameFieldType.FRENET : FrameFieldType.PARALLEL;
				updateGeometry();
			}

		});
		hbox.add(frameCB);
		hbox.add(Box.createHorizontalGlue());
		container.add(hbox);

		container.setPreferredSize(new Dimension(300, 180));
		JPanel panel = new JPanel();
		panel.setName("Parameters");
		panel.add(container);
		panel.add(Box.createVerticalGlue());
		return panel;
	}

}
