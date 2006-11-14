/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
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
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.ViewerAppMenu;
import de.jreality.util.Input;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;
import de.jtem.beans.SimpleColorChooser;


public class ViewerVR {
	
	// defaults for preferences:
	private static final boolean DEFAULT_PANEL_IN_SCENE = true;
	
	private static final boolean DEFAULT_PICK_FACES = true;
	private static final boolean DEFAULT_PICK_EDGES = false;
	private static final boolean DEFAULT_PICK_VERTICES = false;
	
	// defaults for env panel
	private static final String DEFAULT_ENVIRONMENT = "snow";
	private static final boolean DEFAULT_TERRAIN_TRANSPARENT = false;
	private static final boolean DEFAULT_SKYBOX_HIDDEN = false;
	private static final Color DEFAULT_TOP_COLOR = new Color(80,80,120);
	private static final Color DEFAULT_BOTTOM_COLOR = Color.black;
	private static final boolean DEFAULT_BACKGROUND_FLAT = false;
	
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
	
	// defaults for tool panel
	private static final boolean DEFAULT_ROTATION_ENABLED = false;
	private static final boolean DEFAULT_DRAG_ENABLED = false;
	private static final boolean DEFAULT_INVERT_MOUSE = false;
	private static final double DEFAULT_SPEED = 4;
	private static final double DEFAULT_GRAVITY = 9.81;
	
	// default value of tex panel
	private static final double DEFAULT_TEXTURE_SCALE = 20;
	private static final String DEFAULT_TEXTURE = "none";
	
	// defaults for align panel
	private static final double DEFAULT_SIZE = 22;
	private static final double DEFAULT_OFFSET = -.5;
	
	
	// other static constants:
	
	private static final double MAX_OFFSET = 5;
	
	// maximal radius of tubes or points compared to content size
	private static final double MAX_RADIUS = 0.1;
	
	// ratio of maximal versus minimal value for logarithmic sliders
	private static final int LOGARITHMIC_RANGE = 200;
	
	// maximal horizontal diameter of content in meters
	private static final double MAX_CONTENT_SIZE = 100;
	
	// maximal value of texture scale
	private static final double MAX_TEX_SCALE = 400;
	
	// ratio of maximal value and minimal value of texture scale
	private static final double TEX_SCALE_RANGE = 400;

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
	
	
	// texture of content
	private Texture2D tex;

	// parts of the scene that do not change
	private SceneGraphComponent sceneRoot = new SceneGraphComponent(),
			sceneNode = new SceneGraphComponent(),
			avatarNode = new SceneGraphComponent(),
			camNode = new SceneGraphComponent(),
			lightNode = new SceneGraphComponent(), terrainNode;
	private Appearance terrainAppearance = new Appearance(),
			rootAppearance = new Appearance(),
			contentAppearance = new Appearance();
	private Tool rotateTool = new RotateTool(), dragTool = new DraggingTool();
	
	
	private SceneGraphComponent currentContent;
	private HashMap<String, Integer> exampleIndices = new HashMap<String, Integer>();
	private HashMap<String, String> textureNameToTexture = new HashMap<String, String>();
	private HashMap<String, ButtonModel> textureNameToButton = new HashMap<String, ButtonModel>();
	
	
	private DirectionalLight light = new DirectionalLight();
	private SceneGraphPath cameraPath, avatarPath, emptyPickPath;
	private JPanel fileChooserPanel;
	private JFileChooser texFileChooser;
	private SimpleColorChooser contentColorChooser;
	private SimpleColorChooser backgroundColorChooser;
	private ScenePanel sp;
	
	// align panel
	private JSlider sizeSlider;
	private JSlider groundSlider;

	// app panel
	private JSlider tubeRadiusSlider;
	private JSlider pointRadiusSlider;
	private JSlider lineReflectionSlider;
	private JSlider pointReflectionSlider;
	private JSlider faceReflectionSlider;
	
	// tool tab
	private JCheckBox rotate;
	private JCheckBox drag;
	private JCheckBox pickFaces;
	private JCheckBox pickEdges;
	private JCheckBox pickVertices;
	private JSlider gravity;
	private JSlider gain;

	
	// env tab
	private JCheckBox terrainTransparent;
	private JCheckBox skyBoxHidden;
	private Color bottomColor;
	private Color topColor;
	private JButton shadowButton;
	
	// app tab
	private JCheckBox showLines;
	private JCheckBox showPoints;
	private JCheckBox showFaces;
	private JCheckBox transparency;
	private JSlider transparencySlider;
	private JCheckBox pointsReflecting;
	private JCheckBox linesReflecting;
	private JCheckBox facesReflecting;
	private JCheckBox facesFlat;
	
	// tex tab
	private JSlider texScaleSlider;


	
	private CubeMap cmFaces;
	private CubeMap cmEdges;
	private CubeMap cmVertices;
	private double objectScale=1;
	private Container defaultPanel;
	private String currentColor;
	
	private Landscape landscape;
	private AABBPickSystem pickSystem;
	private double[][] terrainPoints;
	private IndexedFaceSet terrain;
	private IndexedFaceSet flatTerrain = Primitives.plainQuadMesh(3, 3, 100, 100);
	private double[][] flatTerrainPoints;
	private boolean flat;
	private JTabbedPane geomTabs;
	private JTabbedPane appearanceTabs;
	private JPanel textureButtonPanel;
	private JPanel placementPanel;
	private JPanel appearancePanel;
	private JPanel envSelection;
	private JTextPane helpText;
	private JPanel buttonGroupComponent;
	private JPanel toolPanel;

	protected boolean currentBackgroundColorTop;
//	private boolean showShadow = false;
	private JCheckBox backgroundFlat;
	private ImageData[] cubeMap;
	private boolean generatePickTrees;
	private ButtonGroup textureGroup;
	private boolean panelInScene = true;
	private JCheckBoxMenuItem panelInSceneCheckBox;

	private JCheckBox invertMouse;

	private ShipNavigationTool shipNavigationTool;

	private HeadTransformationTool headTransformationTool;
	
	public ViewerVR() throws IOException {

		// find out where we are running
		boolean portal = "portal".equals(Secure.getProperty("de.jreality.scene.tool.Config"));

		// build basic scene graph
		sceneRoot.setName("root");
		sceneNode.setName("scene");
		avatarNode.setName("avatar");
		camNode.setName("camNode");
		lightNode.setName("sun");
		MatrixBuilder.euclidean().rotateX(-Math.PI/2).assignTo(sceneNode);
		sceneNode.getTransformation().setName("alignment");

		// root appearance
		rootAppearance.setName("root app");
		ShaderUtility.createRootAppearance(rootAppearance);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.AMBIENT_COEFFICIENT, 0.03);
		rootAppearance.setAttribute(CommonAttributes.OPAQUE_TUBES_AND_SPHERES, true);
		rootAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		rootAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.PICKABLE, false);
		
    rootAppearance.setAttribute(CommonAttributes.RMAN_SHADOWS_ENABLED, true);
    rootAppearance.setAttribute(CommonAttributes.RMAN_RAY_TRACING_REFLECTIONS,true);
    //rootAppearance.setAttribute(CommonAttributes.RMAN_RAY_TRACING_VOLUMES,true);
		
		sceneRoot.setAppearance(rootAppearance);
		Camera cam = new Camera();
		cam.setNear(0.01);
		cam.setFar(1500);
		if (portal) {
			cam.setOnAxis(false);
			cam.setStereo(true);
		}
		// lights
		light.setIntensity(1);
		lightNode.setLight(light);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 1 }).assignTo(lightNode);
		sceneRoot.addChild(lightNode);
		
		
		DirectionalLight HeadLight = new DirectionalLight();
		HeadLight.setName("camera light");
		HeadLight.setColor(new Color(255,255,255,255));
		HeadLight.setIntensity(0.3);
	    camNode.setLight(HeadLight);

		// paths
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

		shipNavigationTool = new ShipNavigationTool();
		avatarNode.addTool(shipNavigationTool);
		if (portal)
			shipNavigationTool.setPollingDevice(false);
		if (!portal) {
			headTransformationTool = new HeadTransformationTool();
			camNode.addTool(headTransformationTool);
		} else {
			try {
				Tool t = (Tool) Class.forName(
						"de.jreality.tools.PortalHeadMoveTool").newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		sceneRoot.addTool(new PickShowTool(null, 0.005));
		
		// content appearearance
		contentAppearance.setName("contentApp");
		sceneNode.setAppearance(contentAppearance);
 
		// terrain
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
		terrainAppearance.setAttribute(CommonAttributes.AMBIENT_COEFFICIENT, .3);
		terrainNode.setAppearance(terrainAppearance);
		sceneRoot.addChild(terrainNode);
		sceneRoot.addChild(sceneNode);
		
		
		// landscape
		landscape = new Landscape();

		// swing widgets
		makeControlPanel();
		makeAlignTab();
		makeAppTab();
		makeEnvTab();
		makeToolTab();
		makeTexTab();
		makeHelpTab();
		makeContentFileChooser();
		makeTextureFileChooser();
		makeColorChoosers();
		
		panelInSceneCheckBox = new JCheckBoxMenuItem( new AbstractAction("Show panel in scene") {
		  public void actionPerformed(ActionEvent e) {
		    setPanelInScene(panelInSceneCheckBox.getState());
		  }
		});
    panelInSceneCheckBox.setSelected(true);
		
		restorePreferences();
		setAvatarPosition(0, landscape.isTerrainFlat() ? -.5 : -.13, 28);

	}

	private void makeControlPanel() {
		sp = new ScenePanel();
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.setZOffset(PANEL_Z_OFFSET);
		
		JTabbedPane tabs = new JTabbedPane();
		
		String os = Secure.getProperty("os.name");
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
						SceneGraphComponent tempRoot = new SceneGraphComponent();
						tempRoot.addChild(read);
						tempRoot.accept(new SceneGraphVisitor() {
							public void visit(SceneGraphComponent c) {
								if (removeAppsCheckBox.isSelected() && c.getAppearance() != null) c.setAppearance(null); 
								c.childrenWriteAccept(this, false, false, false, false, true,
										true);
							}
							public void visit(IndexedFaceSet i) {
								if (i.getFaceAttributes(Attribute.NORMALS) == null) GeometryUtility.calculateAndSetFaceNormals(i);
								if (i.getVertexAttributes(Attribute.NORMALS) == null) GeometryUtility.calculateAndSetVertexNormals(i);
								if (smoothNormalsCheckBox.isSelected()) IndexedFaceSetUtility.assignSmoothVertexNormals(i, -1);
							}
						});
						tempRoot.removeChild(read);
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
		String dataDir = Secure.getProperty("jreality.data");
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
	
	private void makeColorChoosers() {
		contentColorChooser = new SimpleColorChooser();
		contentColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		contentColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				contentAppearance.setAttribute(currentColor, contentColorChooser.getColor());
			}
		});
		ActionListener closeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchToDefaultPanel();
			}
		};
		contentColorChooser.addActionListener(closeListener);
		backgroundColorChooser =  new SimpleColorChooser();
		backgroundColorChooser.setBorder(new EmptyBorder(8,8,8,8));
		backgroundColorChooser.addChangeListener( new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				Color color = backgroundColorChooser.getColor();
				if (currentBackgroundColorTop) {
					setTopColor(color);
				} else {
					setBottomColor(color);
				}
			}
		});
		backgroundColorChooser.addActionListener(closeListener);
	}

	private void updateLandscape() {
		cubeMap = landscape.getCubeMap();
		topColor = landscape.getUpColor();
		bottomColor = landscape.getDownColor();
		
		updateEnablingOfBackgroundEdit();
		updateBackground();
//		Geometry last = terrainNode.getGeometry();
		flat = landscape.isTerrainFlat();
		terrainNode.setGeometry(flat ? flatTerrain : terrain);
		//if (last != terrainNode.getGeometry()) computeShadow();
		
		updateSkyBox();
		
		ImageData terrainTex = landscape.getTerrainTexture();
		setTerrainTexture(
				terrainTex,
				landscape.getTerrainTextureScale()
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
		appearanceTabs.add("env", envSelection);
		sp.getFrame().pack();
	}

	private void makeEnvTab() {
		landscape.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent arg0) {
				updateLandscape();
			}
		});	
		
		Insets insets = new Insets(0,2,0,2);
		
		envSelection = new JPanel(new BorderLayout());
		envSelection.setBorder(new EmptyBorder(0,0,0,0));
		JPanel selectionPanel = new JPanel(new BorderLayout());
		selectionPanel.setBorder(new EmptyBorder(0,5,0,5));
		selectionPanel.add(landscape.getSelectionComponent(), BorderLayout.CENTER);
		envSelection.add(selectionPanel, BorderLayout.CENTER);
		
		Box envControlBox = new Box(BoxLayout.Y_AXIS);
		JPanel shadowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		shadowPanel.setBorder(new EmptyBorder(0,5,0,0));
		JButton computeShadow = new JButton("add shadow");
		computeShadow.setMargin(insets);
		computeShadow.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				computeShadow();
			}
		});
		shadowPanel.add(computeShadow);
		JButton clearShadow = new JButton("clear shadow");
		clearShadow.setMargin(insets);
		clearShadow.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				clearShadow();
			}
		});
		shadowPanel.add(clearShadow);
		envControlBox.add(shadowPanel);
		JPanel terrainTransparentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		terrainTransparent = new JCheckBox("transparent terrain");
		terrainTransparent.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				setTerrainTransparent(terrainTransparent.isSelected());
			}
		});
		terrainTransparentPanel.add(terrainTransparent);
		skyBoxHidden = new JCheckBox("no sky");
		skyBoxHidden.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				updateSkyBox();
			}
		});
		terrainTransparentPanel.add(skyBoxHidden);
		
		envControlBox.add(terrainTransparentPanel);
		
		JPanel backgroundColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		JLabel backgroundLabel = new JLabel("background:");
		backgroundLabel.setBorder(new EmptyBorder(0,5,0,10));
		backgroundColorPanel.add(backgroundLabel);
		
		JButton topColorButton = new JButton("top");
		topColorButton.setMargin(insets);
		topColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToBackgroundColorChooser(true);
			}
		});
		backgroundColorPanel.add(topColorButton);
		JButton bottomColorButton = new JButton("bottom");
		bottomColorButton.setMargin(insets);
		bottomColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				switchToBackgroundColorChooser(false);
			}
		});
		backgroundColorPanel.add(bottomColorButton);
		backgroundFlat = new JCheckBox("flat");
		backgroundFlat.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateEnablingOfBackgroundEdit();
				updateBackground();
			}
		});
		backgroundColorPanel.add(backgroundFlat);
		
		envControlBox.add(backgroundColorPanel);
		envSelection.add(envControlBox, BorderLayout.SOUTH);
	}

	private void updateEnablingOfBackgroundEdit() {
//		backgroundLabel.setEnabled(cubeMap == null);
//		topColorButton.setEnabled(cubeMap == null);
//		backGroundFlat.setEnabled(cubeMap == null);
//		bottomColorButton.setEnabled(cubeMap == null && !backGroundFlat.isSelected());
	}
	
	public boolean isTerrainTransparent() {
		return terrainTransparent.isSelected();
	}
	
	public void setTerrainTransparent(boolean b) {
		terrainTransparent.setSelected(b);
		terrainAppearance.setAttribute(CommonAttributes.TRANSPARENCY, b ? 1.0 : 0.0);
	}
	
	public boolean isTransparencyEnabled() {
		return transparency.isSelected();
	}
	
	public void setTransparencyEnabled(boolean b) {
		transparency.setSelected(b);
		contentAppearance.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, b);
	}

	public void addAppTab() {
		appearanceTabs.add("app", appearancePanel);
		sp.getFrame().pack();
	}

	private void makeAppTab() {
		appearancePanel = new JPanel(new BorderLayout());
		Box appBox = new Box(BoxLayout.Y_AXIS);
		
		int topSpacing = 0;
		int bottomSpacing = 5;
		
		Border boxBorder = new EmptyBorder(topSpacing, 5, bottomSpacing, 5);
		Border sliderBoxBorder = new EmptyBorder(topSpacing, 10, bottomSpacing, 10);
		
		// lines
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
				switchToContentColorChooser(
						CommonAttributes.LINE_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR);
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
				switchToContentColorChooser(
						CommonAttributes.POINT_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR);
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
				switchToContentColorChooser(
						CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR);
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

	public void addAlignTab() {
		geomTabs.add("align", placementPanel);
		sp.getFrame().pack();
	}

	private void makeAlignTab() {
		placementPanel = new JPanel(new BorderLayout());
		placementPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Box placementBox = new Box(BoxLayout.X_AXIS);
		Box sizeBox = new Box(BoxLayout.Y_AXIS);
		sizeBox.setBorder(new EmptyBorder(10, 5, 0, 5));
		JLabel sizeLabel = new JLabel("size");
		sizeSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, 0);
		sizeSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setSize(getSize());
				alignContent(getSize(), getOffset(), null);
//				if (!sizeSlider.getValueIsAdjusting()) {
//					computeShadow();
//				}
			}
		});
		sizeBox.add(sizeLabel);
		sizeBox.add(sizeSlider);
		Box groundBox = new Box(BoxLayout.Y_AXIS);
		groundBox.setBorder(new EmptyBorder(10, 5, 0, 5));
		JLabel groundLabel = new JLabel("level");
		groundSlider = new JSlider(SwingConstants.VERTICAL, -25, 75, 0);
		groundSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setOffset(getOffset());
				alignContent(getSize(), getOffset(), null);
//				if (!groundSlider.getValueIsAdjusting()) {
//					computeShadow();
//				}
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
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateX(
						-PI2).getMatrix());
//				computeShadow();
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
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateX(
						PI2).getMatrix());
//				computeShadow();
			}
		});
		xRotateRight.setMargin(insets);
		xRotateRight.setMaximumSize(dim);
		rotateBox.add(xRotateRight);

		JButton yRotateLeft = new JButton(rotateLeft);
		yRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateY(
						-PI2).getMatrix());
//				computeShadow();
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
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateY(
						PI2).getMatrix());
//				computeShadow();
			}
		});
		yRotateRight.setMargin(insets);
		yRotateRight.setMaximumSize(dim);
		rotateBox.add(yRotateRight);

		JButton zRotateLeft = new JButton(rotateLeft);
		zRotateLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateZ(
						-PI2).getMatrix());
//				computeShadow();
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
				alignContent(getSize(), getOffset(), MatrixBuilder.euclidean().rotateZ(
						PI2).getMatrix());
//				computeShadow();
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
				alignContent(getSize(), getOffset(), null);
			}
		});
		p.add("South", alignButton);
		placementBox.add(sizeBox);
		placementBox.add(groundBox);
		placementBox.add(p);
		placementPanel.add(placementBox);
	}

	public void addLoadTab(final String[][] examples) {
		buttonGroupComponent = new JPanel(new BorderLayout());
		buttonGroupComponent.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		if (examples != null) {
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
		}
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
		geomTabs.add("tools", toolPanel);
		sp.getFrame().pack();
	}

	private void makeToolTab() {
		toolPanel = new JPanel(new BorderLayout());
		toolPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		Box toolBox = new Box(BoxLayout.Y_AXIS);
		Box toolButtonBox = new Box(BoxLayout.X_AXIS);
		toolButtonBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		rotate = new JCheckBox("rotate");
		rotate.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setRotationEnabled(rotate.isSelected());
			}
		});
		toolButtonBox.add(rotate);
		drag = new JCheckBox("drag");
		drag.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setDragEnabled(drag.isSelected());
			}
		});
		toolButtonBox.add(drag);
		toolButtonBox.add(Box.createHorizontalGlue());
		toolBox.add(toolButtonBox);

		
		Box pickButtonBox = new Box(BoxLayout.X_AXIS);
		pickButtonBox.setBorder(new EmptyBorder(5, 5, 5, 5));
		pickButtonBox.add(new JLabel("pick: "));
		pickFaces = new JCheckBox("faces");
		pickFaces.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickFaces(pickFaces.isSelected());
			}
		});
		pickButtonBox.add(pickFaces);
		
		pickEdges = new JCheckBox("edges");
		pickEdges.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickEdges(pickEdges.isSelected());
			}
		});
		pickButtonBox.add(pickEdges);
		
		pickVertices = new JCheckBox("vertices");
		pickVertices.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPickVertices(pickVertices.isSelected());
			}
		});
		pickButtonBox.add(pickVertices);
		pickButtonBox.add(Box.createHorizontalGlue());
		
		toolBox.add(pickButtonBox);
		
		Box invertBox = new Box(BoxLayout.X_AXIS);
		invertBox.setBorder(new EmptyBorder(5, 0, 5, 5));
		invertMouse = new JCheckBox("invert mouse");
		invertMouse.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setInvertMouse(invertMouse.isSelected());
			}
		});
		invertBox.add(invertMouse);
		invertBox.add(Box.createHorizontalGlue());
		toolBox.add(invertBox);
		
		Box gainBox = new Box(BoxLayout.X_AXIS);
		gainBox.setBorder(new EmptyBorder(10,5,10,5));
		JLabel gainLabel = new JLabel("navigation speed");
		gainBox.add(gainLabel);
		gain = new JSlider(0, 1000, (int) (100*DEFAULT_SPEED));
		gain.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setNavigationSpeed(getNavigationSpeed());
			}
		});
		gain.setPreferredSize(new Dimension(70,20));
		gain.setBorder(new EmptyBorder(0,5,0,0));
		gainBox.add(gain);
		toolBox.add(gainBox);
		
		Box gravityBox = new Box(BoxLayout.X_AXIS);
		gravityBox.setBorder(new EmptyBorder(10,5,10,5));
		JLabel gravityLabel = new JLabel("gravity");
		gravityBox.add(gravityLabel);
		gravity = new JSlider(0, 2000, (int) (100*DEFAULT_GRAVITY));
		gravity.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				setGravity(getGravity());
			}
		});
		gravity.setPreferredSize(new Dimension(70,20));
		gravity.setBorder(new EmptyBorder(0,5,0,0));
		gravityBox.add(gravity);
		toolBox.add(gravityBox);
		
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
		shadowButton = new JButton("recompute shadow");
		shadowButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				computeShadow();
			}
		});
		buttonPanel.add(shadowButton);
		toolPanel.add(BorderLayout.SOUTH, buttonPanel);
	}

	protected void setNavigationSpeed(double navigationSpeed) {
		int speed = (int)(100*navigationSpeed);
		gain.setValue(speed);
		shipNavigationTool.setGain(navigationSpeed);
	}

	protected double getNavigationSpeed() {
		double speed = 0.01*gain.getValue();
		return speed;
	}

	protected void setGravity(double g) {
		int grav = (int)(100*g);
		gravity.setValue(grav);
		shipNavigationTool.setGravity(g);
	}

	protected double getGravity() {
		double g = 0.01*gravity.getValue();
		return g;
	}

	public void setInvertMouse(boolean b) {
		invertMouse.setSelected(b);
		headTransformationTool.setInvert(b);
	}
	
	public boolean isInvertMouse() {
		return invertMouse.isSelected();
	}

	public void setPickVertices(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickVertices.setSelected(b);
	}

	public void setPickEdges(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickEdges.setSelected(b);
	}

	public void setPickFaces(boolean b) {
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE, b);
		pickFaces.setSelected(b);
	}

	public void addTexTab() {
		appearanceTabs.add("tex", textureButtonPanel);
		sp.getFrame().pack();
	}

	private void makeTexTab() {
		textureNameToTexture.put("none", null);
		textureNameToTexture.put("metal grid", "textures/boysurface.png");
		textureNameToTexture.put("metal floor", "textures/metal_basic88.png");
		textureNameToTexture.put("chain-link fence", "textures/chainlinkfence.png");
		//textureNameToTexture.put("random dots", "textures/random.png");

		ActionListener texturesListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				setTexture(e.getActionCommand());
			}
		};
		textureButtonPanel = new JPanel(new BorderLayout());
		textureButtonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		Box textureButtonBox = new Box(BoxLayout.Y_AXIS);
		textureGroup = new ButtonGroup();
		for (String name : textureNameToTexture.keySet()) {
			JRadioButton button = new JRadioButton(name);
			button.setActionCommand(name);
			textureNameToButton.put(name, button.getModel());
			button.addActionListener(texturesListener);
			textureButtonBox.add(button);
			textureGroup.add(button);
		}
		textureButtonPanel.add("Center", textureButtonBox);
		
		Box texScaleBox = new Box(BoxLayout.X_AXIS);
		texScaleBox.setBorder(new EmptyBorder(70, 5, 5, 0));
		JLabel texScaleLabel = new JLabel("scale");
		texScaleSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100,0);
		texScaleSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				double d = .01 * texScaleSlider.getValue();
				setTextureScale(Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE);
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
	}

	public void addHelpTab() {
		geomTabs.add("help", new JScrollPane(helpText));
		sp.getFrame().pack();
	}

	private void makeHelpTab() {
		helpText = new JTextPane();
		helpText.setEditable(false);
		helpText.setContentType("text/html");
		helpText.setPreferredSize(new Dimension(100,100));
		helpText.setBackground(rotate.getBackground());
		try {
			helpText.setText(Input.getInput("de/jreality/vr/help.html").getContentAsString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public double getTextureScale() {
		double d = .01 * texScaleSlider.getValue();
		return Math.exp(Math.log(TEX_SCALE_RANGE) * d)/TEX_SCALE_RANGE * MAX_TEX_SCALE;
	}
	
	public void setTextureScale(double d) {
		texScaleSlider.setValue(
				(int)(Math.log(d / MAX_TEX_SCALE * TEX_SCALE_RANGE)/Math.log(TEX_SCALE_RANGE)*100)
			);
		if (tex != null) {
			tex.setTextureMatrix(MatrixBuilder.euclidean().scale(d).getMatrix());
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
	
	private void hidePanel() {
		if (SceneGraphUtility.getIndexOfChild(getSceneRoot(), sp.getComponent()) != -1) {
			getSceneRoot().removeChild(sp.getComponent());
		}
	}
	
	private void updateBackgroundColorChooser(boolean top, Color c) {
		if (currentBackgroundColorTop == top) {
			backgroundColorChooser.setColor(c);
		}
	}
	
	private void switchToBackgroundColorChooser(boolean top) {
		currentBackgroundColorTop = top;
		backgroundColorChooser.setColor(top ? topColor : bottomColor);
		sp.getFrame().setVisible(false);
		sp.getFrame().setContentPane(backgroundColorChooser);
		sp.getFrame().setVisible(true);
	}
	
	private void updateContentColorChooser(String attribute, Color c) {
		if (attribute.equals(currentColor)) {
			contentColorChooser.setColor(c);
		}
	}
	
	private void switchToContentColorChooser(String attribute) {
		currentColor = attribute;
		Object current = contentAppearance.getAttribute(currentColor);
		contentColorChooser.setColor(current != Appearance.INHERITED ? (Color) current
				: Color.white);
		sp.getFrame().setVisible(false);
		sp.getFrame().setContentPane(contentColorChooser);
		sp.getFrame().setVisible(true);
	}

	public boolean isDragEnabled() {
		return drag.isSelected();
	}
	
	public void setDragEnabled(boolean b) {
		toggleTool(dragTool, b);
		drag.setSelected(b);
	}

	public boolean isRotationEnabled() {
		return rotate.isSelected();
	}
	
	public void setRotationEnabled(boolean b) {
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
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TRANSPARENCY, d);
	}

	public double getPointRadius() {
		return .01 * pointRadiusSlider.getValue();
	}
	
	public void setPointRadius(double d) {
		pointRadiusSlider.setValue((int) (d * 100));
		contentAppearance.setAttribute(CommonAttributes.POINT_SHADER + "."
				+ CommonAttributes.POINT_RADIUS, Math.exp(Math.log(LOGARITHMIC_RANGE) * d)
				/ LOGARITHMIC_RANGE * objectScale * MAX_RADIUS);
	}
	
	public double getTubeRadius() {
		return .01 * tubeRadiusSlider.getValue();
	}
	
	public void setTubeRadius(double d) {
		tubeRadiusSlider.setValue((int) (d * 100));
		contentAppearance.setAttribute(CommonAttributes.LINE_SHADER + "."
				+ CommonAttributes.TUBE_RADIUS, Math.exp(Math.log(LOGARITHMIC_RANGE) * d)
				/ LOGARITHMIC_RANGE * objectScale * MAX_RADIUS);
	}

	public Color getDownColor() {
		return bottomColor;
	}
	
	private void updateBackground() {
		if (topColor != null && bottomColor != null) {
			Color down = backgroundFlat.isSelected() ? topColor : bottomColor;
			rootAppearance.setAttribute(CommonAttributes.BACKGROUND_COLORS, new Color[]{
					topColor, topColor, down, down
			});
		}
	}

	public Color getTopColor() {
		return topColor;
	}

	public void setTopColor(Color color) {
		this.topColor = color;
		updateBackground();
		updateBackgroundColorChooser(true, color);
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

	private void setTerrainTexture(ImageData tex, double scale) {
		Texture2D t = TextureUtility.createTexture(terrainAppearance,
				"polygonShader", tex);
		t.setTextureMatrix(MatrixBuilder.euclidean().scale(scale).getMatrix());
	}

	private void updateSkyBox() {
		ImageData[] imgs = skyBoxHidden.isSelected() ? null : cubeMap;
		TextureUtility.createSkyBox(rootAppearance, imgs);
		
		setPointsReflecting(isPointsReflecting());
		setLinesReflecting(isLinesReflecting());
		setFacesReflecting(isFacesReflecting());
		setFaceReflection(getFaceReflection());
	}

	public void setContent(SceneGraphComponent content) {
		if (currentContent != null
				&& sceneNode.getChildNodes().contains(currentContent)) {
			toggleTool(rotateTool,false);
			toggleTool(dragTool,false);
			sceneNode.removeChild(currentContent);
		}
		SceneGraphComponent parent = new SceneGraphComponent();
		parent.setName("content");
		parent.addChild(content);
		currentContent = parent;
		if (isGeneratePickTrees()) PickUtility.assignFaceAABBTrees(content);
		toggleTool(rotateTool, rotate.isSelected());
		toggleTool(dragTool, drag.isSelected());
		Rectangle3D bounds = GeometryUtility
				.calculateChildrenBoundingBox(currentContent);
		// scale
		double[] extent = bounds.getExtent();
		double[] center = bounds.getCenter();
		content.addTool(new DuplicateTriplyPeriodicTool(
				extent[0],extent[1],extent[2],center[0],center[1],center[2]));
		objectScale = Math.max(Math.max(extent[0], extent[2]), extent[1]);
		setTubeRadius(getTubeRadius());
		setPointRadius(getPointRadius());
		sceneNode.addChild(currentContent);
		alignContent(getSize(), getOffset(), null);
//		computeShadow();
	}

	public double getSize() {
		double sliderDiam = 0.01 * sizeSlider.getValue();
		return Math.exp(Math.log(LOGARITHMIC_RANGE)*sliderDiam)/LOGARITHMIC_RANGE * MAX_CONTENT_SIZE;
	}

	public void setSize(double d) {
		double sliderDiam = Math.log(d*LOGARITHMIC_RANGE/MAX_CONTENT_SIZE)/Math.log(LOGARITHMIC_RANGE);
		sizeSlider.setValue((int) (sliderDiam * 100));
	}

	public double getOffset() {
		return .01 * groundSlider.getValue() * MAX_OFFSET;
	}

	public void setOffset(double offset) {
		groundSlider.setValue((int) (offset/MAX_OFFSET * 100));
	}

	private void alignContent(final double diam, final double offset,
			final Matrix rotation) {
		Scene.executeWriter(sceneNode, new Runnable() {
			public void run() {
				if (rotation != null) {
					MatrixBuilder.euclidean(
							new Matrix(sceneNode.getTransformation())).times(rotation)
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
		ViewerApp viewerApp = new ViewerApp(sceneRoot, cameraPath, emptyPickPath, avatarPath);
		tweakMenu(viewerApp.getMenu());
		return viewerApp;
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
		//if (!isShowShadow()) return;
 		if (pickSystem == null) {
			pickSystem = new AABBPickSystem();
			pickSystem.setSceneRoot(sceneNode);
		}
		
		int n = flat ? flatTerrainPoints.length : terrainPoints.length;
		double[] white = new double[]{1,1,1,1};
		double[] black = new double[]{0,0,0,1};
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
	
	private void clearShadow() {
		(flat ? flatTerrain : terrain).setVertexAttributes(
				Attribute.COLORS,
				null
		);
	}
	
	public void restoreDefaults() {
		setPanelInScene(DEFAULT_PANEL_IN_SCENE);
		
		// env panel
		setEnvironment(DEFAULT_ENVIRONMENT);
		setTerrainTransparent(DEFAULT_TERRAIN_TRANSPARENT);
		setSkyBoxHidden(DEFAULT_SKYBOX_HIDDEN);
		setTopColor(DEFAULT_TOP_COLOR);
		setBottomColor(DEFAULT_BOTTOM_COLOR);
		setBackgroundFlat(DEFAULT_BACKGROUND_FLAT);
		
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
		
		// tool panel
		setRotationEnabled(DEFAULT_ROTATION_ENABLED);
		setDragEnabled(DEFAULT_DRAG_ENABLED);
		setPickVertices(DEFAULT_PICK_VERTICES);
		setPickEdges(DEFAULT_PICK_EDGES);
		setPickFaces(DEFAULT_PICK_FACES);
		setInvertMouse(DEFAULT_INVERT_MOUSE);
		setGravity(DEFAULT_GRAVITY);
		setNavigationSpeed(DEFAULT_SPEED);
		
		// tex panel
		setTextureScale(DEFAULT_TEXTURE_SCALE);
		setTexture(DEFAULT_TEXTURE);
		
		// defaults for align panel
		setSize(DEFAULT_SIZE);
		setOffset(DEFAULT_OFFSET);
	}
	
	public void savePreferences() {
		Preferences prefs =  Preferences.userNodeForPackage(this.getClass());
		
		prefs.putBoolean("panelInScene", isPanelInScene());
		
		// env panel
		prefs.put("environment", getEnvironment());
		prefs.putBoolean("terrainTransparent", isTerrainTransparent());
		prefs.putBoolean("skyBoxHidden", isSkyBoxHidden());
		Color c = getTopColor();
		prefs.putInt("topColorRed", c.getRed());
		prefs.putInt("topColorGreen", c.getGreen());
		prefs.putInt("topColorBlue", c.getBlue());
		c = getBottomColor();
		prefs.putInt("bottomColorRed", c.getRed());
		prefs.putInt("bottomColorGreen", c.getGreen());
		prefs.putInt("bottomColorBlue", c.getBlue());
		prefs.putBoolean("backgroundFlat", isBackgroundFlat());
		
		// app panel
		prefs.putBoolean("showPoints", isShowPoints());
		prefs.putDouble("pointRadius", getPointRadius());
		c = getPointColor();
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
		
		
		// tool panel
		prefs.putBoolean("rotationEnabled", isRotationEnabled());
		prefs.putBoolean("dragEnabled", isDragEnabled());
		prefs.putBoolean("pickVertices", isPickVertices());
		prefs.putBoolean("pickEdges", isPickEdges());
		prefs.putBoolean("pickFaces", isPickFaces());
		prefs.putBoolean("invertMouse", isInvertMouse());
		prefs.putDouble("gravity", getGravity());
		prefs.putDouble("navSpeed", getNavigationSpeed());
		
		// tex panel
		prefs.putDouble("textureScale", getTextureScale());
		prefs.put("texture", getTexture());
		
		// defaults for align panel
		prefs.putDouble("size", getSize());
		prefs.putDouble("offset", getOffset());
		try {
			prefs.flush();
		} catch(BackingStoreException e){
			e.printStackTrace();
		}
	}
	
	private boolean isPickFaces() {
		Object v = contentAppearance.getAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_FACES;
	}

	private boolean isPickEdges() {
		Object v = contentAppearance.getAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_EDGES;
	}

	private boolean isPickVertices() {
		Object v = contentAppearance.getAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.PICKABLE);
		return (v instanceof Boolean) ? (Boolean) v : DEFAULT_PICK_VERTICES;
	}

	public void restorePreferences() {
		Preferences prefs =  Preferences.userNodeForPackage(this.getClass());
		
		setPanelInScene(prefs.getBoolean("panelInScene", DEFAULT_PANEL_IN_SCENE));
		
		// env panel
		setEnvironment(prefs.get("environment", DEFAULT_ENVIRONMENT));
		setTerrainTransparent(prefs.getBoolean("terrainTransparent", DEFAULT_TERRAIN_TRANSPARENT));
		setSkyBoxHidden(prefs.getBoolean("skyBoxHidden", DEFAULT_SKYBOX_HIDDEN));
		int r = prefs.getInt("topColorRed", DEFAULT_TOP_COLOR.getRed());
		int g = prefs.getInt("topColorGreen", DEFAULT_TOP_COLOR.getGreen());
		int b = prefs.getInt("topColorBlue", DEFAULT_TOP_COLOR.getBlue());
		setTopColor(new Color(r,g,b));
		r = prefs.getInt("bottomColorRed", DEFAULT_BOTTOM_COLOR.getRed());
		g = prefs.getInt("bottomColorGreen", DEFAULT_BOTTOM_COLOR.getGreen());
		b = prefs.getInt("bottomColorBlue", DEFAULT_BOTTOM_COLOR.getBlue());
		setBottomColor(new Color(r,g,b));
		setBackgroundFlat(prefs.getBoolean("backgroundFlat", DEFAULT_BACKGROUND_FLAT));
		
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
		
		// tool panel
		setRotationEnabled(prefs.getBoolean("rotationEnabled", DEFAULT_ROTATION_ENABLED));
		setDragEnabled(prefs.getBoolean("dragEnabled", DEFAULT_DRAG_ENABLED));
		setPickVertices(prefs.getBoolean("pickVertices", DEFAULT_PICK_VERTICES));
		setPickEdges(prefs.getBoolean("pickEdges", DEFAULT_PICK_EDGES));
		setPickFaces(prefs.getBoolean("pickFaces", DEFAULT_PICK_FACES));
		setInvertMouse(prefs.getBoolean("invertMouse", DEFAULT_INVERT_MOUSE));
		setGravity(prefs.getDouble("gravity", DEFAULT_GRAVITY));
		setNavigationSpeed(prefs.getDouble("navSpeed", DEFAULT_SPEED));
		
		
		// tex panel
		setTextureScale(prefs.getDouble("textureScale", DEFAULT_TEXTURE_SCALE));
		setTexture(prefs.get("texture", DEFAULT_TEXTURE));
		
		// defaults for align panel
		setSize(prefs.getDouble("size", DEFAULT_SIZE));
		setOffset(prefs.getDouble("offset", DEFAULT_OFFSET));
	}
	
	public boolean isSkyBoxHidden() {
		return skyBoxHidden.isSelected();
	}
	
	public void setSkyBoxHidden(boolean b) {
		skyBoxHidden.setSelected(b);
	}
	
	public boolean isShowLines() {
		return showLines.isSelected();
	}
	
	public void setShowLines(boolean selected) {
		showLines.setSelected(selected);
		contentAppearance.setAttribute("showLines", selected);
	}

	public boolean isShowPoints() {
		return showPoints.isSelected();
	}
	
	public void setShowPoints(boolean selected) {
		showPoints.setSelected(selected);
		contentAppearance.setAttribute("showPoints", selected);
	}

	public boolean isShowFaces() {
		return showFaces.isSelected();
	}
	
	public void setShowFaces(boolean selected) {
		showFaces.setSelected(selected);
		contentAppearance.setAttribute("showFaces", selected);
	}
	
	public boolean isGeneratePickTrees() {
		return generatePickTrees;
	}

	public void setGeneratePickTrees(boolean generatePickTrees) {
		this.generatePickTrees = generatePickTrees;
	}

//	public void setShowShadow(boolean b) {
//		showShadow=b;
//		shadowButton.setEnabled(b);
//	}
//
//	public boolean isShowShadow() {
//		return showShadow;
//	}

	public String getEnvironment() {
		return landscape.getEnvironment();
	}

	public void setEnvironment(String environment) {
		landscape.setEvironment(environment);
	}

	public boolean getBackgroundFlat() {
		return backgroundFlat.isSelected();
	}
	
	public Color getBottomColor() {
		return bottomColor;
	}

	public void setBottomColor(Color color) {
		this.bottomColor = color;
		updateBackgroundColorChooser(false, color);
	}
	
	public boolean isBackgroundFlat() {
		return backgroundFlat.isSelected();
	}
	
	public void setBackgroundFlat(boolean b) {
		backgroundFlat.setSelected(b);
		updateEnablingOfBackgroundEdit();
	}
	
	public Color getPointColor() {
		return (Color) contentAppearance.getAttribute(
				CommonAttributes.POINT_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setPointColor(Color c) {
		String attribute = CommonAttributes.POINT_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		contentAppearance.setAttribute(attribute,c);
		updateContentColorChooser(attribute, c);
	}
	
	public Color getLineColor() {
		return (Color) contentAppearance.getAttribute(
				CommonAttributes.LINE_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setLineColor(Color c) {
		String attribute = CommonAttributes.LINE_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		contentAppearance.setAttribute(attribute,c);
		updateContentColorChooser(attribute, c);
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
				TextureUtility.removeReflectionMap(contentAppearance, CommonAttributes.POINT_SHADER);
				cmVertices = null;
			}
		} else {
			cmVertices = TextureUtility.createReflectionMap(contentAppearance, CommonAttributes.POINT_SHADER, cubeMap);
		}
	}

	public void setLinesReflecting(boolean b) {
		linesReflecting.setSelected(b);
		if (!isLinesReflecting()) {
			if (cmEdges != null) {
				TextureUtility.removeReflectionMap(contentAppearance, CommonAttributes.LINE_SHADER);
				cmEdges = null;
			}
		} else {
			cmEdges = TextureUtility.createReflectionMap(contentAppearance, CommonAttributes.LINE_SHADER, cubeMap);
		}
	}

	public void setFacesReflecting(boolean b) {
		facesReflecting.setSelected(b);
		if (!isFacesReflecting()) {
			if (cmFaces != null) {
				TextureUtility.removeReflectionMap(contentAppearance, CommonAttributes.POLYGON_SHADER);
				cmFaces = null;
			}
		} else {
			cmFaces = TextureUtility.createReflectionMap(contentAppearance, CommonAttributes.POLYGON_SHADER, cubeMap);
		}
	}

	public Color getFaceColor() {
		return (Color) contentAppearance.getAttribute(
				CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR
		);
	}
	
	public void setFaceColor(Color c) {
		String attribute = CommonAttributes.POLYGON_SHADER + "."+ CommonAttributes.DIFFUSE_COLOR;
		contentAppearance.setAttribute(attribute,c);
		updateContentColorChooser(attribute, c);
	}
	
	public boolean isFacesFlat() {
		return facesFlat.isSelected();
	}
	
	public void setFacesFlat(boolean b) {
		facesFlat.setSelected(b);
		contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, !b);
	}
	
	public String getTexture() {
		return textureGroup.getSelection().getActionCommand();
	}
	
	public void setTexture(String name) {
		textureGroup.setSelected(textureNameToButton.get(name),true);
		try {
			if ("none".equals(name)) {
				contentAppearance.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, Appearance.INHERITED, Texture2D.class);
			} else {
				ImageData img = ImageData.load(Input.getInput(textureNameToTexture.get(name)));
				tex = TextureUtility.createTexture(contentAppearance, "polygonShader", img, false);
				setTextureScale(getTextureScale());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void tweakMenu(ViewerAppMenu menu) {
    
    //remove Edit and Appearance menu
    menu.removeMenu(ViewerAppMenu.EDIT_MENU);
    menu.removeMenu(ViewerAppMenu.APP_MENU);

    //edit File menu
		JMenu fileMenu = menu.getMenu(ViewerAppMenu.FILE_MENU);
		if (fileMenu != null) {
		  for (int i=0; i<fileMenu.getItemCount(); i++) {
		    JMenuItem item = fileMenu.getItem(i);
		    String name = (item == null)? null : item.getActionCommand();
		    if (!(ViewerAppMenu.SAVE_SCENE.equals(name) ||
		        ViewerAppMenu.EXPORT.equals(name) ||
		        ViewerAppMenu.QUIT.equals(name))) {
		      fileMenu.remove(i--);
		    }
		  }
		  fileMenu.insertSeparator(2);
		  fileMenu.insertSeparator(1);
    }

    //edit View menu
		JMenu viewMenu = menu.getMenu(ViewerAppMenu.VIEW_MENU);
    if (viewMenu != null) {
      for (int i=0; i<5; i++)
        viewMenu.remove(viewMenu.getMenuComponentCount()-1);
    }
    
    //setup ViewerVR menu
    JMenu settings = new JMenu("ViewerVR");
    
    Action panelPopup = new AbstractAction("Toggle panel") {
      private static final long serialVersionUID = -4212517852052390335L;
      {
          putValue(SHORT_DESCRIPTION, "Toggle the ViewerVR panel");
          putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
      }
      public void actionPerformed(ActionEvent e) {
        sp.toggle(sceneRoot, new Matrix(avatarPath.getMatrix(null)));
      }
    };
    settings.add(panelPopup);
    settings.add(panelInSceneCheckBox);
    
    settings.addSeparator();
    
    Action defaults = new AbstractAction("Restore defaults") {
      private static final long serialVersionUID = 1834896899901782677L;

      public void actionPerformed(ActionEvent e) {
        restoreDefaults();
      }
    };
    settings.add(defaults);
    Action restorePrefs = new AbstractAction("Restore preferences") {
      private static final long serialVersionUID = 629286193877652699L;

      public void actionPerformed(ActionEvent e) {
        restorePreferences();
      }
    };
    settings.add(restorePrefs);
    Action savePrefs = new AbstractAction("Save preferences") {
      private static final long serialVersionUID = -3242879996093277296L;

      public void actionPerformed(ActionEvent e) {
        savePreferences();
      }
    };
    settings.add(savePrefs);
    menu.addMenu(settings);
    
    //setup Help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new AbstractAction("Help"){
			private static final long serialVersionUID = 3770710651980089282L;
			public void actionPerformed(ActionEvent e) {
				URL helpURL = null;
				try {
					helpURL = new URL("http://www3.math.tu-berlin.de/jreality/mediawiki/index.php/ViewerVR_User_Manual");
				} catch (MalformedURLException e1) { e1.printStackTrace(); }
				
        try {
          new Statement(Class.forName("java.awt.Desktop"), "browse",
              new Object[]{
                helpURL.toURI()
          }).execute();
				} catch(Exception e2) {
          try {
            new Statement(Class.forName("org.jdesktop.jdic.desktop.Desktop"), "browse",
                new Object[]{
                  helpURL
            }).execute();
          } catch (Exception e3) {
            JOptionPane.showMessageDialog(null, "Please visit "+helpURL);
          }
				}
			}
			
		});
		menu.addMenu(helpMenu);
	}

	public JFrame getExternalFrame() {
		return sp.getExternalFrame();
	}
	
	public static void main(String[] args) throws IOException {
		ViewerVR vr = new ViewerVR();
		final String[][] examples = new String[][] {
				{ "Boy surface", "jrs/boy.jrs" },
				{ "Chen-Gackstatter surface", "obj/Chen-Gackstatter-4.obj" },
				{ "helicoid with 2 handles", "jrs/He2WithBoundary.jrs" },
				{ "tetranoid", "jrs/tetranoid.jrs" },
				{ "Wente torus", "jrs/wente.jrs" },
				{ "Schwarz P", "jrs/schwarz.jrs" },
				{ "Matheon baer", "jrs/baer.jrs" }
		};
		vr.addLoadTab(examples);
//		vr.addLoadTab(null);
		vr.addAlignTab();
		vr.addAppTab();
		vr.addEnvTab();
		vr.addToolTab();
		vr.addTexTab();
		vr.addHelpTab();
		vr.setGeneratePickTrees(true);
		vr.showPanel(false);
		ViewerApp vApp = vr.display();
		vApp.update();
		
		JFrame f = vApp.display();
		f.setSize(800, 600);
		f.validate();
		JFrame external = vr.getExternalFrame();
		external.setLocationRelativeTo(f);
	}

	public boolean isPanelInScene() {
		return panelInScene;
	}

	public void setPanelInScene(boolean b) {
		if (panelInScene != b) {
			panelInScene = b;
			panelInSceneCheckBox.setState(b);
			sp.setInScene(b, sceneRoot,  new Matrix(avatarPath.getMatrix(null)));
		}
	}
}