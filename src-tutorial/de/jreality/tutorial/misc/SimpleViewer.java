package de.jreality.tutorial.misc;

import java.awt.Component;

import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.tools.DraggingTool;
import de.jreality.tools.RotateTool;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

/**
 * A simple class showing how to use a ViewerApp to get a viewing component 
 * which is then packed into another frame.
 * @author Charles Gunn
 *
 */
public class SimpleViewer {

	public static void main(String[] args)	{
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(Primitives.sharedIcosahedron);
		world.addTool(new DraggingTool());
		world.addTool(new RotateTool());  
		// if you don't want ViewerApp to provide lights and camera and some tools
		// then use the constructor 	
		// public ViewerApp(SceneGraphComponent root, SceneGraphPath cameraPath, SceneGraphPath emptyPick, SceneGraphPath avatar) {

		ViewerApp va = new ViewerApp(world);
        JFrame f = new JFrame();
        f.getContentPane().add((Component) va.getViewingComponent());

        f.setSize(512, 512);
        f.validate();
        f.setVisible(true);
	}
 
}
