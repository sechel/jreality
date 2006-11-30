package de.jreality.sunflow;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;

import de.jreality.scene.Viewer;

@SuppressWarnings("serial")
public class Sunflow {

	private Sunflow() {}
	
//	private static FileFilter[] fileFilters;
//
//	static {
//		fileFilters = new FileFilter[3];
//		fileFilters[0] = new FileFilter("PNG Image", "png");
//		fileFilters[1] = new FileFilter("TGA Image", "tga");
//		fileFilters[2] = new FileFilter("HDR Image", "hdr");
//	}
	
	public static void renderAndSave(final Viewer v, RenderOptions options, final Dimension dim, File file) {
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

	public static void render(Viewer v, Dimension dim, RenderOptions options) {
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
//		JMenuBar bar = new JMenuBar();
//		JMenu fileMenu = new JMenu("File");
//		fileMenu.add(new AbstractAction("Save") {
//			public void actionPerformed(ActionEvent e) {
//				save(viewer,frame);
//			}
//		});
//		bar.add(fileMenu);
//		frame.setJMenuBar(bar);
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
	
//	private static void save(SunflowViewer v,JFrame frame) {
//		File file = FileLoaderDialog.selectTargetFile(frame, null, false, fileFilters);
//		v.getViewingComponent().save(file.getAbsolutePath());
//	}

}
