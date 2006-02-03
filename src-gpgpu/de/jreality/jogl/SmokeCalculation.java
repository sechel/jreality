package de.jreality.jogl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import net.java.games.jogl.GL;
import de.jreality.shader.GlslProgram;

public class SmokeCalculation extends AbstractCalculation {
  
  private double ro;
  private double h;
  
  private FloatBuffer dataT0, dataT0_H2;
  private int dataTextureSize;
  
  private int[] texIDs = new int[2];
  
  private boolean hasData;
  private boolean dataTextureSizeChanged;
  private boolean dataChanged;
  
  static String src;
  
  static {
    IntegratorFactory rk = IntegratorFactory.rk2();
    rk.addUniform("vorts0", "samplerRect");
    rk.addUniform("vorts1", "samplerRect");
    rk.addUniform("vorts2", "samplerRect");
    rk.addUniform("ro", "float");
    
    rk.srcT0(
      "  return vec4(biotSavart(point.xyz, vorts0), 0);\n"
    );
  
    rk.srcT0_H2(
      "  return vec4(biotSavart(point.xyz, vorts1), 0);\n"
    );
  
    rk.addMethod("biotSavart", "vec3", "const vec3 pt, const samplerRect vort", 
      "  vec3 ret;\n" + 
      "  \n" + 
      "  vec4 data=textureRect(vort, vec2(0.5,0.5));\n" + 
      "  \n" + 
      "  vec3 v1=data.xyz-pt;\n" + 
      "  vec3 v2;\n" + 
      "  \n" + 
      "  float norm1=length(v1);\n" + 
      "  float norm2;\n" + 
      "    \n" + 
      "  for (int i=0; i < $CNT; i++) {\n" + 
      "    for (int j=0; j < $CNT; j++) {\n" + 
      "      // add biot savart on for one edge" + 
      "      data=textureRect(vort, vec2(j+.5, i+.5));\n" + 
      "      \n" + 
      "      float strength=data.w;\n" + 
      "      \n" + 
      "      v2 =data.xyz-pt;\n" + 
      "      norm2=length(v2);\n" + 
      "      \n" + 
      "      vec3 e = v2-v1;\n" + 
      "      \n" + 
      "      if (strength != 0) {\n" + 
      "        ret += strength*biotSavartEdge(e, pt, v1, norm1, v2, norm2);\n" + 
      "      }\n" + 
      "      vec3 swap = v1;\n" + 
      "      v1 = v2;\n" + 
      "      v2 = swap;\n" + 
      "      norm1 = norm2;\n" + 
      "    }\n" + 
      "  }\n" + 
      "  return ret;\n"
    );

    rk.addMethod("biotSavartEdge", "vec3", "const vec3 edge, const vec3 point, const vec3 v1, const float norm1, const vec3 v2, const float norm2", 
      "  float fac1 = dot(v1, edge);\n" + 
      "  float fac2 = dot(v2, edge);\n" + 
      "  \n" + 
      "  vec3 v1CrossS = cross(v1, edge);\n" + 
      "  vec3 v2CrossS = cross(v2, edge);\n" + 
      "  \n" + 
      "  float edgeLenSqr=dot(edge, edge);\n" + 
      "  \n" + 
      "  float normC1 = dot(v1CrossS, v1CrossS);\n" + 
      "  fac1 /= sqrt(ro*ro+norm1*norm1) * (ro*ro * edgeLenSqr + normC1);\n" + 
      "  \n" + 
      "  float normC2 = dot(v2CrossS, v2CrossS);\n" + 
      "  fac2 /= sqrt(ro*ro+norm2*norm2) * (ro*ro * edgeLenSqr + normC2);\n" + 
      "  \n" + 
      "  return fac2*v2CrossS - fac1*v1CrossS;\n"
    );
    src = rk.toString();
  }
  
  protected String initSource() {
    return src.replaceAll("\\$CNT", ""+dataTextureSize);
  }
  
  protected String updateSource() {
    if (!dataTextureSizeChanged) return null;
    return initSource();
  }
  
  protected void initDataTextures(GL gl) {
    if (dataTextureSizeChanged) {
      if (texIDs[0]!=0) { // delete prev textures
        gl.glDeleteTextures(2, texIDs);
      }
      gl.glGenTextures(2, texIDs);
      setupTexture(gl, texIDs[0], dataTextureSize);
      setupTexture(gl, texIDs[1], dataTextureSize);
      dataTextureSizeChanged = false;
    }
    if (dataChanged) {
      transferToTexture(gl, dataT0, texIDs[0], dataTextureSize);
      transferToTexture(gl, dataT0_H2, texIDs[1], dataTextureSize);
      dataChanged = false;
    }
  }
  
  protected void setUniformValues(GL gl, GlslProgram prog) {
    super.setUniformValues(gl, prog);
    gl.glActiveTexture(GL.GL_TEXTURE1);
    gl.glBindTexture(TEX_TARGET, texIDs[0]);
    prog.setUniform("vorts0", 1);
    
    gl.glActiveTexture(GL.GL_TEXTURE2);
    gl.glBindTexture(TEX_TARGET, texIDs[1]);
    prog.setUniform("vorts1", 2);
    
    prog.setUniform("ro", ro);
    prog.setUniform("h", h);
    prog.setUniform("r3", true);
  }
  
  public void setData(float[] data) {
    if (data == null || data.length == 0) {
      hasData = false;
      return;
    }
    int dataLen = (data.length-1)/2;
    if (dataT0 == null || dataT0.capacity() < data.length) {
      dataT0 = ByteBuffer.allocateDirect(dataLen*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      dataT0_H2 = ByteBuffer.allocateDirect(dataLen*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      int texSize = GpgpuUtility.texSize(dataLen/4);
      if (texSize != dataTextureSize) {
        System.out.println("[setVortexData] new vortex tex size="+texSize+" data.length="+data.length);
        dataTextureSize=texSize;
        dataTextureSizeChanged=true;
      }
      dataChanged=true;
    }
    h=data[0];
    dataT0.put(data, 1, dataLen);
    dataT0_H2.put(data, 1+dataLen, dataLen);
    hasData = true;
  }

  public void setRo(double ro) {
    this.ro=ro;
  }

  public void triggerCalculation() {
    if (hasData) super.triggerCalculation();
  }
  
  protected void calculationFinished() {
    if (numValues < 30) GpgpuUtility.dumpData(getCurrentValues());
  }
}
