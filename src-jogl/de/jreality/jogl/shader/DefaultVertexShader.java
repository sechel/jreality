/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultVertexShader implements Shader {
	Color	ambientColor,
			specularColor;		
	double 	specularExponent, ambientCoefficient, specularCoefficent;	
	float[] specularColorAsFloat, ambientColorAsFloat;

	public static Color RED = new Color(1.0f, 0.0f, 0.0f, 0.5f);
	public static Color GREEN = new Color(0.0f, 1.0f, 0.0f, 0.5f);
	public static Color BLUE = new Color(0.0f, 0.0f, 1.0f, 0.5f);
	public static Color YELLOW = new Color(1.0f, 1.0f, 0.0f, 0.5f);
	public static Color PURPLE = new Color(1.0f, 0.0f, 1.0f, 0.5f);
	public static Color ORANGE = new Color(1.0f, .5f, 0.0f, 0.5f);
	public static Color WHITE = new Color(1.0f, 1.0f, 1.0f, 0.5f);
	public static Color GRAY = new Color(.5f,.5f,.5f, 0.5f);
	public static Color BLACK = new Color(0.0f, 0.0f, 0.0f, 0.5f);
	
	/**
	 * 
	 */
	public DefaultVertexShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		specularExponent = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
		ambientCoefficient = eap.getAttribute(NameSpace.name(name,CommonAttributes.AMBIENT_COEFFICIENT), CommonAttributes.AMBIENT_COEFFICIENT_DEFAULT);
		specularCoefficent = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_COEFFICIENT), CommonAttributes.SPECULAR_COEFFICIENT_DEFAULT);
		ambientColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.AMBIENT_COLOR), CommonAttributes.AMBIENT_COLOR_DEFAULT);
		ambientColorAsFloat = ambientColor.getRGBComponents(null);
		specularColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
		specularColorAsFloat = specularColor.getRGBComponents(null);
		for (int i  = 0; i<3; ++i) ambientColorAsFloat[i] *= (float) specularCoefficent;
		for (int i  = 0; i<3; ++i) specularColorAsFloat[i] *= (float) specularCoefficent;
			
	}

	/**
	 * @return
	 */
	public Color getAmbientColor() {
		return ambientColor;
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
		return ColorToFloat(ambientColor);
	}

	/**
	 * @return
	 */
	public float[] getSpecularColorAsFloat() {
		return ColorToFloat(specularColor);
	}

	private float[] ColorToFloat(Color cc)	{
		return cc.getRGBComponents(null);
		}


	/**
	 * @return
	 */
	public double getSpecularExponent() {
		return specularExponent;
	}

	public void render(JOGLRenderer jr)	{
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambientColorAsFloat);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specularColorAsFloat);
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, (float) getSpecularExponent());
	}

}
