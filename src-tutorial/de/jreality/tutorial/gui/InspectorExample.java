package de.jreality.tutorial.gui;

import de.jreality.tutorial.geom.TubeFactory02;
import de.jreality.ui.viewerapp.Navigator;
import de.jreality.ui.viewerapp.ViewerApp;

/**
 * This class shows how to add GUI elements to the {@link ViewerApp} class. In particular, it
 * <ul>
 * <li>adds an inspection panel to the {@link Navigator}, and</li>
 * <li>adds a key listeners to the viewing component of the ViewerApp instance, and </li>
 * </ul>
 * 
 * @see TubeFactory02 (same class with different name)
 * @author Charles Gunn
 *
 */
public class InspectorExample {

	public static void main(String[] args) {
		TubeFactory02 tf02 = new TubeFactory02();
		tf02.doIt();
	}

}
