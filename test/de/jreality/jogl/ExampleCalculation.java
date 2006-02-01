package de.jreality.jogl;

import java.util.Random;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.GlslSource;
import de.smokering.util.PointSets;

public class ExampleCalculation extends AbstractCalculation {
  
  protected String initSource() {
    return "uniform samplerRect values;\n"+
     "\n"+
     "uniform vec4 alpha;\n"+
     "\n"+
     "void main(void) {\n"+
     "  vec2 pos = gl_TexCoord[0].st;\n"+
     "  vec4 pt = textureRect(values, pos);\n"+
     "  gl_FragColor = pt+alpha+vec4(0.00001*pos, 0, 0);\n"+
     "}\n";
  }

  Random r = new Random();
  float scl=0.001f;
  boolean odd;
  protected void prepareUniformValues(GL gl, GlslProgram prog) {
    super.prepareUniformValues(gl, prog);
    odd=!odd;
    prog.setUniform("alpha", new float[] {scl*r.nextFloat(), scl*r.nextFloat(), scl*r.nextFloat(), 0});
  }
  
  protected void calculationFinished() {
    triggerCalculation();
  }
  
  public static void main(String[] args) {
    ExampleCalculation ev = new ExampleCalculation();
    ev.setDisplayTexture(true);
    if (true) {
      int sl = 256;
      float[] f = new float[sl*sl*4];
      for (int i = 0; i < sl; i++) {
        for (int j = 0; j < sl; j++) {
          f[4*(sl*i+j)+0]=((float)i)/sl;
          f[4*(sl*i+j)+1]=0;
          f[4*(sl*i+j)+2]=((float)j)/sl;
          f[4*(sl*i+j)+3]=1;
        }
      }
      ev.setValues(f);
    } else {
      //ev.setValues(new float[]{1.1f,1.2f,1.3f,1.4f,2.1f,2.2f,2.3f,2.4f,3.1f,3.2f,3.3f,3.4f}); //,4.1f,4.2f,4.3f,4.4f});
      ev.setValues(PointSets.randomFloats(256*256, null, 0, 1));
    }
    ev.triggerCalculation();
    GpgpuUtility.run(ev);
  }

}
