/*
 * Created on Apr 30, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.util.EffectiveAppearance;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultMaterialShader implements Shader {
	Color	ambientColor,
			diffuseColor,
			specularColor,
			edgeColor,
			normalColor;		// for easier handling
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
	public DefaultMaterialShader() {
		super();
		ambientColor = java.awt.Color.WHITE;
		diffuseColor = java.awt.Color.BLUE;
		edgeColor = java.awt.Color.BLACK;
		normalColor = java.awt.Color.BLACK;
		specularColor = java.awt.Color.WHITE;
		specularExponent = 100.0;
		
	}

	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute("ambientColor",java.awt.Color.WHITE);
		ap.setAttribute("diffuseColor",java.awt.Color.BLUE);
		ap.setAttribute("specularColor",java.awt.Color.WHITE);
		ap.setAttribute("edgeColor",java.awt.Color.BLACK);;	
		ap.setAttribute("normalColor",java.awt.Color.GRAY);;	
		ap.setAttribute("specularExponent",100.0);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap)	{
		ambientColor = (Color) eap.getAttribute("ambientColor", CommonAttributes.AMBIENT_COLOR_VALUE);
		diffuseColor = (Color) eap.getAttribute("diffuseColor", CommonAttributes.DIFFUSE_COLOR_VALUE);
		specularColor = (Color) eap.getAttribute("specularColor", CommonAttributes.SPECULAR_COLOR_VALUE);
		edgeColor = (Color) eap.getAttribute("edgeColor", edgeColor);
		normalColor = (Color) eap.getAttribute("normalColor", normalColor);
		specularExponent = eap.getAttribute("specularExponent", specularExponent);
			
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
	public Color getDiffuseColor() {
		return diffuseColor;
	}

	/**
	 * @return
	 */
	public Color getEdgeColor() {
		return edgeColor;
	}

	/**
	 * @return
	 */
	public Color getNormalColor() {
		return normalColor;
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
	public float[] getDiffuseColorAsFloat() {
		return ColorToFloat(diffuseColor);
	}

	/**
	 * @return
	 */
	public float[] getEdgeColorAsFloat() {
		return ColorToFloat(edgeColor);
	}

	/**
	 * @return
	 */
	public float[] getNormalColorAsFloat() {
		return ColorToFloat(normalColor);
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


}
