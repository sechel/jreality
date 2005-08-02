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
public interface PointShader extends PrimitiveShader {
	public double getPointRadius();
	public Color getDiffuseColor();
}
