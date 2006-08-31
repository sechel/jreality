package de.jreality.renderman.shader;

import java.util.Map;

import de.jreality.renderman.RIBVisitor;
import de.jreality.renderman.SLShader;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;

public class FreePolygonShader extends AbstractRendermanShader {
	SLShader orig = new SLShader("orig");
	@Override
	public void setFromEffectiveAppearance(RIBVisitor ribv,
			EffectiveAppearance eap, String name) {
       Object foo = eap.getAttribute(name+"."+CommonAttributes.RMAN_SL_SHADER,orig);
       if (foo == Appearance.DEFAULT || !(foo instanceof SLShader) || foo == orig)
    	   throw new IllegalStateException("FreePolygonShader called without SLShader");
       SLShader sls = (SLShader) foo;
       map = sls.getParameters();
       shaderName = sls.getName();
       type = "Surface";
	}


}
