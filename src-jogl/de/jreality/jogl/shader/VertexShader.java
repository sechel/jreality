/*
 * Created on Jan 31, 2005
 *
  */
package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.shader.GlslProgram;

/**
 * @author gunn
 *
 */
public interface VertexShader extends Shader {
	public void setFrontBack(int f);
	public Color getDiffuseColor();
	public float[] getDiffuseColorAsFloat();
	public void setGlsl(GlslProgram gp);
}
