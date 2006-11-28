package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jtem.beans.Inspector;
import de.jtem.beans.InspectorPanel;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {
	
	private ViewerApp va;
	private JFrame frame;
	private JFrame settingsFrame;
	private SunflowViewer viewer;
	private RenderOptions previewOptions;
	private RenderOptions renderOptions;
	private RenderOptions renderSameOptions;
	
	public SunflowMenu(ViewerApp vapp) {
		super("Sunflow");
		va = vapp;
		frame = new JFrame("Sunflow");
		frame.setLayout(new BorderLayout());
		viewer = new SunflowViewer();
		frame.setContentPane((Container) viewer.getViewingComponent());
		
		settingsFrame = new JFrame("Sunflow Settings");
		settingsFrame.setLocationRelativeTo(vapp.getFrame());
		JTabbedPane tabs = new JTabbedPane();
		
		EmptyBorder border = new EmptyBorder(10,10,10,10);
		previewOptions = new RenderOptions();
		InspectorPanel previewSettings = new InspectorPanel(false);
		previewSettings.setBorder(border);
		previewSettings.setObject(previewOptions,Collections.singleton("nothing"));
		tabs.add("thumb", previewSettings);
		
		renderSameOptions = new RenderOptions();
		InspectorPanel renderSameSettings = new InspectorPanel(false);
		renderSameSettings.setBorder(border);
		renderSameSettings.setObject(renderSameOptions,Collections.singleton("nothing"));
		tabs.add("preview", renderSameSettings);
		
		renderOptions = new RenderOptions();
		InspectorPanel renderSettings = new InspectorPanel(false);
		renderSettings.setBorder(border);
		renderSettings.setObject(renderOptions,Collections.singleton("nothing"));
		tabs.add("render", renderSettings);
		

		settingsFrame.add(tabs);
		settingsFrame.pack();
		
		add(new AbstractAction("thumb") {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						Viewer v = va.getViewer();
						viewer.setCameraPath(v.getCameraPath());
						viewer.setSceneRoot(v.getSceneRoot());
						if (v.hasViewingComponent()){
							Component c = (Component)v.getViewingComponent();
							viewer.setWidth(c.getWidth()/3);
							viewer.setHeight(c.getHeight()/3);
							frame.pack();
							viewer.setOptions(previewOptions);
						} else {
							viewer.setWidth(240);
							viewer.setHeight(180);
							frame.pack();
						}
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
		
		add(new AbstractAction("preview") {
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					public void run() {
						viewer.initializeFrom(va.getViewer());
						viewer.setOptions(renderSameOptions);
						System.out.println(frame.getContentPane());
						frame.validate();
						frame.setSize(frame.getContentPane().getPreferredSize());
						//frame.pack();
						frame.setVisible(true);
						frame.pack();
						viewer.render();
					}
				}).start();
			}
		});
		
		add(new AbstractAction("settings") {
			public void actionPerformed(ActionEvent arg0) {
				settingsFrame.setVisible(true);
				settingsFrame.toFront();
			}
		});
	}
}
