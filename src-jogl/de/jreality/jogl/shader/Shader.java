/*
 * Created on May 27, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.util.EffectiveAppearance;

/**
 * @author Charles Gunn
 *
 */
public interface Shader {
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name);
	public void render(JOGLRenderer jr);
	public void postRender(JOGLRenderer jr);
}
