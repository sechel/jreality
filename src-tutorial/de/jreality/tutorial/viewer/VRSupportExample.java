package de.jreality.tutorial.viewer;

import de.jreality.tutorial.geom.ParametricSurfaceExample;

/**
 * This is a placeholder class which refers to another
 * tutorial class {@link ParametricSurfaceExample}. It provides a way of cross-indexing the themes 
 * without copying the code.
 * 
 * @author Charles Gunn
 *
 */
public class VRSupportExample {
	public static void main(String[] args) {
		ParametricSurfaceExample pse = new ParametricSurfaceExample();
		pse.doIt(true);
	}

}
