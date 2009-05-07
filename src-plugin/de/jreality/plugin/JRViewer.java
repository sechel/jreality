package de.jreality.plugin;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

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
import de.varylab.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class JRViewer {

	private SimpleController
		c = new SimpleController();
	private List<JComponent>
		accessories = new LinkedList<JComponent>();
	private SceneGraphNode
		content = null;
	
	
	protected JRViewer() {
		c.registerPlugin(new AccessoryPlugin());
		c.registerPlugin(new ViewerContentPlugin());
	}
	
	
	public void registerPlugin(Plugin p) {
		c.registerPlugin(p);
	}
	
	
	public void registerPlugins(Set<Plugin> pSet) {
		for (Plugin p : pSet) {
			registerPlugin(p);
		}
	}
	
	public void addAccessory(JComponent c) {
		accessories.add(c);
	}
	
	public void setContent(SceneGraphNode node) {
		if (node != null) {
			if (!(node instanceof Geometry) && !(node instanceof SceneGraphComponent)) {
				throw new IllegalArgumentException("Only Geometry or SceneGraphComponent allowed in JRViewer.setContent()");
			}
		}
		content = node;
	}
	
	
	public void startup() {
		c.startup();
	}
	
	
	public static JRViewer display(SceneGraphNode node) {
		JRViewer v = JRViewer.createViewer();
		v.setContent(node);
		v.startup();
		return v;
	}
	
	
	public static JRViewer createViewer() {
		JRViewer v = new JRViewer();
		Set<Plugin> pSet = new HashSet<Plugin>();
		pSet.add(new View());
		pSet.add(new CameraStand());
		pSet.add(new Lights());
		pSet.add(new Background());
		pSet.add(new ViewMenuBar());
		pSet.add(new AlignedContent());
		pSet.add(new ViewPreferences());
		pSet.add(new Inspector());
		pSet.add(new Shell());
		pSet.add(new ContentAppearance());
		pSet.add(new ContentTools());
		pSet.add(new DisplayOptions());
		pSet.add(new ViewToolBar());
		pSet.add(new Export());
		pSet.add(new ContentLoader());
		pSet.add(new ZoomTool());
		pSet.add(new StatusBar());
		pSet.add(new ManagedContent());
		pSet.add(new ManagedContentGUI());
		v.registerPlugins(pSet);
		return v;
	}

	
	
	public static JRViewer createViewerVR() {
		JRViewer v = new JRViewer();
		Set<Plugin> pSet = new HashSet<Plugin>();
		pSet.add(new View());
		pSet.add(new CameraStand());
		pSet.add(new Lights());
		pSet.add(new Background());
		pSet.add(new ViewMenuBar());
		pSet.add(new AlignedContent());
		pSet.add(new ViewPreferences());
		pSet.add(new Inspector());
		pSet.add(new Shell());
		pSet.add(new ContentAppearance());
		pSet.add(new DisplayOptions());
		pSet.add(new ViewToolBar());
		pSet.add(new Export());
		pSet.add(new ContentLoader());
		pSet.add(new ZoomTool());
		pSet.add(new StatusBar());
		pSet.add(new ManagedContent());
		pSet.add(new ManagedContentGUI());
		pSet.add(new Avatar());
		pSet.add(new HeadUpDisplay());
		pSet.add(new Sky());
		pSet.add(new Terrain());
		v.registerPlugins(pSet);
		return v;
	}
	
	
	
	public static void main(String[] args) {
		JRViewer.createViewer().startup();
	}
	
	
	private class AccessoryPlugin extends Plugin {

		@Override
		public PluginInfo getPluginInfo() {
			return new PluginInfo("Accessory Plugin", "jReality Group");
		}
		
		
		@Override
		public void install(Controller c) throws Exception {
			super.install(c);
			View v = c.getPlugin(View.class);
			int i = 0;
			for (JComponent comp : accessories) {
				String name = "Accessory " + i++;
				if (comp.getName() != null) {
					name = comp.getName();
				}
				ShrinkPanel sp = new ShrinkPanel(name);
				sp.add(comp);
				v.getLeftSlot().addShrinkPanel(sp);
			}
		}
		
	}
	
	
	private class ViewerContentPlugin extends Plugin {

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
	

}
