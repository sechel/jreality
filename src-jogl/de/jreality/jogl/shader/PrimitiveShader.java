/*
 * Created on Feb 1, 2005
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Geometry;

/**
 * @author gunn
 *
 */
public interface PrimitiveShader extends Shader {
	public boolean providesProxyGeometry();
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int signature, boolean useDisplayLists);
	public TextShader getTextShader();
}
