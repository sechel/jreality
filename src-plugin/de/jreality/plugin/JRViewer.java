package de.jreality.plugin;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.JRootPane;

import de.jreality.plugin.audio.Audio;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.StatusBar;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewPreferences;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.CenteredAndScaledContent;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.plugin.scene.Avatar;
import de.jreality.plugin.scene.Lights;
import de.jreality.plugin.scene.Sky;
import de.jreality.plugin.scene.Terrain;
import de.jreality.plugin.view.ManagedContent;
import de.jreality.plugin.view.ManagedContentGUI;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.lnfswitch.LookAndFeelSwitch;
import de.varylab.jrworkspace.plugin.lnfswitch.plugin.CrossPlatformLnF;
import de.varylab.jrworkspace.plugin.lnfswitch.plugin.NimbusLnF;
import de.varylab.jrworkspace.plugin.lnfswitch.plugin.SystemLookAndFeel;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class JRViewer {

	private SimpleController
		c = new SimpleController();
	private View
		view = new View();
	private ContentInjectionPlugin	
		contentInjectionPlugin = new ContentInjectionPlugin();
	private SceneGraphNode
		content = null;
	private static WeakReference<JRViewer>
		lastViewer = new WeakReference<JRViewer>(null);
	
	
	static {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	
	protected JRViewer() {
		c.registerPlugin(view);
		c.registerPlugin(contentInjectionPlugin);
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
		if (node != null) {
			if (!(node instanceof Geometry) && !(node instanceof SceneGraphComponent)) {
				throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed in JRViewer.setContent()");
			}
		}
		content = node;
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
	 * Returns the View that is registered with this JRViewer's controller
	 * @return The View of this JRViewer
	 */
	public View getView() {
		return view;
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
	 * Creates a viewer plug-in set and invokes startup. The given
	 * content node will be added to the scene graph.
	 * @param node
	 */
	public static void display(SceneGraphNode node) {
		JRViewer v = JRViewer.createViewer();
		v.setContent(node);
		v.startup();
	}
	
	/**
	 * Creates a viewer plug-in set and invokes startup. The given
	 * content node will be added to the scene graph.
	 * @param node
	 */
	public static void displayWithAudio(SceneGraphNode node) {
		JRViewer v = JRViewer.createViewerWithAudio();
		v.setContent(node);
		v.startup();
	}
	
	/**
	 * Creates a VR-viewer plug-in set and invokes startup. The given
	 * content node will be added to the scene graph.
	 * @param node
	 */
	public static void displayVR(SceneGraphNode node) {
		JRViewer v = JRViewer.createViewerVR();
		v.setContent(node);
		v.startup();
	}
	

	/**
	 * Creates a VR-viewer plug-in set and invokes startup. The given
	 * content node will be added to the scene graph.
	 * @param node
	 */
	public static void displayVRWithAudio(SceneGraphNode node) {
		JRViewer v = JRViewer.createViewerVRWithAudio();
		v.setContent(node);
		v.startup();
	}
	
	
	/**
	 * Creates a JRViewer and registers only the View class, without a default scene
	 * @return the viewer instance
	 */
	public static JRViewer createEmptyViewer() {
		return new JRViewer();
	}
	
			
	/**
	 * Creates a JRViewer with the recommended viewer plug-in set.
	 * @return A configured JRViewer instance
	 */
	public static JRViewer createViewer() {
		JRViewer v = new JRViewer();
		v.registerPlugin(new Lights());
		v.registerPlugin(new BackgroundColor());
		v.registerPlugin(new ViewMenuBar());
		v.registerPlugin(new CenteredAndScaledContent());
		v.registerPlugin(new ViewPreferences());
		v.registerPlugin(new Inspector());
		v.registerPlugin(new Shell());
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new DisplayOptions());
		v.registerPlugin(new ViewToolBar());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new StatusBar());
		v.registerPlugin(new ExportMenu());
		v.registerPlugin(new ManagedContent());
		v.registerPlugin(new ManagedContentGUI());
		v.registerPlugin(new LookAndFeelSwitch());
		v.registerPlugin(new CrossPlatformLnF());
		v.registerPlugin(new SystemLookAndFeel());
		v.registerPlugin(new NimbusLnF());
		return v;
	}

	/**
	 * Creates a JRViewer with the recommended viewer plug-in set.
	 * In addition to the viewer plug-ins audio plug-ins are registered. 
	 * @return A configured JRViewer instance
	 */
	public static JRViewer createViewerWithAudio() {
		JRViewer v = createViewer();
		v.registerPlugin(new AudioOptions());
		v.registerPlugin(new Audio());
		return v;
	}
	
	
	/**
	 * Creates a JRViewer with the recommended VR-viewer plug-in set.
	 * @return A configured JRViewer instance
	 */
	public static JRViewer createViewerVR() {
		JRViewer v = createViewer();
		v.registerPlugin(new Avatar());
		v.registerPlugin(new Sky());
		v.registerPlugin(new Terrain());
		return v;
	}
	
	
	/**
	 * Creates a JRViewer with the recommended VR-viewer plug-in set.
	 * @return A configured JRViewer instance
	 */
	public static JRViewer createViewerVRWithAudio() {
		JRViewer v = createViewerVR();
		v.registerPlugin(new AudioOptions());
		v.registerPlugin(new Audio());
		return v;
	}

	
	private class ContentInjectionPlugin extends Plugin {

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
			SceneGraphComponent root = null;
			if (content instanceof Geometry) {
				root = new SceneGraphComponent("JRViewer Content");
				root.setGeometry((Geometry)content);
			} else {
				root = (SceneGraphComponent)content;
			}
			Content mc = PluginUtility.getPlugin(c, Content.class);
			mc.setContent(root);
		}
		
	}

	/**
	 * Starts the default plug-in viewer
	 * @param args no arguments are read
	 */
	public static void main(String[] args) {
		JRViewer.createViewerVR().startup();
	}

}
