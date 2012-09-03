package de.jreality.plugin.basic;

import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.scene.Viewer;
import de.jreality.util.LoggingSystem;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.annotation.Experimental;

@Experimental
public class InfoOverlayPlugin extends Plugin {
	
	private View sceneView;
	private InfoOverlay infoOverlay;
	
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
		de.jreality.jogl.JOGLViewer joglViewer = null;
		for (Viewer v : vlist)	{
			if (v instanceof de.jreality.jogl.JOGLViewer) {
				joglViewer = (de.jreality.jogl.JOGLViewer) v;
			}
		}
		if (joglViewer == null)  { // signal error 
			LoggingSystem.getLogger(this).warning("No Jogl Viewer in viewer switch!");
			return;
		}
		infoOverlay = InfoOverlay.perfInfoOverlayFor(joglViewer);
		infoOverlay.setVisible(true);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
//		SceneGraphComponent root = sceneView.getSceneRoot();
		infoOverlay.setVisible(false);
	}
	
	public InfoOverlay getInfoOverlay() {
		return infoOverlay;
	}
}
