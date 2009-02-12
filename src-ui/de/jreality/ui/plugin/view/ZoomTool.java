package de.jreality.ui.plugin.view;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.ClickWheelCameraZoomTool;
import de.jreality.ui.plugin.view.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ZoomTool extends Plugin {
	
	private View sceneView;
	private ClickWheelCameraZoomTool zoomTool;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Zoom";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("luperot.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		sceneView = c.getPlugin(View.class);
		zoomTool = new ClickWheelCameraZoomTool();
		sceneView.getSceneRoot().addTool(zoomTool);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		SceneGraphComponent root = sceneView.getSceneRoot();
		if (root.getTools().contains(zoomTool)) {
			root.removeTool(zoomTool);
		}
	}
}
