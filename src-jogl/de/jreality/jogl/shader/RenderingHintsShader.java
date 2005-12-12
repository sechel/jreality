/*
 * Created on May 27, 2004
 *
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class RenderingHintsShader  {
	double levelOfDetail = 0.0;		// hack for drawing lines in opengl
	boolean 
	   transparencyEnabled = false, 
	   zbufferEnabled = true,
	   lightingEnabled = true, 
	   atInfinity = false,
	   antiAliasingEnabled = false,
	   backFaceCullingEnabled = false,
	   isFastAndDirty = false,
	   useDisplayLists = true;
	   

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
		ap.setAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED, false);
		ap.setAttribute(CommonAttributes.AT_INFINITY,false);
		ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,false);
		ap.setAttribute(CommonAttributes.FAST_AND_DIRTY_ENABLED,false);
		ap.setAttribute(CommonAttributes.LEVEL_OF_DETAIL, 0.0);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		lightingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		transparencyEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY_ENABLED), false);
		zbufferEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.Z_BUFFER_ENABLED), true);
		antiAliasingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.ANTIALIASING_ENABLED), false);
		backFaceCullingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.BACK_FACE_CULLING_ENABLED), false);
		atInfinity = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.AT_INFINITY), false);
		isFastAndDirty = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.FAST_AND_DIRTY_ENABLED), false);
		useDisplayLists = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.ANY_DISPLAY_LISTS), true);
		levelOfDetail = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LEVEL_OF_DETAIL),CommonAttributes.LEVEL_OF_DETAIL_DEFAULT);
		//if (isFastAndDirty) levelOfDetail = 0.0;
	}

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

	public boolean isBackFaceCullingEnabled() {
		return backFaceCullingEnabled;
	}
	public void render(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		//gl.glDisable(GL.GL_TEXTURE_2D);
		//gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
		//if (transparencyEnabled != jr.openGLState.transparencyEnabled)	{
			if (transparencyEnabled)	{
			  gl.glEnable (GL.GL_BLEND);
			  gl.glDepthMask(false);
			  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			} else	{
			  gl.glDepthMask(true);
			  gl.glDisable(GL.GL_BLEND);
			}
			jr.openGLState.transparencyEnabled = transparencyEnabled;
		//}
		// problems with using the openGLState:  possibly related to
		// the fact that we're not doing any "popping" so the last value set
		// remains even if we leave the subtree where the set took placce..
		//if (lightingEnabled != jr.openGLState.lighting)	{
			if (lightingEnabled)			gl.glEnable(GL.GL_LIGHTING);
			else							gl.glDisable(GL.GL_LIGHTING);
			//jr.openGLState.lighting = lightingEnabled;
		//}
		if (backFaceCullingEnabled != jr.openGLState.backFaceCullingEnabled)	{
			if (backFaceCullingEnabled)  {
				gl.glEnable(GL.GL_CULL_FACE);
				gl.glCullFace(GL.GL_BACK);
			} else
				gl.glDisable(GL.GL_CULL_FACE);
			jr.openGLState.backFaceCullingEnabled = backFaceCullingEnabled;
		}
		jr.openGLState.levelOfDetail = levelOfDetail;
		// TODO: implement a handle for this front/back color flag
		//gl.glEnable(GL.GL_COLOR_MATERIAL);
		//gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
			

	}

	public void postRender(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		if (transparencyEnabled)	{
			  gl.glDepthMask(true);
			  gl.glDisable(GL.GL_BLEND);
			}
	}

	public boolean isUseDisplayLists() {
		return useDisplayLists;
	}
}
