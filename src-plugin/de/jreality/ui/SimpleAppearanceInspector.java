package de.jreality.ui;

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
		showLines = new JCheckBox("Lines"),
		showPoints = new JCheckBox("Points"),
		showFaces = new JCheckBox("Faces"),
		transparency = new JCheckBox("Transp."),
		facesFlat = new JCheckBox("Flat Shading"),
		tubes = new JCheckBox ("Tubes"),
		spheres = new JCheckBox("Spheres");
	
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
		lineColorButton.addColorChangedListener(this);
		showLines.addActionListener(this);
		tubeRadiusSlider.addChangeListener(this);
		tubes.addActionListener(this);
		
		// points
		pointColorButton.addColorChangedListener(this);
		showPoints.addActionListener(this);
		sphereRadiusSlider.addChangeListener(this);
		spheres.addActionListener(this);
		
		// faces
		faceColorButton.addColorChangedListener(this);
		showFaces.addActionListener(this);
		transparencySlider.addChangeListener(this);
		transparency.addActionListener(this);
		facesFlat.addActionListener(this);
		
		showFaces.setSelected(true);
		
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
		mainPanel.add(showLines, c);
		c.gridwidth = 1;
		c.weightx = 1.0;
		mainPanel.add(tubes, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(lineColorButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(tubeRadiusSlider, c);

		// points
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(showPoints, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		mainPanel.add(spheres, c);
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
		mainPanel.add(showFaces, c);
		mainPanel.add(facesFlat, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(faceColorButton, c);
		c.gridwidth = 4;
		c.weightx = 0.0;
		mainPanel.add(transparency, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		mainPanel.add(transparencySlider, c);

		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		// lines
		if (showLines == s) {
			updateShowLines();
		} else if (tubes == s) {
			updateTubes();
		} else
		
		// points
		if (showPoints == s) {
			updateShowPoints();
		} else if (spheres == s) {
			updateSpheres();
		} else
		
		// faces
		if (showFaces == s) {
			updateShowFaces();
		} else if (facesFlat == s) {
			updateFacesFlat();
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
		lineColorButton.setEnabled(isShowLines());
		tubes.setEnabled(isShowLines());
		tubeRadiusSlider.setEnabled(isShowLines());
		
		pointColorButton.setEnabled(isShowPoints());
		spheres.setEnabled(isShowPoints());
		sphereRadiusSlider.setEnabled(isShowPoints());
		
		faceColorButton.setEnabled(isShowFaces());
		transparency.setEnabled(isShowFaces());
		transparencySlider.setEnabled(isShowFaces() && isTransparencyEnabled());
		facesFlat.setEnabled(isShowFaces());
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
	
	public boolean isShowPoints() {
		return showPoints.isSelected();
	}
	public void setShowPoints(boolean selected) {
		showPoints.setSelected(selected);
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
		return spheres.isSelected();
	}
	public void setSpheres(boolean b) {
		spheres.setSelected(b);
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
	
	public boolean isShowLines() {
		return showLines.isSelected();
	}
	public void setShowLines(boolean selected) {
		showLines.setSelected(selected);
		updateShowLines();
	}
	private void updateShowLines() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.EDGE_DRAW,
					isShowLines()
			);
		}
		updateEnabledStates();
	}
	
	public boolean isTubes() {
		return tubes.isSelected();
	}
	public void setTubes(boolean b) {
		tubes.setSelected(b);
		updateTubes();
	}
	private void updateTubes() {
		if (appearance != null) {
			boolean tubes = isTubes();
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "." +
					CommonAttributes.TUBES_DRAW,
					tubes
			);
		}
		updateEnabledStates();
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
	
	public boolean isShowFaces() {
		return showFaces.isSelected();
	}

	public void setShowFaces(boolean selected) {
		showFaces.setSelected(selected);
		if (appearance != null) {
			appearance.setAttribute("showFaces", selected);
		}
	}
	
	private void updateShowFaces() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.FACE_DRAW,
					isShowFaces()
			);
		}
		updateEnabledStates();
	}
	
	public boolean isFacesFlat() {
		return facesFlat.isSelected();
	}
	public void setFacesFlat(boolean b) {
		facesFlat.setSelected(b);
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
		return (Color) appearance.getAttribute(
				CommonAttributes.POLYGON_SHADER + "." +
				CommonAttributes.DIFFUSE_COLOR);
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
