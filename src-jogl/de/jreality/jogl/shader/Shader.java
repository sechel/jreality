/*
 * Created on May 27, 2004
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author Charles Gunn
 *
 */
public interface Shader {
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name);
	public void render(JOGLRenderingState jrs);
	public void postRender(JOGLRenderingState jrs);
}
