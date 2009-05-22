package de.jreality.plugin.basic;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPopupMenu;

import de.jreality.plugin.basic.content.CenteredAndScaledContent;
import de.jreality.plugin.basic.content.ContentAppearance;
import de.jreality.plugin.basic.content.ContentLoader;
import de.jreality.plugin.basic.content.ContentTools;
import de.jreality.plugin.basic.content.TerrainAlignedContent;
import de.jreality.plugin.basic.scene.Avatar;
import de.jreality.plugin.basic.scene.Lights;
import de.jreality.plugin.basic.scene.Sky;
import de.jreality.plugin.basic.scene.Terrain;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.varylab.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class BasicViewer {
	
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
	
	public static SimpleController create() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		SimpleController c = new DebugController();
		Scene scene = new Scene();
		c.registerPlugin(scene);

		c.registerPlugin(new View());
		c.registerPlugin(new ToolSystemPlugin());
		c.registerPlugin(new Lights());
		c.registerPlugin(new DisplayOptions());
		c.registerPlugin(new ViewMenuBar());
		c.registerPlugin(new Inspector());
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ContentTools());
		c.registerPlugin(new Shell());
		c.registerPlugin(new ContentAppearance());
		
		c.registerPlugin(new Avatar());
		c.registerPlugin(new Terrain());
		c.registerPlugin(new Sky());
		
		c.registerPlugin(new SwitchCameraPath());
		return c;
	}
	
	public static void main(String[] args) {
		SimpleController c = create();
		c.registerPlugin(new TerrainAlignedContent());
		c.startup();
	}
	
}
