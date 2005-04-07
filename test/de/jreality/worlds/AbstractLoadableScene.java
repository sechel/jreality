/*
 * Created on Jan 27, 2005
 *
 */
package de.jreality.worlds;

import java.awt.Component;

import javax.swing.JMenuBar;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Pn;

/**
 * @author gunn
 *
 */
public abstract class AbstractLoadableScene implements LoadableScene {
	
	public void dispose() {}
	
	ConfigurationAttributes config = null;

	public void customize(JMenuBar menuBar, Viewer viewer) { return; }
	
	public int getSignature() {
		return Pn.EUCLIDEAN;
	}
	public abstract SceneGraphComponent makeWorld();
	
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}
	
	public Component getInspector()	{ return null; }

	public boolean addBackPlane() {
		return false;
	}
	
	public boolean isEncompass() {
		return false;
	}
}
