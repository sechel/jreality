/*
 * Created on Feb 1, 2005
 *
  */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
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

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		implodeFactor = eap.getAttribute(ShaderUtility.nameSpace(name, "implodeFactor"), implodeFactor);
		//JOGLConfiguration.theLog.log(Level.INFO,this+"Imploding with factor of "+implodeFactor);
      }
    
	public double getImplodeFactor() {
		return implodeFactor;
	}
	public boolean providesProxyGeometry() {		
		//if (implodeFactor == 0.0) return false;
		return true;
	}
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig) {
		if (!(original instanceof IndexedFaceSet)) return -1;
		GL gl = jr.globalGL;
		//JOGLConfiguration.theLog.log(Level.INFO,this+"Providing proxy geometry "+implodeFactor);
		IndexedFaceSet ifs =  IndexedFaceSetUtility.implode((IndexedFaceSet) original, implodeFactor);
		double alpha = vertexShader == null ? 1.0 : vertexShader.getDiffuseColorAsFloat()[3];
		int implodeDL = gl.glGenLists(1);
		gl.glNewList(implodeDL, GL.GL_COMPILE);
		//if (jr.isPickMode())	gl.glPushName(JOGLPickAction.GEOMETRY_BASE);
		JOGLRendererHelper.drawFaces(ifs, jr,  isSmoothShading(), alpha, jr.isPickMode(), 0);
		//if (jr.isPickMode())	gl.glPopName();
		gl.glEndList();
		return implodeDL;
	}
}
