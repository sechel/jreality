/*
 * Created on May 27, 2004
 *
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class RenderingHintsShader  {
	double levelOfDetail = 0.0;			// a number between 0= min and 1=max level and detail.
	boolean 
	   transparencyEnabled = false, 
	   zBufferEnabled = false,					// this only matters when transparencyEnabled == true
	   lightingEnabled = true, 
	   antiAliasingEnabled = false,				// do we need this anymore?
	   backFaceCullingEnabled = false,
	   isFastAndDirty = false,
	   useDisplayLists = true,
	   clearColorBuffer = true; 
	   

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
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		lightingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		transparencyEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY_ENABLED), false);
		zBufferEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.Z_BUFFER_ENABLED), false);
		antiAliasingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.ANTIALIASING_ENABLED), false);
		backFaceCullingEnabled = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.BACK_FACE_CULLING_ENABLED), false);
		isFastAndDirty = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.FAST_AND_DIRTY_ENABLED), false);
		useDisplayLists = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.ANY_DISPLAY_LISTS), true);
		levelOfDetail = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LEVEL_OF_DETAIL),CommonAttributes.LEVEL_OF_DETAIL_DEFAULT);
		clearColorBuffer = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.CLEAR_COLOR_BUFFER),true);
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
	public boolean isFastAndDirty() {
		return isFastAndDirty;
	}

	public boolean isBackFaceCullingEnabled() {
		return backFaceCullingEnabled;
	}
	
	public boolean isClearColorBuffer() {
		return clearColorBuffer;
	}

	public void render(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
			if (transparencyEnabled)	{
			  gl.glEnable (GL.GL_BLEND);
			  gl.glDepthMask(zBufferEnabled);
			  gl.glBlendFunc (GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			} else	{
			  gl.glDepthMask(true);
			  gl.glDisable(GL.GL_BLEND);
			}
			jr.openGLState.transparencyEnabled = transparencyEnabled;
			if (lightingEnabled)			gl.glEnable(GL.GL_LIGHTING);
			else							gl.glDisable(GL.GL_LIGHTING);
			if (backFaceCullingEnabled)  {
				gl.glEnable(GL.GL_CULL_FACE);
				gl.glCullFace(GL.GL_BACK);
			} else
				gl.glDisable(GL.GL_CULL_FACE);
			jr.openGLState.levelOfDetail = levelOfDetail;

	}

	public void postRender(JOGLRenderingState jrs)	{
		JOGLRenderer jr = jrs.getRenderer();
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
