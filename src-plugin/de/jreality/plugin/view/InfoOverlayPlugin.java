package de.jreality.plugin.view;

import java.awt.Component;

import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.util.LoggingSystem;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class InfoOverlayPlugin extends Plugin {
	
	private View sceneView;
	private de.jreality.jogl.plugin.InfoOverlay infoOverlay;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "InfoOverlay";
		info.vendorName = "Charles Gunn";
		info.icon = ImageHook.getIcon("luperot.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		sceneView = c.getPlugin(View.class);
//		Component viewComp = sceneView.getViewer().getViewingComponent();
		Viewer[] vlist = sceneView.getViewer().getViewers();
		de.jreality.jogl.Viewer joglViewer = null;
		for (Viewer v : vlist)	{
			if (v instanceof de.jreality.jogl.Viewer) {
				joglViewer = (de.jreality.jogl.Viewer) v;
			}
		}
		if (joglViewer == null)  { // signal error 
			LoggingSystem.getLogger(this).warning("No Jogl Viewer in viewer switch!");
			return;
		}
		infoOverlay = de.jreality.jogl.plugin.InfoOverlay.perfInfoOverlayFor(joglViewer);
		infoOverlay.setVisible(true);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		SceneGraphComponent root = sceneView.getSceneRoot();
		infoOverlay.setVisible(false);
	}
	
	public InfoOverlay getInfoOverlay() {
		return infoOverlay;
	}
}
