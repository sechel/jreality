package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.TextureUtility;
import de.jtem.beans.SimpleColorChooser;

public class AppearancePluginVR extends AbstractPluginVR {

	// defaults for app panel
	private static final boolean DEFAULT_SHOW_POINTS = false;
	private static final boolean DEFAULT_POINTS_REFLECTING = false;
	private static final double DEFAULT_POINT_RADIUS = .4;
	private static final Color DEFAULT_POINT_COLOR = Color.blue;
	private static final boolean DEFAULT_SHOW_LINES = false;
	private static final boolean DEFAULT_LINES_REFLECTING = false;
	private static final double DEFAULT_TUBE_RADIUS = .3;
	private static final Color DEFAULT_LINE_COLOR = Color.red;
	private static final boolean DEFAULT_SHOW_FACES = true;
	private static final boolean DEFAULT_FACES_REFLECTING = true;
	private static final double DEFAULT_FACE_REFLECTION = .7;
	private static final double DEFAULT_LINE_REFLECTION = .7;
	private static final double DEFAULT_POINT_REFLECTION = .7;
	private static final Color DEFAULT_FACE_COLOR = Color.white;
	private static final boolean DEFAULT_TRANSPARENCY_ENABLED = false;
	private static final double DEFAULT_TRANSPARENCY = .7;
	private static final boolean DEFAULT_FACES_FLAT = false;

	// app tab
	private JPanel appearancePanel;
	private SimpleColorChooser lineColorChooser;
	private SimpleColorChooser pointColorChooser;
	private SimpleColorChooser faceColorChooser;
	private JSlider tubeRadiusSlider;
	private JSlider pointRadiusSlider;
	private JSlider lineReflectionSlider;
	private JSlider pointReflectionSlider;
	private JSlider faceReflectionSlider;
	private JCheckBox showLines;
	private JCheckBox showPoints;
	private JCheckBox showFaces;
	private JCheckBox transparency;
	private JSlider transparencySlider;
	private JCheckBox pointsReflecting;
	private JCheckBox linesReflecting;
	private JCheckBox facesReflecting;
	private JCheckBox facesFlat;
	
	CubeMap cmVertices, cmEdges, cmFaces;
	ImageData[] cubeMap;
	
	private ActionListener closeListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			getViewerVR().switchToDefaultPanel();
		}
	};

	public AppearancePluginVR() {
		super("app");
		makeAppTab();
	}
	
	private void makeAppTab() {
		appearancePanel = new JPanel(new BorderLayout());
		Box appBox = new Box(BoxLayout.Y_AXIS);
		
		int topSpacing = 0;
		int bottomSpacing = 5;
		
		Border boxBorder = new EmptyBorder(topSpacing, 5, bottomSpacing, 5);
		Border sliderBoxBorder = new EmptyBorder(topSpacing, 10, bottomSpacing, 10);
		
		// lines
		lineColorChooser = new SimpleColorChooser();
		lineColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		lineColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setLineColor(lineColorChooser.getColor());
			}
		});
		lineColorChooser.addActionListener(closeListener);
		Box lineBox = new Box(BoxLayout.Y_AXIS);
		lineBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box lineButtonBox = new Box(BoxLayout.X_AXIS);
		lineButtonBox.setBorder(boxBorder);
		showLines = new JCheckBox("lines");
		showLines.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setShowLines(showLines.isSelected());
			}
		});
		lineButtonBox.add(showLines);
		linesReflecting = new JCheckBox("reflection");
		linesReflecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLinesReflecting(isLinesReflecting());
			}
		});
		lineButtonBox.add(linesReflecting);
		lineReflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		lineReflectionSlider.setPreferredSize(new Dimension(70,20));
		lineReflectionSlider.setBorder(new EmptyBorder(0,5,0,0));
		lineReflectionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setLineReflection(getLineReflection());
			}
		});
		lineButtonBox.add(lineReflectionSlider);
		JButton lineColorButton = new JButton("color");
		lineColorButton.setMargin(new Insets(0,5,0,5));
		lineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToLineColorChooser();
			}
		});
		lineBox.add(lineButtonBox);

		Box tubeRadiusBox = new Box(BoxLayout.X_AXIS);
		tubeRadiusBox.setBorder(sliderBoxBorder);
		JLabel tubeRadiusLabel = new JLabel("radius");
		tubeRadiusSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		tubeRadiusSlider.setPreferredSize(new Dimension(70,20));
		tubeRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTubeRadius(getTubeRadius());
			}
		});
		tubeRadiusBox.add(tubeRadiusLabel);
		tubeRadiusBox.add(tubeRadiusSlider);
		tubeRadiusBox.add(lineColorButton);
		lineBox.add(tubeRadiusBox);

		appBox.add(lineBox);

		// points
		pointColorChooser = new SimpleColorChooser();
		pointColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		pointColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPointColor(pointColorChooser.getColor());
			}
		});
		pointColorChooser.addActionListener(closeListener);
		Box pointBox = new Box(BoxLayout.Y_AXIS);
		pointBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box pointButtonBox = new Box(BoxLayout.X_AXIS);
		pointButtonBox.setBorder(boxBorder);
		showPoints = new JCheckBox("points");
		showPoints.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setShowPoints(showPoints.isSelected());
			}
		});
		pointButtonBox.add(showPoints);
		pointsReflecting = new JCheckBox("reflection");
		pointsReflecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setPointsReflecting(isPointsReflecting());
			}
		});
		pointButtonBox.add(pointsReflecting);
		pointReflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		pointReflectionSlider.setPreferredSize(new Dimension(70,20));
		pointReflectionSlider.setBorder(new EmptyBorder(0,5,0,0));
		pointReflectionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPointReflection(getPointReflection());
			}
		});
		pointButtonBox.add(pointReflectionSlider);
		JButton pointColorButton = new JButton("color");
		pointColorButton.setMargin(new Insets(0,5,0,5));
		pointColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToPointColorChooser();
			}
		});
		pointBox.add(pointButtonBox);

		Box pointRadiusBox = new Box(BoxLayout.X_AXIS);
		pointRadiusBox.setBorder(sliderBoxBorder);
		JLabel pointRadiusLabel = new JLabel("radius");
		pointRadiusSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		pointRadiusSlider.setPreferredSize(new Dimension(70,20));
		pointRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setPointRadius(0.01 * pointRadiusSlider.getValue());
			}
		});
		pointRadiusBox.add(pointRadiusLabel);
		pointRadiusBox.add(pointRadiusSlider);
		pointRadiusBox.add(pointColorButton);
		pointBox.add(pointRadiusBox);

		appBox.add(pointBox);

		// faces
		faceColorChooser = new SimpleColorChooser();
		faceColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		faceColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFaceColor(faceColorChooser.getColor());
			}
		});
		faceColorChooser.addActionListener(closeListener);
		Box faceBox = new Box(BoxLayout.Y_AXIS);
		faceBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box faceButtonBox = new Box(BoxLayout.X_AXIS);
		faceButtonBox.setBorder(boxBorder);
		showFaces = new JCheckBox("faces");
		showFaces.setSelected(true);
		showFaces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setShowFaces(showFaces.isSelected());
			}
		});
		faceButtonBox.add(showFaces);
		facesReflecting = new JCheckBox("reflecting");
		facesReflecting.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFacesReflecting(isFacesReflecting());
			}
		});
		faceButtonBox.add(facesReflecting);
		faceReflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
		faceReflectionSlider.setPreferredSize(new Dimension(70,20));
		faceReflectionSlider.setBorder(new EmptyBorder(0,5,0,0));
		faceReflectionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setFaceReflection(getFaceReflection());
			}
		});
		faceButtonBox.add(faceReflectionSlider);
		JButton faceColorButton = new JButton("color");
		faceColorButton.setMargin(new Insets(0,5,0,5));
		faceColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToFaceColorChooser();
			}
		});
		faceBox.add(faceButtonBox);

		Box transparencyBox = new Box(BoxLayout.X_AXIS);
		transparencyBox.setBorder(new EmptyBorder(topSpacing,5,0,10));
		transparency = new JCheckBox("transp");
		transparency.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTransparencyEnabled(transparency.isSelected());
			}
		});
		transparencySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 1);
		transparencySlider.setPreferredSize(new Dimension(70,20));
		transparencySlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTransparency(getTransparency());
			}
		});
		transparencyBox.add(transparency);
		transparencyBox.add(transparencySlider);
		transparencyBox.add(faceColorButton);
		faceBox.add(transparencyBox);
		JPanel flatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		//flatPanel.setBorder(new EmptyBorder(5,5,5,5));
		facesFlat = new JCheckBox("flat shading");
		facesFlat.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setFacesFlat(isFacesFlat());
			}
		});
		flatPanel.add(facesFlat);
		faceBox.add(flatPanel);
		appBox.add(faceBox);

		appearancePanel.add(appBox);
	}
	
	@Override
	public JPanel getPanel() {
		return appearancePanel;
	}
	
	@Override
	public void contentChanged() {
		setTubeRadius(getTubeRadius());
		setPointRadius(getPointRadius());
	}
	
	@Override
	public void environmentChanged() {
		setSkyBox(getViewerVR().getEnvironment().getCubeMap());
	}

	public void setSkyBox(ImageData[] c) {
		cubeMap = c;
		
		setPointsReflecting(isPointsReflecting());
		setLinesReflecting(isLinesReflecting());
		setFacesReflecting(isFacesReflecting());
		setFaceReflection(getFaceReflection());
	}
	

	public double getFaceReflection() {
		return .01 * faceReflectionSlider.getValue();
	}
	
	public double getLineReflection() {
		return .01 * lineReflectionSlider.getValue();
	}
	
	public double getPointReflection() {
		return .01 * pointReflectionSlider.getValue();
	}
	
	public void setFaceReflection(double d) {
		faceReflectionSlider.setValue((int)(100*d));
		if (cmFaces != null) cmFaces.setBlendColor(new Color(1f, 1f, 1f, (float) d));
	}
	
	public void setLineReflection(double d) {
		lineReflectionSlider.setValue((int)(100*d));
		if (cmEdges != null) cmEdges.setBlendColor(new Color(1f, 1f, 1f, (float) d));
	}
	
	public void setPointReflection(double d) {
		pointReflectionSlider.setValue((int)(100*d));
		if (cmVertices != null) cmVertices.setBlendColor(new Color(1f, 1f, 1f, (float) d));
	}
	
	public double getTransparency() {
		return .01 * transparencySlider.getValue();
	}
	
	public void setTransparency(double d) {
		transparencySlider.setValue((int)(100 * d));
		getViewerVR().getContentAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, d);
	}
	
	public double getPointRadius() {
		return .01 * pointRadiusSlider.getValue();
	}
	
	public void setPointRadius(double d) {
		pointRadiusSlider.setValue((int) (d * 100));
		getViewerVR().getContentAppearance().setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POINT_RADIUS, Math.exp(Math.log(AlignPluginVR.LOGARITHMIC_RANGE) * d)
				/ AlignPluginVR.LOGARITHMIC_RANGE * getViewerVR().getObjectScale() * AlignPluginVR.MAX_RADIUS);
	}
	
	public double getTubeRadius() {
		return .01 * tubeRadiusSlider.getValue();
	}
	
	public void setTubeRadius(double d) {
		tubeRadiusSlider.setValue((int) (d * 100));
		getViewerVR().getContentAppearance().setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBE_RADIUS, Math.exp(Math.log(AlignPluginVR.LOGARITHMIC_RANGE) * d)
				/ AlignPluginVR.LOGARITHMIC_RANGE * getViewerVR().getObjectScale() * AlignPluginVR.MAX_RADIUS);
	}
	
	public Color getPointColor() {
		return (Color) getViewerVR().getContentAppearance().getAttribute(
				CommonAttributes.POINT_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setPointColor(Color c) {
		pointColorChooser.setColor(c);
		String attribute = CommonAttributes.POINT_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		getViewerVR().getContentAppearance().setAttribute(attribute,c);
	}
	
	public Color getLineColor() {
		return (Color) getViewerVR().getContentAppearance().getAttribute(
				CommonAttributes.LINE_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setLineColor(Color c) {
		lineColorChooser.setColor(c);
		String attribute = CommonAttributes.LINE_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		getViewerVR().getContentAppearance().setAttribute(attribute,c);
	}
	
	public boolean isPointsReflecting() {
		return pointsReflecting.isSelected();
	}
	
	public boolean isLinesReflecting() {
		return linesReflecting.isSelected();
	}
	
	public boolean isFacesReflecting() {
		return facesReflecting.isSelected();
	}
	
	public void setPointsReflecting(boolean b) {
		pointsReflecting.setSelected(b);
		if (!isPointsReflecting()) {
			if (cmVertices != null) {
				TextureUtility.removeReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.POINT_SHADER);
				cmVertices = null;
			}
		} else {
			cmVertices = TextureUtility.createReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.POINT_SHADER, cubeMap);
		}
	}
	
	public void setLinesReflecting(boolean b) {
		linesReflecting.setSelected(b);
		if (!isLinesReflecting()) {
			if (cmEdges != null) {
				TextureUtility.removeReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.LINE_SHADER);
				cmEdges = null;
			}
		} else {
			cmEdges = TextureUtility.createReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.LINE_SHADER, cubeMap);
		}
	}
	
	public void setFacesReflecting(boolean b) {
		facesReflecting.setSelected(b);
		if (!isFacesReflecting()) {
			if (cmFaces != null) {
				TextureUtility.removeReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.POLYGON_SHADER);
				cmFaces = null;
			}
		} else {
			cmFaces = TextureUtility.createReflectionMap(getViewerVR().getContentAppearance(), CommonAttributes.POLYGON_SHADER, cubeMap);
		}
	}
	
	public Color getFaceColor() {
		return (Color) getViewerVR().getContentAppearance().getAttribute(
				CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setFaceColor(Color c) {
		faceColorChooser.setColor(c);
		String attribute = CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		getViewerVR().getContentAppearance().setAttribute(attribute,c);
	}
	
	public boolean isFacesFlat() {
		return facesFlat.isSelected();
	}
	
	public void setFacesFlat(boolean b) {
		facesFlat.setSelected(b);
		getViewerVR().getContentAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, !b);
	}
	
	public boolean isShowLines() {
		return showLines.isSelected();
	}
	
	public void setShowLines(boolean selected) {
		showLines.setSelected(selected);
		getViewerVR().getContentAppearance().setAttribute("showLines", selected);
	}
	
	public boolean isShowPoints() {
		return showPoints.isSelected();
	}
	
	public void setShowPoints(boolean selected) {
		showPoints.setSelected(selected);
		getViewerVR().getContentAppearance().setAttribute("showPoints", selected);
	}
	
	public boolean isShowFaces() {
		return showFaces.isSelected();
	}
	
	public void setShowFaces(boolean selected) {
		showFaces.setSelected(selected);
		getViewerVR().getContentAppearance().setAttribute("showFaces", selected);
	}
	
	public boolean isTransparencyEnabled() {
		return transparency.isSelected();
	}
	
	public void setTransparencyEnabled(boolean b) {
		transparency.setSelected(b);
		getViewerVR().getContentAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, b);
	}
	
	private void switchToLineColorChooser() {
		getViewerVR().switchTo(lineColorChooser);
	}
	
	private void switchToPointColorChooser() {
		getViewerVR().switchTo(pointColorChooser);
	}
	
	private void switchToFaceColorChooser() {
		getViewerVR().switchTo(faceColorChooser);
	}
	
	@Override
	public void storePreferences(Preferences prefs) {
		// app panel
		prefs.putBoolean("showPoints", isShowPoints());
		prefs.putDouble("pointRadius", getPointRadius());
		Color c = getPointColor();
		prefs.putInt("pointColorRed", c.getRed());
		prefs.putInt("pointColorGreen", c.getGreen());
		prefs.putInt("pointColorBlue", c.getBlue());
		prefs.putBoolean("showLines", isShowLines());
		prefs.putDouble("tubeRadius", getTubeRadius());
		c = getLineColor();
		prefs.putInt("lineColorRed", c.getRed());
		prefs.putInt("lineColorGreen", c.getGreen());
		prefs.putInt("lineColorBlue", c.getBlue());
		prefs.putBoolean("showFaces", isShowFaces());
		prefs.putDouble("faceReflection", getFaceReflection());
		prefs.putDouble("lineReflection", getLineReflection());
		prefs.putDouble("pointReflection", getPointReflection());
		c = getFaceColor();
		prefs.putInt("faceColorRed", c.getRed());
		prefs.putInt("faceColorGreen", c.getGreen());
		prefs.putInt("faceColorBlue", c.getBlue());
		prefs.putBoolean("transparencyEnabled", isTransparencyEnabled());
		prefs.putDouble("transparency", getTransparency());
		prefs.putBoolean("facesFlat", isFacesFlat());
	
	}

	@Override
	public void restoreDefaults() {
		// app panel
		setShowPoints(DEFAULT_SHOW_POINTS);
		setPointsReflecting(DEFAULT_POINTS_REFLECTING);
		setPointRadius(DEFAULT_POINT_RADIUS);
		setPointColor(DEFAULT_POINT_COLOR);
		setShowLines(DEFAULT_SHOW_LINES);
		setLinesReflecting(DEFAULT_LINES_REFLECTING);
		setTubeRadius(DEFAULT_TUBE_RADIUS);
		setLineColor(DEFAULT_LINE_COLOR);
		setShowFaces(DEFAULT_SHOW_FACES);
		setFacesReflecting(DEFAULT_FACES_REFLECTING);
		setFaceReflection(DEFAULT_FACE_REFLECTION);
		setFaceReflection(DEFAULT_LINE_REFLECTION);
		setFaceReflection(DEFAULT_POINT_REFLECTION);
		setFaceColor(DEFAULT_FACE_COLOR);
		setTransparencyEnabled(DEFAULT_TRANSPARENCY_ENABLED);
		setTransparency(DEFAULT_TRANSPARENCY);
		setFacesFlat(DEFAULT_FACES_FLAT);
	}

	
	@Override
	public void restorePreferences(Preferences prefs) {
		int r, g, b;
		// app panel
		setShowPoints(prefs.getBoolean("showPoints", DEFAULT_SHOW_POINTS));
		setPointsReflecting(prefs.getBoolean("pointsReflecting", DEFAULT_POINTS_REFLECTING));
		setPointRadius(prefs.getDouble("pointRadius", DEFAULT_POINT_RADIUS));
		r = prefs.getInt("pointColorRed", DEFAULT_POINT_COLOR.getRed());
		g = prefs.getInt("pointColorGreen", DEFAULT_POINT_COLOR.getGreen());
		b = prefs.getInt("pointColorBlue", DEFAULT_POINT_COLOR.getBlue());
		setPointColor(new Color(r,g,b));
		setShowLines(prefs.getBoolean("showLines", DEFAULT_SHOW_LINES));
		setLinesReflecting(prefs.getBoolean("linesReflecting", DEFAULT_LINES_REFLECTING));
		setTubeRadius(prefs.getDouble("tubeRadius", DEFAULT_TUBE_RADIUS));
		r = prefs.getInt("lineColorRed", DEFAULT_LINE_COLOR.getRed());
		g = prefs.getInt("lineColorGreen", DEFAULT_LINE_COLOR.getGreen());
		b = prefs.getInt("lineColorBlue", DEFAULT_LINE_COLOR.getBlue());
		setLineColor(new Color(r,g,b));
		setShowFaces(prefs.getBoolean("showFaces", DEFAULT_SHOW_FACES));
		setFacesReflecting(prefs.getBoolean("facesReflecting", DEFAULT_FACES_REFLECTING));
		setFaceReflection(prefs.getDouble("faceReflection", DEFAULT_FACE_REFLECTION));
		setLineReflection(prefs.getDouble("lineReflection", DEFAULT_LINE_REFLECTION));
		setPointReflection(prefs.getDouble("pointReflection", DEFAULT_POINT_REFLECTION));
		r = prefs.getInt("faceColorRed", DEFAULT_FACE_COLOR.getRed());
		g = prefs.getInt("faceColorGreen", DEFAULT_FACE_COLOR.getGreen());
		b = prefs.getInt("faceColorBlue", DEFAULT_FACE_COLOR.getBlue());
		setFaceColor(new Color(r,g,b));
		setTransparencyEnabled(prefs.getBoolean("transparencyEnabled", DEFAULT_TRANSPARENCY_ENABLED));
		setTransparency(prefs.getDouble("transparency", DEFAULT_TRANSPARENCY));
		setFacesFlat(prefs.getBoolean("facesFlat", DEFAULT_FACES_FLAT));

	}
}
