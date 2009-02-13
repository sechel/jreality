package de.jreality.plugin.view;

import java.awt.Component;

import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.plugin.view.image.ImageHook;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.LoggingSystem;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class ViewerKeyListener extends Plugin {

	private View sceneView;
	private de.jreality.jogl.plugin.ViewerKeyListener vkl = null;
	private Component viewComp;

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "ViewerKeyListener";
		info.vendorName = "Charles Gunn";
		info.icon = ImageHook.getIcon("luperot.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		sceneView = c.getPlugin(View.class);
		viewComp = sceneView.getViewer().getViewingComponent();
		InfoOverlayPlugin iop = c.getPlugin(InfoOverlayPlugin.class);
		iop.getInfoOverlay().setVisible(false);
		Viewer viewer = sceneView.getViewer().getCurrentViewer();
		vkl = new de.jreality.jogl.plugin.ViewerKeyListener(viewer, null, iop == null ? null : iop.getInfoOverlay());
		viewComp.addKeyListener(vkl);
		AlignedContent ac = c.getPlugin(AlignedContent.class);
		if (ac != null)	
			vkl.setSelection(ac.getPathToContent());

	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewComp.removeKeyListener(vkl);
	}
	
	public de.jreality.jogl.plugin.ViewerKeyListener getViewerKeyListener() {
		return vkl;
	}
}
