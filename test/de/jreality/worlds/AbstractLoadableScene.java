/*
 * Created on Jan 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.worlds;

import javax.swing.JMenuBar;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.LoadableScene;
import de.jreality.util.Pn;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class AbstractLoadableScene implements LoadableScene {
	ConfigurationAttributes config = null;

	public void customize(JMenuBar menuBar, Viewer viewer) { return; }
	
	public int getSignature() {
		return Pn.EUCLIDEAN;
	}
	public SceneGraphComponent makeWorld() {
		return null;
	}
	
	public void setConfiguration(ConfigurationAttributes config) {
		this.config = config;
	}

	public boolean addBackPlane() {
		return false;
	}
	public boolean isEncompass() {
		return false;
	}
}
