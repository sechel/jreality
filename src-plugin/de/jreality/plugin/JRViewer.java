package de.jreality.plugin;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Set;

import javax.swing.JPopupMenu;
import javax.swing.JRootPane;

import de.jreality.geometry.Primitives;
import de.jreality.io.JrScene;
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
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.CameraUtility;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class JRViewer {

	private SimpleController
		c = new SimpleController();
	private static WeakReference<JRViewer>
		lastViewer = new WeakReference<JRViewer>(null);
	
	
	static {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	
	public static enum ContentType {
		Direct,
		CenteredAndScaled,
		TerrainAligned,
		Custom
	}
	
	
	public JRViewer() {
		c.registerPlugin(new View());
		c.registerPlugin(new Scene());
		c.registerPlugin(new ToolSystemPlugin());
		c.registerPlugin(new Lights());
		lastViewer = new WeakReference<JRViewer>(this);
	}
	
	public JRViewer(JrScene s) {
		c.registerPlugin(new View());
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
		if (node != null) {
			if (!(node instanceof Geometry) && !(node instanceof SceneGraphComponent)) {
				throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed in JRViewer.setContent()");
			}
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
	
	
	
	
	public <T extends Plugin & Content> void registerCustomContent(T contentPlugin) {
		c.registerPlugin(contentPlugin);
	}
	
	
	
	public void addContentSupport(ContentType type) {
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ContentTools());
		c.registerPlugin(new ContentAppearance());
		switch (type) {
			case Direct:
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

	
	
	public void addVRSupport() {
		c.registerPlugin(new Avatar());
		c.registerPlugin(new Terrain());
		c.registerPlugin(new Sky());
	}
	
	
	
	public void addAudioSupport() {
		c.registerPlugin(new Audio());
		c.registerPlugin(new AudioOptions());
		c.registerPlugin(new AudioPreferences());
	}
	
	
	
	/**
	 * Quick display method with encompass
	 * @param node
	 */
	public static void display(SceneGraphNode node) {
		JRViewer v = new JRViewer();
		v.addContentSupport(ContentType.Direct);
		v.registerPlugin(new ContentInjectionPlugin(node, true));
		v.addBasicUI();
		v.startup();
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
			Content mc = PluginUtility.getPlugin(c, Content.class);
			mc.setContent(content);
			if (encompass) {
				View view = c.getPlugin(View.class);
				CameraUtility.encompass(view.getViewer());
			}
		}
		
	}

	/**
	 * Starts the default plug-in viewer
	 * @param args no arguments are read
	 */
	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.setContent(Primitives.icosahedron());
		v.startup();
	}

}
