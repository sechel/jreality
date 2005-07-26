/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package de.jreality.worlds;

import java.awt.Component;

import de.jreality.examples.jogl.JOGLLoadableScene;

/**
 * @author gunn
 *
 */
public abstract class AbstractJOGLLoadableScene extends AbstractLoadableScene implements JOGLLoadableScene {
	
	
	public Component getInspector()	{ return null; }

	public boolean addBackPlane() {
		return false;
	}
	
	public boolean isEncompass() {
		return false;
	}

	public void dispose() {}
	

}
