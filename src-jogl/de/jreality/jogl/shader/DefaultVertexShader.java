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

import de.jreality.jogl.JOGLRendererNew;
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
	double 	specularExponent;	

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

	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute("ambientColor",java.awt.Color.WHITE);
		ap.setAttribute("specularColor",java.awt.Color.WHITE);
		ap.setAttribute("specularExponent",100.0);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		ambientColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.AMBIENT_COLOR), CommonAttributes.AMBIENT_COLOR_DEFAULT);
		specularColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
		specularExponent = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
			
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

	public void render(JOGLRendererNew jr)	{
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, getSpecularColorAsFloat());
		gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, (float) getSpecularExponent());
	}

}
