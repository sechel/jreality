/*
 * Created on Dec 10, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

/**
 * @author gunn
 *
 */
public interface PolygonShader extends PrimitiveShader {
	public void setSmoothShading(boolean b);
	public boolean isSmoothShading();
	public Color getDiffuseColor();
	public void setFrontBack(int f);
}
