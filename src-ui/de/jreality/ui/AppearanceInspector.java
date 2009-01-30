package de.jreality.ui;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.SwingConstants.HORIZONTAL;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.swing.ColorPicker;

import de.jreality.scene.Appearance;
import de.jreality.scene.Scene;
import de.jreality.shader.CommonAttributes;

@SuppressWarnings("serial")
public class AppearanceInspector extends JPanel implements ActionListener, ChangeListener {

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
		mainPanel = new JPanel(),
		texturePanel = new JPanel(),
		lineColorPanel = new JPanel(),
		pointColorPanel = new JPanel(),
		faceColorPanel = new JPanel();
	private ColorChooseJButton
		lineColorButton = new ColorChooseJButton(false),
		pointColorButton = new ColorChooseJButton(false),
		faceColorButton = new ColorChooseJButton(false);
	private ColorPicker
		lineColorChooser = new ColorPicker(false, false),
		pointColorChooser = new ColorPicker(false, false),
		faceColorChooser = new ColorPicker(false, false);
	private JSliderVR
		tubeRadiusSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		sphereRadiusSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		linesReflectionSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		pointsReflectionSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		facesReflectionSlider = new JSliderVR(HORIZONTAL, 0, 100, 0),
		transparencySlider = new JSliderVR(HORIZONTAL, 0, 100, 0);
	private JCheckBox 
		showLines = new JCheckBox("Visible"),
		showPoints = new JCheckBox("Visible"),
		showFaces = new JCheckBox("Visible"),
		transparency = new JCheckBox("Transp."),
		pointsReflecting = new JCheckBox("Reflection"),
		linesReflecting = new JCheckBox("Reflection"),
		facesReflecting = new JCheckBox("Reflection"),
		facesFlat = new JCheckBox("Flat Shading"),
		tubes = new JCheckBox ("Tubes"),
		spheres = new JCheckBox("Spheres");
	private JButton 
		textureButton = new JButton("Texture..."),
		closeButton = new JButton("<-- Back"),
		closeLineColorsButton = new JButton("<-- Back"),
		closePointColorsButton = new JButton("<-- Back"),
		closeFaceColorsButton = new JButton("<-- Back");
	private Appearance 
		appearance = new Appearance(),
		scaledAppearance = new Appearance();
	private TextureInspector 
		textureInspector = new TextureInspector();
	private GridBagConstraints
		c = new GridBagConstraints();
	private Insets
		insets = new Insets(2, 2, 2, 2);

	
	public AppearanceInspector() {
		setLayout(new GridLayout());
		makeTexturePanel();
		makePanel();
		add(mainPanel);
		
		// lines
		lineColorChooser.getColorPanel().addChangeListener(this);
		lineColorButton.addActionListener(this);
		closeLineColorsButton.addActionListener(this);
		showLines.addActionListener(this);
		linesReflecting.addActionListener(this);
		linesReflectionSlider.addChangeListener(this);
		tubeRadiusSlider.addChangeListener(this);
		tubes.addActionListener(this);
		
		// points
		pointColorChooser.getColorPanel().addChangeListener(this);
		pointColorButton.addActionListener(this);
		closePointColorsButton.addActionListener(this);
		showPoints.addActionListener(this);
		pointsReflecting.addActionListener(this);
		pointsReflectionSlider.addChangeListener(this);
		sphereRadiusSlider.addChangeListener(this);
		spheres.addActionListener(this);
		
		// faces
		faceColorChooser.getColorPanel().addChangeListener(this);
		faceColorButton.addActionListener(this);
		closeFaceColorsButton.addActionListener(this);
		showFaces.addActionListener(this);
		facesReflecting.addActionListener(this);
		facesReflectionSlider.addChangeListener(this);
		transparencySlider.addChangeListener(this);
		transparency.addActionListener(this);
		facesFlat.addActionListener(this);
		closeButton.addActionListener(this);
		textureButton.addActionListener(this);
		
		showFaces.setSelected(true);
		
		lineColorChooser.setMode(0);
		pointColorChooser.setMode(0);
		faceColorChooser.setMode(0);
	}
	
	
	private void makeTexturePanel() {
		c.fill = BOTH;
		c.insets = insets;
		c.gridwidth = REMAINDER;
		c.anchor = WEST;
		c.weightx = 1.0;
		c.weighty = 1.0;
		texturePanel.setLayout(new GridBagLayout());
		texturePanel.add(textureInspector, c);
		c.fill = HORIZONTAL;
		c.weightx = 0.0;
		c.weighty = 0.0;
		texturePanel.add(closeButton, c);
	}
	
	
	private void makePanel() {
		mainPanel.setLayout(new MinSizeGridBagLayout());
		c.fill = BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = WEST;
		
		// lines
		JPanel linesPanel = new JPanel();
		linesPanel.setBorder(createTitledBorder("Lines"));
		linesPanel.setLayout(new GridBagLayout());
		
		c.gridwidth = 1;
		c.weightx = 0.0;
		linesPanel.add(showLines, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		linesPanel.add(lineColorButton, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		linesPanel.add(tubes, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		linesPanel.add(tubeRadiusSlider, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		linesPanel.add(linesReflecting, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		linesPanel.add(linesReflectionSlider, c);

		// points
		JPanel pointsPanel = new JPanel();
		pointsPanel.setBorder(createTitledBorder("Points"));
		pointsPanel.setLayout(new GridBagLayout());
		c.gridwidth = 1;
		c.weightx = 0.0;
		pointsPanel.add(showPoints, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		pointsPanel.add(pointColorButton, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		pointsPanel.add(spheres, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		pointsPanel.add(sphereRadiusSlider, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		pointsPanel.add(pointsReflecting, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		pointsPanel.add(pointsReflectionSlider, c);
		
		// faces
		JPanel facesPanel = new JPanel();
		facesPanel.setBorder(createTitledBorder("Faces"));
		facesPanel.setLayout(new GridBagLayout());
		c.gridwidth = 1;
		c.weightx = 0.0;
		facesPanel.add(showFaces, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		facesPanel.add(faceColorButton, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		facesPanel.add(facesReflecting, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		facesPanel.add(facesReflectionSlider, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		facesPanel.add(transparency, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		facesPanel.add(transparencySlider, c);
	

		facesPanel.add(facesFlat, c);
		facesPanel.add(textureButton, c);
		
		c.fill = BOTH;
		c.gridwidth = REMAINDER;
		c.weighty = 1.0;
		c.weightx = 1.0;
		mainPanel.add(facesPanel, c);
		mainPanel.add(linesPanel, c);
		mainPanel.add(pointsPanel, c);
		
		
		// line color panel
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = insets;
		c.gridwidth = REMAINDER;
		c.anchor = WEST;
		lineColorPanel.setLayout(new GridBagLayout());
		c.fill = BOTH;
		lineColorChooser.setPreferredSize(new Dimension(220, 230));
		lineColorPanel.add(lineColorChooser, c);
		c.fill = VERTICAL;
		lineColorPanel.add(closeLineColorsButton, c);
		// point color panel
		pointColorPanel.setLayout(new GridBagLayout());
		c.fill = BOTH;
		pointColorChooser.setPreferredSize(new Dimension(220, 230));
		pointColorPanel.add(pointColorChooser, c);
		c.fill = VERTICAL;
		pointColorPanel.add(closePointColorsButton, c);
		// face color panel
		faceColorPanel.setLayout(new GridBagLayout());
		c.fill = BOTH;
		faceColorChooser.setPreferredSize(new Dimension(220, 230));
		faceColorPanel.add(faceColorChooser, c);
		c.fill = VERTICAL;
		faceColorPanel.add(closeFaceColorsButton, c);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		// lines
		if (showLines == s) {
			updateShowLines();
		} else if (linesReflecting == s) {
			updateLinesReflecting();
		} else if (tubes == s) {
			updateTubes();
		} else
		
		// points
		if (showPoints == s) {
			updateShowPoints();
		} else if (pointsReflecting == s) {
			updatePointsReflecting();
		} else if (spheres == s) {
			updateSpheres();
		} else
		
		// faces
		if (showFaces == s) {
			updateShowFaces();
		} else if (facesReflecting == s) {
			updateFacesReflecting();
		} else if (textureButton == s) {
			switchTo(texturePanel);
		} else if (facesFlat == s) {
			updateFacesFlat();
		} else if (transparency == s) {
			updateTransparencyEnabled();
		} else
		
		// colors
		if (lineColorButton == s) {
			switchTo(lineColorPanel);
		} else if (pointColorButton == s) {
			switchTo(pointColorPanel);
		} else if (faceColorButton == s) {
			switchTo(faceColorPanel);
		} else
		
		// default back to main panel
		{
			switchTo(mainPanel);
		}
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();
		// lines
		if (lineColorChooser.getColorPanel() == s) {
			updateLineColor();
		} else if (linesReflectionSlider == s) {
			updateLineReflection();
		} else if (tubeRadiusSlider == s) {
			updateTubeRadius();
		} else
		
		// points
		if (pointColorChooser.getColorPanel() == s) {
			updatePointColor();
		} else if (pointsReflectionSlider == s) {
			updatePointReflection();
		} else if (sphereRadiusSlider == s) {
			updateSphereRadius();
		} else
		
		// faces
		if (faceColorChooser.getColorPanel() == s) {
			updateFaceColor();
		} else if (facesReflectionSlider == s) {
			updateFaceReflection();
		} else if (transparencySlider == s) {
			updateTransparency();
		}
	}

	
	public HashMap<String, String> getTextures() {
		return textureInspector.getTextures();
	}
	
	public void setTextures(HashMap<String, String> textures) {
		textureInspector.setTextures(textures);
	}
	
	
	private void updateEnabledStates() {
		lineColorButton.setEnabled(isShowLines());
		linesReflecting.setEnabled(isShowLines() && isTubes());
		linesReflectionSlider.setEnabled(isShowLines() && isLinesReflecting() && isTubes());
		tubes.setEnabled(isShowLines());
		tubeRadiusSlider.setEnabled(isShowLines() && isTubes());
		
		pointColorButton.setEnabled(isShowPoints());
		pointsReflecting.setEnabled(isShowPoints() && isSpheres());
		pointsReflectionSlider.setEnabled(isShowPoints() && isPointsReflecting() && isSpheres());
		spheres.setEnabled(isShowPoints());
		sphereRadiusSlider.setEnabled(isShowPoints() && isSpheres());
		
		faceColorButton.setEnabled(isShowFaces());
		facesReflecting.setEnabled(isShowFaces());
		facesReflectionSlider.setEnabled(isShowFaces() && isFacesReflecting());
		transparency.setEnabled(isShowFaces());
		transparencySlider.setEnabled(isShowFaces() && isTransparencyEnabled());
		textureButton.setEnabled(isShowFaces());
		facesFlat.setEnabled(isShowFaces());
	}
	
	
	public void setColorPickerMode(int mode) {
		lineColorChooser.setMode(mode);
		pointColorChooser.setMode(mode);
		faceColorChooser.setMode(mode);
	}
	
	
	public Appearance getAppearance() {
		return appearance;
	}

	public void setAppearance(final Appearance appearance) {
		this.appearance = appearance;
		Scene.executeWriter(appearance, new Runnable() {
			
			public void run() {
				textureInspector.setAppearance(appearance);
				updateShowPoints();
				updateShowLines();
				updateShowFaces();
				updatePointColor();
				updateLineColor();
				updateFaceColor();
				updatePointsReflecting();
				updateLinesReflecting();
				updateFacesReflecting();
				updatePointReflection();
				updateLineReflection();
				updateFaceReflection();
				updateSpheres();
				updateTubes();
				updateSphereRadius();
				updateTubeRadius();
				updateTransparencyEnabled();
				updateTransparency();
				updateFacesFlat();
			}
		});
	}
	
	public Appearance getScaledAppearance() {
		return scaledAppearance;
	}

	public void setScaledAppearance(Appearance scaledAppearance) {
		this.scaledAppearance = scaledAppearance;
		Scene.executeWriter(appearance, new Runnable() {
			public void run() {
				updateSphereRadius();
				updateTubeRadius();
			}
		});
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
	
	public boolean isPointsReflecting() {
		return pointsReflecting.isSelected();
	}

	public void setPointsReflecting(boolean b) {
		pointsReflecting.setSelected(b);
	}
	
	private void updatePointsReflecting() {
		if (isPointsReflecting()) {
			updatePointReflection();
		} else {
			if (appearance != null) {
				appearance.setAttribute(
						"pointShader.reflectionMap:blendColor",
						new Color(1f, 1f, 1f, 0f)
				);
			}
		}
		updateEnabledStates();
	}
	
	public double getPointReflection() {
		return .01 * pointsReflectionSlider.getValue();
	}

	public void setPointReflection(double d) {
		pointsReflectionSlider.setValue((int)(100*d));
	}
	
	private void updatePointReflection() {
		if (appearance != null) {
			appearance.setAttribute(
					"pointShader.reflectionMap:blendColor",
					new Color(1f, 1f, 1f, (float) getPointReflection())
			);
		}
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
		}
		if (scaledAppearance != null) {
			scaledAppearance.setAttribute(
					CommonAttributes.POINT_SHADER + "." +
					CommonAttributes.POINT_RADIUS,
					r / objectScale
			);
		}
	}
	
	public Color getPointColor() {
		int[] rgb = pointColorChooser.getRGB();
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

	public void setPointColor(Color c) {
		pointColorChooser.setRGB(c.getRed(), c.getGreen(), c.getBlue());
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
		if (appearance != null) {
			appearance.setAttribute("showLines", selected);
		}
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

	public double getLineReflection() {
		return .01 * linesReflectionSlider.getValue();
	}

	public void setLineReflection(double d) {
		linesReflectionSlider.setValue((int)(100*d));
	}
	
	private void updateLineReflection() {
		if (appearance != null) {
			appearance.setAttribute(
					"lineShader.reflectionMap:blendColor",
					new Color(1f, 1f, 1f, (float) getLineReflection())
			);
		}
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
					CommonAttributes.TUBE_RADIUS,
					r
			);
		}
		if (scaledAppearance != null) {
			scaledAppearance.setAttribute(
					CommonAttributes.LINE_SHADER + "."	+
					CommonAttributes.TUBE_RADIUS,
					r / objectScale
			);
		}
	}
	
	public Color getLineColor() {
		int[] rgb = lineColorChooser.getRGB();
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

	public void setLineColor(Color c) {
		lineColorChooser.setRGB(c.getRed(), c.getGreen(), c.getBlue());
	}
	
	private void updateLineColor() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.LINE_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR ,
					getLineColor()
			);
		}
		lineColorButton.setColor(getLineColor());
	}
	
	public boolean isLinesReflecting() {
		return linesReflecting.isSelected();
	}
	
	public void setLinesReflecting(boolean b) {
		linesReflecting.setSelected(b);
	}
	
	private void updateLinesReflecting() {
		if (isLinesReflecting()) {
			updateLineReflection();
		} else {
			if (appearance != null) {
				appearance.setAttribute(
						"lineShader.reflectionMap:blendColor",
						new Color(1f, 1f, 1f, 0f)
				);
			}
		}
		updateEnabledStates();
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
	}
	
	private void updateTransparencyEnabled() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.TRANSPARENCY_ENABLED,
					isTransparencyEnabled()
			);
		}
		updateEnabledStates();
	}
	
	public boolean isFacesReflecting() {
		return facesReflecting.isSelected();
	}

	public void setFacesReflecting(boolean b) {
		facesReflecting.setSelected(b);
	}
	
	private void updateFacesReflecting() {
		if (isFacesReflecting()) {
			updateFaceReflection();
		} else {
			if (appearance != null) {
				appearance.setAttribute(
						"polygonShader.reflectionMap:blendColor",
						new Color(1f, 1f, 1f, 0f)
				);
			}
		}
		updateEnabledStates();
	}
	
	public double getFaceReflection() {
		return .01 * facesReflectionSlider.getValue();
	}

	public void setFaceReflection(double d) {
		facesReflectionSlider.setValue((int)(100*d));
	}
	
	private void updateFaceReflection() {
		if (appearance != null) {
			appearance.setAttribute(
					"polygonShader.reflectionMap:blendColor",
					new Color(1f, 1f, 1f, (float) getFaceReflection())
			);
		}
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
					getTransparency()
			);
		}
	}

	public Color getFaceColor() {
		int[] rgb = faceColorChooser.getRGB();
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

	public void setFaceColor(Color c) {
		faceColorChooser.setRGB(c.getRed(), c.getGreen(), c.getBlue());
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
	
	public String getTexture() {
		return textureInspector.getTexture();
	}
	
	public void setTexture(String texture) {
		textureInspector.setTexture(texture);
	}

	public double getTextureScale() {
		return textureInspector.getTextureScale();
	}
	
	public void setTextureScale(double scale) {
		textureInspector.setTextureScale(scale);
	}
	
	private void switchTo(JComponent content) {
		removeAll();
		add(content);
		revalidate();
		repaint();
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (textureInspector != null) {
			SwingUtilities.updateComponentTreeUI(texturePanel);
		}
		if (lineColorPanel != null) {
			SwingUtilities.updateComponentTreeUI(lineColorPanel);
		}
		if (faceColorPanel != null) {
			SwingUtilities.updateComponentTreeUI(faceColorPanel);
		}
		if (pointColorPanel != null) {
			SwingUtilities.updateComponentTreeUI(pointColorPanel);
		}
	}
	
}
