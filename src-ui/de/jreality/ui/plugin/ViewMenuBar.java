package de.jreality.ui.plugin;

import javax.swing.JMenu;

import de.jreality.ui.plugin.image.ImageHook;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;
import de.varylab.jrworkspace.plugin.menuaggregator.MenuAggregator;

public class ViewMenuBar extends MenuAggregator {

	private View viewerPlugin = null;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Viewer Menu";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("menu.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		viewerPlugin = c.getPlugin(View.class);
		removeMenuAll(getClass());
		
		// File menu
		JMenu fileMenu =  new JMenu("File");
		fileMenu.setMnemonic('f');
		addMenu(getClass(), 0.0, fileMenu);
		
		// Viewer menu
		JMenu viewerMenu = viewerPlugin.getMenu();
		viewerMenu.setMnemonic('v');
		addMenu(getClass(), 1.0, viewerMenu);
		addMenuSeparator(getClass(), 0.0, "Viewer");
		
		// Side panels menu
		JMenu panelsMenu = viewerPlugin.getContaintersMenu();
		panelsMenu.setMnemonic('p');
		addMenu(getClass(), 2.0, panelsMenu);
		addMenuSeparator(getClass(), 0.0, "Side Panels");
		addMenu(getClass(), 1.0, viewerPlugin.getPanelsMenu(), "Side Panels");
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {

	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
