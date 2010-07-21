package de.jreality.portal;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.GuiUtility;

public class PortalUtility {

	static JFrame displayPortalViewingComponent(Component viewingComponent) {
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		
		boolean fullscreen = config.getBool("frame.fullscreen");

		int x = config.getInt("frame.offset.x", 0);
		int y = config.getInt("frame.offset.y", 0);
		
		String title = config.getProperty("frame.title", "no title");
		boolean undecorated = config.getBool("frame.undecorated");
		
		DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		int defWidth = displayMode.getWidth();
		int defHeight = displayMode.getHeight();
		int width = config.getInt("frame.width", defWidth);
		int height = config.getInt("frame.height", defHeight);
		
		
		JFrame frame = new JFrame(title);
		if (fullscreen) {
			frame.setLayout(null);
			viewingComponent.setBounds(x, y, width, height);
			frame.getContentPane().add(viewingComponent);
			frame.dispose();
			frame.setUndecorated(true);
			frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
			frame.validate();
		} else {
			frame.getContentPane().add(viewingComponent);
			frame.setUndecorated(undecorated);
			frame.setSize(width, height);
			frame.setLocation(x, y);
		}
		GuiUtility.hideCursor(viewingComponent);
		frame.setVisible(true);
		frame.toFront();
		return frame;

	}
	
}
