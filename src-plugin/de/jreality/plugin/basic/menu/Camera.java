package de.jreality.plugin.basic.menu;

import java.awt.Component;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import de.jreality.plugin.basic.View;
import de.jreality.ui.viewerapp.ViewerSwitch;
import de.jreality.ui.viewerapp.actions.camera.LoadCameraPreferences;
import de.jreality.ui.viewerapp.actions.camera.SaveCameraPreferences;
import de.jreality.ui.viewerapp.actions.camera.ShiftEyeSeparation;
import de.jreality.ui.viewerapp.actions.camera.ShiftFieldOfView;
import de.jreality.ui.viewerapp.actions.camera.ShiftFocus;
import de.jreality.ui.viewerapp.actions.camera.ToggleStereo;
import de.jreality.ui.viewerapp.actions.view.ToggleShowCursor;
import de.varylab.jrworkspace.plugin.Controller;
import de.varylab.jrworkspace.plugin.Plugin;
import de.varylab.jrworkspace.plugin.PluginInfo;

public class Camera extends Plugin {

	ViewerSwitch viewer;
	Component viewingComp;
	
	private JMenu createCameraMenu() {
		JMenu cameraMenu = new JMenu("Camera");
		cameraMenu.setMnemonic(KeyEvent.VK_C);

		cameraMenu.add(new JMenuItem(new ShiftFieldOfView("Decrease FOV", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftFieldOfView("Increase FOV", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ShiftFocus("Decrease Focus", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftFocus("Increase Focus", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ShiftEyeSeparation("Decrease Eye Separation", viewer, true)));
		cameraMenu.add(new JMenuItem(new ShiftEyeSeparation("Increase Eye Separation", viewer, false)));
		cameraMenu.addSeparator();
		cameraMenu.add(new JMenuItem(new ToggleStereo("Toggle Stereo", viewer)));
		cameraMenu.add(new JMenuItem(new ToggleShowCursor("Toggle Cursor", viewer, viewingComp)));
		cameraMenu.addSeparator();
        cameraMenu.add(new JMenuItem(new LoadCameraPreferences("Load Preferences", viewer)));
        cameraMenu.add(new JMenuItem(new SaveCameraPreferences("Save Preferences", viewer)));

		return cameraMenu;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Camera Menu");
	}
	
	@Override
	public void install(Controller c) throws Exception {
		View view = c.getPlugin(View.class);
		viewer = view.getViewer();
		viewingComp = viewer.getViewingComponent();
		c.getPlugin(ViewMenuBar.class).addMenu(getClass(), 2, createCameraMenu());
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		c.getPlugin(ViewMenuBar.class).removeAll(getClass());
	}

}
