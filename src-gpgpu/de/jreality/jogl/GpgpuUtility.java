package de.jreality.jogl;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import net.java.games.jogl.Animator;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLCapabilities;
import net.java.games.jogl.GLDrawableFactory;
import net.java.games.jogl.GLEventListener;

public class GpgpuUtility {
  private GpgpuUtility() {}
  
  static int texSize(int i) {
    double fl = Math.floor(Math.sqrt(i));
    return (int) ((fl*fl < i) ? fl+1 : fl);
  }

  public static void dumpData(float[] data) {
    for (int i = 0; i < data.length; i++)
      System.out.print(data[i] + ", ");
    System.out.println();
  }

  public static void dumpData(IntBuffer data) {
    data.clear();
  for (int i = 0; i < data.capacity(); i++)
    System.out.print(data.get(i) + ", ");
  System.out.println();
}

  public static void dumpData(FloatBuffer data) {
    data.clear();
  for (int i = 0; i < data.capacity(); i++)
    System.out.print(data.get(i) + ", ");
  System.out.println();
}

  public static void dumpSelectedData(FloatBuffer data) {
    while (data.hasRemaining())
      System.out.print(data.get() + ", ");
    System.out.println();
  }
  
  static void checkBuf(GL gl) {
    String res = checkFrameBufferStatus(gl);
    if (!res.equals("OK")) {
      //System.out.println(res);
      throw new RuntimeException(res);
    }
  }

  private static String checkFrameBufferStatus(GL gl) {
    int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
    switch (status) {
    case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENTS_EXT:
      return "FrameBuffer incomplete attachments";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
      return "FrameBuffer incomplete missing attachment";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DUPLICATE_ATTACHMENT_EXT:
      return "FrameBuffer incomplete duplicate";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
      return "FrameBuffer incomplete dimensions";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
      return "FrameBuffer incomplete formats";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
      return "FrameBuffer incomplete draw buffer";
    case GL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
      return "FrameBuffer incomplete read buffer";
    case GL.GL_FRAMEBUFFER_COMPLETE_EXT:
      return "OK";
    case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
      return "FrameBuffer unsupported";
    default:
      return "FrameBuffer unrecognized error";
    }
  }

  public static void run(GLEventListener listener) {
    JFrame f=new JFrame("gpgpu runner");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // get a GLCanvas
    GLCapabilities capabilities = new GLCapabilities();

    GLCanvas canvas =
          GLDrawableFactory.getFactory().createGLCanvas(capabilities);
    
    Animator animator = new Animator(canvas);
    canvas.setSize(256, 256);
      // add a GLEventListener, which will get called when the
      // canvas is resized or needs a repaint
      canvas.addGLEventListener(listener);
      // now add the canvas to the Frame.  Note we use BorderLayout.CENTER
      // to make the canvas stretch to fill the container (ie, the frame)
      f.getContentPane().add(canvas, BorderLayout.CENTER);
      f.pack();
      f.show();
      animator.start();
  }
  
  /**
   * swaps the R and B values
   * @param fb
   */
  static void atiHack(FloatBuffer fb) {
    for (int i = 0, n = fb.remaining()/4; i < n; i++) {
      float tmp = fb.get(4*i);
      fb.put(4*i, fb.get(4*i+2));
      fb.put(4*i+2, tmp);
    }
  }

  public static float[] makeGradient(int sl) {
    float[] f = new float[sl*sl*4];
    for (int i = 0; i < sl; i++) {
      for (int j = 0; j < sl; j++) {
        f[4*(sl*i+j)+0]=((float)i)/sl;
        f[4*(sl*i+j)+1]=((float)j)/sl;
        f[4*(sl*i+j)+2]=0;
        f[4*(sl*i+j)+3]=1;
      }
    }
    return f;
  }
}
