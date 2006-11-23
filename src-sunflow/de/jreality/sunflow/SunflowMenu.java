package de.jreality.sunflow;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerApp;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {
	
	private ViewerApp va;
	
	public SunflowMenu(ViewerApp vapp) {
		va = vapp;
		add(new AbstractAction("preview") {
			public void actionPerformed(ActionEvent arg0) {
				SunflowViewer sv = new SunflowViewer();
				Viewer v = va.getViewer();
				sv.setSceneRoot(v.getSceneRoot());
				sv.setCameraPath(v.getCameraPath());
				sv.setWidth(300);
				sv.setHeight(200);
				sv.render();
			}
		});
	}
}
