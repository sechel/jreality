/*
 * Created on Feb 1, 2005
 *
  */
package de.jreality.jogl.shader;

import java.util.logging.Level;

import net.java.games.jogl.GL;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author gunn
 *
 */
public class ImplodePolygonShader extends DefaultPolygonShader {
    double implodeFactor;
	private int implodeDL = -1;

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		implodeFactor = eap.getAttribute(ShaderUtility.nameSpace(name, "implodeFactor"), implodeFactor);
     }
    
	public double getImplodeFactor() {
		return implodeFactor;
	}
	public boolean providesProxyGeometry() {		
		//if (implodeFactor == 0.0) return false;
		return true;
	}
	public int proxyGeometryFor(JOGLRenderingState jrs)	{
		final Geometry original = jrs.getCurrentGeometry();
		final JOGLRenderer jr = jrs.getRenderer();
		final int sig = jrs.getCurrentSignature();
		final boolean useDisplayLists = jrs.isUseDisplayLists();
		if (!(original instanceof IndexedFaceSet)) return -1;
		if (implodeDL != -1) return implodeDL;
		GL gl = jr.globalGL;
		JOGLConfiguration.theLog.log(Level.INFO,this+"Providing proxy geometry "+implodeFactor);
		IndexedFaceSet ifs =  IndexedFaceSetUtility.implode((IndexedFaceSet) original, implodeFactor);
		double alpha = vertexShader == null ? 1.0 : vertexShader.getDiffuseColorAsFloat()[3];
		if (useDisplayLists) {
			implodeDL = gl.glGenLists(1);
			gl.glNewList(implodeDL, GL.GL_COMPILE);
		}
		//if (jr.isPickMode())	gl.glPushName(JOGLPickAction.GEOMETRY_BASE);
		jr.helper.drawFaces(ifs,  isSmoothShading(), alpha);
		//if (jr.isPickMode())	gl.glPopName();
		if (useDisplayLists) gl.glEndList();
		return implodeDL;
	}

	public void flushCachedState(JOGLRenderer jr) {
		super.flushCachedState(jr);
		if (implodeDL != -1) { jr.globalGL.glDeleteLists(implodeDL, 1);  implodeDL = -1; }
	}
}
