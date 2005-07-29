/*
 * Created on Apr 30, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.OpenGLState;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;
import de.jreality.util.Rn;
import de.jreality.util.ShaderUtility;


/**
 * @author gunn
 *
 */
public class DefaultVertexShader implements VertexShader {
	// TODO add the diffuse color here also, and transparency
	// the polygon shader if queried, consults this shader for these values
	Color	ambientColor,
			diffuseColor,
			specularColor;		
	double 	specularExponent, ambientCoefficient, diffuseCoefficient, specularCoefficient, transparency;	
	float[] specularColorAsFloat, ambientColorAsFloat, diffuseColorAsFloat;
	int frontBack = DefaultPolygonShader.FRONT_AND_BACK;
	
	/**
	 * 
	 */
	public DefaultVertexShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		specularExponent = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
		ambientCoefficient = eap.getAttribute(NameSpace.name(name,CommonAttributes.AMBIENT_COEFFICIENT), CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
		diffuseCoefficient = eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COEFFICIENT), CommonAttributes.DIFFUSE_COEFFICIENT_DEFAULT);
		specularCoefficient = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_COEFFICIENT), CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
		ambientColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.AMBIENT_COLOR), CommonAttributes.AMBIENT_COLOR_DEFAULT);
		ambientColorAsFloat = ambientColor.getRGBComponents(null);
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.DIFFUSE_COLOR_DEFAULT);
		transparency= eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		//JOGLConfiguration.theLog.log(Level.INFO,"Name is "+name+" transparency is "+transparency);
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transparency);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		specularColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
		specularColorAsFloat = specularColor.getRGBComponents(null);
		for (int i  = 0; i<3; ++i) ambientColorAsFloat[i] *= (float) ambientCoefficient;
		for (int i  = 0; i<3; ++i) diffuseColorAsFloat[i] *= (float) diffuseCoefficient;
		for (int i  = 0; i<3; ++i) specularColorAsFloat[i] *= (float) specularCoefficient;
	}

	/**
	 * @return
	 */
	public Color getAmbientColor() {
		return ambientColor;
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}

	/**
	 * @return
	 */
	public Color getSpecularColor() {
		return specularColor;
	}
	/**
	 * @return
	 */
	public float[] getAmbientColorAsFloat() {
		return ambientColorAsFloat;
	}

	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}

	/**
	 * @return
	 */
	public float[] getSpecularColorAsFloat() {
		return specularColorAsFloat;
	}


	/**
	 * @return
	 */
	public double getSpecularExponent() {
		return specularExponent;
	}

	
	public int getFrontBack() {
		return frontBack;
	}
	public void setFrontBack(int frontBack) {
		this.frontBack = frontBack;
	}
	
	public void render(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();

		if (jr.openGLState.frontBack != frontBack)	{
			gl.glColorMaterial(frontBack, GL.GL_DIFFUSE);
			jr.openGLState.frontBack = frontBack;
		}
//		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
//			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
//		}
		gl.glMaterialfv(frontBack, GL.GL_AMBIENT, ambientColorAsFloat);
		gl.glMaterialfv(frontBack, GL.GL_SPECULAR, specularColorAsFloat);
		gl.glMaterialf(frontBack, GL.GL_SHININESS, (float) getSpecularExponent());
		JOGLConfiguration.theLog.log(Level.FINEST,"VertexShader: Setting diffuse color to: "+Rn.toString(getDiffuseColorAsFloat()));
	}

	public void postRender(JOGLRenderer jr) {
	}

}
