/*
 * Created on Jan 31, 2005
 *
 */
package de.jreality.jogl.shader;

import de.jreality.shader.EffectiveAppearance;

/**
 * @author gunn
 *
 */
public class FlatPolygonShader extends DefaultPolygonShader {

	/**
	 * 
	 */
	public FlatPolygonShader() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		super.setFromEffectiveAppearance(eap, name);
		smoothShading = false;
	}

}
