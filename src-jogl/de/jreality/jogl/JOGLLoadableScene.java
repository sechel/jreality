/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package de.jreality.jogl;

import java.awt.Component;

import de.jreality.util.LoadableScene;

/**
 * @author gunn
 *
 */
public interface JOGLLoadableScene extends LoadableScene {
	/* This is a provisional method which allows the LoadableScene access to
	 * a menu bar and the viewer
	 * in order to customize settings not yet implemented via configuration attributes.
	 * This is called after makeWorld() has been called.
	 */
	public Component getInspector();
	/* These also are temporary until I figure out out ConfigurationAttributes works
	 */
	public boolean isEncompass();
	public boolean addBackPlane();
	public void dispose();		// turn off timers, etc

}
