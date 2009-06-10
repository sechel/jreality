package de.jreality.plugin;

import static de.jreality.util.CameraUtility.encompass;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.UIManager;

import de.jreality.io.JrScene;
import de.jreality.math.Pn;
import de.jreality.plugin.audio.Audio;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.audio.AudioPreferences;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.CenteredAndScaledContent;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.content.DirectContent;
import de.jreality.plugin.content.TerrainAlignedContent;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.plugin.scene.Avatar;
import de.jreality.plugin.scene.Lights;
import de.jreality.plugin.scene.Sky;
import de.jreality.plugin.scene.Terrain;
import de.jreality.plugin.scene.VRExamples;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.util.Secure;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class JRViewer {

	private SimpleController
		c = new SimpleController();
	private View
		view = new View();
	private ViewPreferences
		viewPreferences = new ViewPreferences();
	private static WeakReference<JRViewer>
		lastViewer = new WeakReference<JRViewer>(null);
	
	
	static {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		String lnfClass = UIManager.getSystemLookAndFeelClassName();
		if (lnfClass.contains("Aqua") || lnfClass.contains("Windows")) {
			Secure.setProperty("apple.laf.useScreenMenuBar", "true");
			try {
				UIManager.setLookAndFeel(lnfClass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static enum ContentType {
		Raw,
		CenteredAndScaled,
		TerrainAligned,
		Custom
	}
	
	
	/**
	 * Create a JRViewer with default scene and lights.
	 */
	public JRViewer() {
		this(true);
	}
	
	/**
	 * Create a JRViewer with default scene. Flag indicates
	 * whether to add the standard lights (plugin Lights) or not.
	 * @param addLights if true, standard lights are added.
	 */
	public JRViewer(boolean addLights) {
		this(null);
		if (addLights) {
			c.registerPlugin(new Lights());
		}
	}

	/**
	 * create a JRViewer with a custom scene.
	 * 
	 * @param s the scene
	 */
	public JRViewer(JrScene s) {
		setShowPanelSlots(false, false, false, false);
		c.setManageLookAndFeel(false);
		c.setPropertiesFile(null);
		c.registerPlugin(view);
		c.registerPlugin(viewPreferences);
		c.registerPlugin(new Scene(s));
		c.registerPlugin(new ToolSystemPlugin());
		lastViewer = new WeakReference<JRViewer>(this);
	}
	


	/**
	 * Returns the last created instance of JRViewer
	 * @return a JRViewer or null
	 */
	public static JRViewer getLastJRViewer() {
		return lastViewer.get();
	}
	
	
	/**
	 * Adds a plug-in to this JTViewer's registered plug-ins. The
	 * viewer application is then assembled on startup by these plug-ins.
	 * @param p
	 */
	public void registerPlugin(Plugin p) {
		c.registerPlugin(p);
	}
	
	
	/**
	 * Registered a set of plug-ins at once
	 * @param pSet a set of plug-ins
	 */
	public void registerPlugins(Set<Plugin> pSet) {
		for (Plugin p : pSet) {
			registerPlugin(p);
		}
	}
	
	
	/**
	 * Returns a previously registered plug in instance
	 * @param <T>
	 * @param clazz the class of the plug-in
	 * @return a plug-in instance or null if no such plug-in
	 * was registered
	 */
	public <T extends Plugin> T getPlugin(Class<T> clazz) {
		return c.getPlugin(clazz);
	}
	
	
	/**
	 * Sets a content node. The content node will be added to the
	 * scene graph on startup
	 * @param node
	 */
	public void setContent(SceneGraphNode node) {
		if (node == null) {
			return;
		}
		if (!(node instanceof Geometry) && !(node instanceof SceneGraphComponent)) {
			throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed in JRViewer.setContent()");
		}
		c.registerPlugin(new ContentInjectionPlugin(node));
	}
	
	
	/**
	 * Sets the properties File of this JRViewer's controller
	 * @param filename a file name
	 */
	public void setPropertiesFile(String filename) {
		File f = new File(filename);
		setPropertiesFile(f);
	}
	
	/**
	 * Sets the properties File of this JRViewer's controller
	 * @param filename a file name
	 */
	public void setPropertiesFile(File file) {
		c.setPropertiesFile(file);
	}
	
	/**
	 * Sets the properties InputStream of this JRViewer's controller
	 * @param in An InputStream
	 */
	public void setPropertiesInputStream(InputStream in) {
		c.setPropertiesInputStream(in);
	}
	
	/**
	 * Returns the controller of this JRViewer which is a SimpleController
	 * @return the SimpleController
	 */
	public SimpleController getController() {
		return c;
	}
	
	
	/**
	 * Starts this JRViewer's controller and installs all registered 
	 * plug-ins. Not registered but dependent plug-ins will be added
	 * automatically.
	 */
	public void startup() {
		c.startup();
	}
	
	/**
	 * Starts this JRViewer's controller and installs all registered 
	 * plug-ins. Not registered but dependent plug-ins will be added
	 * automatically.
	 * This method does not open the main window. Instead it returns the 
	 * root pane.
	 * @return
	 */
	public JRootPane startupLocal() {
		return c.startupLocal();
	}
	
	
	/**
	 * Configures the visibility of the shrink panels slots
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 */
	public void setShowPanelSlots(boolean left, boolean right, boolean top, boolean bottom) {
		view.setShowLeft(left);
		view.setShowRight(right);
		view.setShowTop(top);
		view.setShowBottom(bottom);
	}
	
	/**
	 * Show or hide the menu bar
	 * @param show
	 */
	public void setShowMenuBar(boolean show) {
		viewPreferences.setShowMenuBar(show);
	}
	
	/**
	 * Show or hide the tool bar 
	 * @param show
	 */
	public void setShowToolBar(boolean show) {
		viewPreferences.setShowToolBar(show);
	}
	
	
	/**
	 * Registers a custom {@link Content} plug-in which is
	 * an implementation of the abstract class {@link Content}
	 * @param contentPlugin a content plug-in
	 */
	public void registerCustomContent(Content contentPlugin) {
		c.registerPlugin(contentPlugin);
	}
	
	
	/**
	 * Registers one of the predefined content plug-ins
	 * @param type a content enumeration type
	 */
	public void addContentSupport(ContentType type) {
		switch (type) {
			case Raw:
				c.registerPlugin(new DirectContent());
				break;
			case CenteredAndScaled:
				c.registerPlugin(new CenteredAndScaledContent());
				break;
			case TerrainAligned:
				c.registerPlugin(new TerrainAlignedContent());
				break;
			case Custom:
				break;
		}
	}
	
	/**
	 * Registers advanced content tools. Includes an appearance
	 * inspector, transformation tools, and file loaders
	 */
	public void addContentUI() {
		c.registerPlugin(new ContentTools());
		c.registerPlugin(new ContentAppearance());
		c.registerPlugin(new ContentLoader());
	}
	
	/**
	 * Basic UI support like scene graph inspector, bean shell,
	 * background color, export menu, camera settings menu...
	 */
	public void addBasicUI() {
		c.registerPlugin(new Inspector());
		c.registerPlugin(new Shell());
		
		c.registerPlugin(new BackgroundColor());
		c.registerPlugin(new DisplayOptions());
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new ViewToolBar());
		
		c.registerPlugin(new ExportMenu());
		c.registerPlugin(new CameraMenu());
	}


	/**
	 * Virtual reality support. A sky box and environment map, 
	 * a terrain and a movable avatar
	 */
	public void addVRSupport() {
		c.registerPlugin(new Avatar());
		c.registerPlugin(new Terrain());
		c.registerPlugin(new Sky());
	}
	
	
	/**
	 * Audio plug-ins. An options panel and audio preferences.
	 */
	public void addAudioSupport() {
		c.registerPlugin(new Audio());
		c.registerPlugin(new AudioOptions());
		c.registerPlugin(new AudioPreferences());
	}
	
	
	
	/**
	 * Quick display method with encompass
	 * @param node
	 */
	public static Viewer display(SceneGraphNode node) {
		JRViewer v = new JRViewer();
		v.registerPlugin(new DirectContent());
		v.registerPlugin(new ContentTools());
		if (node != null) {
			v.registerPlugin(new ContentInjectionPlugin(node, true));
		} else {
			v.registerPlugin(new ContentLoader());
		}
		v.addBasicUI();
		v.startup();
		return v.getPlugin(View.class).getViewer();
	}

	
	/**
	 * Call after startup. Encompasses the view
	 */
	public void encompassEuclidean() {
		Scene scene = getPlugin(Scene.class);
		SceneGraphPath avatarPath = scene.getAvatarPath();
		SceneGraphPath contentPath = scene.getContentPath();
		SceneGraphPath cameraPath = scene.getCameraPath();
		try {
			encompass(avatarPath, contentPath, cameraPath, 1.75, Pn.EUCLIDEAN);
		} catch (Exception e) {}
	}
	
	
	
	private static class ContentInjectionPlugin extends Plugin {

		private SceneGraphNode
			content = null;
		private boolean
			encompass = false;
		
		
		public ContentInjectionPlugin(SceneGraphNode content) {
			this.content = content; 
		}
		
		
		public ContentInjectionPlugin(SceneGraphNode content, boolean encompass) {
			this(content);
			this.encompass = encompass;
		}
		
		@Override
		public PluginInfo getPluginInfo() {
			return new PluginInfo("JRViewer Content Plugin", "jReality Group");
		}
		
		@Override
		public void install(Controller c) throws Exception {
			super.install(c);
			if (content == null) {
				return;
			}
			Content mc = JRViewerUtility.getContentPlugin(c);
			if (mc == null) {
				System.err.println("No content plug-in registered");
				return;
			}
			mc.setContent(content);
			if (encompass) {
				Scene scene = c.getPlugin(Scene.class);
				JRViewerUtility.encompassEuclidean(scene);
			}
		}
		
	}

	/**
	 * Starts the default plug-in viewer
	 * @param args no arguments are read
	 */
	public static void main(String[] args) {
		Set<String> params = new HashSet<String>();
		for (String param : args) params.add(param.toLowerCase());
		JRViewer v = new JRViewer();
		v.addBasicUI();
		if (params.contains("-vr")) {
			v.addContentUI();
			v.addVRSupport();
			v.addContentSupport(ContentType.TerrainAligned);
			v.setShowPanelSlots(true, false, false, false);
			VRExamples vrExamples = new VRExamples();
			vrExamples.getShrinkPanel().setShrinked(false);
			v.registerPlugin(vrExamples);
		} else {
			v.registerPlugin(new ContentLoader());
			v.registerPlugin(new ContentTools());
			v.getPlugin(Inspector.class).setInitialPosition(ShrinkPanelPlugin.SHRINKER_LEFT);
			v.addContentSupport(ContentType.CenteredAndScaled);
			v.setShowPanelSlots(true, false, false, false);
		}
		v.startup();
	}

}
