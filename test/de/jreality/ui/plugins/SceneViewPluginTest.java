package de.jreality.ui.plugins;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.UIManager;

import de.jreality.ui.plugin.AlignedContent;
import de.jreality.ui.plugin.Background;
import de.jreality.ui.plugin.CameraStand;
import de.jreality.ui.plugin.ContentAppearance;
import de.jreality.ui.plugin.ContentLoader;
import de.jreality.ui.plugin.ContentTools;
import de.jreality.ui.plugin.Export;
import de.jreality.ui.plugin.Inspector;
import de.jreality.ui.plugin.Lights;
import de.jreality.ui.plugin.Shell;
import de.jreality.ui.plugin.View;
import de.jreality.ui.plugin.ViewMenuBar;
import de.jreality.ui.plugin.ViewPreferences;
import de.jreality.ui.plugin.ZoomTool;
import de.jreality.vr.plugin.Avatar;
import de.jreality.vr.plugin.HeadUpDisplay;
import de.jreality.vr.plugin.Sky;
import de.jreality.vr.plugin.Terrain;
import de.varylab.jrworkspace.plugin.simplecontroller.SimpleController;

public class SceneViewPluginTest {
	
	
	private static class ExitAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public ExitAction() {
			putValue(AbstractAction.NAME, "Exit");
		}
		
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
		
	}

	
	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean viewerVR = true;
		
		File propFile = new File("SceneViewPluginTest.jrw");
		SimpleController c = new SimpleController(propFile);

		ViewMenuBar viewerMenu = new ViewMenuBar();
		
		c.registerPlugin(new View());
		c.registerPlugin(new HeadUpDisplay());
		c.registerPlugin(new CameraStand());
		c.registerPlugin(new Lights());
		c.registerPlugin(new Background());
		c.registerPlugin(viewerMenu);
		if (viewerVR) {
			Avatar avatarPlugin = new Avatar();
			avatarPlugin.setShowPanel(false);
			c.registerPlugin(avatarPlugin);
			c.registerPlugin(new Terrain());
			c.registerPlugin(new ZoomTool());
		}
		AlignedContent contentPlugin = new AlignedContent();
		c.registerPlugin(contentPlugin);
		c.registerPlugin(new ContentLoader());
		c.registerPlugin(new ViewPreferences());
		c.registerPlugin(new Inspector());
		c.registerPlugin(new Shell());
		c.registerPlugin(new ContentAppearance());
		c.registerPlugin(new Sky());
		c.registerPlugin(new Export());
		
		ContentTools contentToolsPlugin = new ContentTools();
		contentToolsPlugin.setShowPanel(false);
		c.registerPlugin(contentToolsPlugin);
		
		viewerMenu.addMenuSeparator(SceneViewPluginTest.class, 19.0, "File");
		viewerMenu.addMenuItem(SceneViewPluginTest.class, 20.0, new ExitAction(), "File");
		
		// look and feel switch
		c.startup();
	}
}
