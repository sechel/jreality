package de.jreality.ui.plugin;

import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.jreality.ui.plugin.image.ImageHook;
import de.jreality.ui.viewerapp.SunflowMenu;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.file.ExportPDF;
import de.jreality.ui.viewerapp.actions.file.ExportPS;
import de.jreality.ui.viewerapp.actions.file.ExportRIB;
import de.jreality.ui.viewerapp.actions.file.ExportSTL;
import de.jreality.ui.viewerapp.actions.file.ExportSVG;
import de.jreality.ui.viewerapp.actions.file.ExportU3D;
import de.jreality.ui.viewerapp.actions.file.ExportVRML;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Export extends Plugin {
	
	private ViewMenuBar viewMenuBar;
	private JMenu exportMenu;
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Export";
		info.vendorName = "Ulrich Pinkall";
		info.icon = ImageHook.getIcon("arrow.png");
		return info;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		ViewerSwitch viewer = c.getPlugin(View.class).getViewer();
		Component parent = viewer.getViewingComponent();
		exportMenu = new JMenu("Export");
		exportMenu.add(new JMenuItem(new ExportRIB("RIB", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportSVG("SVG", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportPS("PS", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportVRML("VRML", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportSTL("STL", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportU3D("U3D", viewer, parent)));
		exportMenu.add(new JMenuItem(new ExportPDF("PDF", viewer, parent)));
		exportMenu.add(new SunflowMenu(viewer));
		viewMenuBar.addMenuItem(getClass(), 2, exportMenu, "File");
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		viewMenuBar.removeMenu(getClass(), exportMenu);
		super.uninstall(c);
	}

}
