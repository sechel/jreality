/*
 * Created on Dec 10, 2004
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
public interface PolygonShader extends Shader {
	public void setSmoothShading(boolean b);
	public boolean isSmoothShading();
	public void setDiffuseColor(Color dc);		// need to be able to overwrite the diffuse color by line and point diffuse color/**
}
