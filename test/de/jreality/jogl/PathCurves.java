package de.jreality.jogl;

import net.java.games.jogl.GL;
import de.jreality.shader.GlslProgram;
import de.smokering.util.PointSets;

public class PathCurves extends AbstractCalculation {
  
  protected String initSource() {
    String uniforms =
     "uniform mat4 matrix;\n";
    String methods = 
      "vec4 evaluateT0(const vec4 pt) {\n"+
      "  return matrix*pt;" +
      "}\n" +
      "vec4 evaluateT0_H2(const vec4 pt) {\n"+
      "  return matrix*pt;" +
      "}\n" +
      "vec4 evaluateT0_H(const vec4 pt) {\n"+
      "  return matrix*pt;" +
      "}\n";
    return RungeKuttaGlslCode.rkUniforms() + uniforms
      + RungeKuttaGlslCode.rk4MethodDeclarations()
      + RungeKuttaGlslCode.rk4Main() + methods;
      
  }

  protected void prepareUniformValues(GL gl, GlslProgram prog) {
    super.prepareUniformValues(gl, prog);
    prog.setUniform("matrix", new float[]{0,1,0,0, -1,0,0,0, 0,0,1,0, 0,0,0,1});
    prog.setUniform("h", 0.01);
  }
  
  protected void calculationFinished() {
    if (numValues < 64) 
      GpgpuUtility.dumpData(getCurrentValues());
    triggerCalculation();
  }
  
  public static void main(String[] args) {
    PathCurves ev = new PathCurves();
    ev.setDisplayTexture(true);
    if (true) {
      int sl = 128;
      float[] f = new float[sl*sl*4];
      for (int i = 0; i < sl; i++) {
        for (int j = 0; j < sl; j++) {
          f[4*(sl*i+j)+0]=((float)i)/sl;
          f[4*(sl*i+j)+1]=((float)j)/sl;
          f[4*(sl*i+j)+2]=0f;
          f[4*(sl*i+j)+3]=1;
        }
      }
      ev.setValues(f);
    } else {
      ev.setValues(new float[]{1,0,0,1, 0,1,0,1, 0,0,1,1, 1,1,0,1});
      //ev.setValues(PointSets.randomFloats(256*256, null, 0.5, 1));
    }
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
