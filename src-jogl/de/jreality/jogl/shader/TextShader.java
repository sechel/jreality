/*
 * Author	gunn
 * Created on Apr 12, 2005
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.OpenGLState;
import de.jreality.scene.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author gunn
 *
 */
public class TextShader implements Shader {
	Color  diffuseColor = null;
	float[] diffuseColorAsFloat = null;
	
	public void postRender(JOGLRenderer jr) {
	}

	public void render(JOGLRenderer jr) {
		GL gl = jr.getCanvas().getGL();
		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
			//jr.openGLState.diffuseColor = diffuseColorAsFloat;
		}
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.DIFFUSE_COLOR_DEFAULT);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
	}
}
