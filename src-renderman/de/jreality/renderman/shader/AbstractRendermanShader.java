package de.jreality.renderman.shader;

import java.util.HashMap;
import java.util.Map;

import de.jreality.renderman.RIBVisitor;
import de.jreality.shader.EffectiveAppearance;

public abstract class AbstractRendermanShader implements RendermanShader {
	protected HashMap map = new HashMap();
	protected String shaderName;
	protected String type;
	protected RIBVisitor ribV = null;
	
	abstract public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance ap, String name);
	abstract public Map getAttributes();
	abstract public String getType() ;
	abstract public String getName();

}
