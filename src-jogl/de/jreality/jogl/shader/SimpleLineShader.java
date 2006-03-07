/*
 * This shader is designed when only the diffuse color needs to be updated.
 * Use it instead of the default line shader.
 * Author	gunn
 * Created on Oct 12, 2005
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Geometry;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

public class SimpleLineShader extends AbstractPrimitiveShader implements LineShader {

	Color	diffuseColor;		
	double 	transparency, diffuseCoefficient;	
	float[] diffuseColorAsFloat;


	public void renderOld(JOGLRenderer jr) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glColor4fv( diffuseColorAsFloat);
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
