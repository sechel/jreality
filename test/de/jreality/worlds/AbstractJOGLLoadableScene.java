/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package de.jreality.worlds;

import java.awt.Component;

import javax.swing.JMenuBar;

import de.jreality.jogl.JOGLLoadableScene;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;

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
