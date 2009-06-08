package de.jreality.plugin.basic;

import javax.swing.JMenu;

import de.jreality.plugin.icon.ImageHook;
import de.jreality.ui.viewerapp.actions.file.Quit;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.PluginInfo;
import de.varylab.jrworkspace.plugin.aggregators.MenuAggregator;
import de.varylab.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ViewMenuBar extends MenuAggregator {

	private View viewerPlugin = null;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Viewer Menu";
		info.vendorName = "jReality Group";
		info.icon = ImageHook.getIcon("menu.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		viewerPlugin = c.getPlugin(View.class);
		removeAll(getClass());
		
		// File menu
		JMenu fileMenu =  new JMenu("File");
		fileMenu.setMnemonic('f');
		addMenu(getClass(), 0.0, fileMenu);
	
		addMenuSeparator(getClass(), 99, "File");
		addMenuItem(getClass(), 100, new Quit("Exit"), "File");
		
		// Viewer menu
		JMenu viewerMenu = viewerPlugin.createViewerMenu();
		viewerMenu.setMnemonic('v');
		addMenu(getClass(), 1.0, viewerMenu);
		addMenuSeparator(getClass(), 0.0, "Viewer");
		
		// Side panels menu
		JMenu slotsMenu = viewerPlugin.getContaintersMenu();
		slotsMenu.setMnemonic('w');
		slotsMenu.setText("Window");
		addMenu(getClass(), 100.0, slotsMenu);
		addMenuSeparator(getClass(), 10.0, "Window");
		addMenu(getClass(), 10.1, viewerPlugin.getPanelsMenu(), "Window");
	}
	

	@Override
	public void uninstall(Controller c) throws Exception {

	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
