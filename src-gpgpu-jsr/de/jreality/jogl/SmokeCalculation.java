package de.jreality.jogl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;

import de.jreality.shader.GlslProgram;

public class SmokeCalculation extends AbstractCalculation {
  
  private double a;
  private double h;
  
  private FloatBuffer dataT0, dataT0_H2;
  private int dataTextureSize;
  
  private int[] texIDs = new int[2];
  
  private boolean hasData;
  private boolean dataTextureSizeChanged;
  private boolean dataChanged;
  
  static String src;
  
  static boolean geforce6=false;
  
  static {
    IntegratorFactory rk = IntegratorFactory.rk2();
    rk.addConstant("const float PI = 3.141592653589793;");
    rk.addUniform("vorts0", "samplerRect");
    rk.addUniform("vorts1", "samplerRect");
    rk.addUniform("a2", "float");
    if (geforce6) rk.addUniform("CNT", "int");
    
    rk.srcT0(
      "  return vec4(biotSavart(point.xyz, vorts0), 0);\n"
    );
  
    rk.srcT0_H2(
      "  return vec4(biotSavart(point.xyz, vorts1), 0);\n"
    );
  
    rk.addMethod("biotSavart", "vec3", "const vec3 pt, const samplerRect vorts", 
        "  vec3 ret;\n" + 
        "  \n" + 
        "  vec2 texCoord=vec2(0.5,0.5);\n" +
        "  vec4 data=textureRect(vorts, texCoord);\n" + 
        "  \n" + 
        "  vec3 v1=data.xyz-pt;\n" + 
        "  vec3 v2;\n" + 
        "  \n" + 
        "  float norm1=dot(v1, v1);\n" + 
        "  float norm2;\n" + 
        "    \n" + 
        "  for (int i=0; i < CNT; i++) {\n" + 
        "    for (int j=i>0?0:1; j < CNT; j++) {\n" + 
        "      // add biot savart on for one edge \n" +
        "      texCoord.x=float(j)+0.5; \n" +
        "      texCoord.y=float(i)+0.5; \n" +
        "      data=textureRect(vorts, texCoord);\n" + 
        "      \n" + 
        "      float strength=data.w;\n" + 
        "      \n" + 
        "      v2 =data.xyz-pt;\n" + 
        "      norm2=dot(v2, v2);\n" + 
        "      \n" + 
        "      vec3 e = v2-v1;\n" + 
        "      \n" + 
        "      if (strength != 0.0) {\n" + 
        "        ret += strength/(4.*PI)*biotSavartEdge(e, v1, norm1, v2, norm2);\n" + 
        "      }\n" + 
        "      vec3 swap = v1;\n" + 
        "      v1 = v2;\n" + 
        "      v2 = swap;\n" + 
        "      norm1 = norm2;\n" + 
        "    }\n" + 
        "  }\n" + 
        "  return ret;\n"
      );

    rk.addMethod("biotSavartEdge", "vec3", "const vec3 edge, const vec3 v1, const float norm1, const vec3 v2, const float norm2", 
      "  \n" + 
      "  float scpG = dot(v1, v2);\n" + 
      "  \n" + 
      "  float fac1 = (scpG - norm1) / sqrt(a2 + norm1);\n" + 
      "  float fac2 = (norm2 - scpG) / sqrt(a2 + norm2);\n" + 
      "  \n" + 
      "  vec3 ret = cross(v1, v2);\n" + 
      "  \n" + 
      "  float denom = dot(edge, edge) * a2 + dot(ret, ret);\n" + 
      "  return (denom > 1.E-9 ? (fac2 - fac1)/denom : 0.) * ret;\n"
    );
    src = rk.toString();
  }
  
  protected String initSource() {
    return geforce6 ? src : src.replaceAll("CNT", ""+dataTextureSize);
  }
  
  protected String updateSource() {
    if (geforce6 || !dataTextureSizeChanged) return null;
    return initSource();
  }
  
  protected void initDataTextures(GL gl) {
    if (dataTextureSizeChanged) {
      if (texIDs[0]!=0) { // delete prev textures
        gl.glDeleteTextures(2, texIDs,0);
      }
      gl.glGenTextures(2, texIDs,0);
      setupTexture(gl, texIDs[0], dataTextureSize);
      setupTexture(gl, texIDs[1], dataTextureSize);
      dataTextureSizeChanged = false;
    }
    if (dataChanged) {
      dataT0.clear();
      dataT0_H2.clear();
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
    
    prog.setUniform("a2", a*a);
    prog.setUniform("h", h);
    prog.setUniform("r3", true);
    if (geforce6) prog.setUniform("CNT", dataTextureSize);

  }
  
  public void setData(float[] data) {
    if (data == null || data.length == 1) {
      hasData = false;
      return;
    }
    int dataLen = (data.length-1)/2;
    if (dataT0 == null || dataT0.capacity() != dataLen) {
      int texSize = GpgpuUtility.texSize(dataLen/4);
      if (texSize != dataTextureSize) {
        System.out.println("[setVortexData] new vortex tex size="+texSize+" data.length="+data.length);
        dataTextureSize=texSize;
        dataTextureSizeChanged=true;
        dataT0 = ByteBuffer.allocateDirect(texSize*texSize*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        dataT0_H2 = ByteBuffer.allocateDirect(texSize*texSize*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      }
    }
    h=data[0];
    dataT0.clear();
    dataT0_H2.clear();
    dataT0.put(data, 1, dataLen);
    dataT0_H2.put(data, 1+dataLen, dataLen);
    while (dataT0.hasRemaining()) {
      dataT0.put(0f); dataT0_H2.put(0f);
    }
    dataChanged=hasData=true;
  }

  public void setA(double a) {
    this.a=a;
  }

  public void triggerCalculation() {
    if (hasData) super.triggerCalculation();
  }
  
  protected void calculationFinished() {
    if (numValues < 30) GpgpuUtility.dumpData(getCurrentValues());
  }
}
