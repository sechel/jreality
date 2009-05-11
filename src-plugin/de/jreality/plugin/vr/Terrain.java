package de.jreality.plugin.vr;

import static de.jreality.geometry.BoundingBoxUtility.calculateBoundingBox;
import static de.jreality.geometry.BoundingBoxUtility.removeZeroExtends;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.swing.ColorPicker;

import de.jreality.geometry.QuadMeshFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAccessory;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.view.AlignedContent.ContentDelegate;
import de.jreality.plugin.view.ViewPreferences.ColorPickerModeChangedListener;
import de.jreality.plugin.vr.image.ImageHook;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.JSliderVR;
import de.jreality.ui.TextureInspector;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel.MinSizeGridBagLayout;

public class Terrain extends ContentAccessory implements ActionListener, ChangeListener, ColorPickerModeChangedListener {

	// maximal value of texture scale
	private static final double 
		MAXIMAL_TEXTURE_SCALE = 1;
	// ratio of maximal value and minimal value of texture scale
	private static final double 
		LOGARITHMIC_RANGE = 200;
	// default texture scale
	private static double 
		DEFAULT_TEXTURE_SCALE = .1;

	// Plug-ins
	private View 
		view = null;
	private CameraStand 
		cameraStand = null;
	private AlignedContent 
		alignedContent = null;
	private TerrainContentDelegate 
		contentDelegate = null;
	private ViewPreferences
		viewPreferences = null;
	
	// scene graph
	private SceneGraphComponent 
		terrain = new SceneGraphComponent("Terrain"),
		plane = new SceneGraphComponent("Terrain Plane");
	private Appearance 
		appearance = new Appearance("Terrain Appearance");
	private MirrorAppearance
		mirrorAppearance = new MirrorAppearance();
	
	// swing layout
	private JPanel 
		panel = new JPanel(),
		colorPanel = new JPanel();
	private JButton 
		closeButton = new JButton("<-- Back"),
		faceColorButton = new JButton("Color...");
	private JCheckBox 
		facesReflecting = new JCheckBox("Reflection"),
		reflectScene = new JCheckBox("Reflect Scene Content"),
		transparency = new JCheckBox("Transp."),
		visibleCheckBox = new JCheckBox("Visible");
	private JSliderVR 
		faceReflectionSlider = new JSliderVR(SwingConstants.HORIZONTAL, 0, 100, 0),
		transparencySlider = new JSliderVR(SwingConstants.HORIZONTAL, 0, 100, 1);
	private ColorPicker 
		faceColorChooser = new ColorPicker(false, false);
	private TextureInspector 
		textureInspector = new TextureInspector();
	private ShrinkPanel 
		textureShrinker = new ShrinkPanel("Terrain Texture");

	private HashMap<String, String> 
		textures = new HashMap<String, String>();
	

	public Terrain() {
		appearance.setAttribute(CommonAttributes.EDGE_DRAW, false);
		appearance.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		terrain.setAppearance(appearance);

		MatrixBuilder.euclidean().rotateX(Math.PI/2).assignTo(plane);
		plane.setGeometry(bigMesh(50, 50, 2000));
		plane.getGeometry().setGeometryAttributes("infinite plane", Boolean.TRUE);
		PickUtility.assignFaceAABBTrees(plane);
		terrain.addChild(plane);

		textures.put("2 Grid", "textures/grid.jpeg");
		textures.put("3 Black Grid", "textures/gridBlack.jpg");
		textures.put("4 Tiles", "textures/recycfloor1_clean2.png");
		textures.put("5 Rust","textures/outfactory3.png");
		textures.put("1 None", null);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2, 2, 2, 2);
		c.fill = BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridwidth = REMAINDER;
		c.anchor = WEST;
		colorPanel.setLayout(new GridBagLayout());
		faceColorChooser.setPreferredSize(new Dimension(220, 230));
		colorPanel.add(faceColorChooser, c);
		c.fill = VERTICAL;
		colorPanel.add(closeButton, c);
		
		// panel
		panel.setLayout(new MinSizeGridBagLayout());
		c.fill = BOTH;

		visibleCheckBox.setSelected(true);
		c.weightx = 0.0;
		c.gridwidth = RELATIVE;
		panel.add(visibleCheckBox, c);
		c.weightx = 1.0;
		c.gridwidth = REMAINDER;
		panel.add(faceColorButton, c);
		
		textureInspector.setMaximalTextureScale(MAXIMAL_TEXTURE_SCALE);
		textureInspector.setLogarithmicRange(LOGARITHMIC_RANGE);
		textureInspector.setTextureScale(DEFAULT_TEXTURE_SCALE );
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridwidth = REMAINDER;
		textureShrinker.setHeaderColor(new Color(0.8f, 0.8f, 0.5f));
		textureShrinker.setShrinked(true);
		textureShrinker.setLayout(new GridLayout());
		textureShrinker.add(textureInspector);
		panel.add(textureShrinker, c);
		
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = RELATIVE;
		panel.add(facesReflecting, c);
		c.weightx = 1.0;
		c.gridwidth = REMAINDER;
		panel.add(faceReflectionSlider, c);
		
		c.weightx = 1.0;
		c.gridwidth = REMAINDER;	
		panel.add(reflectScene, c);
//		reflectScene.setEnabled(false);
		reflectScene.setToolTipText("Coming soon...");
		
		c.weightx = 0.0;
		c.gridwidth = RELATIVE;
		panel.add(transparency, c);
		c.weightx = 1.0;
		c.gridwidth = REMAINDER;
		panel.add(transparencySlider, c);


		// set defaults
		textureInspector.setTexture("Black Grid");
		textureInspector.setTextureScale(.5);
		setFaceColor(Color.white);
		setInitialPosition(SHRINKER_RIGHT);
		setFacesReflecting(true);
		setFaceReflection(.5);
		setTransparencyEnabled(false);
		setTransparency(.5);
		
		closeButton.addActionListener(this);
		visibleCheckBox.addActionListener(this);
		facesReflecting.addActionListener(this);
		transparency.addActionListener(this);
		faceColorButton.addActionListener(this);
		reflectScene.addActionListener(this);
		faceReflectionSlider.addChangeListener(this);
		transparencySlider.addChangeListener(this);
		faceColorChooser.getColorPanel().addChangeListener(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (closeButton == s) {
			switchTo(panel);
		} else
		if (visibleCheckBox == s) {
			updateVisible();
		} else 
		if (facesReflecting == s) {
			updateFacesReflecting();
		} else 
		if (transparency == s) {
			updateTransparencyEnabled();
		} else 
		if (faceColorButton == s) {
			switchTo(colorPanel);
		} else
		if (reflectScene == s) {
			updateReflectSceneContent();
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();
		if (faceReflectionSlider == s) {
			updateFaceReflection();
		} else 
		if (transparencySlider == s) {
			updateTransparency();
		} else 
		if (faceColorChooser.getColorPanel() == s) {
			updateFaceColor();
		}
	}
	
	public void colorPickerModeChanged(int mode) {
		faceColorChooser.setMode(mode);		
	}

	
	public TextureInspector getTextureInspector() {
		return textureInspector;
	}
	
	public JPanel getPanel() {
		return panel;
	}

	public boolean isFacesReflecting() {
		return facesReflecting.isSelected();
	}

	public void setFacesReflecting(boolean b) {
		facesReflecting.setSelected(b);
	}
	
	public void setReflectSceneContent(boolean b) {
		reflectScene.setSelected(b);
	}
	
	
	private void updateFacesReflecting() {
		if (isFacesReflecting()) {
			updateFaceReflection();
		} else {
			appearance.setAttribute(
					"polygonShader.reflectionMap:blendColor",
					new Color(1f, 1f, 1f, 0f)
			);
		}
	}
	
	public double getFaceReflection() {
		return .01 * faceReflectionSlider.getValue();
	}

	public void setFaceReflection(double d) {
		faceReflectionSlider.setValue((int)(100*d));
	}

	public boolean isReflectSceneContent() {
		return reflectScene.isSelected();
	}
	
	
	private void updateFaceReflection() {
		if (isFacesReflecting()) {
			appearance.setAttribute(
					"polygonShader.reflectionMap:blendColor",
					new Color(1f, 1f, 1f, (float) getFaceReflection())
			);
		}
	}

	public Color getFaceColor() {
		return faceColorChooser.getColor();
	}

	public void setFaceColor(Color c) {
		faceColorChooser.setColor(c);
	}
	
	private void updateFaceColor() {
		if (appearance != null) {
			appearance.setAttribute(
					CommonAttributes.POLYGON_SHADER + "." +
					CommonAttributes.DIFFUSE_COLOR,
					getFaceColor()
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
	}
	
	private void updateReflectSceneContent() {
		boolean reflect = reflectScene.isSelected();
		if (reflect) {
			terrain.setAppearance(mirrorAppearance);
			
		} else {
			terrain.setAppearance(appearance);
		}
	}

	private void switchTo(JComponent content) {
		shrinkPanel.removeAll();
		shrinkPanel.add(content);
		shrinkPanel.revalidate();
		shrinkPanel.repaint();
	}

	public SceneGraphComponent getSceneGraphComponent() {
		return terrain;
	}

	public boolean isVisible() {
		return visibleCheckBox.isSelected();
	}

	public void setVisible(boolean b) {
		visibleCheckBox.setSelected(b);
		updateVisible();
	}
	
	private void updateVisible() {
		terrain.setVisible(isVisible());
	}

	@Override
	public void install(Controller c) throws Exception {

		super.install(c);
		
		// scene
		view = c.getPlugin(View.class);
		view.getSceneRoot().addChild(terrain);
		
		// cameraStand
		cameraStand = c.getPlugin(CameraStand.class);
		MatrixBuilder.euclidean().translate(
				0,
				1.7,
				0
		).rotateX(
				Math.toRadians(10)
		).assignTo(cameraStand.getCameraComponent());
		
		// alignedContent
		alignedContent = c.getPlugin(AlignedContent.class);
		contentDelegate = new TerrainContentDelegate();
		alignedContent.setContentDelegate(contentDelegate);

		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(panel);
		
		viewPreferences = c.getPlugin(ViewPreferences.class);
		viewPreferences.addColorPickerChangedListener(this);
		faceColorChooser.getColorPanel().setMode(viewPreferences.getColorPickerMode());
		
		
		textureInspector.setAppearance(appearance);
		updateFaceColor();
		updateFacesReflecting();
		updateFaceReflection();
		updateTransparencyEnabled();
		updateTransparency();
		
		alignedContent.contentChanged();
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		alignedContent.setContentDelegate(null);
		view.getSceneRoot().removeChild(terrain);
		viewPreferences.removeColorPickerChangedListener(this);
		super.uninstall(c);
	}
	
	
	public static class TerrainContentDelegate implements ContentDelegate {

		private double verticalOffset = .2;
		private double scale = 1;
		private AlignedContent alignedContent;
		private double contentSize = 20;
		private Rectangle3D bounds;

		public double getContentSize() {
			return contentSize;
		}

		public void setContentSize(double contentSize) {
			this.contentSize = contentSize;
		}

		public double getScale() {
			return scale;
		}

		public void setAlignedContent(AlignedContent alignedContent) {
			this.alignedContent = alignedContent;
		}

		public void contentChanged() {
			alignContent(false);
		}

		
		public void alignContent(boolean fire) {
			try {
				bounds = calculateBoundingBox(alignedContent.getContent());
			} catch (Exception e) {
				return;
			}
			removeZeroExtends(bounds);
			double[] e = bounds.getExtent();
			double[] center = bounds.getCenter();
			double objectSize = Math.max(Math.max(e[0], e[1]), e[2]);
			scale = contentSize/objectSize;
			center[0] *= -scale;
			center[1] *= -scale;
			center[2] *= -scale;
			Matrix matrix = MatrixBuilder.euclidean().scale(
					scale
			).translate(
					center
			).getMatrix();
			matrix.assignTo(alignedContent.getScalingComponent());
			
			// translate contentComponent
			bounds = bounds.transformByMatrix(
					bounds,
					matrix.getArray()
			);
			center = bounds.getCenter();
			Matrix m = MatrixBuilder.euclidean().translate(
					-center[0], 
					-bounds.getMinY() + verticalOffset,
					-center[2]
			).getMatrix();
			m.assignTo(alignedContent.getTransformationComponent());
			bounds = bounds.transformByMatrix(
					bounds,
					m.getArray()
			);
		}
		
		public double getVerticalOffset() {
			return verticalOffset;
		}

		public void setVerticalOffset(double verticalOffset) {
			this.verticalOffset = verticalOffset;
		}

		public Rectangle3D getBounds() {
			Rectangle3D boundingBox = new Rectangle3D();
			bounds.copyInto(boundingBox);
			return boundingBox;
		}
	}

	public Appearance getAppearance() {
		return appearance;
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Terrain";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("world.png");
		return info; 
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		setFaceColor(c.getProperty(getClass(), "faceColor", getFaceColor()));
		setTransparency(c.getProperty(getClass(), "transparency", getTransparency()));
		setTransparencyEnabled(c.getProperty(getClass(), "transparencyEnabled", isTransparencyEnabled()));
		setFacesReflecting(c.getProperty(getClass(), "facesReflecting", isFacesReflecting()));
		setFaceReflection(c.getProperty(getClass(), "faceReflection", getFaceReflection()));
		setReflectSceneContent(c.getProperty(getClass(), "reflectSceneContent", isReflectSceneContent()));
		textureInspector.setTextures(c.getProperty(getClass(), "textures", textures));
		textureInspector.setTexture(c.getProperty(getClass(), "texture", textureInspector.getTexture()));
		textureInspector.setTextureScale(c.getProperty(getClass(), "textureScale", textureInspector.getTextureScale()));
		setVisible(c.getProperty(getClass(), "visible", isVisible()));
		super.restoreStates(c);
	}

	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "textures", textureInspector.getTextures());
		c.storeProperty(getClass(), "texture", textureInspector.getTexture());
		c.storeProperty(getClass(), "textureScale", textureInspector.getTextureScale());
		c.storeProperty(getClass(), "visible", isVisible());
		c.storeProperty(getClass(), "faceColor", getFaceColor());
		c.storeProperty(getClass(), "transparency", getTransparency());
		c.storeProperty(getClass(), "transparencyEnabled", isTransparencyEnabled());
		c.storeProperty(getClass(), "facesReflecting", isFacesReflecting());
		c.storeProperty(getClass(), "faceReflection", getFaceReflection());
		c.storeProperty(getClass(), "reflectSceneContent", isReflectSceneContent());
		c.storeProperty(getClass(), "visible", isVisible());
		super.storeStates(c);
	}
	
	
	@Override
	public void mainUIChanged(String uiClass) {
		super.mainUIChanged(uiClass);
		if (textureInspector != null) {
			SwingUtilities.updateComponentTreeUI(textureInspector);
		}
		if (colorPanel != null) {
			SwingUtilities.updateComponentTreeUI(colorPanel);
		}
	}
	
	
	private static IndexedFaceSet bigMesh(int discretization, double cameraHeight, double size) {
		int n = discretization;
		QuadMeshFactory factory = new QuadMeshFactory();
		factory.setULineCount(n);
		factory.setVLineCount(n);
		factory.setGenerateEdgesFromFaces(true);
		factory.setGenerateTextureCoordinates(false);
		double totalAngle = Math.atan(size/cameraHeight);
		double dt = 2 * totalAngle/(n-1);
		double[] normal = new double[]{0,0,-1};
		double[][] normals = new double[n*n][];
		Arrays.fill(normals, normal);
		
		double[][][] coords = new double[n][n][3];

		for (int i=0; i<n; i++) {
			double y = cameraHeight * Math.tan(-totalAngle + i * dt);
			for (int j=0; j<n; j++) {
				coords[i][j][0] = cameraHeight * Math.tan(-totalAngle + j * dt);
				coords[i][j][1] = y;
			}
		}
		
		double[][][] texCoords = new double[n][n][2];
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				texCoords[i][j][0] = coords[i][j][0];
				texCoords[i][j][1] = coords[i][j][1];
			}
		}
		
		factory.setVertexCoordinates(coords);
		factory.setVertexNormals(normals);
		factory.setVertexTextureCoordinates(texCoords);
		factory.update();
		
		return factory.getIndexedFaceSet();
	}
	
	
	
	@Override
	public String getHelpDocument() {
		return "Terrain.html";
	}
	
	@Override
	public Class<?> getHelpHandle() {
		return getClass();
	}

	@Override
	public SceneGraphComponent getTriggerComponent() {
		System.out.println("Terrain.getTriggerComponent(): "+terrain);
		return terrain;
	}
	
	
}
