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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

import de.jreality.jogl.shader.GlslLoader;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.shader.GlslProgram;

public abstract class AbstractCalculation implements GLEventListener {

  private static final String RENDER_PROGRAM = "uniform samplerRect values;\n" +
  "uniform float scale;\n" +
  "void main(void) {\n" +
  "  vec2 pos = gl_TexCoord[0].st;\n" +
  "  vec4 col = textureRect(values, pos);" +
  "  gl_FragColor = abs(col/col.w); //vec4(scale*rescale*a, 1.);\n" + 
  "}\n";

private static final double[] ID = Rn.identityMatrix(4);

  protected boolean doIntegrate;
  
  private boolean tex2D;
  protected int TEX_TARGET;
  private int TEX_INTERNAL_FORMAT;
  protected static int TEX_FORMAT = GL.GL_RGBA;
  private boolean atiHack;
  
  protected GlslProgram program;
  
  private GlslProgram renderer;
  
  private int[] fbos = new int[1]; // 1 framebuffer
  private int[] vbo = new int[1]; // 1 vertexbuffer
  private int[] initialValuesTextureID = new int[2]; // ping pong textures
  private int[] attachments = {GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT};
  protected int readTex;

  private int writeTex = 1;

  protected FloatBuffer valueBuffer;
  protected int valueTextureSize;
  protected int numValues;

  protected boolean valuesChanged;
  protected boolean valueTextureSizeChanged;
  private boolean hasValues;

  private boolean hasValidVBO;

  private boolean readData=true;

  private boolean displayTexture;
  private int[] canvasViewport = new int[2];

  private boolean measureCPS=true;

  public void init(GLAutoDrawable drawable) {
    if (!drawable.getGL().isExtensionAvailable("GL_ARB_fragment_shader")
        || !drawable.getGL().isExtensionAvailable("GL_ARB_vertex_shader")
        || !drawable.getGL().isExtensionAvailable("GL_ARB_shader_objects")
        || !drawable.getGL().isExtensionAvailable("GL_ARB_shading_language_100")) {
      JOptionPane.showMessageDialog(null, "<html><center>Driver does not support OpenGL Shading Language!<br>Cannot execute program.</center></html>");
      System.exit(-1);
    }
    if (drawable.getGL().isExtensionAvailable("GL_APPLE_client_storage")) {
    	System.out.println("AbstractCalculation.init(): GL_APPLE_client_storage");
    }
   //drawable.setGL(new DebugGL(drawable.getGL()));
   String vendor = drawable.getGL().glGetString(GL.GL_VENDOR);
   tex2D = true;//!vendor.startsWith("NVIDIA");
   atiHack=false;//tex2D;
   TEX_TARGET = tex2D ? GL.GL_TEXTURE_2D : GL.GL_TEXTURE_RECTANGLE_ARB;
   TEX_INTERNAL_FORMAT = tex2D ? GL.GL_RGBA32F_ARB : GL.GL_RGBA32F_ARB;
   renderer = new GlslProgram(new Appearance(), "foo", null, isTex2D() ? RENDER_PROGRAM.replaceAll("Rect", "2D") : RENDER_PROGRAM);
  }
  
  public void display(GLAutoDrawable drawable) {
    //GL gl = new DebugGL(drawable.getGL());
    GL gl = drawable.getGL();
    GLU glu = new GLU(); //drawable.getGLU();
    if (doIntegrate && hasValues) {
      
      gl.glEnable(TEX_TARGET);
      gl.glMatrixMode(GL.GL_TEXTURE);
      gl.glLoadTransposeMatrixd(ID,0);

      initPrograms(gl);
      initFBO(gl);
      if (!readData) initVBO(gl);
      initViewport(gl, glu, true);
      // ping pong textures
      initTextures(gl);
      // user data textures
      initDataTextures(gl);

      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
          attachments[readTex], TEX_TARGET, initialValuesTextureID[readTex], 0);      
      gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
          attachments[writeTex], TEX_TARGET, initialValuesTextureID[writeTex], 0);

      GpgpuUtility.checkBuf(gl);
      
      gl.glDrawBuffer(attachments[writeTex]);
      
      // set all values
      // ping pong - current values
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glBindTexture(TEX_TARGET, initialValuesTextureID[readTex]);
      program.setUniform("values", 0);
      
      // user uniforms
      setUniformValues(gl, program);
      
      GlslLoader.render(program, gl);
      
      renderQuad(gl);
      
      gl.glFinish();

      if (readData) {
        valueBuffer.clear();
        transferFromTexture(gl, valueBuffer);
        if (atiHack) GpgpuUtility.atiHack(valueBuffer);
      } else {
        transferFromTextureToVBO(gl);
      }
      
      // do swap
      int tmp = readTex;
      readTex = writeTex;
      writeTex = tmp;
  
      GlslLoader.postRender(program, gl); // any postRender just resets the shader pipeline

      // switch back to old buffer
      gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);

      doIntegrate=false;
      measure();
      calculationFinished();
      if (displayTexture) {
        initViewport(gl, glu, false);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(TEX_TARGET, initialValuesTextureID[writeTex]);
        renderer.setUniform("values", 0);
        renderer.setUniform("scale", 1.); //findScale());
        GlslLoader.render(renderer, gl);
        renderQuad(gl);
        GlslLoader.postRender(renderer, gl);
      }
      gl.glDisable(TEX_TARGET);
    }
  }

  int cnt;
  long st;

  protected void measure() {
    if (measureCPS) {
      if (st == 0) st = System.currentTimeMillis();
      cnt++;
      long ct = System.currentTimeMillis();
      if (ct - st > 5000) {
        System.out.println("CPS="+(1000.*cnt)/(ct-st));
        cnt=0;
        st=ct;
      }
    }
  }

  /**
   * use this method to create and update data that
   * is used as a samplerRect in the Glsl program
   * @param gl
   */
  protected void initDataTextures(GL gl) {
  }

  /**
   * used to update/change the glsl sourcecode
   * @return null if code didn't change, the 
   * whole new frag program otherwise
   */
  protected String updateSource() {
    return null;
  }

  /**
   * @return the complete fragment shader program
   */
  protected abstract String initSource();

  /**
   * use this method to set/update uniform values of the program -
   * also activate and bind data textures here
   * @param gl
   * @param prog the Glsl program (from init/updateSource)
   */
  protected void setUniformValues(GL gl, GlslProgram prog) {
  }
  
  /**
   * just a callback when the calculation is done, can be used
   * to retrigger the calculation again,...
   */
  protected void calculationFinished() {
  }
  
  protected void renderQuad(GL gl) {
    gl.glColor3f(1,0,0);
    gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
    gl.glBegin(GL.GL_QUADS);
      gl.glTexCoord2d(0.0, 0.0);
      gl.glVertex2d(0.0,0.0);
      gl.glTexCoord2d(tex2D ? 1 : valueTextureSize, 0.0);
      gl.glVertex2d(valueTextureSize, 0.0);
      gl.glTexCoord2d(tex2D ? 1 : valueTextureSize, tex2D ? 1 : valueTextureSize);
      gl.glVertex2d(valueTextureSize, valueTextureSize);
      gl.glTexCoord2d(0.0, tex2D ? 1 : valueTextureSize);
      gl.glVertex2d(0.0, valueTextureSize);
    gl.glEnd();
  }

  private void initVBO(GL gl) {
    if (vbo[0] == 0) {
      gl.glGenBuffersARB(1, vbo, 0);
      System.out.println("created VBO=" + vbo[0]);
      gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbo[0]);
      gl.glBufferDataARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 1024*1024*4*4, (Buffer)null, GL.GL_DYNAMIC_DRAW_ARB);
      gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
      hasValidVBO=true;
    }
  }

  protected void initPrograms(GL gl) {
    String src = program == null ? initSource() : updateSource();
    if (src == null) return;
    if (program != null) GlslLoader.dispose(gl, program);
    if (isTex2D()) src = src.replaceAll("Rect", "2D");
    program = new GlslProgram(new Appearance(), "foo", null, src);
  }

  protected void initTextures(GL gl) {
    if (valueTextureSizeChanged) {
      gl.glEnable(TEX_TARGET);
      if (initialValuesTextureID[0] != 0) {
        gl.glDeleteTextures(2, initialValuesTextureID, 0);
      }
      gl.glGenTextures(2, initialValuesTextureID, 0);
      setupTexture(gl, initialValuesTextureID[0], valueTextureSize);
      setupTexture(gl, initialValuesTextureID[1], valueTextureSize);
      valueTextureSizeChanged=false;
      System.out.println("[initTextures] new particles tex size: "+valueTextureSize);
    }
    if (valuesChanged) {
      gl.glEnable(TEX_TARGET);
      valueBuffer.clear();
      transferToTexture(gl, valueBuffer, initialValuesTextureID[readTex], valueTextureSize);
      System.out.println("[initTextures] new particle data");
//      if (!readData) {
//          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbos[0]);
//          gl.glBufferDataARB(GL.GL_PIXEL_PACK_BUFFER_EXT, theWidth*theWidth*4*4, particleBuffer, GL.GL_STREAM_COPY);
//          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
//          hasValidVBO=true;
//      }
      valuesChanged=false;
    }
  }

  /**
   * set up a texture with the given size
   * @param gl
   * @param texID the texID
   * @param size the size
   */
  protected void setupTexture(GL gl, int texID, int size) {
    gl.glBindTexture(TEX_TARGET, texID);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
    gl.glTexImage2D(TEX_TARGET, 0, TEX_INTERNAL_FORMAT, size, size, 0,
        TEX_FORMAT, GL.GL_FLOAT, (Buffer) null);
//    System.out.println("created texture: id="+texID+" size="+size+".");
  }

  /**
   * Transfers data from currently texture, and stores it in given array.
   */
  private void transferFromTexture(GL gl, FloatBuffer data) {
    // version (a): texture is attached
    // recommended on both NVIDIA and ATI
    //if (!isTex2D()) {
      gl.glReadBuffer(attachments[writeTex]);
      gl.glReadPixels(0, 0, valueTextureSize, valueTextureSize, TEX_FORMAT, GL.GL_FLOAT, data);
//    } else {
//      // version b: texture is not neccessarily attached
//      gl.glBindTexture(TEX_TARGET, valueTextures[writeTex]);
//      gl.glGetTexImage(TEX_TARGET, 0, TEX_FORMAT, GL.GL_FLOAT, data.clear());
//    }
  }

  /**
   * Transfers data to the texture with texid
   */
  protected void transferToTexture(GL gl, FloatBuffer buffer, int texID, int size) {
    // version (a): HW-accelerated on NVIDIA
	gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, size);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_FALSE);
    gl.glBindTexture(TEX_TARGET, texID);
    gl.glTexSubImage2D(TEX_TARGET, 0, 0, 0, size, size, TEX_FORMAT,
        GL.GL_FLOAT, buffer);

    // version (b): HW-accelerated on ATI
    //    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, textureParameters.texTarget, texID, 0);
    //    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
    //    glRasterPos2i(0,0);
    //    glDrawPixels(texSize,texSize,textureParameters.texFormat,GL_FLOAT,data);
  }

  private void transferFromTextureToVBO(GL gl) {
    gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbo[0]);
    gl.glReadPixels(0, 0, valueTextureSize, valueTextureSize, TEX_FORMAT, GL.GL_FLOAT, 0l);
    gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
    hasValidVBO=true;
  }

  protected void initFBO(GL gl) {
    if (fbos[0] == 0) {
      gl.glGenFramebuffersEXT(1, fbos, 0);
      System.out.println("created FBO=" + fbos[0]);
    }
    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbos[0]);
  }

//  private void initVBO(GL gl) {
//    if (vbos[0] == 0) {
//      gl.glGenBuffersARB(1, vbos);
//      System.out.println("created VBO=" + vbos[0]);
//    }
//  }
  
  protected void initViewport(GL gl, GLU glu, boolean gpgpu) {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0, valueTextureSize, 0, valueTextureSize);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    if (gpgpu) gl.glViewport(0, 0, valueTextureSize, valueTextureSize);
    else  gl.glViewport(0, 0, canvasViewport[0], canvasViewport[1]);
  }
  
  /**
   * get the current result - returns null if !readData()
   * @return a FloatBuffer that contains the latest calculated values
   */
  public FloatBuffer getCurrentValues() {
    if (!readData || valueBuffer == null) return null;
    valueBuffer.position(0).limit(numValues);
    return valueBuffer.asReadOnlyBuffer();
  }


  /**
   * set the 4d values to start the calculation with
   * @param values
   */
  public void setValues(float[] values) {
    System.out.println("AbstractCalculation.setParticles()");
    if (values == null || values.length == 0) return;
    if (numValues != values.length) {
      int texSize = GpgpuUtility.texSize(values.length/4);
      if (valueTextureSize!=texSize) {
        System.out.println("[setParticles] new particles tex size="+texSize);
        valueBuffer = ByteBuffer.allocateDirect(texSize*texSize*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        valueTextureSize=texSize;
        valueTextureSizeChanged=true;
      }
      numValues = valueTextureSize*valueTextureSize*4;
    }
    
    valueBuffer.position(0).limit();
    valueBuffer.put(values);
    
    for(
      valueBuffer.position(numValues).limit(valueBuffer.capacity());
      valueBuffer.hasRemaining();
      valueBuffer.put(0f)
    );
    
    valuesChanged=true;
    hasValues=true;
  }
      
  /**
   * read the data from the texture into a FloatBuffer?
   */
  public boolean isReadData() {
  	  return readData;
  }
  
  /**
   * read the data from the texture into a FloatBuffer?
   */
  public void setReadData(boolean readData) {
  	  this.readData = readData;
  }
  
  public int getValueTextureSize() {
    return valueTextureSize;
  }
  
  /**
   * call this method to trigger a calculation the next time display is called
   *
   */
  public void triggerCalculation() {
    doIntegrate=true;
  }
  
  protected boolean isTex2D() {
    return tex2D;
  }
  
  public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
    canvasViewport[0]=arg3;
    canvasViewport[1]=arg4;
  }

  public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
  }

  /**
   * display the current value texture in the gl canvas?
   */
  public boolean isDisplayTexture() {
    return displayTexture;
  }

  /**
   * display the current value texture in the gl canvas?
   * @param displayTexture
   */
  public void setDisplayTexture(boolean displayTexture) {
    this.displayTexture = displayTexture;
  }

  /**
   * print cps to system.out?
   */
  public boolean isMeasureCPS() {
    return measureCPS;
  }

  /**
   * print cps to system.out?
   * @param measureCPS
   */
  public void setMeasureCPS(boolean measureCPS) {
    this.measureCPS = measureCPS;
  }

  /**
   * doesn't (yet) work
   */
  public void renderPoints(JOGLRenderer jr) {
    if (!hasValidVBO || readData) return;
    GL gl = jr.globalGL;
    
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER, vbo[0]);
    gl.glVertexPointer(4, GL.GL_FLOAT, 0, 0l);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER, 0);  
    
    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    gl.glDrawArrays(GL.GL_POINTS, 0, valueTextureSize*valueTextureSize);
    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
  }

}
