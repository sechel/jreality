/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


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
private int vortexLength;
  
  static String src;
  
  static boolean geforce6=true;
  
  static {
    IntegratorFactory rk = IntegratorFactory.rk2();
    rk.addConstant("const float PI = 3.141592653589793;");
    rk.addUniform("vorts0", "samplerRect");
    rk.addUniform("vorts1", "samplerRect");
    rk.addUniform("VORTEX_LENGTH", "int");
    rk.addUniform("a2", "float");
    if (geforce6) rk.addUniform("CNT", "int");
    
    rk.srcT0(
      "  return vec4(biotSavart(point.xyz, vorts0), 1);\n"
    );
  
    rk.srcT0_H2(
      "  return vec4(biotSavart(point.xyz, vorts1), 1);\n"
    );
  
    rk.addMethod("biotSavart", "vec3", "const vec3 pt, const samplerRect vorts", 
            "  vec3 ret;\n" + 
            "  \n" + 
            "  for (int i=0; i < VORTEX_LENGTH-1; i++) { \n" +
            "     int s1 = i%CNT;\n" +
            "     int t1 = (i-s1)/CNT;\n" +
            "     int s2 = (i+1)%CNT;\n" +
            "     int t2 = ((i+1)-s2)/CNT;\n" +
            
            "  vec2 texCoord1=vec2(float(s1),float(t1));\n" +
            "  vec4 data1=textureRect(vorts, texCoord1);\n" + 
            "  \n" + 
            "  vec3 v1=data1.xyz-pt;\n" + 
            "  \n" + 
            "  vec2 texCoord2=vec2(float(s2),float(t2));\n" +
            "  vec4 data2=textureRect(vorts, texCoord2);\n" + 
            "  \n" + 
            "  vec3 v2=data2.xyz-pt;\n" + 
            "  \n" + 
            "  float norm1=dot(v1, v1);\n" + 
            "  float norm2=dot(v2, v2);\n" + 
            "    \n" + 
            "      // add biot savart on for one edge \n" +
            "      \n" + 
            "      float strength=data2.w;\n" + 
            "      \n" + 
            "      vec3 e = v2-v1;\n" + 
            "      \n" + 
            "      if (strength != 0.0) {\n" + 
            "        ret += strength/(4.*PI)*biotSavartEdge(e, v1, norm1, v2, norm2);\n" + 
            "      }\n" + 
            " } \n" +
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
    prog.setUniform("VORTEX_LENGTH", vortexLength);
    if (geforce6) prog.setUniform("CNT", dataTextureSize);

  }
  
  public void setData(float[] data) {
    if (data == null || data.length == 1) {
      hasData = false;
      return;
    }
    int dataLen = (data.length-1)/2;
    if (dataT0 == null || dataT0.capacity() != dataLen) {
    vortexLength = dataLen/4;
	int texSize = GpgpuUtility.texSize(vortexLength);
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
