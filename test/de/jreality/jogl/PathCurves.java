package de.jreality.jogl;

import java.nio.FloatBuffer;

import net.java.games.jogl.GL;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.shader.GlslProgram;
import de.smokering.util.PointSets;

public class PathCurves extends AbstractCalculation {
  
  protected String initSource() {
    String uniforms =
      "uniform mat4 matrix;\n";
    String methods =
      "vec4 eval(const vec4 pt) {\n" +
      "  vec4 ret = matrix*pt;\n" +
      "  //ret.w = 0.;\n" +
      "  return ret;\n" +
      "}\n" +
      "vec4 evaluateT0(const vec4 pt) {\n"+
      "  return eval(pt);\n" +
      "}\n" +
      "vec4 evaluateT0_H2(const vec4 pt) {\n"+
      "  return eval(pt);\n" +
      "}\n" +
      "vec4 evaluateT0_H(const vec4 pt) {\n"+
      "  return eval(pt);\n" +
      "}\n";
    return RungeKuttaGlslCode.rkUniforms() + uniforms
      + RungeKuttaGlslCode.rk4MethodDeclarations() + "vec4 eval(const vec4 pt);\n"
      + RungeKuttaGlslCode.rk4Main() + methods;
    
//    return
//    "uniform samplerRect values;\n" +
//    "uniform float h;\n" +
//    "uniform bool r3;\n" +
//    "uniform mat4 matrix;\n" +
//      "void main(void) {\n" +
//      "  vec2 pos = gl_TexCoord[0].st;\n" +
//      "  vec4 pt = textureRect(values, pos);\n" +
//      "  vec4 res = pt + h*(matrix*pt);\n" +
//      "  if (r3) res.w = 1.;\n" +
//      "  gl_FragColor = res;\n" +
//      "}\n";
  }

  protected void prepareUniformValues(GL gl, GlslProgram prog) {
    super.prepareUniformValues(gl, prog);
    // some matrix in gl(3)
    double a=3, b=2, c=-.1;
    Matrix m = new Matrix(0,-a,-b,0, a,0,-c,0, b,c,0,0, 0,0,0,0);//MatrixBuilder.euclidean().rotate(Math.PI/2, 0,0,1).getMatrix();
    prog.setUniform("matrix", m.getArray());
    prog.setUniform("h", 0.005);
    prog.setUniform("r3", true);
  }
  
  protected void calculationFinished() {
//    if (numValues < 64) 
//      GpgpuUtility.dumpData(getCurrentValues());
//    else {
//      FloatBuffer fb = getCurrentValues();
//      fb.position(0).limit(3*4);
//      GpgpuUtility.dumpSelectedData(fb);
//    }
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
          f[4*(sl*i+j)+2]=0;
          f[4*(sl*i+j)+3]=1;
        }
      }
      ev.setValues(f);
    } else {
      //ev.setValues(new float[]{1,0,0,1, 0,1,0,1, 0,0,1,1, 1,1,0,1});
      ev.setValues(PointSets.randomFloats(128*128, null, 0, 1));
    }
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
