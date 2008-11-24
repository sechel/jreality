package de.jreality.ui.sceneview;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ContentAppearance implements ChangeListener {
	
	public static final boolean DEFAULT_SHOW_POINTS = false;
	public static final boolean DEFAULT_POINTS_REFLECTING = false;
	public static final double DEFAULT_POINT_RADIUS = .6;
	public static final Color DEFAULT_POINT_COLOR = Color.blue;
	public static final boolean DEFAULT_SHOW_LINES = false;
	public static final boolean DEFAULT_LINES_REFLECTING = false;
	public static final double DEFAULT_TUBE_RADIUS = .5;
	public static final Color DEFAULT_LINE_COLOR = Color.red;
	public static final boolean DEFAULT_SHOW_FACES = true;
	public static final boolean DEFAULT_FACES_REFLECTING = true;
	public static final double DEFAULT_FACE_REFLECTION = .7;
	public static final double DEFAULT_LINE_REFLECTION = .7;
	public static final double DEFAULT_POINT_REFLECTION = .7;
	public static final Color DEFAULT_FACE_COLOR = Color.white;
	public static final boolean DEFAULT_TRANSPARENCY_ENABLED = false;
	public static final double DEFAULT_TRANSPARENCY = .7;
	public static final boolean DEFAULT_FACES_FLAT = false;
	public static final boolean DEFAULT_TUBES = true;
	public static final boolean DEFAULT_SPHERES = true;
	
	private Content content;
	private AppearanceInspector appearanceInspector;

	public AppearanceInspector getPanel() {
		return appearanceInspector;
	}

	public ContentAppearance(Content content) {
		this.content = content;
		appearanceInspector = new AppearanceInspector(
				content.getContentAppearance(),
				content.getScaledAppearance()
		);
		appearanceInspector.setAppearance(content.getContentAppearance());
		restoreDefaults();

		content.addChangeListener(this);
	}

	public void unInstall() {
		content.removeChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == content) {
			
			double scale = content.getContentScale();
			if (scale != appearanceInspector.getObjectScale()) {
				appearanceInspector.setObjectScale(scale);
			}
		}
	}
	
	public void restoreDefaults() {
		appearanceInspector.setShowPoints(DEFAULT_SHOW_POINTS);
		appearanceInspector.setPointsReflecting(DEFAULT_POINTS_REFLECTING);
		appearanceInspector.setPointRadius(DEFAULT_POINT_RADIUS);
		appearanceInspector.setPointColor(DEFAULT_POINT_COLOR);
		appearanceInspector.setShowLines(DEFAULT_SHOW_LINES);
		appearanceInspector.setLinesReflecting(DEFAULT_LINES_REFLECTING);
		appearanceInspector.setTubeRadius(DEFAULT_TUBE_RADIUS);
		appearanceInspector.setLineColor(DEFAULT_LINE_COLOR);
		appearanceInspector.setShowFaces(DEFAULT_SHOW_FACES);
		appearanceInspector.setFacesReflecting(DEFAULT_FACES_REFLECTING);
		appearanceInspector.setFaceReflection(DEFAULT_FACE_REFLECTION);
		appearanceInspector.setLineReflection(DEFAULT_LINE_REFLECTION);
		appearanceInspector.setPointReflection(DEFAULT_POINT_REFLECTION);
		appearanceInspector.setFaceColor(DEFAULT_FACE_COLOR);
		appearanceInspector.setTransparencyEnabled(DEFAULT_TRANSPARENCY_ENABLED);
		appearanceInspector.setTransparency(DEFAULT_TRANSPARENCY);
		appearanceInspector.setFacesFlat(DEFAULT_FACES_FLAT);
		appearanceInspector.setTubes(DEFAULT_TUBES);
		appearanceInspector.setSpheres(DEFAULT_SPHERES);
	}
}
