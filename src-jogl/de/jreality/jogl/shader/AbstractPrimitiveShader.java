/*
 * Author	gunn
 * Created on Mar 6, 2006
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.DefaultTextShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

public abstract class AbstractPrimitiveShader implements PrimitiveShader {
	public DefaultTextShader textShader = null;
	
	public boolean providesProxyGeometry() {
		return false;
	}

	public int proxyGeometryFor(JOGLRenderingState jrs) {
		return -1;
	}

	public DefaultTextShader getTextShader() {
		return textShader;
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
	    if (AttributeEntityUtility.hasAttributeEntity(DefaultTextShader.class, ShaderUtility.nameSpace(name,"textShader"), eap));
	    	textShader = (DefaultTextShader) AttributeEntityUtility.createAttributeEntity(DefaultTextShader.class, ShaderUtility.nameSpace(name,"textShader"), eap);
	}

	public void flushCachedState(JOGLRenderer jr) {
	}

	public void postRender(JOGLRenderingState jrs) {
	}

	public void render(JOGLRenderingState jrs) {
	}
}
