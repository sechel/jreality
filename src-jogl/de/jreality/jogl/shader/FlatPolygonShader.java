/*
 * Created on Jan 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.shader;

import de.jreality.util.EffectiveAppearance;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
