package de.jreality.renderman.shader;

import java.util.Map;

import de.jreality.renderman.RIBVisitor;
import de.jreality.shader.EffectiveAppearance;

public interface RendermanShader {
	public void setFromEffectiveAppearance(RIBVisitor ribv, EffectiveAppearance ap, String name);
	public Map getAttributes();
	public String getType();
	public String getName();
}
