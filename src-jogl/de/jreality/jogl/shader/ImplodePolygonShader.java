/*
 * Created on Feb 1, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * @author gunn
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ImplodePolygonShader extends DefaultPolygonShader {
    double implodeFactor;

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		implodeFactor = eap.getAttribute(NameSpace.name(name, "implodeFactor"), implodeFactor);
		//System.out.println(this+"Imploding with factor of "+implodeFactor);
      }
    
	public double getImplodeFactor() {
		return implodeFactor;
	}
	public boolean providesProxyGeometry() {		
		if (implodeFactor == 0.0) return false;
		return true;
	}
	public int proxyGeometryFor(Geometry original, GL gl, int sig) {
		//System.out.println("Preparing to implode.");
		IndexedFaceSet ifs =  GeometryUtility.implode((IndexedFaceSet) original, implodeFactor);
		//System.out.println("Imploding with factor of "+implodeFactor);
		int implodeDL = gl.glGenLists(1);
		gl.glNewList(implodeDL, GL.GL_COMPILE);
		JOGLRendererHelper.drawFaces(ifs, gl, false, true, 1.0);
		gl.glEndList();
		return implodeDL;
	}
}
