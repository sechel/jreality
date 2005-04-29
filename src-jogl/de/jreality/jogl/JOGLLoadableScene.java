/*
 * Author	gunn
 * Created on Apr 28, 2005
 *
 */
package de.jreality.jogl;

import java.awt.Component;

import javax.swing.JMenuBar;

import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;

/**
 * @author gunn
 *
 */
public interface JOGLLoadableScene extends LoadableScene {
	public void setConfiguration(ConfigurationAttributes config);

	/* This is a provisional method which allows the LoadableScene access to
	 * a menu bar and the viewer
	 * in order to customize settings not yet implemented via configuration attributes.
	 * This is called after makeWorld() has been called.
	 */
	public void customize(JMenuBar menuBar, Viewer viewer);
	public Component getInspector();
	/* These also are temporary until I figure out out ConfigurationAttributes works
	 */
	public boolean isEncompass();
	public boolean addBackPlane();
	public void dispose();		// turn off timers, etc

}
