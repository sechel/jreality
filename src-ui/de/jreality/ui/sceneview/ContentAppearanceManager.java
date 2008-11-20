package de.jreality.ui.sceneview;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.vr.AppearancePanel;

public class ContentAppearanceManager implements ChangeListener {
	
	public static final boolean DEFAULT_SHOW_POINTS = false;
	public static final boolean DEFAULT_POINTS_REFLECTING = false;
	public static final double DEFAULT_POINT_RADIUS = .4;
	public static final Color DEFAULT_POINT_COLOR = Color.blue;
	public static final boolean DEFAULT_SHOW_LINES = false;
	public static final boolean DEFAULT_LINES_REFLECTING = false;
	public static final double DEFAULT_TUBE_RADIUS = .3;
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
	
	private SceneView sceneView;
	private ContentManager contentManager;
	private Appearance contentAppearance;
	private SceneGraphComponent contentParent;
	private AppearancePanel appearancePanel;

	public AppearancePanel getAppearancePanel() {
		return appearancePanel;
	}

	public ContentAppearanceManager() {
		appearancePanel = new AppearancePanel();
		contentAppearance = new Appearance();
		appearancePanel.setAppearance(contentAppearance);
		restoreDefaults();
	}
	
	public void install(SceneView sceneView, ContentManager contentManager) {
		this.sceneView = sceneView;
		contentParent  = sceneView.getContentParent();
		contentParent.setAppearance(contentAppearance);
		sceneView.addChangeListener(this);
		this.contentManager = contentManager;
		contentManager.addChangeListener(this);
	}

	public void unInstall(SceneView sceneView, ContentManager contentManager) {
		sceneView.removeChangeListener(this);
		contentManager.removeChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sceneView) {
			if (sceneView.getContentParent() != contentParent) {
				contentParent.setAppearance(null);
				contentParent = sceneView.getContentParent();
				contentParent.setAppearance(contentAppearance);
			}
		}
		if (e.getSource() == contentManager) {
			double scale = contentManager.getContentScale();
			if (scale != appearancePanel.getObjectScale()) {
				appearancePanel.setObjectScale(scale);
			}
		}
	}
	
	public void restoreDefaults() {
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
		appearancePanel.setTubes(DEFAULT_TUBES);
		appearancePanel.setSpheres(DEFAULT_SPHERES);
	}
}
