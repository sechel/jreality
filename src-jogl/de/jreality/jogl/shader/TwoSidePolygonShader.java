/*
 * Created on Jan 31, 2005
 *
  */
package de.jreality.jogl.shader;


import java.util.logging.Level;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.LoggingSystem;

/**
 * @author gunn
 *
 */
public class TwoSidePolygonShader extends AbstractPrimitiveShader implements PolygonShader {
	   private PolygonShader front;
	   private PolygonShader back;

		/**
		 * 
		 */
	public TwoSidePolygonShader() {
		super();
		front = new ImplodePolygonShader();
		back = new ImplodePolygonShader();
	}


	public void render(JOGLRenderingState jrs) {
		Geometry g = jrs.getCurrentGeometry();
		jrs.setCurrentGeometry(null);
		if (back != null) back.render(jrs);
		jrs.setCurrentGeometry(g);
		if (front != null) front.render(jrs);
	}
	
	public void postRender(JOGLRenderingState jrs) {
		if (front != null) front.postRender(jrs);
		if (back != null) back.postRender(jrs);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String shaderName) {
	      front = (PolygonShader) ShaderLookup.getShaderAttr(eap, shaderName, CommonAttributes.POLYGON_SHADER, "front");
	      LoggingSystem.getLogger(this).log(Level.FINER,"Front shader is "+front.getClass().toString());
	      front.setFrontBack(DefaultPolygonShader.FRONT);
	      back = (PolygonShader) ShaderLookup.getShaderAttr(eap, shaderName,CommonAttributes.POLYGON_SHADER, "back");
	      back.setFrontBack(DefaultPolygonShader.BACK);
	}

	public void setFrontBack(int f) {
		// TODO figure out how to set up interface to avoid this absurd method
	}

	public boolean providesProxyGeometry() {
		LoggingSystem.getLogger(this).log(Level.FINER,"Front has proxy: "+front.providesProxyGeometry());
		if (front != null) return front.providesProxyGeometry();
		return false;
	}

	public  int proxyGeometryFor(JOGLRenderingState jrs)	{
		int dp = 0;
		if (front != null) {
			dp = front.proxyGeometryFor(jrs);
			LoggingSystem.getLogger(this).log(Level.FINER,"Providing dl "+dp);
			return dp;
		}
		return  -1;
	}

	public void flushCachedState(JOGLRenderer jr) {
		if (front != null) front.flushCachedState(jr);
		if (back != null) back.flushCachedState(jr);
	}

}
