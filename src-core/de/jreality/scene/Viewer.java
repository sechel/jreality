/*
 * Created on May 27, 2004
 *
 */
package de.jreality.scene;

import java.awt.Component;


/**
 * @author Charles Gunn
 *
 */
public interface Viewer {
	public SceneGraphComponent getSceneRoot();
	public void setSceneRoot(SceneGraphComponent r);
	public SceneGraphPath getCameraPath();
	public void setCameraPath(SceneGraphPath p);
	public void render();
	public boolean hasViewingComponent();
	public Component getViewingComponent();
	public int getSignature();		// See de.jreality.util.Pn for definitions
	public void setSignature(int sig);
	public void setAuxiliaryRoot(SceneGraphComponent ar);
	public SceneGraphComponent getAuxiliaryRoot();
}
