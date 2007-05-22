package de.jreality.portal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import de.jreality.toolsystem.PortalToolSystem;
import de.jreality.ui.viewerapp.ViewerApp;

public class RemoteExecutor {

	public static PortalToolSystem startRemote(Class<?> clazz, String... params) {
		ViewerApp va=null;
		try {
			Method m = clazz.getMethod("remoteMain", String[].class);
			if (params == null) params=new String[0];
			va = (ViewerApp) m.invoke(null, new Object[]{(String[]) params});
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (va == null || !(va instanceof ViewerApp)) throw new IllegalArgumentException("no remoteMain method in "+clazz);
		
		JFrame fsf = new JFrame("jReality Viewer");
	    fsf.setUndecorated(true);
	    
	    // clear cursor
	    BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D gfx = cursorImg.createGraphics();
	    gfx.setColor(new Color(0, 0, 0, 0));
	    gfx.fillRect(0, 0, 16, 16);
	    gfx.dispose();
	    fsf.setCursor(fsf.getToolkit().createCustomCursor(cursorImg, new Point(), ""));
	    
	    fsf.getContentPane().add(va.getViewingComponent());
	    fsf.validate();
	    fsf.getGraphicsConfiguration().getDevice().setFullScreenWindow(fsf);
		
//		return (PortalToolSystem) va.getViewer().getToolSystem();
		return (PortalToolSystem) va.getToolSystem();
	}
	
}
