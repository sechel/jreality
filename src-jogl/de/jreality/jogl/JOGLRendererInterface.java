/*
 * Created on Dec 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl;

import net.java.games.jogl.GLEventListener;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickPoint;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface JOGLRendererInterface extends GLEventListener {
	public void setAuxiliaryRoot(SceneGraphComponent sgc);
	public double getFramerate();
	public boolean isUseDisplayLists();
	public void setUseDisplayLists(boolean b);
	public PickPoint[] performPick(double[] d);
}
