/*
 * Created on May 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.jogl.JOGLRendererNew;
import de.jreality.scene.Appearance;
import de.jreality.util.EffectiveAppearance;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface Shader {
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name);
	public void setDefaultValues(Appearance ap);
	public void render(JOGLRendererNew jr);
}
