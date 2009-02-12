package de.jreality.tutorial.viewer;

import javax.swing.JSlider;

import de.jreality.geometry.Primitives;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.plugin.PluginViewerApp;

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
		JSlider s = new JSlider();
		pva.addAccessory(s, "Test");
		pva.display();
	}
 
}
