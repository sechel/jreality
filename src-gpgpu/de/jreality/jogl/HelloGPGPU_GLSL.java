package de.jreality.jogl;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.FloatBuffer;
import java.util.Random;

import net.java.games.jogl.Animator;
import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLCapabilities;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLDrawableFactory;
import net.java.games.jogl.GLU;
import de.jreality.jogl.shader.GlslLoader;
import de.jreality.scene.Appearance;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;

/*
 * Initial porting of C++ version of helloGPGPU
 *
 * GPGPU CONCEPTS Introduced:
 * 1) Texture = Array
 * 2) Fragment Program = Computational Kernel
 * 3) One-to-one Pixel to Texel Mapping :
 *       a) Data-Dimensioned Viewport, and
 *       b) Orthographic Projection
 * 4) Viewport-Sized Quad = Data Stream Generator
 * 5) Copy To Texture = feedback
 *
 */

public class HelloGPGPU_GLSL extends Viewer {
   
	  private static int TEX_TARGET = GL.GL_TEXTURE_RECTANGLE_NV;
	  private static int TEX_INTERNAL_FORMAT = GL.GL_FLOAT_RGBA32_NV;
	  private static int TEX_FORMAT = GL.GL_RGBA;
	
	  private int[] attachments = {GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT};
	  private int readTex, writeTex = 1;

	private int[] fbos=new int[1];
	private int[] pingTex=new int[2];
	private int[] dataTex=new int[1];
   
	private int pingSize;
	private int dataSize;
	
    private Frame f;
	private GlslProgram progMerge;
    
   public HelloGPGPU_GLSL(int Width, int Height) throws Exception {
      f=new Frame("HelloGPGPU_GLSL using jogl");
      f.setSize(Width, Height);
      
      // get a GLCanvas
      GLCapabilities capabilities = new GLCapabilities();

      GLCanvas canvas =
            GLDrawableFactory.getFactory().createGLCanvas(capabilities);
      
      Animator animator = new Animator(canvas);
      
        // add a GLEventListener, which will get called when the
        // canvas is resized or needs a repaint
        canvas.addGLEventListener(this);
        // now add the canvas to the Frame.  Note we use BorderLayout.CENTER
        // to make the canvas stretch to fill the container (ie, the frame)
        f.add (canvas, BorderLayout.CENTER);
        f.show();
        animator.start();
//        f.hide();
   }
   
   /**
    * @param args
    */
   public static void main(String[] args) throws Exception {
      new HelloGPGPU_GLSL(512,512);
   }
   
   public void init(GLDrawable drawable) {
       super.init(drawable);

	      pingSize = 2;
	      dataSize = 2;
	   // Need to check whether support OpenGL Shading Language
      GL gl = new DebugGL(drawable.getGL());
	if ( !gl.isExtensionAvailable("GL_ARB_fragment_shader") ||
            !gl.isExtensionAvailable("GL_ARB_vertex_shader") ||
            !gl.isExtensionAvailable("GL_ARB_shader_objects") ||
            !gl.isExtensionAvailable("GL_ARB_shading_language_100")
            ) {
         System.out.println("Driver does not support OpenGL Shading Language\n");
         System.exit(-1);
      }
	
	initPrograms(gl);

      // init framebuffer
      gl.glGenFramebuffersEXT(1, fbos);
      System.out.println("created fbo="+fbos[0]);
      
      // initit textures
      gl.glGenTextures(2,pingTex);
      setupTexture(gl, pingTex[0], pingSize, pingSize);
      setupTexture(gl, pingTex[1], pingSize, pingSize);
      
      gl.glGenTextures(1, dataTex);
      setupTexture(gl, dataTex[0], dataSize, dataSize);
            
      Random rand = new Random();
      float[] particles = new float[pingSize*pingSize*4];
      for (int i = 0; i < pingSize*pingSize; i++) {
        float len = 2*(rand.nextFloat()-0.5f);
        particles[4*i] = len * (rand.nextFloat()-0.5f);
        particles[4*i+1] = len * (rand.nextFloat()-0.5f);
        particles[4*i+2] = 1+ len * (rand.nextFloat()-0.5f);
        particles[4*i+3] = 1;
        
      }

      transferToTexture(gl, particles, pingTex[readTex], pingSize, pingSize);

      System.out.println("Wrote: ");
      EulerViewer.dumpData(particles);

      float[] vorts = new float[dataSize*dataSize*4];
      for (int i = 0; i < dataSize*dataSize; i++) {
        vorts[4*i] = (float) Math.cos(2*i*Math.PI/(dataSize*dataSize-1));
        vorts[4*i+1] = (float) Math.sin(2*i*Math.PI/(dataSize*dataSize-1));
        vorts[4*i+3] = i == 0 ? 0 : 1;
      }
      
      transferToTexture(gl, vorts, dataTex[0], dataSize, dataSize);
      
      gl.glFinish();
   }

   public void display(GLDrawable drawable) {
	   
	 GL gl = new DebugGL(drawable.getGL());
	 
	 gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbos[0]);

	 // init viewport
     initViewport(gl, drawable.getGLU());

     gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, attachments[readTex], TEX_TARGET, pingTex[readTex], 0);      
     gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, attachments[writeTex], TEX_TARGET, pingTex[writeTex], 0);
//     gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT2_EXT, TEX_TARGET, dataTex[0], 0);

	 checkBuf(gl);
	 
     gl.glDrawBuffer(attachments[writeTex]);
     
     gl.glEnable(GL.GL_TEXTURE_2D);

     gl.glActiveTexture(GL.GL_TEXTURE0);
     gl.glBindTexture(TEX_TARGET, pingTex[readTex]);
     gl.glActiveTexture(GL.GL_TEXTURE1);
     gl.glBindTexture(TEX_TARGET, dataTex[0]);
     
     progMerge.setUniform("roSquared", 0.00001);
     progMerge.setUniform("h", 0.001);
     
     progMerge.setUniform("particles", 0);
     progMerge.setUniform("vorticity", 1);
     
     GlslLoader.render(progMerge, drawable);
     
     renderQuad(gl);
     float[] f = new float[pingSize*pingSize*4];
     transferFromTexture(gl, f);
     
     System.out.println("Read: ");
     EulerViewer.dumpData(f);
     
     // do swap
     int tmp = readTex;
     readTex = writeTex;
     writeTex = tmp;

   }

   public void reshape(GLDrawable drawable, int x, int y, int width, int height) {
   }

   public void displayChanged(GLDrawable drawable, boolean modeChanged, boolean deviceChanged) {
   }

   private void initViewport(GL gl, GLU glu) {
	    gl.glMatrixMode(GL.GL_PROJECTION);
	    gl.glLoadIdentity();
	    glu.gluOrtho2D(0, pingSize, 0, pingSize);
	    gl.glMatrixMode(GL.GL_MODELVIEW);
	    gl.glLoadIdentity();
	    gl.glViewport(0, 0, pingSize, pingSize);
	  }

   private void renderQuad(GL gl) {
	    gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
	    gl.glBegin(GL.GL_QUADS);
		  gl.glTexCoord2d(0.0, 0.0);
		  gl.glVertex2d(0.0, 0.0);
		  gl.glTexCoord2d(pingSize, 0.0);
		  gl.glVertex2d(pingSize, 0.0);
		  gl.glTexCoord2d(pingSize, pingSize);
		  gl.glVertex2d(pingSize, pingSize);
		  gl.glTexCoord2d(0.0, pingSize);
		  gl.glVertex2d(0.0, pingSize);
	    gl.glEnd();
	  }

   /**
    * Transfers data to texture.
    */
   void transferToTexture(GL gl, float[] buffer, int texID, int theWidth, int theHeight) {
     // version (a): HW-accelerated on NVIDIA
     gl.glBindTexture(TEX_TARGET, texID);
     gl.glTexSubImage2D(TEX_TARGET, 0, 0, 0, theWidth, theHeight, TEX_FORMAT,
         GL.GL_FLOAT, buffer);

     // version (b): HW-accelerated on ATI
     //    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, textureParameters.texTarget, texID, 0);
     //    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
     //    glRasterPos2i(0,0);
     //    glDrawPixels(texSize,texSize,textureParameters.texFormat,GL_FLOAT,data);
   }

   /**
    * Transfers data to texture.
    */
   void transferToTexture(GL gl, FloatBuffer buffer, int texID, int theWidth, int theHeight) {
     // version (a): HW-accelerated on NVIDIA
     gl.glBindTexture(TEX_TARGET, texID);
     gl.glTexSubImage2D(TEX_TARGET, 0, 0, 0, theWidth, theHeight, TEX_FORMAT,
         GL.GL_FLOAT, buffer);

     // version (b): HW-accelerated on ATI
     //    glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, textureParameters.texTarget, texID, 0);
     //    glDrawBuffer(GL_COLOR_ATTACHMENT0_EXT);
     //    glRasterPos2i(0,0);
     //    glDrawPixels(texSize,texSize,textureParameters.texFormat,GL_FLOAT,data);
   }

   /**
    * Transfers data from currently texture, and stores it in given array.
    */
   void transferFromTexture(GL gl, float[] data) {
     // version (a): texture is attached
     // recommended on both NVIDIA and ATI
     gl.glReadBuffer(attachments[writeTex]);
     gl.glReadPixels(0, 0, pingSize, pingSize, TEX_FORMAT, GL.GL_FLOAT, data);

     // version b: texture is not neccessarily attached
//         gl.glBindTexture(TEX_TARGET, pingTex[writeTex]);
//         gl.glGetTexImage(TEX_TARGET, 0, TEX_FORMAT, GL.GL_FLOAT, data);

   }

//   /**
//    * Transfers data from currently texture, and stores it in given array.
//    */
//   void transferFromTexture(GL gl, FloatBuffer data) {
//     // version (a): texture is attached
//     // recommended on both NVIDIA and ATI
//     gl.glReadBuffer(attachments[writeTex]);
//     gl.glReadPixels(0, 0, pingSize, pingSize, TEX_FORMAT, GL.GL_FLOAT, data);
//
//     // version b: texture is not neccessarily attached
//     gl.glBindTexture(TEX_TARGET, Texs[writeTex]);
//     gl.glGetTexImage(TEX_TARGET, 0, TEX_FORMAT, GL.GL_FLOAT, data.clear());
//
//   }

   private void setupTexture(GL gl, int i, int theWidth, int theHeight) {
	    gl.glBindTexture(TEX_TARGET, i);
	    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
	    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
	    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
	    gl.glTexParameteri(TEX_TARGET, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
	    gl.glTexImage2D(TEX_TARGET, 0, TEX_INTERNAL_FORMAT, theWidth, theHeight, 0, TEX_FORMAT, GL.GL_FLOAT, (float[]) null);
	  }

   private void initPrograms(GL gl) {
//	   String prog = "uniform samplerRect positions;\n"+
//	   				 "\n"+
//	   				 "void main(void) {\n"+
//	   				 "  vec2 pos = gl_TexCoord[0].st;\n"+
//	   			  	 "  vec4 pt = textureRect(positions, pos);\n"+
//	   				 "  gl_FragColor = pt+vec4(1,1,1,1);\n"+
//	   				 "}\n";
//     progMerge = new GlslProgram(new Appearance(), "foo", null, prog);
	 
	   try {
       // read biot savart formula
       String cst = "const int cnt="+dataSize+";\n"
       +"const float PI="+Math.PI+";\n"
       +"uniform float roSquared;\n";
       String biotSavart="";
       System.out.println("recompiling program: prefix="+cst);
       LineNumberReader lnr = new LineNumberReader(Input.getInput(EulerViewer.class.getResource("biot_savart-impl.glsl")).getReader());
       for (String line=lnr.readLine(); line != null; line=lnr.readLine()) biotSavart += line+"\n";
       lnr.close();
       
       String rk = "\n";

       lnr = new LineNumberReader(Input.getInput(EulerViewer.class.getResource("Euler.glsl")).getReader());
       for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
       String prog = cst+rk+biotSavart;
       System.out.println("PROG: "+prog);
	progMerge = new GlslProgram(new Appearance(), "foo", null, prog);
     } catch (IOException e) {
    	 	e.printStackTrace();
     }
   }

   private void checkBuf(GL gl) {
	    String res = checkFrameBufferStatus(gl);
	    if (!res.equals("OK"))
	      System.out.println("["+fbos[0]+"] "+res);
	  }

	  private static String checkFrameBufferStatus(GL gl) {
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

} 