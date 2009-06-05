package de.jreality.tutorial.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.jogl.GLJPanelViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.SystemProperties;

/**
 * A simple class showing how to use a {@link ViewerApp} to get a viewing component 
 * which is then packed into another frame.
 * @author Charles Gunn
 *
 */
public class GLJPanelViewerExample {

	public static void main(String[] args)	{
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(Primitives.sharedIcosahedron);
		// if you don't want ViewerApp to provide lights and camera and some tools
		// then use the constructor 	
		// public ViewerApp(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {
	    System.setProperty(SystemProperties.VIEWER, "de.jreality.jogl.GLJPanelViewer"); // de.jreality.portal.DesktopPortalViewer");
	      
		ViewerApp va = new ViewerApp(world);
		va.getViewer().getSceneRoot().getAppearance().setAttribute("backgroundColor", new Color(0,255,0,128));
		final GLJPanelViewer glpv = (GLJPanelViewer) va.getCurrentViewer();
		glpv.addRenderListener(new GLJPanelViewer.GLJPanelListener() {

			public void postRender(Graphics2D g2) {
				if (g2 == null) return;
				g2.setColor(Color.pink);
				g2.fillRect(0, 0, 50, 50);
			}

			public void preRender(Graphics2D g2) {
				if (g2 == null) return;
				g2.setColor(Color.blue);
				g2.fillRect(0, 50, 50, 50);		
			}
			
		});
        JFrame f = new JFrame();
        f.getContentPane().add((Component) va.getViewingComponent());

        f.setSize(512, 512);
        f.validate();
        f.setVisible(true);
	}
 
}
