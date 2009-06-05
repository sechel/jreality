package de.jreality.tutorial.viewer;

import java.awt.Component;

import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * A simple class showing how to use a {@link ViewerApp} to get a viewing component 
 * which is then packed into another frame.
 * @author Charles Gunn
 *
 */
public class SimpleViewer {

	public static void main(String[] args)	{
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(Primitives.sharedIcosahedron);
		// if you don't want ViewerApp to provide lights and camera and some tools
		// then use the constructor 	
		// public ViewerApp(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {

		Viewer v = JRViewer.display(world);
        JFrame f = new JFrame();
        f.getContentPane().add((Component) v.getViewingComponent());

        f.setSize(512, 512);
        f.validate();
        f.setVisible(true);
	}
 
}
