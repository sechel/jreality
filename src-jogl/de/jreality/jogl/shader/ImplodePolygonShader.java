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

//    public final void shadePolygon(Polygon p, double vertexData[], Environment environment) {
//		double centerX = 0;
//		double centerY = 0;
//		double centerZ = 0;
//		for(int i = 0; i< p.length;i++) {
//			int pos = p.vertices[i];
//			centerX += vertexData[pos+Polygon.SX];
//			centerY += vertexData[pos+Polygon.SY];
//			centerZ += vertexData[pos+Polygon.SZ];
//		}
//
//		double oml = (1-implodeFactor)/p.length;
//		centerX *= oml;
//		centerY *= oml;
//		centerZ *= oml;
//		
//		for(int i = 0; i< p.length;i++) {
//			int pos = p.vertices[i];
//			vertexData[pos+Polygon.SX] = implodeFactor * vertexData[pos+Polygon.SX] + centerX;
//			vertexData[pos+Polygon.SY] = implodeFactor * vertexData[pos+Polygon.SY] + centerY;
//			vertexData[pos+Polygon.SZ] = implodeFactor * vertexData[pos+Polygon.SZ] + centerZ;
//			vertexShader.shadeVertex(vertexData,p.vertices[i],environment);
//		}
//    }


 
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		super.setFromEffectiveAppearance(eap, name);
		implodeFactor = eap.getAttribute(NameSpace.name(name, "implodeFactor"), implodeFactor);
		//System.out.println("Imploding with factor of "+implodeFactor);
      }
    
	public double getImplodeFactor() {
		return implodeFactor;
	}
	public boolean providesProxyGeometry() {		
		if (implodeFactor == 0.0) return false;
		return true;
	}
	public int proxyGeometryFor(Geometry original, GL gl) {
		System.out.println("Preparing to implode.");
		IndexedFaceSet ifs =  GeometryUtility.implode((IndexedFaceSet) original, implodeFactor);
		System.out.println("Imploding with factor of "+implodeFactor);
		int implodeDL = gl.glGenLists(1);
		gl.glNewList(implodeDL, GL.GL_COMPILE);
		JOGLRendererHelper.drawFaces(ifs, gl, false, true, 1.0);
		gl.glEndList();
		return implodeDL;
	}
}
