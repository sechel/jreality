package de.jreality.plugin.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import de.jreality.plugin.basic.View;
import de.jreality.plugin.icon.ImageHook;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.file.Quit;
import de.jreality.ui.viewerapp.actions.file.SaveScene;
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
		info.vendorName = "Ulrich Pinkall";
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
	
		addMenuItem(getClass(), 0.5, new SaveScene("Save scene", viewerPlugin.getViewer(), viewerPlugin.getCenterComponent()), "File");
		
		addMenuSeparator(getClass(), 2, "File");
		addMenuItem(getClass(), 2, new Quit("Exit"), "File");
		
		// Viewer menu
		JMenu viewerMenu = createViewerMenu(viewerPlugin);
		viewerMenu.setMnemonic('v');
		addMenu(getClass(), 1.0, viewerMenu);
		addMenuSeparator(getClass(), 0.0, "Viewer");
		addMenuItem(getClass(), 1.5, c.getPlugin(DisplayOptions.class).getHideMouseToggle().createMenuItem(), "Viewer");
		
		// Side panels menu
		JMenu panelsMenu = viewerPlugin.getContaintersMenu();
		panelsMenu.setMnemonic('p');
		addMenu(getClass(), 2.0, panelsMenu);
		addMenuSeparator(getClass(), 0.0, "Side Panels");
		addMenu(getClass(), 1.0, viewerPlugin.getPanelsMenu(), "Side Panels");
	}
	
	private JMenu createViewerMenu(View view) {
		JMenu menu = new JMenu("Viewer");
		final ViewerSwitch viewerSwitch = view.getViewer();
		String[] viewerNames = viewerSwitch.getViewerNames();
		ButtonGroup bgr = new ButtonGroup();
		for (int i=0; i<viewerSwitch.getNumViewers(); i++) {
			final int index = i;
			final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
			new AbstractAction(viewerNames[index]) {
				private static final long serialVersionUID = 1L;
				public void actionPerformed(ActionEvent e) {
					viewerSwitch.selectViewer(index);
					viewerSwitch.getCurrentViewer().renderAsync();
				}
			});
			item.setSelected(index==0);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, 0));
			bgr.add(item);
			menu.add(item);
		}
		
		return menu;
	}

	@Override
	public void uninstall(Controller c) throws Exception {

	}
	
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
