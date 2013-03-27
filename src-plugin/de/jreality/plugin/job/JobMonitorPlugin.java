package de.jreality.plugin.job;

import java.io.File;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jreality.ui.JRealitySplashScreen;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;
import de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode;
import de.jtem.jrworkspace.plugin.simplecontroller.widget.SplashScreen;

public class JobMonitorPlugin extends ShrinkPanelPlugin {

	public JobMonitorPlugin() {
		
	}
	
	public static void main(String[] args) {
		SplashScreen splash = new JRealitySplashScreen();
		splash.setVisible(true);
		JRViewer v = new JRViewer();
		v.getController().setPropertiesMode(PropertiesMode.StaticPropertiesFile);
		v.getController().setStaticPropertiesFile(new File("JobMonitorTest.xml"));
		v.addBasicUI();
		v.addContentUI();
		v.registerPlugin(JobMonitorPlugin.class);
		v.setSplashScreen(splash);
		v.startup();
		splash.setVisible(false);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
