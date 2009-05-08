package de.jreality.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import javax.swing.JRootPane;

import de.jreality.plugin.audio.AudioLauncher;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.view.AlignedContent;
import de.jreality.plugin.view.Background;
import de.jreality.plugin.view.CameraStand;
import de.jreality.plugin.view.ContentAppearance;
import de.jreality.plugin.view.ContentLoader;
import de.jreality.plugin.view.ContentTools;
import de.jreality.plugin.view.DisplayOptions;
import de.jreality.plugin.view.Export;
import de.jreality.plugin.view.Inspector;
import de.jreality.plugin.view.Lights;
import de.jreality.plugin.view.ManagedContent;
import de.jreality.plugin.view.ManagedContentGUI;
import de.jreality.plugin.view.Shell;
import de.jreality.plugin.view.StatusBar;
import de.jreality.plugin.view.View;
import de.jreality.plugin.view.ViewMenuBar;
import de.jreality.plugin.view.ViewPreferences;
import de.jreality.plugin.view.ViewToolBar;
import de.jreality.plugin.view.ZoomTool;
import de.jreality.plugin.vr.Avatar;
import de.jreality.plugin.vr.HeadUpDisplay;
import de.jreality.plugin.vr.Sky;
import de.jreality.plugin.vr.Terrain;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class JRViewer {

	private SimpleController
		c = new SimpleController();
	private View
		view = new View();
	private SceneGraphNode
		content = null;
	
	
	protected JRViewer() {
		c.registerPlugin(view);
		c.registerPlugin(new JRViewerContentInjection());
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
	 * Creates a JRViewer and registers only the View class
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
		JRViewer v = createEmptyViewer();
		v.registerPlugin(new CameraStand());
		v.registerPlugin(new Lights());
		v.registerPlugin(new Background());
		v.registerPlugin(new ViewMenuBar());
		v.registerPlugin(new AlignedContent());
		v.registerPlugin(new ViewPreferences());
		v.registerPlugin(new Inspector());
		v.registerPlugin(new Shell());
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentTools());
		v.registerPlugin(new DisplayOptions());
		v.registerPlugin(new ViewToolBar());
		v.registerPlugin(new Export());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new ZoomTool());
		v.registerPlugin(new StatusBar());
		v.registerPlugin(new ManagedContent());
		v.registerPlugin(new ManagedContentGUI());
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
		v.registerPlugin(new AudioLauncher());
		return v;
	}
	
	
	/**
	 * Creates a JRViewer with the recommended VR-viewer plug-in set.
	 * @return A configured JRViewer instance
	 */
	public static JRViewer createViewerVR() {
		JRViewer v = createEmptyViewer();
		v.registerPlugin(new CameraStand());
		v.registerPlugin(new Lights());
		v.registerPlugin(new Background());
		v.registerPlugin(new ViewMenuBar());
		v.registerPlugin(new AlignedContent());
		v.registerPlugin(new ViewPreferences());
		v.registerPlugin(new Inspector());
		v.registerPlugin(new Shell());
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new DisplayOptions());
		v.registerPlugin(new ViewToolBar());
		v.registerPlugin(new Export());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new ZoomTool());
		v.registerPlugin(new StatusBar());
		v.registerPlugin(new ManagedContent());
		v.registerPlugin(new ManagedContentGUI());
		v.registerPlugin(new Avatar());
		v.registerPlugin(new HeadUpDisplay());
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
		v.registerPlugin(new AudioLauncher());
		return v;
	}
	
	
	private class JRViewerContentInjection extends Plugin {

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
			ManagedContent mc = c.getPlugin(ManagedContent.class);
			mc.setContent(getClass(), root);
		}
		
	}
	
	
	/**
	 * Starts the default plug-in viewer
	 * @param args no arguments are read
	 */
	public static void main(String[] args) {
		JRViewer.createViewerVRWithAudio().startup();
	}

}
