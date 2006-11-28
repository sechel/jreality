package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;

import de.jreality.ui.viewerapp.ViewerApp;
import de.jtem.beans.InspectorPanel;

@SuppressWarnings("serial")
public class SunflowMenu extends JMenu {

	private ViewerApp va;
	private JFrame settingsFrame;
	private RenderOptions previewOptions;
	private RenderOptions renderOptions;
	private RenderOptions renderSameOptions;

	public SunflowMenu(ViewerApp vapp) {
		super("Sunflow");
		va = vapp;

		settingsFrame = new JFrame("Sunflow Settings");
		settingsFrame.setLocationRelativeTo(vapp.getFrame());
		JTabbedPane tabs = new JTabbedPane();

		EmptyBorder border = new EmptyBorder(10, 10, 10, 10);
		previewOptions = new RenderOptions();
		InspectorPanel previewSettings = new InspectorPanel(false);
		previewSettings.setBorder(border);
		previewSettings.setObject(previewOptions, Collections
				.singleton("nothing"));
		tabs.add("thumb", previewSettings);

		renderSameOptions = new RenderOptions();
		InspectorPanel renderSameSettings = new InspectorPanel(false);
		renderSameSettings.setBorder(border);
		renderSameSettings.setObject(renderSameOptions, Collections
				.singleton("nothing"));
		tabs.add("preview", renderSameSettings);

		renderOptions = new RenderOptions();
		InspectorPanel renderSettings = new InspectorPanel(false);
		renderSettings.setBorder(border);
		renderSettings.setObject(renderOptions, Collections
				.singleton("nothing"));
		tabs.add("render", renderSettings);

		settingsFrame.add(tabs);
		settingsFrame.pack();

		add(new AbstractAction("thumb") {
			public void actionPerformed(ActionEvent arg0) {
				Dimension d = va.getViewer().getViewingComponentSize();
				render(va, new Dimension(d.width/3, d.height/3), previewOptions);
			}
		});

		add(new AbstractAction("preview") {
			public void actionPerformed(ActionEvent arg0) {
				render(va, va.getViewer().getViewingComponentSize(), renderSameOptions);
			}
		});

		add(new AbstractAction("settings") {
			public void actionPerformed(ActionEvent arg0) {
				settingsFrame.setVisible(true);
				settingsFrame.toFront();
			}
		});
	}

	private void render(ViewerApp va, Dimension dim, RenderOptions options) {
		final SunflowViewer viewer = new SunflowViewer();
		viewer.setWidth(dim.width);
		viewer.setHeight(dim.height);
		viewer.setSceneRoot(va.getViewer().getSceneRoot());
		viewer.setCameraPath(va.getViewer().getCameraPath());
		final JFrame frame = new JFrame("Sunflow");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println(".windowClosing()");
				viewer.cancel();
			}
		});
		frame.setLayout(new BorderLayout());
		viewer.setOptions(options);
		frame.setContentPane((Container) viewer.getViewingComponent());
		frame.pack();
		frame.setVisible(true);
		
		new Thread(new Runnable() {
			public void run() {
				viewer.render();
			}
		}).start();
	}
}
