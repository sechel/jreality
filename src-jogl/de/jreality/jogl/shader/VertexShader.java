/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.shader;

import java.awt.Color;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface VertexShader extends Shader {
	public void setFrontBack(int f);
	public Color getDiffuseColor();
	public float[] getDiffuseColorAsFloat();
}
