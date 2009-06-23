package de.jreality.portal;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import de.jreality.scene.Viewer;
import de.jreality.toolsystem.PortalToolSystem;
import de.jreality.toolsystem.ToolSystem;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.GuiUtility;

public class RemoteExecutor {

	public static PortalToolSystem startRemote(Class<?> clazz, String... params) {
		Object va=null;
		Exception cause=null;
		try {
			Method m = null;
			if (params == null) params=new String[0];
			try {
				m = clazz.getMethod("remoteMain", String[].class);
				va = m.invoke(null, new Object[]{(String[]) params});
				if (va == null) {
					IllegalArgumentException ex = new IllegalArgumentException("calling remoteMain failed on "+clazz);
					ex.initCause(cause);
					throw ex;
				}
			} catch (NoSuchMethodException nsme) {
				m = clazz.getMethod("main", String[].class);
				m.invoke(null, new Object[]{(String[]) params});
			}
		} catch (Exception e) {
			cause = e;
		}

		Component viewingComponent=null;
		ToolSystem toolSystem=null;
		
		if (va instanceof ViewerApp) {
			viewingComponent = ((ViewerApp) va).getViewingComponent();
			toolSystem = ((ViewerApp) va).getToolSystem();
		} else if (va instanceof Viewer) {
			Viewer viewer = (Viewer)va;
			viewingComponent = (Component)viewer.getViewingComponent();
			toolSystem = ToolSystem.getToolSystemForViewer(viewer);
		} else {
			/* TODO: fix dependencies here...
			JRViewer v = JRViewer.getLastJRViewer();
			if (v == null) throw new IllegalArgumentException("insufficient return value of remoteMain of "+clazz);
			viewingComponent = v.getPlugin(View.class).getViewer().getViewingComponent();
			toolSystem = v.getPlugin(ToolSystemPlugin.class).getToolSystem();
			*/
		}
		
		ConfigurationAttributes config = ConfigurationAttributes.getDefaultConfiguration();
		
		JFrame frame = new JFrame("no title");
		frame.getContentPane().add(viewingComponent);
		GuiUtility.hideCursor(frame);

		if (config.getBool("fullscreen")) {
			frame.dispose();
			frame.setUndecorated(true);
			frame.getGraphicsConfiguration().getDevice().setFullScreenWindow(frame);
			frame.validate();
		} else {
			int w = config.getInt("screen.width");
			int h = config.getInt("screen.height");
			frame.setSize(w, h);
			frame.setTitle(ConfigurationAttributes.getDefaultConfiguration().getProperty("frametitle"));
		}

		frame.setVisible(true);
		
		return (PortalToolSystem) toolSystem;
	}
	
}
