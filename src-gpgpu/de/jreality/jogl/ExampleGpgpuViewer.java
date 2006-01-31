package de.jreality.jogl;

import net.java.games.jogl.GL;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.GlslSource;

public class ExampleGpgpuViewer extends AbstractGpgpuViewer {
  
  public ExampleGpgpuViewer() {
    super(true);
  }

  protected GlslSource initSource() {
    String prog = "uniform samplerRect values;\n"+
     "\n"+
     "uniform vec4 alpha;\n"+
     "\n"+
     "void main(void) {\n"+
     "  vec2 pos = gl_TexCoord[0].st;\n"+
     "  vec4 pt = textureRect(values, pos);\n"+
     "  gl_FragColor = pt+alpha;\n"+
     "}\n";
    if (isTex2D()) prog = prog.replaceAll("Rect", "2D");
    return new GlslSource(null, prog);
  }
  
  float[] alpha1 = new float[]{1,1,1,1};
  float[] alpha2 = new float[]{-1,-1,-1,-1};
  boolean odd;
  protected void prepareUniformValues(GL gl, GlslProgram prog) {
    super.prepareUniformValues(gl, prog);
    odd=!odd;
    prog.setUniform("alpha", odd ? alpha1 : alpha2);
  }
  
  protected void calculationFinished() {
    GpgpuUtility.dumpData(getCurrentValues());
    triggerCalculation();
  }
  
  public static void main(String[] args) {
    ExampleGpgpuViewer ev = new ExampleGpgpuViewer();
    ev.setValues(new float[]{1.1f,1.2f,1.3f,1.4f,2.1f,2.2f,2.3f,2.4f,3.1f,3.2f,3.3f,3.4f}); //,4.1f,4.2f,4.3f,4.4f});
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }
}
