/*
 * Author	gunn
 * Created on Apr 11, 2005
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.math.Rn;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author gunn
 *
 */
public class BrickPolygonShader extends SimpleJOGLShader {
	float[] rtable = new float[1000];
	Color brickColor = new Color(1f, .3f, .2f), 
	mortarColor = new Color(.85f, .85f, .84f);
	double[] brickSize = new double[]{0.3, 0.15},
		brickPct = new double[]{0.9, 0.85},
		lightPosition = new double[]{0.0, 0.0, 2.0};
	double specularContribution = 0.3;
	boolean changed = true;
	/**
	 * 
	 */
	public BrickPolygonShader()	{
		super("brick.vert","brick.frag");
		for (int i = 0; i<1000; ++i)	rtable[i] = (float) (Math.random()*.5 + .5);
	}
	
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		JOGLConfiguration.theLog.log(Level.INFO,"Evaluating appearance");

		Object foo = eap.getAttribute(ShaderUtility.nameSpace(name,"BrickColor"), brickColor);
		if (foo instanceof Color)	brickColor = (Color) foo;
		foo = eap.getAttribute(ShaderUtility.nameSpace(name,"MortarColor"), mortarColor);
		if (foo instanceof Color)	mortarColor = (Color) foo;
		foo = eap.getAttribute(ShaderUtility.nameSpace(name,"BrickSize"), brickSize);
		if (foo instanceof double[])	brickSize = (double[]) foo;
		foo = eap.getAttribute(ShaderUtility.nameSpace(name,"BrickPct"), brickPct);
		if (foo instanceof double[])	brickPct = (double[]) foo;
		foo = eap.getAttribute(ShaderUtility.nameSpace(name,"LightPosition"), lightPosition);
		if (foo instanceof double[])	lightPosition = (double[]) foo;
		specularContribution = eap.getAttribute(ShaderUtility.nameSpace(name,"specularCoefficient"), specularContribution);
		changed = true;
	}
	public void render(JOGLRenderer jr) {
		GL gl = jr.getCanvas().getGL();
		super.render(jr);
		if (changed)	{
		    gl.glUniform1fvARB(getUniLoc(program, "rtable",gl),100, rtable);
		    gl.glUniform1fARB(getUniLoc(program, "SpecularContribution",gl),(float) specularContribution);
		    gl.glUniform1fARB(getUniLoc(program, "DiffuseContribution",gl),(float) (1f - specularContribution));
			JOGLConfiguration.theLog.log(Level.INFO,"Setting specular coef to "+specularContribution);
			float[] chans = brickColor.getRGBComponents(null);
		    gl.glUniform3fARB(getUniLoc(program, "BrickColor",gl),chans[0], chans[1], chans[2]);
		    chans = mortarColor.getRGBComponents(null);
		    gl.glUniform3fARB(getUniLoc(program, "MortarColor",gl), chans[0], chans[1], chans[2]);
		    gl.glUniform2fARB(getUniLoc(program, "BrickSize",gl), (float) brickSize[0], (float) brickSize[1]);
			JOGLConfiguration.theLog.log(Level.INFO,"Brick Size is: "+Rn.toString(brickSize));
		    gl.glUniform2fARB(getUniLoc(program, "BrickPct",gl), (float) brickPct[0], (float) brickPct[1]);
		    gl.glUniform2fARB(getUniLoc(program, "MortarPct",gl), 1f - (float) brickPct[0], 1f - (float) brickPct[1]);
		    gl.glUniform3fARB(getUniLoc(program, "LightPosition",gl), (float)lightPosition[0], (float)lightPosition[1], (float)lightPosition[2]);
			changed = false;
		}
	   
	}
	
}
