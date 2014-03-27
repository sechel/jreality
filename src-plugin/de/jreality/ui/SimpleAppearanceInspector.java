package de.jreality.ui;

import static de.jreality.scene.Appearance.INHERITED;
import static de.jreality.shader.CommonAttributes.Z_BUFFER_ENABLED;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.SwingConstants.HORIZONTAL;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.ColorChooseJButton.ColorChangedEvent;
import de.jreality.ui.ColorChooseJButton.ColorChangedListener;

/** The gui component of an {@link Appearance} plugin.
 *
 */
public class SimpleAppearanceInspector extends JPanel implements ActionListener, ChangeListener, ColorChangedListener {

	private static final long serialVersionUID = 1L;
	
	private enum LinesState {
		HIDE("Hide"), LINES("Lines"), TUBES("Tubes");

		private String displayName = "";
		
		private LinesState(String str) {
			displayName = str;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	private enum VertexState {
		HIDE("Hide"), POINTS("Points"), SPHERES("Spheres");

		private String displayName = "";
		
		private VertexState(String str) {
			displayName = str;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	private enum FaceState {
		HIDE("Hide"), FLAT("Flat"), SMOOTH("Smooth");

		private String displayName = "";
		
		private FaceState(String str) {
			displayName = str;
		}
		
		@Override
		public String toString() {
			return displayName;
		}
	}
	
	/*
	 * maximal radius of tubes or points compared to content size
	 */
	private double 
		maximalRadius = 1.5,
		objectScale = 1.0;
	
	/*
	 * ratio of maximal versus minimal value for logarithmic sliders
	 */
	private int 
		logarithmicRange = 200;
	
	private JPanel
		mainPanel = new JPanel();

	private ColorChooseJButton
		lineColorButton = new ColorChooseJButton(true),
		pointColorButton = new ColorChooseJButton(true),
		faceColorButton = new ColorChooseJButton(true);
	
	private JSliderVR
		tubeRadiusSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		sphereRadiusSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		transparencySlider = new JSliderVR(HORIZONTAL, 0, 100, 0);

	private JCheckBox 
		lines = new JCheckBox("Lines"),
		points = new JCheckBox("Points"),
		faces = new JCheckBox("Faces"),
		transparency = new JCheckBox("Transp.");
	
	private JButton
		linesButton = new JButton(LinesState.TUBES.toString()),
		pointsButton = new JButton(VertexState.SPHERES.toString()),
		facesButton = new JButton(FaceState.FLAT.toString());
	
	private Appearance appearance = new Appearance();
	
	private GridBagConstraints
		c = new GridBagConstraints();
	
	private Insets
		insets = new Insets(1,0,1,0);
	
	public SimpleAppearanceInspector() {
		setLayout(new GridLayout());
		makePanel();
		add(mainPanel);
		
		// lines
		lines.addActionListener(this);
		linesButton.addActionListener(this);
		lineColorButton.addColorChangedListener(this);
//		showLines.addActionListener(this);
		tubeRadiusSlider.addChangeListener(this);
		
		// points
		points.addActionListener(this);
		pointsButton.addActionListener(this);
		pointColorButton.addColorChangedListener(this);
		sphereRadiusSlider.addChangeListener(this);
		
		// faces
		faces.addActionListener(this);
		facesButton.addActionListener(this);
		faceColorButton.addColorChangedListener(this);
		transparencySlider.addChangeListener(this);
		transparency.addActionListener(this);
	}
	
	
	private void makePanel() {
		mainPanel.setLayout(new GridBagLayout());
		c.fill = BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = WEST;
		
		// lines
		
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(lines, c);
		c.gridwidth = 1;
		c.weightx = 1.0;
		mainPanel.add(linesButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(lineColorButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(tubeRadiusSlider, c);

		// points
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(points, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		mainPanel.add(pointsButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(pointColorButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(sphereRadiusSlider, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		
		// faces
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(faces, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		mainPanel.add(facesButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(faceColorButton, c);
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(transparency, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(transparencySlider, c);

		updateEnabledStates();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		// lines
		if (lines == s) {
			if(isEditLines()) {
				updateLines();
			} else {
				resetLinesToInherited();
			}
			
		} else 
		if (linesButton == s) {
			if(linesButton.getText().equals("Hide")) {
				linesButton.setText("Lines");
			} else if(linesButton.getText().equals("Lines")) {
				linesButton.setText("Tubes");
			} else if(linesButton.getText().equals("Tubes")) {
				linesButton.setText("Hide");
			}
			updateLines();
		}
		
		// points
		if (points == s) {
			if(isEditPoints()) {
				updatePoints();
			} else {
				resetPointsToInherited();
			}
		} else if (pointsButton == s) {
			if(pointsButton.getText().equals(VertexState.HIDE.toString())) {
				pointsButton.setText(VertexState.POINTS.toString());
			} else if(pointsButton.getText().equals(VertexState.POINTS.toString())) {
				pointsButton.setText(VertexState.SPHERES.toString());
			} else if(pointsButton.getText().equals(VertexState.SPHERES.toString())) {
				pointsButton.setText(VertexState.HIDE.toString());
			}
			updatePoints();
		} else
		
		// faces
		if (faces == s) {
			if(isEditFaces()) {
				updateFaces();
			} else {
				resetFacesToInherited();
			}
		} else if (facesButton == s) {
			if(facesButton.getText().equals(FaceState.HIDE.toString())) {
				facesButton.setText(FaceState.FLAT.toString());
			} else if(facesButton.getText().equals(FaceState.FLAT.toString())) {
				facesButton.setText(FaceState.SMOOTH.toString());
			} else if(facesButton.getText().equals(FaceState.SMOOTH.toString())) {
				facesButton.setText(FaceState.HIDE.toString());
			}
			updateFaces();
		} else if (transparency == s) {
			updateTransparencyEnabled();
		} else
		
		if (lineColorButton == s) {
			updateLineColor();
		} else if (pointColorButton == s) {
			updatePointColor();
		} else if (faceColorButton == s) {
			updateFaceColor();
		}
		updateEnabledStates();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();
		// lines
		if (tubeRadiusSlider == s) {
			updateTubeRadius();
		} else

		// points
		if (sphereRadiusSlider == s) {
			updateSphereRadius();
		} else
		
		// faces
		if (transparencySlider == s) {
			updateTransparency();
		}
	}

	
	public void updateEnabledStates() {
		lineColorButton.setEnabled(isEditLines());
		linesButton.setEnabled(isEditLines());
		tubeRadiusSlider.setEnabled(isEditLines());
		
		pointColorButton.setEnabled(isEditPoints());
		pointsButton.setEnabled(isEditPoints());
		sphereRadiusSlider.setEnabled(isEditPoints());
		
		faceColorButton.setEnabled(isEditFaces());
		transparency.setEnabled(isEditFaces());
		transparencySlider.setEnabled(isEditFaces() && isTransparencyEnabled());
		facesButton.setEnabled(isEditFaces());
	}
	
	public Appearance getAppearance() {
		return appearance;
	}

	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
	}

	public double getMaximalRadius() {
		return maximalRadius;
	}

	public double getObjectScale() {
		return objectScale;
	}

	public void setObjectScale(double d) {
		objectScale=d;
		updateSphereRadius();
		updateTubeRadius();
	}
	
	public void setMaximalRadius(double maximalRadius) {
		this.maximalRadius = maximalRadius;
		updateSphereRadius();
		updateTubeRadius();
	}

	public int getLogarithmicRange() {
		return logarithmicRange;
	}

	public void setLogarithmicRange(int logarithmicRange) {
		this.logarithmicRange = logarithmicRange;
		updateSphereRadius();
		updateTubeRadius();
	}
	
	public boolean isEditPoints() {
		return points.isSelected();
	}
	public boolean isShowPoints() {
		return !pointsButton.getText().equals(VertexState.HIDE.toString());
	}
	public void setShowPoints(VertexState state) {
		pointsButton.setText(state.toString());
		updatePoints();
	}
	private void updatePoints() {
		updateShowPoints();
		updateSpheres();
		updatePointColor();
		updateSphereRadius();
		updateEnabledStates();
	}
	private void updateShowPoints() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.VERTEX_DRAW,
					isShowPoints()
			);
		}
		updateEnabledStates();
	}
	
	public boolean isSpheres() {
		return pointsButton.getText().equals(VertexState.SPHERES.toString());
	}
	public void setSpheres(boolean b) {
		if(isShowPoints()) {
			if(b) {
				pointsButton.setText(VertexState.SPHERES.toString());
			} else {
				pointsButton.setText(VertexState.POINTS.toString());
			}
		}
		updatePoints();
	}
	private void updateSpheres() {
		boolean spheres = isSpheres();
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.SPHERES_DRAW,
					spheres
			);
		}
		updateEnabledStates();
	}
	
	public double getPointRadius() {
		return .01 * sphereRadiusSlider.getValue();
	}
	public void setPointRadius(double d) {
		sphereRadiusSlider.setValue((int) (d * 100));
	}
	private void updateSphereRadius() {
		double r =
			Math.exp(Math.log(logarithmicRange) * getPointRadius()) /
			logarithmicRange * maximalRadius;
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.POINT_RADIUS,
					r
			);
			// 64 pixels is the maximum size for point sprites
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.POINT_SIZE,
					getPointRadius() * 64
			);
			appearance.setAttribute(CommonAttributes.LINE_SHADER + "." + 
					CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
		}
	}
	
	public Color getPointColor() {
		return pointColorButton.getColor();
	}
	public void setPointColor(Color c) {
		pointColorButton.setColor(c);
		updatePointColor();
	}
	private void updatePointColor() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,
					getPointColor()
			);
		}
		pointColorButton.setColor(getPointColor());
	}
	private void resetPointsToInherited() {
		if(appearance != null) {
			appearance.setAttribute(
					CommonAttributes.VERTEX_DRAW,
					INHERITED
			);
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.SPHERES_DRAW,
					INHERITED
			);
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.POINT_RADIUS,
					INHERITED
			);
			// 64 pixels is the maximum size for point sprites
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.POINT_SIZE,
					INHERITED
			);
			appearance.setAttribute(CommonAttributes.LINE_SHADER + "." + 
					CommonAttributes.DEPTH_FUDGE_FACTOR, INHERITED);
			appearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,
					INHERITED
			);
		}
	}
	
	//-----------------------------lines---------------------
	public boolean isEditLines() {
		return lines.isSelected();
	}
	
	public boolean isShowLines() {
		return !linesButton.getText().equals(LinesState.HIDE.toString());
	}
	public void setShowLines(LinesState state) {
		linesButton.setText(state.toString());
		updateLines();
	}
	private void updateLines() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.EDGE_DRAW,
					isShowLines()
			);
			boolean tubes = isTubes();
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "." +
					CommonAttributes.TUBES_DRAW,
					tubes
			);
		}
		updateTubeRadius();
		updateLineColor();
		updateEnabledStates();
	}
	private void resetLinesToInherited() {
		appearance.setAttribute(
				CommonAttributes.EDGE_DRAW,
				INHERITED
		);
		appearance.setAttribute(
				CommonAttributes.LINE_SHADER + "." +
				CommonAttributes.TUBES_DRAW,
				INHERITED
		);
		
		appearance.setAttribute(
				CommonAttributes.LINE_SHADER + "."	+
				CommonAttributes.LINE_WIDTH,
				INHERITED
		);
		appearance.setAttribute(
				CommonAttributes.LINE_SHADER + "."	+
				CommonAttributes.TUBE_RADIUS,
				INHERITED
		);
		
		appearance.setAttribute(
				CommonAttributes.LINE_SHADER + "." +
				CommonAttributes.DIFFUSE_COLOR ,
				INHERITED
		);
	}
	
	public boolean isTubes() {
		return linesButton.getText().equals(LinesState.TUBES.toString());
	}
	public void setTubes(boolean b) {
		if(isShowLines()) {
			if(b) {
				linesButton.setText(LinesState.TUBES.toString());
			} else {
				linesButton.setText(LinesState.LINES.toString());
			}
		}
		updateLines();
	}

	public double getTubeRadius() {
		return .01 * tubeRadiusSlider.getValue();
	}
	public void setTubeRadius(double d) {
		tubeRadiusSlider.setValue((int) (d * 100));
	}
	private void updateTubeRadius() {
		double r = Math.exp(Math.log(logarithmicRange) * getTubeRadius())
		/ logarithmicRange * maximalRadius;
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "."	+
					CommonAttributes.LINE_WIDTH,
					getTubeRadius() * 10
			);
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "."	+
					CommonAttributes.TUBE_RADIUS,
					r
			);
		}
	}
	
	public Color getLineColor() {
		return lineColorButton.getColor();
	}
	public void setLineColor(Color c) {
		lineColorButton.setColor(c);
		updateLineColor();
	}
	private void updateLineColor() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR ,
					getLineColor()
			);
		}
	}
	
	
	public boolean isEditFaces() {
		return faces.isSelected();
	}
	public boolean isShowFaces() {
		return !facesButton.getText().equals(FaceState.HIDE.toString());
	}

	public void setShowFaces(FaceState state) {
		facesButton.setText(state.toString());
		updateFaces();
	}
	
	private void updateFaces() {
		updateShowFaces();
		updateFacesFlat();
		updateFaceColor();
		updateTransparencyEnabled();
		updateTransparency();
	}
	
	private void updateShowFaces() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.FACE_DRAW,
					isShowFaces()
			);
		}
	}
	
	public boolean isFacesFlat() {
		return facesButton.getText().equals(FaceState.FLAT.toString());
	}
	public void setFacesFlat(boolean b) {
		if(isShowFaces()) {
			if(b) {
				facesButton.setText(FaceState.FLAT.toString());
			} else {
				facesButton.setText(FaceState.SMOOTH.toString());
			}
		}
		updateFaces();
	}
	private void updateFacesFlat() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.SMOOTH_SHADING,
					!isFacesFlat()
			);
		}
	}

	public boolean isTransparencyEnabled() {
		return transparency.isSelected();
	}
	public void setTransparencyEnabled(boolean b) {
		transparency.setSelected(b);
		updateTransparency();
	}
	private void updateTransparencyEnabled() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.TRANSPARENCY_ENABLED,
					isTransparencyEnabled()
			);
			appearance.setAttribute(Z_BUFFER_ENABLED, true);
		}
		updateTransparency();
		updateEnabledStates();
	}

	public double getTransparency() {
		return .01 * transparencySlider.getValue();
	}
	public void setTransparency(double d) {
		transparencySlider.setValue((int)(100 * d));
	}
	private void updateTransparency() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.TRANSPARENCY,
					// TODO this is a workaround for a jogl backend bug
					isTransparencyEnabled() ? getTransparency() : 0.0
			);
		}
	}

	public Color getFaceColor() {
		return faceColorButton.getColor();
	}
	public void setFaceColor(Color c) {
		appearance.setAttribute(
				CommonAttributes.POLYGON_SHADER + "." +
				CommonAttributes.DIFFUSE_COLOR,c);
	}
	private void updateFaceColor() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,
					getFaceColor()
			);
		}
		faceColorButton.setColor(getFaceColor());
	}
	private void resetFacesToInherited() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.FACE_DRAW,
					INHERITED
			);
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.SMOOTH_SHADING,
					INHERITED
			);
			appearance.setAttribute(
					CommonAttributes.TRANSPARENCY_ENABLED,
					INHERITED
			);
			appearance.setAttribute(Z_BUFFER_ENABLED, INHERITED);
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.TRANSPARENCY,
					INHERITED
			);
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,INHERITED);
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,
					INHERITED
			);
			
		}
	}
	
	
	@Override
	public void colorChanged(ColorChangedEvent cce) {
		Object s = cce.getSource();
		if(s == lineColorButton) {
			setLineColor(cce.getColor());
		} else if(s == pointColorButton) {
			setPointColor(cce.getColor());
		} else if(s == faceColorButton) {
			setFaceColor(cce.getColor());
		}
	}
	
}
