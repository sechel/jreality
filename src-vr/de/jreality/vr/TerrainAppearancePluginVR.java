package de.jreality.vr;

import java.awt.Color;
import java.util.prefs.Preferences;

import javax.swing.JPanel;

public class TerrainAppearancePluginVR extends AbstractPluginVR {

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
	private static final boolean DEFAULT_FACES_REFLECTING = false;
	private static final double DEFAULT_FACE_REFLECTION = .5;
	private static final double DEFAULT_LINE_REFLECTION = .5;
	private static final double DEFAULT_POINT_REFLECTION = .5;
	private static final Color DEFAULT_FACE_COLOR = Color.white;
	private static final boolean DEFAULT_TRANSPARENCY_ENABLED = false;
	private static final double DEFAULT_TRANSPARENCY = .7;
	private static final boolean DEFAULT_FACES_FLAT = false;
	private AppearancePanel appearancePanel;

	public TerrainAppearancePluginVR() {
		super("terrain-app");
		makeAppTab();
	}
	
	private void makeAppTab() {
		appearancePanel = new AppearancePanel();
	}
	
	@Override
	public void setViewerVR(ViewerVR vvr) {
		super.setViewerVR(vvr);
		appearancePanel.setAppearance(getViewerVR().getTerrainAppearance());
	}
	
	@Override
	public JPanel getPanel() {
		return appearancePanel;
	}
	
	@Override
	public void environmentChanged() {
		appearancePanel.setSkyBox(getViewerVR().getEnvironment().getCubeMap());
	}
	
	@Override
	public void terrainChanged() {
		appearancePanel.setObjectScale(getViewerVR().getTerrainScale());
	}

	@Override
	public void storePreferences(Preferences prefs) {
		// app panel
		prefs.putBoolean("terrain.showPoints", appearancePanel.isShowPoints());
		prefs.putDouble("terrain.pointRadius", appearancePanel.getPointRadius());
		Color c = appearancePanel.getPointColor();
		prefs.putInt("terrain.pointColorRed", c.getRed());
		prefs.putInt("terrain.pointColorGreen", c.getGreen());
		prefs.putInt("terrain.pointColorBlue", c.getBlue());
		prefs.putBoolean("terrain.showLines", appearancePanel.isShowLines());
		prefs.putDouble("terrain.tubeRadius", appearancePanel.getTubeRadius());
		c = appearancePanel.getLineColor();
		prefs.putInt("terrain.lineColorRed", c.getRed());
		prefs.putInt("terrain.lineColorGreen", c.getGreen());
		prefs.putInt("terrain.lineColorBlue", c.getBlue());
		prefs.putBoolean("terrain.showFaces", appearancePanel.isShowFaces());
		prefs.putDouble("terrain.faceReflection", appearancePanel.getFaceReflection());
		prefs.putDouble("terrain.lineReflection", appearancePanel.getLineReflection());
		prefs.putDouble("terrain.pointReflection", appearancePanel.getPointReflection());
		c = appearancePanel.getFaceColor();
		prefs.putInt("terrain.faceColorRed", c.getRed());
		prefs.putInt("terrain.faceColorGreen", c.getGreen());
		prefs.putInt("terrain.faceColorBlue", c.getBlue());
		prefs.putBoolean("terrain.transparencyEnabled", appearancePanel.isTransparencyEnabled());
		prefs.putDouble("terrain.transparency", appearancePanel.getTransparency());
		prefs.putBoolean("terrain.facesFlat", appearancePanel.isFacesFlat());
	
	}

	@Override
	public void restoreDefaults() {
		// app panel
		appearancePanel.setShowPoints(DEFAULT_SHOW_POINTS);
		appearancePanel.setPointsReflecting(DEFAULT_POINTS_REFLECTING);
		appearancePanel.setPointRadius(DEFAULT_POINT_RADIUS);
		appearancePanel.setPointColor(DEFAULT_POINT_COLOR);
		appearancePanel.setShowLines(DEFAULT_SHOW_LINES);
		appearancePanel.setLinesReflecting(DEFAULT_LINES_REFLECTING);
		appearancePanel.setTubeRadius(DEFAULT_TUBE_RADIUS);
		appearancePanel.setLineColor(DEFAULT_LINE_COLOR);
		appearancePanel.setShowFaces(DEFAULT_SHOW_FACES);
		appearancePanel.setFacesReflecting(DEFAULT_FACES_REFLECTING);
		appearancePanel.setFaceReflection(DEFAULT_FACE_REFLECTION);
		appearancePanel.setFaceReflection(DEFAULT_LINE_REFLECTION);
		appearancePanel.setFaceReflection(DEFAULT_POINT_REFLECTION);
		appearancePanel.setFaceColor(DEFAULT_FACE_COLOR);
		appearancePanel.setTransparencyEnabled(DEFAULT_TRANSPARENCY_ENABLED);
		appearancePanel.setTransparency(DEFAULT_TRANSPARENCY);
		appearancePanel.setFacesFlat(DEFAULT_FACES_FLAT);
	}

	
	@Override
	public void restorePreferences(Preferences prefs) {
		int r, g, b;
		// app panel
		appearancePanel.setShowPoints(prefs.getBoolean("terrain.showPoints", DEFAULT_SHOW_POINTS));
		appearancePanel.setPointsReflecting(prefs.getBoolean("terrain.pointsReflecting", DEFAULT_POINTS_REFLECTING));
		appearancePanel.setPointRadius(prefs.getDouble("terrain.pointRadius", DEFAULT_POINT_RADIUS));
		r = prefs.getInt("terrain.pointColorRed", DEFAULT_POINT_COLOR.getRed());
		g = prefs.getInt("terrain.pointColorGreen", DEFAULT_POINT_COLOR.getGreen());
		b = prefs.getInt("terrain.pointColorBlue", DEFAULT_POINT_COLOR.getBlue());
		appearancePanel.setPointColor(new Color(r,g,b));
		appearancePanel.setShowLines(prefs.getBoolean("terrain.showLines", DEFAULT_SHOW_LINES));
		appearancePanel.setLinesReflecting(prefs.getBoolean("terrain.linesReflecting", DEFAULT_LINES_REFLECTING));
		appearancePanel.setTubeRadius(prefs.getDouble("terrain.tubeRadius", DEFAULT_TUBE_RADIUS));
		r = prefs.getInt("terrain.lineColorRed", DEFAULT_LINE_COLOR.getRed());
		g = prefs.getInt("terrain.lineColorGreen", DEFAULT_LINE_COLOR.getGreen());
		b = prefs.getInt("terrain.lineColorBlue", DEFAULT_LINE_COLOR.getBlue());
		appearancePanel.setLineColor(new Color(r,g,b));
		appearancePanel.setShowFaces(prefs.getBoolean("terrain.showFaces", DEFAULT_SHOW_FACES));
		appearancePanel.setFacesReflecting(prefs.getBoolean("terrain.facesReflecting", DEFAULT_FACES_REFLECTING));
		appearancePanel.setFaceReflection(prefs.getDouble("terrain.faceReflection", DEFAULT_FACE_REFLECTION));
		appearancePanel.setLineReflection(prefs.getDouble("terrain.lineReflection", DEFAULT_LINE_REFLECTION));
		appearancePanel.setPointReflection(prefs.getDouble("terrain.pointReflection", DEFAULT_POINT_REFLECTION));
		r = prefs.getInt("terrain.faceColorRed", DEFAULT_FACE_COLOR.getRed());
		g = prefs.getInt("terrain.faceColorGreen", DEFAULT_FACE_COLOR.getGreen());
		b = prefs.getInt("terrain.faceColorBlue", DEFAULT_FACE_COLOR.getBlue());
		appearancePanel.setFaceColor(new Color(r,g,b));
		appearancePanel.setTransparencyEnabled(prefs.getBoolean("terrain.transparencyEnabled", DEFAULT_TRANSPARENCY_ENABLED));
		appearancePanel.setTransparency(prefs.getDouble("terrain.transparency", DEFAULT_TRANSPARENCY));
		appearancePanel.setFacesFlat(prefs.getBoolean("terrain.facesFlat", DEFAULT_FACES_FLAT));
	}
}
