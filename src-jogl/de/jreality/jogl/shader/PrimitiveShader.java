/*
 * Created on Feb 1, 2005
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.shader.DefaultTextShader;

/**
 * @author gunn
 *
 */
public interface PrimitiveShader extends Shader {
	public boolean providesProxyGeometry();
	public int proxyGeometryFor(JOGLRenderingState jrs);
	public DefaultTextShader getTextShader();
	public void render(JOGLRenderingState jrs);
	public void flushCachedState(JOGLRenderer jr);
}
