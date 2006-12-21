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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.Statement;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import de.jreality.geometry.GeometryUtility;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.DirectionalLight;
import de.jreality.scene.PointLight;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.scene.pick.AABBPickSystem;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.Tool;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
//import de.jreality.sunflow.RenderOptions;
//import de.jreality.sunflow.Sunflow;
import de.jreality.swing.ScenePanel;
import de.jreality.tools.DuplicateTriplyPeriodicTool;
import de.jreality.tools.HeadTransformationTool;
import de.jreality.tools.PickShowTool;
import de.jreality.tools.ShipNavigationTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.ui.viewerapp.ViewerAppMenu;
import de.jreality.util.PickUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jreality.util.Secure;


public class ViewerVR {

	// defaults for light panel
	private static final double DEFAULT_SUN_LIGHT_INTENSITY = 1;
	private static final double DEFAULT_HEAD_LIGHT_INTENSITY = .3;
	private static final double DEFAULT_SKY_LIGHT_INTENSITY = .2;

	// defaults for preferences:
	private static final boolean DEFAULT_PANEL_IN_SCENE = true;


	// other static constants:

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

	// diam of the terrain
	private static final double TERRAIN_SIZE=100;

	// parts of the scene that do not change
	private SceneGraphComponent sceneRoot = new SceneGraphComponent("root"),
	sceneNode = new SceneGraphComponent("scene"),
	avatarNode = new SceneGraphComponent("avatar"),
	camNode = new SceneGraphComponent("cam"),
	lightNode = new SceneGraphComponent("sun"),
	terrainNode = new SceneGraphComponent("terrain");
	private Appearance rootAppearance = new Appearance("app"),
	terrainAppearance = new Appearance("terrain app"),
	contentAppearance = new Appearance("content app");
	private SceneGraphComponent alignmentComponent, currentContent;

	private SceneGraphPath cameraPath, avatarPath, emptyPickPath;

	// default lights
	private DirectionalLight sunLight = new DirectionalLight();
	private PointLight headLight = new PointLight();
	private DirectionalLight skyLight = new DirectionalLight();

	// the scale of the currently loaded content
	private double objectScale=1;

	// the scale of the currently loaded terrain
	private double terrainScale=1;

	private ScenePanel sp;

	// the default panel content - the tabs containing plugin panels
	private Container defaultPanel;

	// macosx hack - split panels into two groups
	// such that for each group the tabs fit into one row
	private JTabbedPane geomTabs;
	private JTabbedPane appearanceTabs;

	// the current environment
	private ImageData[] environment;

	// flag indicating wether aabb-trees will be generated
	// when content is set
	private boolean generatePickTrees;

	private JCheckBoxMenuItem panelInSceneCheckBox;

	// navigation tools
	private ShipNavigationTool shipNavigationTool;
	private HeadTransformationTool headTransformationTool;

	// content alignment
	private double contentSize=20;
	private double contentOffset=.3;
	private Matrix contentMatrix=null;

	// list of registered plugins
	private List<PluginVR> plugins=new ArrayList<PluginVR>();

	@SuppressWarnings("serial")
	public ViewerVR() {

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
				Tool t = (Tool) Class.forName("de.jreality.tools.PortalHeadMoveTool").newInstance();
				camNode.addTool(t);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		sceneRoot.addTool(new PickShowTool(null, 0.005));

		terrainAppearance.setAttribute("showLines", false);
		terrainAppearance.setAttribute("showPoints", false);
		terrainAppearance.setAttribute("diffuseColor", Color.white);
		terrainAppearance.setAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED, true);
		terrainAppearance.setAttribute(CommonAttributes.SPECULAR_COEFFICIENT, 0);
		terrainAppearance.setAttribute(CommonAttributes.SPECULAR_COLOR, Color.black);
		terrainNode.setAppearance(terrainAppearance);
		sceneRoot.addChild(terrainNode);

		// content appearearance
		contentAppearance.setName("contentApp");
		sceneNode.setAppearance(contentAppearance);

		sceneRoot.addChild(sceneNode);

		// swing widgets
		makeControlPanel();

		panelInSceneCheckBox = new JCheckBoxMenuItem( new AbstractAction("Show panel in scene") {
			public void actionPerformed(ActionEvent e) {
				setPanelInScene(panelInSceneCheckBox.getState());
			}
		});

		//		 lights
		sunLight = new DirectionalLight();
		sunLight.setName("sun light");
		SceneGraphComponent lightNode = new SceneGraphComponent("sun");
		lightNode.setLight(sunLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				//new double[] { 0, 1, 1 }).assignTo(lightNode);
				//new double[] { 0.39, .24, 0.89 }).assignTo(lightNode);
				new double[] { 0.39, Math.sqrt(.39*.39+0.89*0.89), 0.89 }).assignTo(lightNode);
		getSceneRoot().addChild(lightNode);

		SceneGraphComponent skyNode = new SceneGraphComponent();
		skyLight = new DirectionalLight();
		skyLight.setAmbientFake(true);
		skyLight.setName("sky light");
		skyNode.setLight(skyLight);
		MatrixBuilder.euclidean().rotateFromTo(new double[] { 0, 0, 1 },
				new double[] { 0, 1, 0 }).assignTo(skyNode);
		getSceneRoot().addChild(skyNode);

		headLight.setAmbientFake(true);
		headLight.setFalloff(1, 0, 0);
		headLight.setName("camera light");
		headLight.setColor(new Color(255,255,255,255));
		getCameraPath().getLastComponent().setLight(headLight);

		setHeadLightIntensity(DEFAULT_HEAD_LIGHT_INTENSITY);
		setSunIntensity(DEFAULT_SUN_LIGHT_INTENSITY);
		setSkyLightIntensity(DEFAULT_SKY_LIGHT_INTENSITY);

		setAvatarPosition(0, 0, 25);

		setTerrain(TerrainPluginVR.FLAT_TERRAIN);
	}

	public SceneGraphComponent getTerrain() {
		return terrainNode.getChildNodes().size() > 0 ? terrainNode.getChildComponent(0) : null;
	}

	public void setTerrain(final SceneGraphComponent c) {
		while (terrainNode.getChildComponentCount() > 0) terrainNode.removeChild(terrainNode.getChildComponent(0));
		if (c==null) return;
		Scene.executeWriter(terrainNode, new Runnable() {
			public void run() {
				//while (terrainNode.getChildComponentCount() > 0) terrainNode.removeChild(terrainNode.getChildComponent(0));
				MatrixBuilder.euclidean().assignTo(terrainNode);
				terrainNode.addChild(c);
				Rectangle3D bounds = GeometryUtility.calculateBoundingBox(terrainNode);
				// scale
				double[] extent = bounds.getExtent();
				double maxExtent = Math.max(extent[0], extent[2]);
				if (maxExtent != 0) {
					terrainScale = TERRAIN_SIZE / maxExtent;
					double[] translation = bounds.getCenter();

					// determine offset in y-direction (up/down)
					AABBPickSystem ps = new AABBPickSystem();
					ps.setSceneRoot(terrainNode);
					List<PickResult> picks = ps.computePick(translation, new double[]{0,-1,0,0});
					if (picks.isEmpty()) {
						picks = ps.computePick(translation, new double[]{0,1,0,0});
					}
					final double offset=picks.isEmpty() ? bounds.getMinY() : picks.get(0).getWorldCoordinates()[1];
//					System.out.println("offset="+offset);
//					System.out.println("min-y="+bounds.getMinY());
//					System.out.println("scale="+scale);

					translation[1] = -terrainScale * offset;
					translation[0] *= -terrainScale;
					translation[2] *= -terrainScale;

					MatrixBuilder mb = MatrixBuilder.euclidean().translate(
							translation).scale(terrainScale);
					if (terrainNode.getTransformation() != null)
						mb.times(terrainNode.getTransformation().getMatrix());
					mb.assignTo(terrainNode);
				}
			}
		});
		if (getShipNavigationTool().getGravity() != 0) {
			// move the avatar onto the next floor
			Matrix m = new Matrix(avatarNode.getTransformation());
			AABBPickSystem ps = new AABBPickSystem();
			ps.setSceneRoot(terrainNode);
			double[] pos = m.getColumn(3);
			List<PickResult> picks = ps.computePick(pos, new double[]{0,-1,0,0});
			if (picks.isEmpty()) {
				picks = ps.computePick(pos, new double[]{0,1,0,0});
			}
			if (!picks.isEmpty()) {
				setAvatarHeight(picks.get(0).getWorldCoordinates()[1]);
			}
		}
		for (PluginVR plugin : plugins) plugin.terrainChanged();
	}

	public ImageData[] getEnvironment() {
		return environment;
	}

	public void setEnvironment(ImageData[] datas) {
		environment = datas;
		for (PluginVR plugin : plugins) plugin.environmentChanged();
	}

	private void makeControlPanel() {
		sp = AccessController.doPrivileged(new PrivilegedAction<ScenePanel>() {
			public ScenePanel run() {
				return new ScenePanel();
			}
		});
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.setBelowGround(0);
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

	public void registerPlugin(PluginVR plugin) {
		plugin.setViewerVR(this);
		JPanel panel = plugin.getPanel();
		if (panel != null) {
			// XXX: macosx hack...
			((plugins.size() % 2) == 0 ? appearanceTabs:geomTabs).add(plugin.getName(), panel);
		}
		sp.getFrame().pack();
		plugins.add(plugin);
	}

	public void switchToDefaultPanel() {
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.getFrame().setContentPane(defaultPanel);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}

	public void switchTo(JPanel panel) {
		sp.getFrame().setVisible(false);
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(PANEL_WIDTH);
		sp.setAboveGround(PANEL_ABOVE_GROUND);
		sp.getFrame().setContentPane(panel);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}

	public void switchToFileChooser(JComponent fileChooser) {
		sp.getFrame().setVisible(false);
		sp.getFrame().setVisible(false);
		sp.setPanelWidth(FILE_CHOOSER_PANEL_WIDTH);
		sp.setAboveGround(FILE_CHOOSER_ABOVE_GROUND);
		sp.getFrame().setContentPane(fileChooser);
		sp.getFrame().pack();
		sp.getFrame().setVisible(true);
	}

	public void showPanel() {
		sp.show(getSceneRoot(), new Matrix(avatarPath.getMatrix(null)));
	}

	public void setContent(SceneGraphComponent content) {
		if (alignmentComponent != null
				&& sceneNode.getChildNodes().contains(alignmentComponent)) {
			sceneNode.removeChild(alignmentComponent);
		}
		SceneGraphComponent parent = new SceneGraphComponent();
		parent.setName("content");
		parent.addChild(content);
		alignmentComponent = parent;
		currentContent = content;
		if (isGeneratePickTrees()) PickUtility.assignFaceAABBTrees(content);
		Rectangle3D bounds = GeometryUtility
		.calculateChildrenBoundingBox(alignmentComponent);
		// scale
		double[] extent = bounds.getExtent();
		double[] center = bounds.getCenter();
		content.addTool(new DuplicateTriplyPeriodicTool(
				extent[0],extent[1],extent[2],center[0],center[1],center[2]));
		objectScale = Math.max(Math.max(extent[0], extent[2]), extent[1]);
		sceneNode.addChild(alignmentComponent);
		alignContent();
		for (PluginVR plugin : plugins) plugin.contentChanged();
	}

	public void alignContent() {
		final double diam=getContentSize();
		final double offset=getContentOffset();
		final Matrix rotation=getContentMatrix();
		Scene.executeWriter(sceneNode, new Runnable() {
			public void run() {
				if (rotation != null) {
					rotation.assignTo(alignmentComponent);
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

	/**
	 * Initializes a ViewerApp to display the scene. Restores
	 * preferences for all plugins. Set custom values after
	 * calling this method!
	 * 
	 * @return A ViewerApp to display the scene.
	 */
	public ViewerApp initialize() {
		restorePreferences();
		ViewerApp viewerApp = new ViewerApp(sceneRoot, cameraPath, emptyPickPath, avatarPath);
		tweakMenu(viewerApp);
		return viewerApp;
	}

	/**
	 * @deprecated use {@link ViewerVR.initialize()}
	 */
	public ViewerApp display() {
		return initialize();
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

	public void restoreDefaults() {
		setPanelInScene(DEFAULT_PANEL_IN_SCENE);	
		for (PluginVR plugin : plugins) plugin.restoreDefaults();

	}

	public void savePreferences() {
		Preferences prefs = getPreferences();
		prefs.putBoolean("panelInScene", isPanelInScene());
		for (PluginVR plugin : plugins) plugin.storePreferences(prefs);
		try {
			prefs.flush();
		} catch(BackingStoreException e){
			e.printStackTrace();
		}
	}

	private Preferences getPreferences() {
		return AccessController.doPrivileged(new PrivilegedAction<Preferences>() {
			public Preferences run() {
				return Preferences.userNodeForPackage(ViewerVR.class);
			}
		});
	}

	public void restorePreferences() {
		Preferences prefs = getPreferences();
		setPanelInScene(prefs.getBoolean("panelInScene", DEFAULT_PANEL_IN_SCENE));
		for (PluginVR plugin : plugins) plugin.restorePreferences(prefs);

	}

	public double getObjectScale() {
		return objectScale;
	}

	public double getTerrainScale() {
		return terrainScale;
	}

	public boolean isGeneratePickTrees() {
		return generatePickTrees;
	}

	public void setGeneratePickTrees(boolean generatePickTrees) {
		this.generatePickTrees = generatePickTrees;
	}

	private void tweakMenu(final ViewerApp vapp) {
		ViewerAppMenu menu = vapp.getMenu();
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

//		Action bakeTerrain = new AbstractAction("Bake") {
//			private static final long serialVersionUID = -4212517852052390335L;
//			{
//				putValue(SHORT_DESCRIPTION, "Bake terrain lightmap");
//				putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
//			}
//			public void actionPerformed(ActionEvent e) {
//				bakeTerrain(vapp.getViewer());
//			}
//		};
//		settings.add(bakeTerrain);

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

//	public void bakeTerrain(Viewer v) {
//		RenderOptions opts = new RenderOptions();
//		opts.setThreadsLowPriority(true);
//		List<SceneGraphPath> paths = SceneGraphUtility.getPathsBetween(
//				getSceneRoot(),
//				getTerrain().getChildComponent(0)
//		);
//		SceneGraphPath bakingPath = paths.get(0);
//		Sunflow.renderToTexture(
//				v,
//				new Dimension(256,256),
//				opts,
//				bakingPath,
//				getTerrainAppearance()
//		);
//	}
	
	public JFrame getExternalFrame() {
		return sp.getExternalFrame();
	}

	public boolean isPanelInScene() {
		return panelInSceneCheckBox.isSelected();
	}

	public void setPanelInScene(boolean b) {
		panelInSceneCheckBox.setState(b);
		sp.setInScene(b, sceneRoot,  new Matrix(avatarPath.getMatrix(null)));
	}

	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	public SceneGraphComponent getTerrainNode() {
		return terrainNode;
	}

	public Appearance getContentAppearance() {
		return contentAppearance;
	}

	public ShipNavigationTool getShipNavigationTool() {
		return shipNavigationTool;
	}

	public HeadTransformationTool getHeadTransformationTool() {
		return headTransformationTool;
	}

	public SceneGraphComponent getCurrentContent() {
		return currentContent;
	}

	public Appearance getRootAppearance() {
		return rootAppearance;
	}

	public SceneGraphPath getCameraPath() {
		return cameraPath;
	}

	public Appearance getTerrainAppearance() {
		return terrainAppearance;
	}

	public Matrix getContentMatrix() {
		return contentMatrix;
	}

	public void setContentMatrix(Matrix contentMatrix) {
		this.contentMatrix = contentMatrix;
		alignContent();
	}

	public double getContentOffset() {
		return contentOffset;
	}

	public void setContentOffset(double contentOffset) {
		this.contentOffset = contentOffset;
		alignContent();
	}

	public double getContentSize() {
		return contentSize;
	}

	public void setContentSize(double contentSize) {
		this.contentSize = contentSize;
		alignContent();
	}

	public Color getSunLightColor() {
		return sunLight.getColor();
	}

	public void setSunLightColor(Color c) {
		sunLight.setColor(c);
	}

	public Color getHeadLightColor() {
		return headLight.getColor();
	}

	public void setHeadLightColor(Color c) {
		headLight.setColor(c);
	}

	public Color getSkyLightColor() {
		return skyLight.getColor();
	}

	public void setSkyLightColor(Color c) {
		skyLight.setColor(c);
	}

	public double getSunIntensity() {
		return sunLight.getIntensity();
	}

	public void setSunIntensity(double x) {
		sunLight.setIntensity(x);
	}

	public double getHeadLightIntensity() {
		return headLight.getIntensity();
	}

	public void setHeadLightIntensity(double x) {
		headLight.setIntensity(x);
	}

	public double getSkyLightIntensity() {
		return skyLight.getIntensity();
	}

	public void setSkyLightIntensity(double x) {
		skyLight.setIntensity(x);
	}

	public void setLightIntensity(double intensity) {
		sunLight.setIntensity(intensity);
	}

	public double getLightIntensity() {
		return sunLight.getIntensity();
	}

	public void addEnvTab() {
		registerPlugin(new EnvironmentPluginVR());
	}

	public void addTerrainTab() {
		registerPlugin(new TerrainPluginVR());
	}

	public void addAppTab() {
		registerPlugin(new AppearancePluginVR());
	}

	public void addTerrainAppTab() {
		registerPlugin(new TerrainAppearancePluginVR());
	}

	public void addLightTab() {
		registerPlugin(new LightPluginVR());
	}

	public void addAlignTab() {
		registerPlugin(new AlignPluginVR());
	}

	public void addLoadTab(final String[][] examples) {
		registerPlugin(new LoadPluginVR(examples));
	}

	public void addToolTab() {
		registerPlugin(new ToolPluginVR());
	}

	public void addTexTab() {
		registerPlugin(new TexturePluginVR());
	}

	/**
	 * @deprecated
	 */
	public void addHelpTab() {
	}

	public static void main(String[] args) {
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
		vr.addAlignTab();
		vr.addAppTab();
		vr.addEnvTab();
		vr.addTerrainTab();
		vr.addToolTab();
		vr.addTexTab();
		//vr.addLightTab();
		vr.setGeneratePickTrees(true);
		vr.showPanel();
		ViewerApp vApp = vr.initialize();
		vApp.update();

		JFrame f = vApp.display();
		f.setSize(800, 600);
		f.validate();
		JFrame external = vr.getExternalFrame();
		external.setLocationRelativeTo(f);
	}

}