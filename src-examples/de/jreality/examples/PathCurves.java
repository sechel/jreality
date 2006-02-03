package de.jreality.examples;

import net.java.games.jogl.GL;
import de.jreality.jogl.AbstractCalculation;
import de.jreality.jogl.GpgpuUtility;
import de.jreality.jogl.RungeKuttaFactory;
import de.jreality.math.Matrix;
import de.jreality.shader.GlslProgram;

public class PathCurves extends AbstractCalculation {
  
  protected String initSource() {
    RungeKuttaFactory rk = RungeKuttaFactory.rk4();

      rk.addUniform("matrix", "mat4");
      rk.srcAll(
        "  vec4 ret = matrix*pt;\n" +
        "  //ret.w = 0.;\n" +
        "  return ret;\n"
      );

      String ret = rk.toString();
      System.out.println(ret);
    
      return ret;
    // this is the euler version
    
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

  protected void setUniformValues(GL gl, GlslProgram prog) {
    super.setUniformValues(gl, prog);
    // some matrix in gl(3)
    double a=3, b=2, c=-.1;
    Matrix m = new Matrix(0,-a,-b,0, a,0,-c,0, b,c,0,0, 0,0,0,0);
    //Matrix m = MatrixBuilder.euclidean().rotate(Math.PI/2, 0,0,1).getMatrix();
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
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
