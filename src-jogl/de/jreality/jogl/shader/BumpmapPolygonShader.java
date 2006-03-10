/*
 * Author	gunn
 * Created on Jul 13, 2005
 *
 */
package de.jreality.jogl.shader;


import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import de.jreality.shader.ShaderUtility;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.shader.EffectiveAppearance;

/**
 * @author gunn
 *
 */
public class BumpmapPolygonShader extends SimpleJOGLShader {

	float[] rtable = new float[1000];
	Color surfaceColor = new Color(1f, .3f, .2f);
	double[] lightPosition = {0,0,0};
	double bumpDensity= 16.0, bumpSize = 0.15, specularFactor = 0.5;
	boolean changed = true;
	/**
	 * 
	 */
	public BumpmapPolygonShader()	{
		super("bumpmap.vert","bumpmap.frag");
	}
	
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		JOGLConfiguration.theLog.log(Level.INFO,"Evaluating appearance");

		Object foo = eap.getAttribute(ShaderUtility.nameSpace(name,"surfaceColor"), surfaceColor);
		if (foo instanceof Color)	surfaceColor = (Color) foo;
		bumpDensity = eap.getAttribute(ShaderUtility.nameSpace(name,"bumpDensity"), bumpDensity);
		bumpSize = eap.getAttribute(ShaderUtility.nameSpace(name,"bumpSize"), bumpSize);
		specularFactor = eap.getAttribute(ShaderUtility.nameSpace(name,"specularFactor"), specularFactor);
		changed = true;
	}
	public void render(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GL gl = jr.getCanvas().getGL();
		super.render(jrs);
		if (changed)	{
		    gl.glUniform1fARB(getUniLoc(program, "SpecularFactor",gl),(float) specularFactor);
		    gl.glUniform1fARB(getUniLoc(program, "BumpDensity",gl),(float) bumpDensity);
		    gl.glUniform1fARB(getUniLoc(program, "BumpSize",gl),(float) bumpSize);
			float[] chans = surfaceColor.getRGBComponents(null);
		    gl.glUniform3fARB(getUniLoc(program, "SurfaceColor",gl),chans[0], chans[1], chans[2]);
		    gl.glUniform3fARB(getUniLoc(program, "LightPosition",gl), (float)lightPosition[0], (float)lightPosition[1], (float)lightPosition[2]);
			changed = false;
		}
	   
	}
	
}
