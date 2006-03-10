/*
 * Author	gunn
 * Created on Aug 12, 2005
 *
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author gunn
 * 
 */
public class StandardOGLPolygonShader extends SimpleJOGLShader {

	boolean changed = true;
	/**
	 * 
	 */
	public StandardOGLPolygonShader() {
		super("standardOGL.vert", null);
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		changed = true;
	}

	public void render(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GL gl = jr.getCanvas().getGL();
		super.render(jrs);
		if (changed) {
			changed = false;
		}

	}

}
