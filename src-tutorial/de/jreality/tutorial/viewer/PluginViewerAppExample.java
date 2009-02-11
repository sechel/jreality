package de.jreality.tutorial.viewer;

import java.awt.Component;

import javax.swing.JFrame;

import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.PluginViewerApp;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * A simple class showing how to use a {@link PluginViewerApp}, the latest and greatest (as of February 2009)
 * tool in the jReality toolbox.
 * 
 * @author Charles Gunn
 *
 */
public class PluginViewerAppExample {

	public static void main(String[] args)	{
		SceneGraphComponent world = new SceneGraphComponent();
		world.setGeometry(Primitives.sharedIcosahedron);
		PluginViewerApp pva = new PluginViewerApp(world);
		pva.display();
	}
 
}
