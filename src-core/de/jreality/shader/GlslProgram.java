package de.jreality.shader;

import java.io.IOException;

import de.jreality.scene.Appearance;
import de.jreality.shader.GlslSource.UniformParameter;
import de.jreality.util.Input;

public class GlslProgram {
  
  private static final Object EMPTY=new Object();

  private final GlslSource source;
  private final Appearance app;
  private final EffectiveAppearance eApp;
  
  private final String pre;
  
  public static boolean hasGlslProgram(EffectiveAppearance eap, String prefix) {
    Object prog = eap.getAttribute(prefix+"::glsl-source", EMPTY, Object.class);
    return !(prog == EMPTY);
  }
  
  public GlslProgram(Appearance app, String prefix, Input vertexProgram, Input fragmentProgram) throws IOException {
		 this(app, prefix,   new GlslSource(vertexProgram, fragmentProgram));
  }

  public GlslProgram(Appearance app, String prefix, GlslSource s) {
	  	source = s;
	    this.app = app;
	    this.eApp = EffectiveAppearance.create().create(app);
	    pre = prefix+"::glsl-";
	    app.setAttribute(pre+"source", source);	  
  }
 
  public GlslProgram(Appearance app, String prefix, String vertexProgram, String fragmentProgram) {
	    source = new GlslSource(vertexProgram, fragmentProgram);
	    this.app = app;
	    this.eApp = EffectiveAppearance.create().create(app);
	    pre = prefix+"::glsl-";
	    app.setAttribute(pre+"source", source);
	  
  }

  public GlslProgram(Appearance app, String prefix, String[] vertexProgram, String[] fragmentProgram) {
    source = new GlslSource(vertexProgram, fragmentProgram);
    this.app = app;
    this.eApp = EffectiveAppearance.create().create(app);
    pre = prefix+"::glsl-";
    app.setAttribute(pre+"source", source);
  }
  
  public GlslProgram(EffectiveAppearance eap, String prefix) {
    this.app = null;
    this.eApp = eap;
    pre = prefix+"::"+"glsl-";
    if (!hasGlslProgram(eap, prefix)) throw new IllegalStateException("no program!");
    source = (GlslSource) eap.getAttribute(pre+"source", EMPTY, Object.class);
  }
  
  private void checkWrite() {
    if (app == null) throw new IllegalStateException("not writable!");
  }
  
  private void checkParam(String name, String type, boolean array, boolean matrix) {
    UniformParameter param = source.getUniformParameter(name);
    if (param == null) throw new IllegalStateException("no such parameter: "+name);
    if (type != null && !param.getType().equals(type)) throw new IllegalArgumentException("wrong type");
  }
  
  public void setUniform(String name, boolean value) {
    checkWrite();
    checkParam(name, "bool", false, false);
    assign(name, new float[]{value ? 1 : 0});
  }

  public void setUniform(String name, float value) {
    checkWrite();
    checkParam(name, "float", false, false);
    assign(name, new float[]{value});
  }

  public void setUniform(String name, int value) {
    checkWrite();
    checkParam(name, null, false, false);
    assign(name, new int[]{value});
  }

  public void setUniform(String name, float[] values) {
    checkWrite();
    checkParam(name, null, true, false);
    assign(name, values.clone());
  }
  
  public void setUniform(String name, int[] values) {
    checkWrite();
    checkParam(name, null, true, false);
    assign(name, values.clone());
  }

  private void assign(String name, Object values) {
    app.setAttribute(pre+name, values);
  }
  
  public void setUniform(String name, double value) {
    setUniform(name, (float)value);
  }
  
  public void setUniform(String name, double[] values) {
    float[] floats = new float[values.length];
    for (int i = 0; i < values.length; i++) floats[i] = (float) values[i];
    setUniform(name, floats);
  }

  public void setUniformMatrix(String name, float[] matrix) {
    checkWrite();
    checkParam(name, null, true, true);
    assign(name, matrix);
  }
  
  public void setUniformMatrix(String name, double[] matrix) {
    float[] floats = new float[matrix.length];
    for (int i = 0; i < matrix.length; i++) floats[i] = (float) matrix[i];
    setUniformMatrix(name, floats);
  }
  
  public Object getUniform(String name) {
    UniformParameter param = source.getUniformParameter(name);
    if (param == null) throw new IllegalArgumentException("no such uniform param");
    Object val = eApp.getAttribute(pre+name, EMPTY, Object.class);
    if (val == EMPTY) return null;
    return val;
  }
  
  public GlslSource getSource() {
    return source;
  }
  
  public static class UniformValue {
    private final UniformParameter param;
    private final Object value;
    private UniformValue(UniformParameter param, Object value) {
      this.param=param;
      this.value=value;
    }
    public UniformParameter getParameter() {
      return param;
    }
    public Object getValue() {
      return value;
    }
  }
}
