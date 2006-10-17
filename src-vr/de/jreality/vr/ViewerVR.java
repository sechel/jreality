package de.jreality.vr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileSystemView;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.reader.Readers;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.swing.ScenePanel;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.DuplicateTriplyPeriodicTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.RotateTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.ui.beans.SimpleColorChooser;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;

public class ViewerVR {
	
	protected static final double PI2 = Math.PI / 2;
	
	// width of control panel in meters
	private static final double PANEL_WIDTH = 1;
	
	// distance of all panels from avatar in meters
	private static final double PANEL_Z_OFFSET = -2.2;
	
	// height of upper edge of control panel in meters
	private static final double PANEL_ABOVE_GROUND = 1.8;
	
	// height of upper edge of file browser panel in meters
	private static final int FILE_CHOOSER_ABOVE_GROUND = 2;
	
	// width of file browser panel in meters
	private static final int FILE_CHOOSER_PANEL_WIDTH = 2;

	private static final double DEFAULT_POINT_RADIUS = .4;
	
	private static final double DEFAULT_TUBE_RADIUS = .3;
	
	// maximal radius of tubes or points compared to content size
	private static final double MAX_RADIUS = 0.1;
	
	// ratio of maximal versus minimal value for logarithmic sliders
	private static final int LOGARITHMIC_RANGE = 200;
	
	// maximal horizontal diameter of content in meters
	private static final double MAX_CONTENT_SIZE = 100;
	
	// default value of texture scale
	private static final double DEFAULT_TEX_SCALE = 20;
	
	// maximal value of texture scale
	private static final double MAX_TEX_SCALE = 400;
	
	// ratio of maximal value and minimal value of texture scale
	private static final double TEX_SCALE_RANGE = 400;

	private static final Object DARK_DIFFUSE_COLOR = new Color(128,128,0);
	
	// texture of content
	private Texture2D tex;

	// root of scene graph
	private SceneGraphComponent sceneRoot = new SceneGraphComponent(),
			sceneNode = new SceneGraphComponent(),
//			reflectedSceneNode = new SceneGraphComponent(),
			avatarNode = new SceneGraphComponent(),
			camNode = new SceneGraphComponent(),
			lightNode = new SceneGraphComponent(), terrainNode;

	private Tool rotateTool = new RotateTool(), dragTool = new DraggingTool();

	private SceneGraphComponent currentContent;

	private HashMap<String, Integer> exampleIndices = new HashMap<String, Integer>();
	private HashMap<String, Integer> textureIndices = new HashMap<String, Integer>();

	private Appearance terrainAppearance = new Appearance(),
			rootAppearance = new Appearance(),
			contentAppearance = new Appearance();

	private DirectionalLight light = new DirectionalLight();

	private SceneGraphPath cameraPath, avatarPath, emptyPickPath;

	private double diam = 22, offset = -.5;

	private JPanel fileChooserPanel;
	private JFileChooser texFileChooser;
	private SimpleColorChooser colorChooser;

	private JPanel colorChooserPanel;

	private JSlider sizeSlider;

	private JSlider groundSlider;

	private ScenePanel sp;
	private JSlider tubeRadiusSlider;
	private JSlider pointRadiusSlider;
	private JSlider reflectionSlider;
	private CubeMap cm;
	private double objectScale=1;
	private JCheckBox rotate;
	private JCheckBox drag;
	private Container defaultPanel;
	private String currentColor;
	private JSlider texScaleSlider;
	private Landscape landscape;

	private AABBPickSystem pickSystem;
	private double[][] terrainPoints;

	private IndexedFaceSet terrain;
	private IndexedFaceSet flatTerrain = Primitives.plainQuadMesh(3, 3, 100, 100);

	private double[][] flatTerrainPoints;

	private boolean flat;

	private JTabbedPane geomTabs;

	private JTabbedPane appearanceTabs;
	
	public ViewerVR() throws IOException {

		boolean portal = "portal".equals(System
				.getProperty("de.jreality.scene.tool.Config"));

		sceneRoot.setName("root");
		sceneNode.setName("scene");
		avatarNode.setName("avatar");
		camNode.setName("camNode");
		lightNode.setName("sun");
		sceneRoot.addChild(sceneNode);

		rootAppearance.setName("root app");
		ShaderUtility.createRootAppearance(rootAppearance);
		rootAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.AMBIENT_COEFFICIENT, 0.07);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.AMBIENT_COEFFICIENT, 0.03);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		rootAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		Color downColor = new Color(0,0,0);
		Color upColor = new Color(80,80,120);
		rootAppearance.setAttribute(CommonAttributes.BACKGROUND_COLORS, new Color[]{
				upColor, upColor, downColor, downColor
		});
		sceneRoot.setAppearance(rootAppearance);

		Camera cam = new Camera();
		cam.setNear(0.01);
		cam.setFar(1500);

		if (portal) {
			cam.setOnAxis(false);
			cam.setStereo(true);
		}

		// lights
		light.setIntensity(2);
		lightNode.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 1 }).assignTo(lightNode);
		sceneRoot.addChild(lightNode);

		// prepare paths
		sceneRoot.addChild(avatarNode);
		avatarNode.addChild(camNode);
		camNode.setCamera(cam);
		cameraPath = new SceneGraphPath();
		cameraPath.push(sceneRoot);
		emptyPickPath = cameraPath.pushNew(sceneNode);
		cameraPath.push(avatarNode);
		cameraPath.push(camNode);
		avatarPath = cameraPath.popNew();
		cameraPath.push(cam);

		MatrixBuilder.euclidean().translate(0, 1.7, 0).rotateX(5*Math.PI/180).assignTo(camNode);

		// add tools
		ShipNavigationTool shipNavigationTool = new ShipNavigationTool();
		avatarNode.addTool(shipNavigationTool);
		if (portal)
			shipNavigationTool.setPollingDevice(false);

		if (!portal)
			camNode.addTool(new HeadTransformationTool());
		else {
			try {
				Tool t = (Tool) Class.forName(
						"de.jreality.tools.PortalHeadMoveTool").newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, Color.blue);
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, Color.white);
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.DIFFUSE_COLOR, Color.red);
		contentAppearance.setAttribute("showLines", false);
		contentAppearance.setAttribute("showPoints", false);
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		sceneNode.setAppearance(contentAppearance);

		sceneRoot.addTool(new PickShowTool(null, 0.005));
 
		// prepare  terrain
		terrainNode = Readers
				.read(Input.getInput("de/jreality/vr/terrain.3ds"))
				.getChildComponent(0);
		terrain = (IndexedFaceSet) terrainNode.getGeometry();
		MatrixBuilder.euclidean().scale(1 / 3.).translate(0, 7, 0).assignTo(terrainNode);
		terrainNode.setName("terrain");
		IndexedFaceSet terrainGeom = (IndexedFaceSet) terrainNode.getGeometry();
		terrainPoints = terrainGeom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int n = terrainPoints.length;
		for (int j=0; j<n; j++) {
			terrainPoints[j][0] /= 3;
			terrainPoints[j][1] /= 3;
			terrainPoints[j][1] += 7;
			terrainPoints[j][2] /= 3;
		}
		
		GeometryUtility.calculateAndSetNormals(terrainGeom);
		terrainGeom.setName("terrain Geometry");
		PickUtility.assignFaceAABBTree(terrainGeom);
		
		flatTerrainPoints = flatTerrain.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		n = flatTerrainPoints.length;
		for (int j=0; j<n; j++) {
			double y = flatTerrainPoints[j][1];
			//double z = flatTerrainPoints[j][2];
			flatTerrainPoints[j][1] = -8.5;
			flatTerrainPoints[j][2] = y;
		}
		flatTerrain.setVertexAttributes(Attribute.COORDINATES, new DoubleArrayArray.Array(flatTerrainPoints, 3));
		Random r = new Random();
		for (int j=0; j<n; j++) {			
			flatTerrainPoints[j][0] /= 3;
			flatTerrainPoints[j][0] += (-.5+r.nextDouble())*1E-1;
			flatTerrainPoints[j][1] = -1.5;
			flatTerrainPoints[j][2] /= 3;
			flatTerrainPoints[j][2] += (-.5+r.nextDouble())*1E-1;
		}
		
		GeometryUtility.calculateAndSetNormals(flatTerrain);
		flatTerrain.setName("flat terrain Geometry");
		PickUtility.assignFaceAABBTree(flatTerrain);
		
		terrainAppearance.setAttribute("showLines", false);
		terrainAppearance.setAttribute("showPoints", false);
		terrainAppearance.setAttribute("diffuseColor", Color.white);
		terrainNode.setAppearance(terrainAppearance);
		
//		reflectedSceneNode.setName("reflectedScene");
//		MatrixBuilder.euclidean().reflect(new double[]{0,1,0,0}).assignTo(reflectedSceneNode);
//		reflectedSceneNode.addChild(sceneNode);
//		sceneRoot.addChild(reflectedSceneNode);
		sceneRoot.addChild(terrainNode);
//		reflectedSceneNode.setVisible(false);
		
		
		// landscape
		landscape = new Landscape(System.getProperty("javaws.ViewerVR.landscape"));

		// swing widgets
		makeControlPanel();
		makeContentFileChooser();
		makeTextureFileChooser();
		makeColorChooser();
		
		updateLandscape();

		setAvatarPosition(0, landscape.isTerrainFlat() ? -.5:0, 28);
}

	private void makeControlPanel() {
		sp = new ScenePanel();
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.setZOffset(PANEL_Z_OFFSET);
		
		JTabbedPane tabs = new JTabbedPane();
		
		String os = System.getProperty("os.name");
		boolean macOS = os.equalsIgnoreCase("Mac OS X");
		if (macOS) {
			geomTabs = new JTabbedPane();
			appearanceTabs = new JTabbedPane();
			tabs.add("geometry", geomTabs);
			tabs.add("appearance", appearanceTabs);
		} else {
			geomTabs = tabs;
			appearanceTabs = tabs;
		}
		sp.getFrame().getContentPane().add(tabs);
		getTerrainNode().addTool(sp.getPanelTool());
		defaultPanel = sp.getFrame().getContentPane();
	}

	private void makeContentFileChooser() {
		this.fileChooserPanel = new JPanel(new BorderLayout());
		final JFileChooser fileChooser = FileLoaderDialog.createFileChooser();
		final JCheckBox smoothNormalsCheckBox = new JCheckBox("smooth normals");
		final JCheckBox removeAppsCheckBox = new JCheckBox("ignore appearances");
		JPanel checkBoxPanel = new JPanel(new FlowLayout());
		fileChooserPanel.add(BorderLayout.CENTER, fileChooser);
		checkBoxPanel.add(smoothNormalsCheckBox);
		checkBoxPanel.add(removeAppsCheckBox);
		fileChooserPanel.add(BorderLayout.SOUTH, checkBoxPanel);
		fileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = fileChooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION
							&& file != null) {
						SceneGraphComponent read = Readers.read(Input.getInput(file));
						SceneGraphComponent hack = new SceneGraphComponent();
						hack.addChild(read);
						hack.accept(new SceneGraphVisitor() {
							public void visit(SceneGraphComponent c) {
								if (removeAppsCheckBox.isSelected() && c.getAppearance() != null) c.setAppearance(null); 
								c.childrenWriteAccept(this, false, false, false, false, true,
										true);
							}
							public void visit(IndexedFaceSet i) {
								GeometryUtility.calculateAndSetNormals(i);
								if (smoothNormalsCheckBox.isSelected()) IndexedFaceSetUtility.assignSmoothVertexNormals(i, -1);
							}
						});
						hack.removeChild(read);
						setContent(read);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				smoothNormalsCheckBox.setSelected(false);
				removeAppsCheckBox.setSelected(false);
				switchToDefaultPanel();
			}
		});
	}

	private void makeTextureFileChooser() {
		FileSystemView view = FileSystemView.getFileSystemView();
		String texDir = ".";
		String dataDir = System.getProperty("jreality.data");
		if (dataDir!= null) texDir = dataDir+"/textures";
		File defaultDir = new File(texDir);
		texFileChooser = new JFileChooser(!defaultDir.exists() ? view.getHomeDirectory() : defaultDir, view);
		texFileChooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				File file = texFileChooser.getSelectedFile();
				try {
					if (ev.getActionCommand() == JFileChooser.APPROVE_SELECTION
							&& file != null) {
						ImageData img = ImageData.load(Input.getInput(file));
						tex = TextureUtility.createTexture(contentAppearance, "polygonShader", img, false);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				switchToDefaultPanel();
			}
		});
	}

	private void makeColorChooser() {
		colorChooser = new SimpleColorChooser();
		colorChooser.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				contentAppearance.setAttribute(currentColor, colorChooser.getColor());
			}
		});
		colorChooserPanel = new JPanel(new BorderLayout());
		colorChooserPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

		colorChooserPanel.add("Center", colorChooser);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchToDefaultPanel();
			}
		});
		buttonPanel.add(closeButton);
		colorChooserPanel.add("South", buttonPanel);
	}

	private void updateLandscape() {
		ImageData[] cubeMap = landscape.getCubeMap();
		Color upColor = landscape.getUpColor();
		Color downColor = landscape.getDownColor();
		
		String diffCol = CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR;
		Color currentDiffuseColor = (Color) contentAppearance.getAttribute(diffCol);
		if (cubeMap == null) {
			if (currentDiffuseColor.equals(Color.white)) {
				contentAppearance.setAttribute(diffCol, DARK_DIFFUSE_COLOR);
			}
		} else {
			if (currentDiffuseColor.equals(DARK_DIFFUSE_COLOR)) {
				contentAppearance.setAttribute(diffCol, Color.white);
			}
		}
		Geometry last = terrainNode.getGeometry();
		flat = landscape.isTerrainFlat();
		terrainNode.setGeometry(flat ? flatTerrain : terrain);
		if (last != terrainNode.getGeometry()) computeShadow();
		
		setSkyBox(cubeMap);
		
		ImageData terrainTex = landscape.getTerrainTexture();
		setTerrainTexture(
				terrainTex,
				landscape.getTerrainTextureScale()
		);

		rootAppearance.setAttribute(
				CommonAttributes.BACKGROUND_COLORS,
				upColor != null && downColor != null ?
						new Color[]{upColor, upColor, downColor, downColor} :
							Appearance.INHERITED
		);
		
			Matrix m = new Matrix(avatarNode.getTransformation());
			AABBPickSystem ps = new AABBPickSystem();
			ps.setSceneRoot(terrainNode);
			double[] pos = m.getColumn(3);
			double[] dest = pos.clone();
			dest[1]-=1.5;
			List<PickResult> picks = ps.computePick(pos, dest);
			if (picks.isEmpty()) {
				picks = ps.computePick(pos, new double[]{0,1,0,0});
			}
			if (!picks.isEmpty()) {
				setAvatarHeight(picks.get(0).getWorldCoordinates()[1]);
			}
	}
	
	public void addEnvTab() {
		landscape.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				updateLandscape();
			}
		});	
		
		JPanel envSelection = new JPanel(new BorderLayout());
		envSelection.setBorder(new EmptyBorder(20,20,0,0));
		envSelection.add(landscape.getSelectionComponent(), BorderLayout.CENTER);

		appearanceTabs.add("env", envSelection);
		sp.getFrame().pack();
	}

	public void addAppTab() {
		JPanel appearancePanel = new JPanel(new BorderLayout());
		Box appBox = new Box(BoxLayout.Y_AXIS);
		
		// lines
		Box lineBox = new Box(BoxLayout.Y_AXIS);
		lineBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box lineButtonBox = new Box(BoxLayout.X_AXIS);
		lineButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		final JCheckBox lines = new JCheckBox("lines");
		lines.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				contentAppearance.setAttribute("showLines", lines.isSelected());
			}
		});
		lineButtonBox.add(lines);
		JButton lineColorButton = new JButton("line color");
		lineColorButton.setMaximumSize(new Dimension(200, 20));
		lineColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToColorChooser(CommonAttributes.LINE_SHADER + "."
						+ CommonAttributes.DIFFUSE_COLOR);
			}
		});

		lineButtonBox.add(lineColorButton);
		lineBox.add(lineButtonBox);

		Box tubeRadiusBox = new Box(BoxLayout.X_AXIS);
		tubeRadiusBox.setBorder(new EmptyBorder(5, 5, 5, 0));
		JLabel tubeRadiusLabel = new JLabel("radius");
		tubeRadiusSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,
				(int) (DEFAULT_TUBE_RADIUS * 100));
		tubeRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTubeRadius(0.01 * tubeRadiusSlider.getValue());
			}
		});
		tubeRadiusBox.add(tubeRadiusLabel);
		tubeRadiusBox.add(tubeRadiusSlider);
		lineBox.add(tubeRadiusBox);

		appBox.add(lineBox);

		// points
		Box pointBox = new Box(BoxLayout.Y_AXIS);
		pointBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box pointButtonBox = new Box(BoxLayout.X_AXIS);
		pointButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		final JCheckBox points = new JCheckBox("points");
		points.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				contentAppearance.setAttribute("showPoints", points
						.isSelected());
			}
		});
		pointButtonBox.add(points);
		JButton pointColorButton = new JButton("point color");
		pointColorButton.setMaximumSize(new Dimension(200, 20));
		pointColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToColorChooser(CommonAttributes.POINT_SHADER + "."
						+ CommonAttributes.DIFFUSE_COLOR);
			}
		});
		pointButtonBox.add(pointColorButton);
		pointBox.add(pointButtonBox);

		Box pointRadiusBox = new Box(BoxLayout.X_AXIS);
		pointRadiusBox.setBorder(new EmptyBorder(5, 5, 5, 0));
		JLabel pointRadiusLabel = new JLabel("radius");
		pointRadiusSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,
				(int) (DEFAULT_POINT_RADIUS * 100));
		pointRadiusSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setPointRadius(0.01 * pointRadiusSlider.getValue());
			}
		});
		pointRadiusBox.add(pointRadiusLabel);
		pointRadiusBox.add(pointRadiusSlider);
		pointBox.add(pointRadiusBox);

		appBox.add(pointBox);

		// faces
		Box faceBox = new Box(BoxLayout.Y_AXIS);
		faceBox.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5),
				LineBorder.createGrayLineBorder()));
		Box faceButtonBox = new Box(BoxLayout.X_AXIS);
		faceButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		final JCheckBox faces = new JCheckBox("faces");
		faces.setSelected(true);
		faces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				contentAppearance.setAttribute("showFaces", faces.isSelected());
			}
		});
		faceButtonBox.add(faces);

		JButton faceColorButton = new JButton("face color");
		faceColorButton.setMaximumSize(new Dimension(200, 20));
		faceColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToColorChooser(CommonAttributes.POLYGON_SHADER + "."
						+ CommonAttributes.DIFFUSE_COLOR);
			}
		});
		faceButtonBox.add(faceColorButton);
		faceBox.add(faceButtonBox);

		Box reflectionBox = new Box(BoxLayout.X_AXIS);
		reflectionBox.setBorder(new EmptyBorder(5, 5, 5, 0));
		JLabel reflectionLabel = new JLabel("reflection");
		reflectionSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 60);
		reflectionSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setReflection(0.01 * reflectionSlider.getValue());
			}
		});
		reflectionBox.add(reflectionLabel);
		reflectionBox.add(reflectionSlider);
		faceBox.add(reflectionBox);

		appBox.add(faceBox);

		appearancePanel.add(appBox);
		appearanceTabs.add("app", appearancePanel);
		sp.getFrame().pack();
	}

	public void addAlignTab() {
		JPanel placementPanel = new JPanel(new BorderLayout());
		placementPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Box placementBox = new Box(BoxLayout.X_AXIS);
		Box sizeBox = new Box(BoxLayout.Y_AXIS);
		sizeBox.setBorder(new EmptyBorder(10, 5, 0, 5));
		JLabel sizeLabel = new JLabel("size");
		int sliderDiam = (int)(Math.log(diam*LOGARITHMIC_RANGE/MAX_CONTENT_SIZE)/Math.log(LOGARITHMIC_RANGE)*100);
		sizeSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, sliderDiam);
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double sliderDiam = 0.01 * sizeSlider.getValue();
				setDiam(Math.exp(Math.log(LOGARITHMIC_RANGE)*sliderDiam)/LOGARITHMIC_RANGE * MAX_CONTENT_SIZE);
				alignContent(diam, offset, null);
				if (!sizeSlider.getValueIsAdjusting()) {
					computeShadow();
				}
			}
		});
		sizeBox.add(sizeLabel);
		sizeBox.add(sizeSlider);
		Box groundBox = new Box(BoxLayout.Y_AXIS);
		groundBox.setBorder(new EmptyBorder(10, 5, 0, 5));
		JLabel groundLabel = new JLabel("level");
		groundSlider = new JSlider(SwingConstants.VERTICAL, -100, 100,
				(int) (offset * 100));
		groundSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setOffset(0.01 * groundSlider.getValue());
				alignContent(diam, offset, null);
				if (!groundSlider.getValueIsAdjusting()) {
					computeShadow();
				}
			}
		});
		groundBox.add(groundLabel);
		groundBox.add(groundSlider);

		URL imgURL = ViewerVR.class.getResource("rotleft.gif");
		ImageIcon rotateLeft = new ImageIcon(imgURL);
		imgURL = ViewerVR.class.getResource("rotright.gif");
		ImageIcon rotateRight = new ImageIcon(imgURL);

		JPanel rotateBox = new JPanel(new GridLayout(3, 3));
		rotateBox.setBorder(new EmptyBorder(20, 0, 20, 0));

		JButton xRotateLeft = new JButton(rotateLeft);
		xRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateX(
						-PI2).getMatrix());
				computeShadow();
			}
		});
		Insets insets = new Insets(0, 0, 0, 0);
		Dimension dim = new Dimension(25, 22);
		xRotateLeft.setMargin(insets);
		xRotateLeft.setMaximumSize(dim);
		rotateBox.add(xRotateLeft);
		JLabel label = new JLabel("x");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton xRotateRight = new JButton(rotateRight);
		xRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateX(
						PI2).getMatrix());
				computeShadow();
			}
		});
		xRotateRight.setMargin(insets);
		xRotateRight.setMaximumSize(dim);
		rotateBox.add(xRotateRight);

		JButton yRotateLeft = new JButton(rotateLeft);
		yRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateY(
						-PI2).getMatrix());
				computeShadow();
			}
		});
		yRotateLeft.setMargin(insets);
		yRotateLeft.setMaximumSize(dim);
		rotateBox.add(yRotateLeft);
		label = new JLabel("y");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton yRotateRight = new JButton(rotateRight);
		yRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateY(
						PI2).getMatrix());
				computeShadow();
			}
		});
		yRotateRight.setMargin(insets);
		yRotateRight.setMaximumSize(dim);
		rotateBox.add(yRotateRight);

		JButton zRotateLeft = new JButton(rotateLeft);
		zRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateZ(
						-PI2).getMatrix());
				computeShadow();
			}
		});
		zRotateLeft.setMargin(insets);
		zRotateLeft.setMaximumSize(dim);
		rotateBox.add(zRotateLeft);
		label = new JLabel("z");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		rotateBox.add(label);
		JButton zRotateRight = new JButton(rotateRight);
		zRotateRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, MatrixBuilder.euclidean().rotateZ(
						PI2).getMatrix());
				computeShadow();
			}
		});
		zRotateRight.setMargin(insets);
		zRotateRight.setMaximumSize(dim);
		rotateBox.add(zRotateRight);

		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(new EmptyBorder(5, 30, 5, 20));
		p.add("Center", rotateBox);
		JButton alignButton = new JButton("align");
		alignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(diam, offset, null);
			}
		});
		p.add("South", alignButton);
		placementBox.add(sizeBox);
		placementBox.add(groundBox);
		placementBox.add(p);
		placementPanel.add(placementBox);
		geomTabs.add("align", placementPanel);
		sp.getFrame().pack();
	}

	public void addLoadTab() {
		final String[][] examples = new String[][] {
				{ "Boy surface", "jrs/boy.jrs" },
				{ "Chen-Gackstatter surface", "obj/Chen-Gackstatter-4.obj" },
				{ "helicoid with 2 handles", "jrs/He2WithBoundary.jrs" },
				{ "tetranoid", "3ds/tetranoid.3ds" },
				{ "Wente torus", "jrs/wente.jrs" },
				{ "Matheon baer", "jrs/baer.jrs" } };
		ActionListener examplesListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedBox = e.getActionCommand();
				int selectionIndex = ((Integer) exampleIndices.get(selectedBox)).intValue();
				try {
					SceneGraphComponent read = Readers.read(Input
												.getInput(examples[selectionIndex][1]));
					setContent(read);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		JPanel buttonGroupComponent = new JPanel(new BorderLayout());
		buttonGroupComponent.setBorder(new EmptyBorder(10, 10, 10, 10));
		Box buttonGroupPanel = new Box(BoxLayout.Y_AXIS);
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < examples.length; i++) {
			JRadioButton button = new JRadioButton(examples[i][0]);
			button.addActionListener(examplesListener);
			buttonGroupPanel.add(button);
			group.add(button);
			exampleIndices.put(examples[i][0], new Integer(i));
		}
		buttonGroupComponent.add("Center", buttonGroupPanel);
		JButton loadButton = new JButton("load ...");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToFileBrowser();
			}
		});
		buttonGroupComponent.add("South", loadButton);
		geomTabs.add("load", buttonGroupComponent);
		sp.getFrame().pack();
	}

	public void addToolTab() {
		JPanel toolPanel = new JPanel(new BorderLayout());
		toolPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Box toolBox = new Box(BoxLayout.Y_AXIS);
		Box toolButtonBox = new Box(BoxLayout.X_AXIS);
		toolButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		rotate = new JCheckBox("rotate");
		rotate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setRotate(rotate.isSelected());
			}
		});
		toolButtonBox.add(rotate);
		drag = new JCheckBox("drag");
		drag.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setDrag(drag.isSelected());
			}
		});
		toolButtonBox.add(drag);

		toolBox.add(toolButtonBox);

		toolPanel.add(BorderLayout.CENTER, toolBox);
		
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton resetButton = new JButton("reset");
		resetButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (currentContent != null) {
					MatrixBuilder.euclidean().assignTo(currentContent);
				}
			}
		});
		buttonPanel.add(resetButton);
		JButton shadowButton = new JButton("recompute shadow");
		shadowButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				computeShadow();
			}
		});
		buttonPanel.add(shadowButton);
		toolPanel.add(BorderLayout.SOUTH, buttonPanel);
		geomTabs.add("tools", toolPanel);
		sp.getFrame().pack();
	}

	public void addTexTab() {
		final String[][] textures = new String[][] {
				{ "none", null },
				{ "metal grid", "textures/boysurface.png" },
				{ "metal floor", "textures/metal_basic88.png" }
		};
		ActionListener texturesListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				String selectedBox = e.getActionCommand();
				int selectionIndex = ((Integer) textureIndices.get(selectedBox))
						.intValue();
				try {
					if (textures[selectionIndex][0] == "none") {
						contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.INHERITED, Texture2D.class);
					} else {
						ImageData img = ImageData.load(Input.getInput(textures[selectionIndex][1]));
						tex = TextureUtility.createTexture(contentAppearance, "polygonShader", img, false);
						setTexScale(texScaleSlider.getValue()*.01);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		};
		JPanel textureButtonPanel = new JPanel(new BorderLayout());
		textureButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		Box textureButtonBox = new Box(BoxLayout.Y_AXIS);
		ButtonGroup textureGroup = new ButtonGroup();
		for (int i = 0; i < textures.length; i++) {
			JRadioButton button = new JRadioButton(textures[i][0]);
			button.addActionListener(texturesListener);
			textureButtonBox.add(button);
			textureGroup.add(button);
			textureIndices.put(textures[i][0], new Integer(i));
		}
		textureButtonPanel.add("Center", textureButtonBox);
		
		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(70, 5, 5, 0));
		JLabel texScaleLabel = new JLabel("scale");
		int sliderTexScale = (int)(Math.log(DEFAULT_TEX_SCALE /MAX_TEX_SCALE*TEX_SCALE_RANGE)/Math.log(TEX_SCALE_RANGE)*100);
		texScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, sliderTexScale);
		texScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setTexScale(0.01 * texScaleSlider.getValue());
			}
		});
		texScaleBox.add(texScaleLabel);
		texScaleBox.add(texScaleSlider);
		textureButtonBox .add(texScaleBox);
		
		JButton textureLoadButton = new JButton("load ...");
		textureLoadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToTextureBrowser(contentAppearance);
			}
		});
		textureButtonPanel.add("South", textureLoadButton);
		appearanceTabs.add("tex", textureButtonPanel);
		sp.getFrame().pack();
	}

	public void addHelpTab() {
		JTextPane helpText = new JTextPane();
		helpText.setEditable(false);
		helpText.setContentType("text/html");
		helpText.setPreferredSize(new Dimension(100,100));
		helpText.setBackground(rotate.getBackground());
		try {
			helpText.setText(Input.getInput("de/jreality/vr/help.html").getContentAsString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		geomTabs.add("help", helpText);
		sp.getFrame().pack();
	}

	protected void setTexScale(double d) {
		texScaleSlider.setValue((int) (d * 100));
		if (tex != null) {
			double texScale = Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE;
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(texScale).getMatrix());
		}
	}

	private void switchToDefaultPanel() {
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.getFrame().setContentPane(defaultPanel);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}

	protected void showPanel(boolean showFileChooser) {
		if (showFileChooser) {
			sp.setPanelWidth(1.9);
			sp.getFrame().setContentPane(fileChooserPanel);
			sp.getFrame().pack();
		}
		sp.show(getSceneRoot(), new Matrix(avatarPath.getMatrix(null)));
	}

	private void switchToColorChooser(String attribute) {
		currentColor = attribute;
		Object current = contentAppearance.getAttribute(currentColor);
		System.out.println(current);
		colorChooser.setColor(current != Appearance.INHERITED ? (Color) current
				: Color.white);
		sp.getFrame().setVisible(false);
		sp.getFrame().setContentPane(colorChooserPanel);
		sp.getFrame().setVisible(true);
	}

	protected void setDrag(boolean b) {
		toggleTool(dragTool, b);
		drag.setSelected(b);
	}

	protected void setRotate(boolean b) {
		toggleTool(rotateTool, b);
		rotate.setSelected(b);
	}

	private void toggleTool(Tool rotateTool, boolean b) {
		if (currentContent == null)
			return;
		if (!b && currentContent != null
				&& currentContent.getTools().contains(rotateTool)) {
			currentContent.removeTool(rotateTool);
		} else {
			if (b && !currentContent.getTools().contains(rotateTool))
				currentContent.addTool(rotateTool);
		}
	}

	protected void setReflection(double d) {
		cm.setBlendColor(new Color(1f, 1f, 1f, (float) d));
	}

	protected void setPointRadius(double d) {
		pointRadiusSlider.setValue((int) (d * 100));
		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POINT_RADIUS, Math.exp(Math.log(LOGARITHMIC_RANGE) * d)
				/ LOGARITHMIC_RANGE * objectScale * MAX_RADIUS);
	}

	protected void setTubeRadius(double d) {
		tubeRadiusSlider.setValue((int) (d * 100));
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBE_RADIUS, Math.exp(Math.log(LOGARITHMIC_RANGE) * d)
				/ LOGARITHMIC_RANGE * objectScale * MAX_RADIUS);
	}

	public void switchToFileBrowser() {
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(FILE_CHOOSER_PANEL_WIDTH);
		sp.setAboveGround(FILE_CHOOSER_ABOVE_GROUND);
		sp.getFrame().setContentPane(fileChooserPanel);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}
	
	public void switchToTextureBrowser(Appearance app) {
		//currentAppearance = app;
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(FILE_CHOOSER_PANEL_WIDTH);
		sp.setAboveGround(FILE_CHOOSER_ABOVE_GROUND);
		sp.getFrame().setContentPane(texFileChooser);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}

	public void setTerrainTexture(ImageData tex, double scale) {
		Texture2D t = TextureUtility.createTexture(terrainAppearance,
				"polygonShader", tex);
		t.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
	}

	public void setSkyBox(ImageData[] imgs) {
		TextureUtility.createSkyBox(rootAppearance, imgs);
		cm = TextureUtility.createReflectionMap(contentAppearance,
				"polygonShader", imgs);
	}

	public void setContent(SceneGraphComponent content) {
		if (currentContent != null
				&& sceneNode.getChildNodes().contains(currentContent)) {
			setDrag(false);
			setRotate(false);
			sceneNode.removeChild(currentContent);
		}
		SceneGraphComponent parent = new SceneGraphComponent();
		parent.addChild(content);
		currentContent = parent;
		PickUtility.assignFaceAABBTrees(content);
		rotate.setSelected(false);
		drag.setSelected(false);
		Rectangle3D bounds = GeometryUtility
				.calculateChildrenBoundingBox(currentContent);
		// scale
		double[] extent = bounds.getExtent();
		double[] center = bounds.getCenter();
		content.addTool(new DuplicateTriplyPeriodicTool(
				extent[0],extent[1],extent[2],center[0],center[1],center[2]));
		objectScale = Math.max(Math.max(extent[0], extent[2]), extent[1]);
		setTubeRadius(DEFAULT_TUBE_RADIUS);
		setPointRadius(DEFAULT_POINT_RADIUS);
		sceneNode.addChild(currentContent);
		alignContent(diam, offset, null);
		computeShadow();
	}

	public double getDiam() {
		return diam;
	}

	public void setDiam(double d) {
		diam = d;
		double sliderDiam = Math.log(diam*LOGARITHMIC_RANGE/MAX_CONTENT_SIZE)/Math.log(LOGARITHMIC_RANGE);
		sizeSlider.setValue((int) (sliderDiam * 100));
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
		groundSlider.setValue((int) (offset * 100));
	}

	private void alignContent(final double diam, final double offset,
			final Matrix rotation) {
		Scene.executeWriter(sceneNode, new Runnable() {
			public void run() {
				if (rotation != null) {
					MatrixBuilder.euclidean(rotation).times(
							new Matrix(sceneNode.getTransformation()))
							.assignTo(sceneNode);
				}
				Rectangle3D bounds = GeometryUtility
						.calculateBoundingBox(sceneNode);
				// scale
				double[] extent = bounds.getExtent();
				double maxExtent = Math.max(extent[0], extent[2]);
				if (maxExtent != 0) {
					double scale = diam / maxExtent;
					double[] translation = bounds.getCenter();
					translation[1] = -scale * bounds.getMinY() + offset;
					translation[0] *= -scale;
					translation[2] *= -scale;

					MatrixBuilder mb = MatrixBuilder.euclidean().translate(
							translation).scale(scale);
					if (sceneNode.getTransformation() != null)
						mb.times(sceneNode.getTransformation().getMatrix());
					mb.assignTo(sceneNode);
				}
			}
		});
	}

	public ViewerApp display() {
		return new ViewerApp(sceneRoot, cameraPath, emptyPickPath, avatarPath);
	}

	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	public void setLightIntensity(double intensity) {
		light.setIntensity(intensity);
	}

	public double getLightIntensity() {
		return light.getIntensity();
	}

	public SceneGraphComponent getTerrainNode() {
		return terrainNode;
	}

	public void setAvatarPosition(double x, double y, double z) {
		MatrixBuilder.euclidean().translate(x, y, z).assignTo(avatarNode);
	}
	public void setAvatarHeight(double y) {
		Matrix m = new Matrix(avatarNode.getTransformation());
		double delta = y-m.getEntry(1, 3);
		m.setEntry(1, 3, y);
		m.assignTo(avatarNode);
		sp.adjustHeight(delta);
	}

	private void computeShadow() {
		if (pickSystem == null) {
			pickSystem = new AABBPickSystem();
			pickSystem.setSceneRoot(sceneNode);
		}
		
		int n = flat ? flatTerrainPoints.length : terrainPoints.length;
		double[] white = new double[]{1,1,1,1};
		double[] black = new double[]{.2,.2,.2,1};
		double[] sun = new double[]{0,1,1,0};
		double[][] color = new double[n][];
		for (int j=0; j<n; j++) {
			List<PickResult> hits = pickSystem.computePick((flat ? flatTerrainPoints : terrainPoints)[j], sun);
			color[j] = hits.size() > 0 ? black : white;
		}
		(flat ? flatTerrain : terrain).setVertexAttributes(
				Attribute.COLORS,
				new DoubleArrayArray.Array(color)
		);
	}
	
	public static void main(String[] args) throws IOException {
//		System.setProperty("de.jreality.ui.viewerapp.synchRender", "true");
		ViewerVR vr = new ViewerVR();
		vr.addLoadTab();
		vr.addAlignTab();
		vr.addAppTab();
		vr.addEnvTab();
		vr.addToolTab();
		vr.addTexTab();
		vr.addHelpTab();
		vr.showPanel(false);
		ViewerApp vApp = vr.display();
//		vApp.setAttachNavigator(true);
//		vApp.setAttachBeanShell(true);
//		vApp.setShowMenu(true);
		vApp.update();
		JFrame f = vApp.display();
		f.setSize(800, 600);
		f.validate();
	}
}