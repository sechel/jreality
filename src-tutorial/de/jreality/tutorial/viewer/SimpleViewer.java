package de.jreality.tutorial.viewer;

import de.jreality.geometry.Primitives;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.SceneGraphComponent;
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
		JRViewer.display(world);
	}
 
}
