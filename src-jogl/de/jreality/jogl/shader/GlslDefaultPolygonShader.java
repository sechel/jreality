/*
 * Author	gunn
 * Created on Dec 22, 2005
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.math.Rn;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

public class GlslDefaultPolygonShader extends SimpleJOGLShader {
//		public Color	ambientColor,
//		diffuseColor,
//		specularColor;		
//		public double 	specularExponent, ambientCoefficient, diffuseCoefficient, specularCoefficient, transparency;	
//		public float[] specularColorAsFloat, ambientColorAsFloat, diffuseColorAsFloat;
	    protected int reflectionTextureUnit = -1;
		int frontBack = DefaultPolygonShader.FRONT_AND_BACK;
	    boolean	lightingEnabled = true;
//	    int numLights = 2;
		boolean changed = true;

		public GlslDefaultPolygonShader() {
			super("standard3dlabs.vert",null);
		}


	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
			lightingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
//			specularExponent = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
//			ambientCoefficient = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.AMBIENT_COEFFICIENT), CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
//			diffuseCoefficient = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COEFFICIENT), CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
//			specularCoefficient = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPECULAR_COEFFICIENT), CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
//			ambientColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.AMBIENT_COLOR), CommonAttributes.AMBIENT_COLOR_DEFAULT);
//			ambientColorAsFloat = ambientColor.getRGBComponents(null);
//			diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.DIFFUSE_COLOR_DEFAULT);
//			transparency= eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
//			//JOGLConfiguration.theLog.log(Level.INFO,"Name is "+name+" transparency is "+transparency);
//			diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transparency);
//			diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
//			specularColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
//			specularColorAsFloat = specularColor.getRGBComponents(null);
//			for (int i  = 0; i<3; ++i) {
//				ambientColorAsFloat[i] *= (float) ambientCoefficient;
//				diffuseColorAsFloat[i] *= (float) diffuseCoefficient;
//				specularColorAsFloat[i] *= (float) specularCoefficient;
//			}
			changed = true;
	}
	
	
	public int getFrontBack() {
		return frontBack;
	}
	public void setFrontBack(int frontBack) {
		this.frontBack = frontBack;
	}
	
	// we use openGL state (as set by the DefaultVertexShader) inside the
	// glsl shader, so we don't need to ship over those values here
	// only the lighting/no lighting flag is required.
	public void render(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		super.render(jrs);
		if (!changed) return;
		//System.err.println("Writing glsl values");
		// this seems to override the diffuse color
		//gl.glColor4fv( diffuseColorAsFloat);
//		JOGLConfiguration.theLog.fine("Writing "+numLights+" lights");
//	    gl.glUniform1iARB(getUniLoc(program, "numLights",gl),jr.openGLState.numLights );
//	    gl.glUniform1iARB(getUniLoc(program, "lightingEnabled",gl), lightingEnabled ? 1 : 0 );
//	    gl.glUniform1iARB(getUniLoc(program, "reflectionTextureUnit",gl), reflectionTextureUnit);
//	    gl.glUniform1fvARB(getUniLoc(program, "ambientColor",gl), 4,ambientColorAsFloat);
//	    gl.glUniform1fvARB(getUniLoc(program, "diffuseColor",gl), 4,diffuseColorAsFloat);
//	    gl.glUniform1fvARB(getUniLoc(program, "specularColor",gl), 4,specularColorAsFloat);
//	    gl.glUniform1fARB(getUniLoc(program, "ambientCoefficient",gl), 1f);
//	    gl.glUniform1fARB(getUniLoc(program, "diffuseCoefficient",gl), 1f);
//	    gl.glUniform1fARB(getUniLoc(program, "specularCoefficient",gl), 1f);
	    changed = false;
	}
	
}
