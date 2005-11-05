package de.jreality.jogl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import de.jreality.jogl.shader.GlslLoader;
import de.jreality.scene.Appearance;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;

import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;

public class GpgpuViewer extends Viewer {

  private static final boolean ATI=true;
  
  private static final boolean dump=false;
  
  //private static int TEX_TARGET = GL.GL_TEXTURE_2D;
  private static int TEX_TARGET = ATI ? GL.GL_TEXTURE_2D : GL.GL_TEXTURE_RECTANGLE_ARB;
  private static int TEX_INTERNAL_FORMAT = ATI ? GL.GL_RGBA32F_ARB : GL.GL_FLOAT_RGBA32_NV;
  private static int TEX_FORMAT = GL.GL_RGBA;
  
  //    String vert = "void main(void) { gl_Position = ftransform(); }";
  static String[] frag = ATI ? new String[] { "uniform sampler2D textureY;"
    + "uniform sampler2D textureX;" + "uniform float alpha;"
    + "void main(void) { "
    + "vec4 y = texture2D(textureY, gl_TexCoord[0].st);"
    + "vec4 x = texture2D(textureX, gl_TexCoord[0].st);"
    + "gl_FragColor = y + alpha*x;" + "}" }
  
  :
    
    new String[] { "uniform samplerRect textureY;\n"
      + "uniform samplerRect textureX;\n" + "uniform float alpha;\n"
      + "void main(void) { "
      + "vec4 y = textureRect(textureY, gl_TexCoord[0].st);"
      + "vec4 x = textureRect(textureX, gl_TexCoord[0].st);"
      + "gl_FragColor = x;" + "}" };

    
    
    int cnt;
    long st;
    
    GlslProgram prog;//= new GlslProgram(new Appearance(), "foo", (String[])null, frag);
    {
      try {
        prog= new GlslProgram(new Appearance(), "foo", null, Input.getInput("../../dipl/software/glsl/biot_savart.glsl"));
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
  private int theWidth=1;
  private int theHeight=theWidth;
  int vortexTextureSize=2;

  private int[] fbos = new int[1];
  private int[] yTexs = new int[2];
  private int[] xTexs = new int[1];

  private int[] attachmentpoints = new int[] { GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT };

  int readTex, writeTex = 1;

  private float[] Y = new float[theWidth * theHeight * 4];

  
  private FloatBuffer read = ByteBuffer.allocateDirect(
      theWidth * theHeight * 4 * 4).order(ByteOrder.nativeOrder())
      .asFloatBuffer();

  private int numIts = 1;
  float[] X = new float[vortexTextureSize*vortexTextureSize*4];

  {
    for (int i = 0; i < Y.length; i++) {
      Y[i] = (float)i; //(i%4 == 3) ? 1.0f : (float) (1.3+0.01*i);
//      X[i] = 0.1f;
    }
  }
  
  {
    int numPts = vortexTextureSize*vortexTextureSize;
    for (int i = 0; i < numPts; i++) {
      X[4*i] = (float) Math.sin(2*i*Math.PI/numPts);
      X[4*i+1] = (float) Math.cos(2*i*Math.PI/numPts);
      X[4*i+3] = 1f;
    }
  }

  public void display(GLDrawable drawable) {
    cnt++;
    if (cnt == 20) {
      long t = System.currentTimeMillis();
      if (st != 0) System.out.println("cps="+ (((double)cnt)/(0.001*(t-st)) ) );
      st = System.currentTimeMillis();
      cnt=0;
    }
    GL gl = new DebugGL(drawable.getGL());
    GLU glu = drawable.getGLU();
    initFBO(gl);
    initViewport(gl, glu);
    initTextures(gl);
    //prog.setUniform("tex0", 0);
    //prog.setUniform("vort", 1);
    
    GlslLoader.render(prog, drawable);

    performCalculation(gl);

    if (dump) System.out.println("calc done");
    
    transferFromTexture(gl, read);
    dumpData(read);

    // switch back to old buffer
    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
    //gl.glUseProgramObjectARB(0);

    GlslLoader.postRender(prog, drawable);
    
    //gl.glDeleteFramebuffersEXT(1, fbos);

    //System.exit(0);

    //super.display(drawable);
  }

  private void dumpData(float[] data) {
    for (int i = 0; i < data.length; i++)
      System.out.print(data[i] + ", ");
    System.out.println();
  }

  private void dumpData(FloatBuffer data) {
    for (int i = 0; i < data.capacity(); i++)
      System.out.print(data.get(i) + ", ");
    System.out.println();
  }

  private void performCalculation(GL gl) {
    if (dump) System.out.println("attatching first buffer...");
    // attach two textures to FBO
    gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
        attachmentpoints[writeTex], TEX_TARGET, yTexs[writeTex], 0);
    if (dump) System.out.println("...done. attatching second buffer...");
    gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
        attachmentpoints[readTex], TEX_TARGET, yTexs[readTex], 0);
    // check if that worked
    if (dump) System.out.println("...done.");
    checkBuf(gl);
    if (dump) System.out.println("checked buffer status");

    gl.glFinish();

    if (dump) System.out.println("starting calc loop...");
    
    for (int i = 0; i < numIts; i++) {
      gl.glDrawBuffer(attachmentpoints[writeTex]);
      if (dump) System.out.println("set glDrawBuffer");
      // enable texture y_old (read-only)
      gl.glActiveTexture(GL.GL_TEXTURE0);
      gl.glBindTexture(TEX_TARGET, yTexs[readTex]);
      //gl.glUniform1iARB(yParam, 0); // texunit 0
      if (dump) System.out.println("set yParam");
      // enable texture x (read-only)
      gl.glActiveTexture(GL.GL_TEXTURE1);
      gl.glBindTexture(TEX_TARGET, xTexs[0]);
      //gl.glUniform1iARB(xParam, 1); // texunit 1
      if (dump) System.out.println("set xParam");
      // enable scalar alpha
      //gl.glUniform1fARB(alphaParam, alpha);
      
      if (dump) System.out.println("set alpha");
      
      // and render multitextured viewport-sized quad
      // depending on the texture target, switch between
      // normalised ([0,1]^2) and unnormalised ([0,w]x[0,h])
      // texture coordinates

      // make quad filled to hit every pixel/texel
      // (should be default but we never know)
      gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);

      if (dump) System.out.println("polygon mode");

      // and render the quad

      gl.glBegin(GL.GL_QUADS);
      if (TEX_TARGET == GL.GL_TEXTURE_RECTANGLE_ARB) {
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2d(0.0, 0.0);
        gl.glTexCoord2d(theWidth, 0.0);
        gl.glVertex2d(theWidth, 0.0);
        gl.glTexCoord2d(theWidth, theHeight);
        gl.glVertex2d(theWidth, theHeight);
        gl.glTexCoord2d(0.0, theHeight);
        gl.glVertex2d(0.0, theHeight);
      } else {
        if (dump) System.out.println("draw poly start");
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex2d(0.0, 0.0);
        gl.glTexCoord2d(1, 0.0);
        gl.glVertex2d(theWidth, 0.0);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(theWidth, theHeight);
        gl.glTexCoord2d(0.0, 1);
        gl.glVertex2d(0.0, theHeight);
        if (dump) System.out.println("draw poly done");
      }
      if (dump) System.out.println("calling end.");
      gl.glEnd();

      if (dump) System.out.println("swapping...");
      // do swap
      int tmp = readTex;
      readTex = writeTex;
      writeTex = tmp;
      if (dump) System.out.println("done.");

    }

  }

  private void initTextures(GL gl) {
    if (yTexs[0] == 0) {
      gl.glEnable(TEX_TARGET);
      gl.glGenTextures(2, yTexs);
      setupTexture(gl, yTexs[0], theWidth, theHeight);
      setupTexture(gl, yTexs[1], theWidth, theHeight);
      transferToTexture(gl, Y, yTexs[readTex], theWidth, theHeight);
      gl.glGenTextures(1, xTexs);
      setupTexture(gl, xTexs[0], vortexTextureSize, vortexTextureSize);
      transferToTexture(gl, X, xTexs[0], vortexTextureSize, vortexTextureSize);
    }
  }

  private void setupTexture(GL gl, int i, int theWidth, int theHeight) {
    gl.glBindTexture(TEX_TARGET, i);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
    gl.glTexImage2D(TEX_TARGET, 0, TEX_INTERNAL_FORMAT, theWidth, theHeight, 0,
        TEX_FORMAT, GL.GL_FLOAT, (float[]) null);

  }

  /**
   * Transfers data from currently texture, and stores it in given array.
   */
  void transferFromTexture(GL gl, float[] data) {
    // version (a): texture is attached
    // recommended on both NVIDIA and ATI
    gl.glReadBuffer(attachmentpoints[readTex]);
    gl.glReadPixels(0, 0, theWidth, theHeight, TEX_FORMAT, GL.GL_FLOAT, data);

    // version b: texture is not neccessarily attached
    //    glBindTexture(textureParameters.texTarget,yTexID[readTex]);
    //    glGetTexImage(textureParameters.texTarget,0,textureParameters.texFormat,GL_FLOAT,data);

  }

  /**
   * Transfers data from currently texture, and stores it in given array.
   */
  void transferFromTexture(GL gl, FloatBuffer data) {
    // version (a): texture is attached
    // recommended on both NVIDIA and ATI
    gl.glReadBuffer(attachmentpoints[readTex]);
    gl.glReadPixels(0, 0, theWidth, theHeight, TEX_FORMAT, GL.GL_FLOAT, data);

    // version b: texture is not neccessarily attached
    //    glBindTexture(textureParameters.texTarget,yTexID[readTex]);
    //    glGetTexImage(textureParameters.texTarget,0,textureParameters.texFormat,GL_FLOAT,data);

  }

  /**
   * Transfers data to texture.
   */
  void transferToTexture(GL gl, float[] data, int texID, int theWidth, int theHeight) {
    // version (a): HW-accelerated on NVIDIA
    gl.glBindTexture(TEX_TARGET, texID);
    gl.glTexSubImage2D(TEX_TARGET, 0, 0, 0, theWidth, theHeight, TEX_FORMAT,
        GL.GL_FLOAT, data);

    // version (b): HW-accelerated on ATI
    //    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, textureParameters.texTarget, texID, 0);
    //    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
    //    glRasterPos2i(0,0);
    //    glDrawPixels(texSize,texSize,textureParameters.texFormat,GL_FLOAT,data);
  }

  private void initFBO(GL gl) {
    if (fbos[0] == 0) {
      gl.glGenFramebuffersEXT(1, fbos);
      System.out.println("created FBO=" + fbos[0]);
    }
    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbos[0]);
  }

  private void checkBuf(GL gl) {
    String res = checkFrameBufferStatus(gl);
    if (!res.equals("OK"))
      System.out.println(res);
  }

  private void initViewport(GL gl, GLU glu) {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0, theWidth, 0, theHeight);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glViewport(0, 0, theWidth, theHeight);
  }

  public static String checkFrameBufferStatus(GL gl) {
    int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
    switch (status) {
    case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENTS_EXT:
      return "FrameBuffer incoplete attachments";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
      return "FrameBuffer incoplete missing attachment";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT:
      return "FrameBuffer incoplete duplicate";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
      return "FrameBuffer incoplete dimensions";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
      return "FrameBuffer incoplete formats";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
      return "FrameBuffer incoplete draw buffer";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
      return "FrameBuffer incoplete read buffer";
    case GL.GL_FRAMEBUFFER_COMPLETE_EXT:
      return "OK";
    case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
      return "FrameBuffer unsupported";
    default:
      return "FrameBuffer unreckognized error";
    }
  }

  private static void printInfoLog(String name, int objectHandle, GL gl) {
    int[] logLength = new int[1];
    int[] charsWritten = new int[1];
    byte[] infoLog;

    gl.glGetObjectParameterivARB(objectHandle,
        GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, logLength);

    if (logLength[0] > 0) {
      infoLog = new byte[logLength[0]];
      gl.glGetInfoLogARB(objectHandle, logLength[0], charsWritten, infoLog);
      StringBuffer foo = new StringBuffer(charsWritten[0]);

      for (int i = 0; i < charsWritten[0]; ++i)
        foo.append((char) infoLog[i]);
      if (foo.length() > 0)
        System.out.println("[" + name + "] GLSL info log: " + foo.toString());
    }
  }

}
