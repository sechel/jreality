package de.jreality.portal;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import de.jreality.toolsystem.PortalToolSystem;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.plugin.view.View;
import de.jreality.ui.viewerapp.ViewerApp;

public class RemoteExecutor {

	public static PortalToolSystem startRemote(Class<?> clazz, String... params) {
		Object va=null;
		Exception cause=null;
		try {
			Method m = clazz.getMethod("remoteMain", String[].class);
			if (params == null) params=new String[0];
			va = m.invoke(null, new Object[]{(String[]) params});
		} catch (Exception e) {
			cause = e;
		}
		if (va == null) {
			IllegalArgumentException ex = new IllegalArgumentException("calling remoteMain failed on "+clazz);
			ex.initCause(cause);
			throw ex;
		}

		Component viewingComponent=null;
		ToolSystem toolSystem=null;
		
		if (va instanceof ViewerApp) {
			viewingComponent = ((ViewerApp) va).getViewingComponent();
			toolSystem = ((ViewerApp) va).getToolSystem();
		} else if (va instanceof View) {
			viewingComponent = ((View) va).getViewer().getViewingComponent();
			toolSystem = ((View) va).getToolSystem();
		} else {
			throw new IllegalArgumentException("insufficient return value of remoteMain of "+clazz);
		}
		
		
		JFrame fsf = new JFrame("jReality Viewer");
	    fsf.setUndecorated(true);
	    
	    // clear cursor
	    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D gfx = cursorImg.createGraphics();
	    gfx.setColor(new Color(0, 0, 0, 0));
	    gfx.fillRect(0, 0, 16, 16);
	    gfx.dispose();
	    fsf.setCursor(fsf.getToolkit().createCustomCursor(cursorImg, new Point(), ""));
	    
		fsf.getContentPane().add(viewingComponent);
	    fsf.validate();
	    fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(fsf);
		
		return (PortalToolSystem) toolSystem;
	}
	
}
