/*
 * Created on Jan 31, 2005
 *
  */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.shader.PolygonShader;
import de.jreality.scene.Geometry;
import de.jreality.util.EffectiveAppearance;

/**
 * @author gunn
 *
 */
public class TwoSidePolygonShader implements PolygonShader {
	   private PolygonShader front;
	   private PolygonShader back;

		/**
		 * 
		 */
	public TwoSidePolygonShader() {
		super();
		front = new DefaultPolygonShader();
		back = new DefaultPolygonShader();
	}

	public Color getDiffuseColor() {
		return front.getDiffuseColor();
	}

	public boolean isSmoothShading() {
		return front.isSmoothShading();
	}

//	public void setDiffuseColor(Color dc) {
//		front.setDiffuseColor(dc);
//		back.setDiffuseColor(dc);
//	}

	public void setSmoothShading(boolean b) {
		front.setSmoothShading(b);
		back.setSmoothShading(b);
	}

	public void render(JOGLRenderer jr) {
		if (front != null) front.render(jr);
		if (back != null) back.render(jr);
	}
	
	public void postRender(JOGLRenderer jr) {
		if (front != null) front.postRender(jr);
		if (back != null) back.postRender(jr);
	}
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String shaderName) {
	      front = ShaderLookup.getPolygonShaderAttr(eap, shaderName, "front");
	      front.setFrontBack(DefaultPolygonShader.FRONT);
	      back = ShaderLookup.getPolygonShaderAttr(eap, shaderName, "back");
	      back.setFrontBack(DefaultPolygonShader.BACK);
	      JOGLConfiguration.theLog.log(Level.FINE,"Front color is "+front.getDiffuseColor().toString());
	      JOGLConfiguration.theLog.log(Level.FINE,"Back color is "+back.getDiffuseColor().toString());
	}

	public void setFrontBack(int f) {
		// TODO figure out how to set up interface to avoid this absurd method
	}

	public boolean providesProxyGeometry() {
		return false;
	}

	public  int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig) {
		return  -1;
	}

}
