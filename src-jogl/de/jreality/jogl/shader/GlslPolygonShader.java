package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.scene.Geometry;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;

/**
 * it is assumed that the shader source code stayes FIXED!
 * 
 * @author gollwas
 *
 */
public class GlslPolygonShader extends AbstractPrimitiveShader implements PolygonShader {

  GlslProgram program;
  
  public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
	super.setFromEffectiveAppearance(eap, name);
   program = new GlslProgram(eap, name);
  }

  public void render(JOGLRenderingState jrs) {
	  JOGLRenderer jr = jrs.getRenderer();
    GlslLoader.render(program, jr.getCanvas());
  }

  public void postRender(JOGLRenderingState jrs) {
	  JOGLRenderer jr = jrs.getRenderer();
    GlslLoader.postRender(program, jr.getCanvas());
  }

public void setFrontBack(int f) {
  }

public void setProgram(GlslProgram program) {
	this.program = program;
}


}
