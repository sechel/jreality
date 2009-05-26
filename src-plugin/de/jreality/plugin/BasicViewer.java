package de.jreality.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import de.jreality.io.JrScene;
import de.jreality.plugin.audio.Audio;
import de.jreality.plugin.audio.AudioOptions;
import de.jreality.plugin.audio.AudioPreferences;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.DebugController;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.Shell;
import de.jreality.plugin.basic.ToolSystemPlugin;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.content.DirectContent;
import de.jreality.plugin.content.TerrainAlignedContent;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jreality.plugin.menu.ViewMenuBar;
import de.jreality.plugin.scene.Avatar;
import de.jreality.plugin.scene.Lights;
import de.jreality.plugin.scene.Sky;
import de.jreality.plugin.scene.Terrain;
import de.jreality.reader.ReaderJRS;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.Input;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class BasicViewer {
	
	static {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	static class SwitchCameraPath extends ShrinkPanelPlugin {

		public SwitchCameraPath() {
			
		}
		@Override
		public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
			return View.class;
		}

		@Override
		public PluginInfo getPluginInfo() {
			return new PluginInfo();
		}
		
		@Override
		public void install(Controller c) throws Exception {
			super.install(c);
			final Scene scene = c.getPlugin(Scene.class);
			JButton b = new JButton("new cam path...");
			shrinkPanel.add(b);
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SceneGraphComponent r = scene.getSceneRoot();
					SceneGraphComponent na = new SceneGraphComponent("new avatar");
					SceneGraphComponent ncc = new SceneGraphComponent("new camCmp");
					Camera nc = new Camera("new cam");
					na.addChild(ncc);
					ncc.setCamera(nc);
					r.addChild(na);
					SceneGraphPath nap = new SceneGraphPath(r, na);
					SceneGraphPath ncp = nap.pushNew(ncc);
					ncp.push(nc);
					
					scene.setAvatarPath(nap);
					scene.setCameraPath(ncp);
					scene.setMicrophonePath(ncp);
				}
			});
		}
	}
	
	public static SimpleController createSceneAndView(JrScene jrscene) {
		SimpleController c = new DebugController();
		Scene scene = new Scene(jrscene);
		c.registerPlugin(scene);
		c.registerPlugin(new View());
		c.registerPlugin(new ToolSystemPlugin());
		return c;
	}
	
	public static SimpleController addBasicUI(SimpleController c) {
		c.registerPlugin(new Inspector());
		c.registerPlugin(new Shell());
		
		c.registerPlugin(new DisplayOptions());
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new ViewToolBar());
		
		c.registerPlugin(new ExportMenu());
		c.registerPlugin(new de.jreality.plugin.menu.CameraMenu());
		
		return c;
	}
	
	public static void addContentSupport(SimpleController c) {
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ContentTools());
		c.registerPlugin(new ContentAppearance());
	}
	
	public static void addVRSupport(SimpleController c) {
		c.registerPlugin(new Avatar());
		c.registerPlugin(new Terrain());
		c.registerPlugin(new Sky());
//		c.registerPlugin(new SwitchCameraPath());
//		c.registerPlugin(new HeadUpDisplay());
	}
	
	public static void addAudioSupport(SimpleController c) {
		c.registerPlugin(new Audio());
		c.registerPlugin(new AudioOptions());
		c.registerPlugin(new AudioPreferences());
	}
	
	public static void addDefaultLights(SimpleController c) {
		c.registerPlugin(new Lights());
	}
	
	public static void display(final SceneGraphNode n) {
		SimpleController c = createSceneAndView(null);
		addBasicUI(c);
		addContentSupport(c);
		addDefaultLights(c);
		c.registerPlugin(new DirectContent());
		c.registerPlugin(new Plugin() {
			@Override
			public PluginInfo getPluginInfo() {
				return new PluginInfo();
			}
			@Override
			public void install(Controller c) throws Exception {
				PluginUtility.getPlugin(c, Content.class).setContent(n);
			}
		});
		c.startup();
	}
	
	public static void main(String[] args) throws Exception {
		if (false) {
			ReaderJRS r = new ReaderJRS();
			r.setInput(Input.getInput("/home/weissman/tetranoidAusstellung.jrs"));
			SimpleController c = createSceneAndView(r.getScene());
			addBasicUI(c);
			c.startup();
		} else {		
			SimpleController c = createSceneAndView(null);
			addBasicUI(c);
			addContentSupport(c);
			addVRSupport(c);
			//addAudioSupport(c);
			c.registerPlugin(new Lights());
			c.registerPlugin(new TerrainAlignedContent());
			//c.registerPlugin(new CenteredAndScaledContent());
			//c.registerPlugin(new DirectContent());
			c.startup();
		}
	}
	
}
