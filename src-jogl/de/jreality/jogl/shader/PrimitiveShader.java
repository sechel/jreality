/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Geometry;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface PrimitiveShader extends Shader {
	public boolean providesProxyGeometry();
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int signature);
}
