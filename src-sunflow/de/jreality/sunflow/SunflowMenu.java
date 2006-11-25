package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerApp;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {
	
	private ViewerApp va;
	private JFrame frame;
	private SunflowViewer viewer;
	
	public SunflowMenu(ViewerApp vapp) {
		super("Sunflow");
		va = vapp;
		frame = new JFrame("Sunflow");
		//frame.setSize(800,600);
		frame.setLayout(new BorderLayout());
		viewer = new SunflowViewer();
		//frame.add(viewer.getViewingComponent());
		frame.setContentPane((Container) viewer.getViewingComponent());
		
		add(new AbstractAction("render") {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						viewer.initializeFrom(va.getViewer());
						if (!frame.isVisible()) {
							frame.validate();
							frame.setVisible(true);
						}
						frame.pack();
						viewer.render();
					}
				}).start();
			}
		});
	}
}
