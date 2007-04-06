/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.jogl.shader;


import java.awt.Color;
import java.util.logging.Level;

import javax.media.opengl.GL;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author gunn
 *
 */
public class BumpmapPolygonShader extends SimpleGLSLShader {

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
		GL gl = jr.getGL();
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
