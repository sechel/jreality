package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collections;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.FileFilter;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jtem.beans.DimensionPanel;
import de.jtem.beans.InspectorPanel;

@SuppressWarnings("serial")
public class SunflowPlugin {

	private JFrame settingsFrame;
	private RenderOptions previewOptions;
	private RenderOptions renderOptions;
	private RenderOptions renderSameOptions;
	private FileFilter[] fileFilters;
	private DimensionPanel dimPanel;

	public SunflowPlugin() {
		
		settingsFrame = new JFrame("Sunflow Settings");
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


		fileFilters = new FileFilter[3];
		fileFilters[0] = new FileFilter("PNG Image", "png");
		fileFilters[1] = new FileFilter("TGA Image", "tga");
		fileFilters[2] = new FileFilter("HDR Image", "hdr");
	}
	
	public void renderAndSave(final Viewer v, RenderOptions options) {
		if (dimPanel == null) {
			dimPanel = new DimensionPanel();
			dimPanel.setDimension(new Dimension(800,600));
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dimension");
			dimPanel.setBorder(title);
		}
		File file = FileLoaderDialog.selectTargetFile(null, dimPanel, false, fileFilters);
		final Dimension dim = dimPanel.getDimension();
		if (file != null && dim != null) {
			final RenderDisplay renderDisplay = new RenderDisplay(file.getAbsolutePath());

			new Thread(new Runnable() {
				public void run() {
					SunflowRenderer renderer = new SunflowRenderer();
					renderer.render(
							v.getSceneRoot(),
							v.getCameraPath(),
							renderDisplay,
							dim.width,
							dim.height
					);
				}
			}).start();
		}
	}

	public void render(Viewer v, Dimension dim, RenderOptions options) {
		final SunflowViewer viewer = new SunflowViewer();
		viewer.setWidth(dim.width);
		viewer.setHeight(dim.height);
		viewer.setSceneRoot(v.getSceneRoot());
		viewer.setCameraPath(v.getCameraPath());
		final JFrame frame = new JFrame("Sunflow");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				viewer.cancel();
			}
		});
		frame.setLayout(new BorderLayout());
		JMenuBar bar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new AbstractAction("Save") {
			public void actionPerformed(ActionEvent e) {
				save(viewer,frame);
			}
		});
		bar.add(fileMenu);
		frame.setJMenuBar(bar);
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
	
	public void showSettingsInspector() {
		settingsFrame.setVisible(true);
		settingsFrame.toFront();
	}

	private void save(SunflowViewer v,JFrame frame) {
		File file = FileLoaderDialog.selectTargetFile(frame, null, false, fileFilters);
		v.getViewingComponent().save(file.getAbsolutePath());
	}

	public RenderOptions getPreviewOptions() {
		return previewOptions;
	}

	public RenderOptions getRenderOptions() {
		return renderOptions;
	}

	public RenderOptions getRenderSameOptions() {
		return renderSameOptions;
	}
}
