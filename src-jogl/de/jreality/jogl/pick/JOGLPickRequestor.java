/*
 * Created on Aug 16, 2004
 *
 */
package de.jreality.jogl.pick;

import de.jreality.scene.pick.PickPoint;

/**
 * @author gunn
 *
 */
public interface JOGLPickRequestor {
	public void pickPerformed(PickPoint[] hits);
}
