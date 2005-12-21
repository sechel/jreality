package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Geometry;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;

/**
 * it is assumed that the shader source code stayes FIXED!
 * 
 * @author gollwas
 *
 */
public class GlslPolygonShader implements PolygonShader {

  GlslProgram program;
  
  public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
    program = new GlslProgram(eap, name);
  }

  public void render(JOGLRenderer jr) {
    GlslLoader.render(program, jr.getCanvas());
  }

  public void postRender(JOGLRenderer jr) {
    GlslLoader.postRender(program, jr.getCanvas());
  }

  public boolean providesProxyGeometry() {
    return false;
  }

  public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int signature, boolean useDisplayLists) {
    return 0;
  }
  public void setFrontBack(int f) {
  }

public void setProgram(GlslProgram program) {
	this.program = program;
}

}
