package de.jreality.sunflow;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;

import de.jreality.ui.viewerapp.ViewerApp;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {

	private ViewerApp va;
	private SunflowPlugin plugin = new SunflowPlugin();

	public SunflowMenu(ViewerApp vapp) {
		super("Sunflow");
		va = vapp;

		add(new AbstractAction("preview") {
			public void actionPerformed(ActionEvent arg0) {
				plugin.render(va.getViewer(), va.getViewer().getViewingComponentSize(), plugin.getRenderSameOptions());
			}
		});
		
		add(new AbstractAction("thumb") {
			public void actionPerformed(ActionEvent arg0) {
				Dimension d = va.getViewer().getViewingComponentSize();
				plugin.render(va.getViewer(), new Dimension(d.width/3, d.height/3), plugin.getPreviewOptions());
			}
		});

		add(new AbstractAction("render") {
			public void actionPerformed(ActionEvent arg0) {
				plugin.renderAndSave(va.getViewer(), plugin.getRenderOptions());
			}
		});

		add(new AbstractAction("settings") {
			public void actionPerformed(ActionEvent arg0) {
				plugin.showSettingsInspector();
			}
		});
	}
}
