/*
 * Created on Dec 10, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.scene.Geometry;

/**
 * @author gunn
 *
 */
public interface PointShader extends PrimitiveShader {
	public double getPointRadius();
	public Color getDiffuseColor();
}
