/*
 * Created on May 27, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import javax.swing.JPanel;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;

import de.jreality.jogl.JOGLRendererNew;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Texture2D;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class RenderingHintsShader implements Shader {
	double levelOfDetail = 0.0;
	boolean 
	   transparencyEnabled = false, 
	   lightingEnabled = true, 
	   atInfinity = false,
	   antiAliasingEnabled = false,
	   isFastAndDirty = false;
	   

	/**
	 * 
	 */
	public RenderingHintsShader() {
		super();
	}

	public static RenderingHintsShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		RenderingHintsShader drh = new RenderingHintsShader();
		drh.setFromEffectiveAppearance(eap, name);
		return drh;
	}
	
	/* (non-Javadoc)
	 * @see de.jreality.jogl.Shader#setFromEffectiveAppearance(de.jreality.util.EffectiveAppearance)
	 */
	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
		ap.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,false);
		ap.setAttribute(CommonAttributes.AT_INFINITY,false);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		ap.setAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED,false);
		ap.setAttribute(CommonAttributes.LEVEL_OF_DETAIL, 0.0);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		lightingEnabled = eap.getAttribute(NameSpace.name(name,CommonAttributes.LIGHTING_ENABLED), true);
		transparencyEnabled = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY_ENABLED), false);
		antiAliasingEnabled = eap.getAttribute(NameSpace.name(name,CommonAttributes.ANTIALIASING_ENABLED), false);
		atInfinity = eap.getAttribute(NameSpace.name(name,CommonAttributes.AT_INFINITY), false);
		isFastAndDirty = eap.getAttribute(NameSpace.name(name,CommonAttributes.FAST_AND_DIRTY_ENABLED), false);
		levelOfDetail = eap.getAttribute(NameSpace.name(name,CommonAttributes.LEVEL_OF_DETAIL), 0.0);
		if (isFastAndDirty) levelOfDetail = 0.0;
	}
	/**
	 * @return
	 */
	public boolean isAntiAliasingEnabled() {
		return antiAliasingEnabled;
	}

	/**
	 * @return
	 */
	public double getLevelOfDetail() {
		return levelOfDetail;
	}

	/**
	 * @return
	 */
	public boolean isLightingEnabled() {
		return lightingEnabled;
	}

	/**
	 * @return
	 */
	public boolean isTransparencyEnabled() {
		return transparencyEnabled;
	}

	/**
	 * @return
	 */
	public boolean isAtInfinity() {
		return atInfinity;
	}

	/**
	 * @return
	 */
	public boolean isFastAndDirty() {
		return isFastAndDirty;
	}

	public void render(JOGLRendererNew jr)	{
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glDisable(GL.GL_TEXTURE_2D);
		if (isTransparencyEnabled())	{
			  gl.glEnable (GL.GL_BLEND);
			  gl.glDepthMask(false);
			  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
		} else	{
			  gl.glDepthMask(true);
			  gl.glDisable(GL.GL_BLEND);
		}
		if (isLightingEnabled())		gl.glEnable(GL.GL_LIGHTING);
		else							gl.glDisable(GL.GL_LIGHTING);

	}

	public class Inspector extends JPanel {
		Inspector()		{
			super();
			
		}
	}

}
