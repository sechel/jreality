package de.jreality.examples;

import javax.media.opengl.GL;

import de.jreality.jogl.AbstractCalculation;
import de.jreality.jogl.GpgpuUtility;
import de.jreality.jogl.IntegratorFactory;
import de.jreality.math.Matrix;
import de.jreality.shader.GlslProgram;

public class PathCurves extends AbstractCalculation {
  
  protected String initSource() {
    IntegratorFactory rk = IntegratorFactory.rk4();
    rk.addUniform("matrix", "mat4");
    rk.srcAll(
      "  vec4 ret = matrix*point;\n" +
      "  //ret.w = 0.;\n" +
      "  return ret;\n"
    );
    return rk.toString();
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
    ev.setReadData(true);
    int sl = 128;
    float[] f = GpgpuUtility.makeGradient(sl, 254);
    ev.setValues(f);
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
