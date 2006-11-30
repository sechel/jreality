package de.jreality.sunflow;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.sunflow.SunflowAPI;
import org.sunflow.core.Display;
import org.sunflow.image.Color;

public class RenderDisplay implements Display {
	private String filename;
	private JFrame frame;
	private CancelableImagePanel imagePanel;

	public RenderDisplay() {
		this(null);
	}

	public RenderDisplay(String filename) {
		this.filename = filename;
		frame = null;
	}

	public void imageBegin(int w, int h, int bucketSize) {
		if (frame == null) {
			frame = new JFrame("Sunflow v" + SunflowAPI.VERSION);
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (JOptionPane.showConfirmDialog(
							frame,
							"Do you want to continue rendering in background?",
							"Sunflow",
							JOptionPane.YES_NO_OPTION
					) == JOptionPane.NO_OPTION) {
						imagePanel.cancel();
					}
				}
			});
			imagePanel = new CancelableImagePanel();
			imagePanel.imageBegin(w, h, bucketSize);
			Dimension screenRes = Toolkit.getDefaultToolkit().getScreenSize();
			boolean needFit = false;
			if (w >= (screenRes.getWidth() - 200) || h >= (screenRes.getHeight() - 200)) {
				imagePanel.setPreferredSize(new Dimension((int) screenRes.getWidth() - 200, (int) screenRes.getHeight() - 200));
				needFit = true;
			} else
				imagePanel.setPreferredSize(new Dimension(w, h));
			System.out.println("dimension "+w+", "+h);
			frame.setContentPane(imagePanel);
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			if (needFit)
				imagePanel.fit();
		} else
			imagePanel.imageBegin(w, h, bucketSize);
	}

	public void imagePrepare(int x, int y, int w, int h, int id) {
		imagePanel.imagePrepare(x, y, w, h, id);
	}

	public void imageUpdate(int x, int y, int w, int h, Color[] data) {
		imagePanel.imageUpdate(x, y, w, h, data);
	}

	public void imageFill(int x, int y, int w, int h, Color c) {
		imagePanel.imageFill(x, y, w, h, c);
	}

	public void imageEnd() {
		imagePanel.imageEnd();
		if (filename != null)
			imagePanel.save(filename);
	}

	public JFrame getFrame() {
		return frame;
	}
}
