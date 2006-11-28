package de.jreality.sunflow;

import java.awt.Image;

import org.sunflow.image.Color;
import org.sunflow.system.ImagePanel;

public class CancelableImagePanel extends ImagePanel {

	private boolean cancel;

	@Override
	public synchronized void imageUpdate(int x, int y, int w, int h, Color[] data) {
		if (cancel) throw new RuntimeException("cancel");
		super.imageUpdate(x, y, w, h, data);
	}
	
	@Override
	public synchronized void imageFill(int x, int y, int w, int h, Color c) {
		if (cancel) throw new RuntimeException("cancel");
		else super.imageFill(x, y, w, h, c);
	}
	
	public synchronized void cancel() {
		cancel=true;
	}
	
}
