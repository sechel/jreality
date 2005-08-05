/*
 * Author	gunn
 * Created on Jul 27, 2005
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author gunn
 *
 */
public class SimpleVertexShader implements VertexShader {
    	Color	diffuseColor;		
    	double 	transparency, diffuseCoefficient;	
    float[] diffuseColorAsFloat;
    int frontBack = DefaultPolygonShader.FRONT_AND_BACK;
	public Color getDiffuseColor() {
		return diffuseColor;
	}
	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}
	public void setFrontBack(int f) {
		frontBack = f;
	}
	public void postRender(JOGLRenderer jr) {
		
	}
	public void render(JOGLRenderer jr) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		JOGLConfiguration.theLog.log(Level.FINER,"Rendering simple vertex shader");

		if (jr.openGLState.frontBack != frontBack)	{
			gl.glColorMaterial(frontBack, GL.GL_DIFFUSE);
			jr.openGLState.frontBack = frontBack;
		}
//		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
//			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
//		}
	}
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		JOGLConfiguration.theLog.log(Level.FINER,"Setting simple vertex shader");
		diffuseCoefficient = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COEFFICIENT), CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.DIFFUSE_COLOR_DEFAULT);
		transparency= eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transparency);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
	}
}
