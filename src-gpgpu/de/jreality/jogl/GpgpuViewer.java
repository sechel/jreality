package de.jreality.jogl;

import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import net.java.games.jogl.DebugGL;
import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import de.jreality.jogl.shader.GlslLoader;
import de.jreality.scene.Appearance;
import de.jreality.shader.GlslProgram;
import de.jreality.util.Input;

public class GpgpuViewer extends Viewer {

  private boolean forthOrder;
  private boolean orderChanged; // TODO: use this flag...
  
  private boolean doIntegrate;
  
  private static final boolean ATI=false;
  
  private static final boolean dump=false;

  //private static int TEX_TARGET = GL.GL_TEXTURE_2D;
  public static int TEX_TARGET = ATI ? GL.GL_TEXTURE_2D : GL.GL_TEXTURE_RECTANGLE_ARB;
  private static int TEX_INTERNAL_FORMAT = ATI ? GL.GL_RGBA32F_ARB : /*GL.GL_RGBA_FLOAT32_ATI; */ GL.GL_FLOAT_RGBA32_NV;
  private static int TEX_FORMAT = GL.GL_RGBA;
  
  // performance check variables
  int cnt;
  long st;
    
  GlslProgram progK1;
  GlslProgram progK2;
  GlslProgram progK3;
  GlslProgram progK4;
  GlslProgram progMerge;
  
  private boolean programsLoaded;  
  
  private int theWidth;
  private int theHeight;
  private int vortexTextureWidth;
  private int vortexTextureHeight;

  private int[] vbos = new int[1]; // 1 framebuffer
  private int[] fbos = new int[1]; // 1 framebuffer
  private int[] particleTexs = new int[2]; // ping pong textures
  private int[] intermediateTexs = new int[4]; // ping pong textures
  private int[] vortexTexs = new int[3]; // vortex polygons

  private int[] attachments = {GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_COLOR_ATTACHMENT1_EXT};
  private int readTex, writeTex = 1;

  private FloatBuffer particleBuffer;
  float[] vorts0;
  private FloatBuffer vortexBuffer;
  
  private boolean hasVortices;
  private boolean hasParticles;

  private boolean particlesChanged;
  private boolean particlesTexSizeChanged;

  private boolean vortexDataChanged;
  private boolean vortexTexSizeChanged;

  private double dt=0.001;

  private boolean recompilePrograms;

  private int numFloats;

  private int statsInterval=100;

  private double ro;

  private boolean readData=true;
  private boolean hasValidVBO;
  
  public GpgpuViewer() {
    super();
  }

  public GpgpuViewer(boolean foo) {
    this();
    Random rand = new Random();
    int numParticles = 1;
    float[] particles = new float[numParticles*4];
    for (int i = 0; i < numParticles; i++) {
      float len = 2*(rand.nextFloat()-0.5f);
      particles[4*i] = len * (rand.nextFloat()-0.5f);
      particles[4*i+1] = len * (rand.nextFloat()-0.5f);
      particles[4*i+2] = 1+ len * (rand.nextFloat()-0.5f);
      particles[4*i+3] = 1;
    }
    setParticles(particles);
    
    int numEdges=3;
    float[] vorts = new float[numEdges*4*3];
    for (int i = 0; i < numEdges; i++) {
      vorts[4*i] = (float) Math.cos(2*i*Math.PI/(numEdges-1));
      vorts[4*i+1] = (float) Math.sin(2*i*Math.PI/(numEdges-1));
      vorts[4*i+3] = i == 0 ? 0 : 1;
    }
    System.arraycopy(vorts, 0, vorts, numEdges*4, numEdges*4);
    System.arraycopy(vorts, 0, vorts, numEdges*8, numEdges*4);
    setVortexData(vorts);
  }
  
  public void display(GLDrawable drawable) {
      GL gl = drawable.getGL();
      GLU glu = drawable.getGLU();
      gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
      if (doIntegrate && hasVortices && hasParticles) {
        cnt++;
        if (cnt == statsInterval) {
          long t = System.currentTimeMillis();
          if (st != 0) System.out.println("cps="+ (((double)cnt)/(0.001*(t-st)) ) );
          st = System.currentTimeMillis();
          cnt=0;
        }
        if (orderChanged) {
          recompilePrograms=true;
          orderChanged=false;
        }
        initPrograms(gl);
        initFBO(gl);
        initVBO(gl);
        initViewport(gl, glu);
        initTextures(gl);
        
        double roSquared=ro*ro;
        
        progK1.setUniform("particles", 0);
        progK1.setUniform("vorticity", 1);
        progK1.setUniform("roSquared", roSquared);
  
        progK2.setUniform("particles", 0);
        progK2.setUniform("vorticity", 1);
        progK2.setUniform("roSquared", roSquared);
        progK2.setUniform("K1", 2);
        progK2.setUniform("h", dt);
        
        if (forthOrder) {
          progK3.setUniform("particles", 0);
          progK3.setUniform("vorticity", 1);
          progK3.setUniform("roSquared", roSquared);
          progK3.setUniform("K2", 2);
          progK3.setUniform("h", dt);
    
          progK4.setUniform("particles", 0);
          progK4.setUniform("vorticity", 1);
          progK4.setUniform("roSquared", roSquared);
          progK4.setUniform("K3", 2);
          progK4.setUniform("h", dt);
        }
        
        progMerge.setUniform("particles", 0);
        progMerge.setUniform("h", dt);
        progMerge.setUniform("K2", 2);
        if (forthOrder) {
          progMerge.setUniform("K1", 1);
          progMerge.setUniform("K3", 3);
          progMerge.setUniform("K4", 4);
        }        
        GlslLoader.render(progK1, drawable);  
  
        // first eval
        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
            attachments[readTex], TEX_TARGET, particleTexs[readTex], 0);      
        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
            attachments[writeTex], TEX_TARGET, particleTexs[writeTex], 0);

        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
            GL.GL_COLOR_ATTACHMENT2_EXT, TEX_TARGET, intermediateTexs[0], 0);      
        checkBuf(gl);
//        gl.glFinish();
        gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT2_EXT);
        
        // enable particles
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(TEX_TARGET, particleTexs[readTex]);
        
        // enable vorticities
        gl.glActiveTexture(GL.GL_TEXTURE1);
        gl.glBindTexture(TEX_TARGET, vortexTexs[0]);
        
        renderQuad(gl);
            
        // second eval
        gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
            GL.GL_COLOR_ATTACHMENT2_EXT, TEX_TARGET, intermediateTexs[1], 0);      
        checkBuf(gl);
//        gl.glFinish();
        GlslLoader.render(progK2, drawable);
        
        gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT2_EXT);
        
        // enable particles 
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(TEX_TARGET, particleTexs[readTex]);
        
        // enable vorticities
        gl.glActiveTexture(GL.GL_TEXTURE1);
        gl.glBindTexture(TEX_TARGET, vortexTexs[1]);
  
        // enable K1
        gl.glActiveTexture(GL.GL_TEXTURE2);
        gl.glBindTexture(TEX_TARGET, intermediateTexs[0]);
        
        renderQuad(gl);
        
        if (forthOrder) {
          
          // third eval
          gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
              GL.GL_COLOR_ATTACHMENT2_EXT, TEX_TARGET, intermediateTexs[2], 0);      
          checkBuf(gl);
//          gl.glFinish();
  
          GlslLoader.render(progK3, drawable);
      
          gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT2_EXT);
          
          // enable particles
          gl.glActiveTexture(GL.GL_TEXTURE0);
          gl.glBindTexture(TEX_TARGET, particleTexs[readTex]);
          
          // enable vorticities
          gl.glActiveTexture(GL.GL_TEXTURE1);
          gl.glBindTexture(TEX_TARGET, vortexTexs[1]);
    
          // enable K2
          gl.glActiveTexture(GL.GL_TEXTURE2);
          gl.glBindTexture(TEX_TARGET, intermediateTexs[1]);
          
          renderQuad(gl);
          
          // forth eval
          gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT,
              GL.GL_COLOR_ATTACHMENT2_EXT, TEX_TARGET, intermediateTexs[3], 0);
          checkBuf(gl);
//          gl.glFinish();
  
          GlslLoader.render(progK3, drawable);
          
          gl.glDrawBuffer(GL.GL_COLOR_ATTACHMENT2_EXT);
          
          // enable particles
          gl.glActiveTexture(GL.GL_TEXTURE0);
          gl.glBindTexture(TEX_TARGET, particleTexs[readTex]);
          
          // enable vorticities
          gl.glActiveTexture(GL.GL_TEXTURE1);
          gl.glBindTexture(TEX_TARGET, vortexTexs[2]);
    
          // enable K3
          gl.glActiveTexture(GL.GL_TEXTURE2);
          gl.glBindTexture(TEX_TARGET, intermediateTexs[2]);      
          
          renderQuad(gl);
        }
        
//        gl.glFinish();
  
        GlslLoader.render(progMerge, drawable);

        programsLoaded = true;
        
        // merge step
        gl.glDrawBuffer(attachments[writeTex]);
        
        // enable particles
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(TEX_TARGET, particleTexs[readTex]);
        
        // enable K1
        gl.glActiveTexture(GL.GL_TEXTURE1);
        gl.glBindTexture(TEX_TARGET, intermediateTexs[0]);
  
        // enable K2
        gl.glActiveTexture(GL.GL_TEXTURE2);
        gl.glBindTexture(TEX_TARGET, intermediateTexs[1]);      
        
        if (forthOrder) {
          // enable K3
          gl.glActiveTexture(GL.GL_TEXTURE3);
          gl.glBindTexture(TEX_TARGET, intermediateTexs[2]);      
          
          // enable K4
          gl.glActiveTexture(GL.GL_TEXTURE4);
          gl.glBindTexture(TEX_TARGET, intermediateTexs[3]);      
        }
        
        renderQuad(gl);
        
        if (readData) {
          transferFromTexture(gl, particleBuffer);
        } else {
          transferFromTextureToVBO(gl);
        }
        
        // do swap
        int tmp = readTex;
        readTex = writeTex;
        writeTex = tmp;
    
        // switch back to old buffer
        gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);
  
        GlslLoader.postRender(progK1, drawable); // any postRender just resets the shader pipeline
        doIntegrate=false;
      }
    gl.glPopAttrib();
    gl.glPushAttrib(GL.GL_ALL_ATTRIB_BITS);
    super.display(drawable);
    gl.glPopAttrib();
    		
  }

  private void transferFromTextureToVBO(GL gl) {
    gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbos[0]);
    gl.glReadBuffer(attachments[writeTex]);
    gl.glReadPixels(0, 0, theWidth, theWidth, TEX_FORMAT, GL.GL_FLOAT, (Buffer) null);
    gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
    hasValidVBO=true;
  }

  private void renderQuad(GL gl) {
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
  }

  private void initPrograms(GL gl) {
    if (recompilePrograms) {
      if (programsLoaded) {
        GlslLoader.dispose(gl, progK1);
        GlslLoader.dispose(gl, progK2);
        if (progK3 != null) GlslLoader.dispose(gl, progK3);
        if (progK4 != null) GlslLoader.dispose(gl, progK4);
        GlslLoader.dispose(gl, progMerge);
        progK1=progK2=progK3=progK4=progMerge=null;
      }
      try {
        
        // read biot savart formula
        String cst = "const int cnt="+vortexTextureWidth+";\n"
        +"const float PI="+Math.PI+";\n"
        +"uniform float roSquared;\n";
        String biotSavart="";
        System.out.println("recompiling program: prefix="+cst);
        LineNumberReader lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("biot_savart-impl.glsl")).getReader());
        for (String line=lnr.readLine(); line != null; line=lnr.readLine()) biotSavart += line+"\n";
        lnr.close();
        
        String rk = "\n";
        // read RK-1
        lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("K1.glsl")).getReader());
        for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
        progK1 = new GlslProgram(new Appearance(), "foo", null, cst+rk+biotSavart);
        
        rk = "\n";
        // read RK-2
        lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("K2.glsl")).getReader());
        for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
        progK2 = new GlslProgram(new Appearance(), "foo", null, cst+rk+biotSavart);

        if (forthOrder) {
          rk = "\n";
          // read RK-3
          lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("K3.glsl")).getReader());
          for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
          progK3 = new GlslProgram(new Appearance(), "foo", null, cst+rk+biotSavart);
  
          rk = "\n";
          // read RK-4
          lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("K4.glsl")).getReader());
          for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
          progK4 = new GlslProgram(new Appearance(), "foo", null, cst+rk+biotSavart);
  
          rk = "\n";
          // read RK-merge
          lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("RK4.glsl")).getReader());
          for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
          progMerge = new GlslProgram(new Appearance(), "foo", null, rk);
        } else {
          rk = "\n";
          // read RK-merge
          lnr = new LineNumberReader(Input.getInput(GpgpuViewer.class.getResource("RK2.glsl")).getReader());
          for (String line=lnr.readLine(); line != null; line=lnr.readLine()) rk += line+"\n";
          progMerge = new GlslProgram(new Appearance(), "foo", null, rk);
        }
        programsLoaded = false;
      } catch (IOException ioe) {
        throw new Error("cant find program template!");
      }
      recompilePrograms=false;
    }
  }

  private void initTextures(GL gl) {
    if (particlesTexSizeChanged) {
      gl.glEnable(TEX_TARGET);
      if (particleTexs[0] != 0) {
        gl.glDeleteTextures(2, particleTexs);
        gl.glDeleteTextures(4, intermediateTexs);
      }
      gl.glGenTextures(2, particleTexs);
      gl.glGenTextures(4, intermediateTexs);
      setupTexture(gl, particleTexs[0], theWidth, theHeight);
      setupTexture(gl, particleTexs[1], theWidth, theHeight);
      setupTexture(gl, intermediateTexs[0], theWidth, theHeight);
      setupTexture(gl, intermediateTexs[1], theWidth, theHeight);
      setupTexture(gl, intermediateTexs[2], theWidth, theHeight);
      setupTexture(gl, intermediateTexs[3], theWidth, theHeight);
      particlesTexSizeChanged=false;
      System.out.println("[initTextures] new particles tex size: "+theWidth);
    }
    if (particlesChanged) {
      gl.glEnable(TEX_TARGET);
      particleBuffer.clear();
      transferToTexture(gl, particleBuffer, particleTexs[readTex], theWidth, theHeight);
      System.out.println("[initTextures] new particle data");
      if (!readData) {
          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, vbos[0]);
          gl.glBufferDataARB(GL.GL_PIXEL_PACK_BUFFER_EXT, theWidth*theWidth*4*4, particleBuffer, GL.GL_STREAM_COPY);
          gl.glBindBufferARB(GL.GL_PIXEL_PACK_BUFFER_EXT, 0);
          hasValidVBO=true;
      }
      particlesChanged=false;
    }
    if (vortexTexSizeChanged) {
      gl.glEnable(TEX_TARGET);
      if (vortexTexs[0] != 0) {
        gl.glDeleteTextures(vortexTexs.length, vortexTexs);
      }
      gl.glGenTextures(3, vortexTexs);
      setupTexture(gl, vortexTexs[0], vortexTextureWidth, vortexTextureHeight);
      setupTexture(gl, vortexTexs[1], vortexTextureWidth, vortexTextureHeight);
      if (forthOrder) setupTexture(gl, vortexTexs[2], vortexTextureWidth, vortexTextureHeight);
      vortexTexSizeChanged=false;
      vortexBuffer = ByteBuffer.allocateDirect(vortexTextureWidth*vortexTextureHeight*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
      System.out.println("[initTextures] new vortex tex size: "+vortexTextureWidth);
    }
    if (vortexDataChanged) {
      gl.glEnable(TEX_TARGET);
      dt = vorts0[0];
      int n = (vorts0.length-1)/3; // TODO: later read only two time steps for 2nd order
      for(
        vortexBuffer.position(n).limit(vortexBuffer.capacity());
        vortexBuffer.hasRemaining();
        vortexBuffer.put(0f)
      );
      vortexBuffer.position(0).limit(n);
      vortexBuffer.put(vorts0, 1, n);
      vortexBuffer.clear();
      transferToTexture(gl, vortexBuffer, vortexTexs[0], vortexTextureWidth, vortexTextureHeight);
      vortexBuffer.position(0).limit(n);
      vortexBuffer.put(vorts0, n+1, n);
      vortexBuffer.clear();
      transferToTexture(gl, vortexBuffer, vortexTexs[1], vortexTextureWidth, vortexTextureHeight);
      if (forthOrder) {
        vortexBuffer.position(0).limit(n);
        vortexBuffer.put(vorts0, 2*n+1, n);
        vortexBuffer.clear();
        transferToTexture(gl, vortexBuffer, vortexTexs[2], vortexTextureWidth, vortexTextureHeight);
      }
      vortexDataChanged=false;
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
    gl.glReadBuffer(GL.GL_COLOR_ATTACHMENT0_EXT);
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
    gl.glReadBuffer(attachments[writeTex]);
    gl.glReadPixels(0, 0, theWidth, theHeight, TEX_FORMAT, GL.GL_FLOAT, data);

    // version b: texture is not neccessarily attached
//    gl.glBindTexture(TEX_TARGET, particleTexs[writeTex]);
//    gl.glGetTexImage(TEX_TARGET, 0, TEX_FORMAT, GL.GL_FLOAT, data.clear());

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

  private void initFBO(GL gl) {
    if (fbos[0] == 0) {
      gl.glGenFramebuffersEXT(1, fbos);
      System.out.println("created FBO=" + fbos[0]);
    }
    gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbos[0]);
  }

  private void initVBO(GL gl) {
    if (vbos[0] == 0) {
      gl.glGenBuffersARB(1, vbos);
      System.out.println("created VBO=" + vbos[0]);
    }
  }
  
  private void initViewport(GL gl, GLU glu) {
    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
    glu.gluOrtho2D(0, theWidth, 0, theHeight);
    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glViewport(0, 0, theWidth, theHeight);
  }
  
  public int getParticleTexID() {
	  return particleTexs[readTex];
  }
  
  public FloatBuffer getCurrentParticlePositions() {
    if (!readData) return null;
    particleBuffer.position(0).limit(numFloats);
    return particleBuffer.asReadOnlyBuffer();
  }

  public void setParticles(float[] particles) {
    hasValidVBO=false;
    System.out.println("GpgpuViewer.setParticles()");
    if (numFloats != particles.length) {
      int texSize = texSize(particles.length/4);
      if (theWidth!=texSize || theHeight != texSize) {
        System.out.println("[setParticles] new particles tex size="+texSize);
        particleBuffer = ByteBuffer.allocateDirect(texSize*texSize*4*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        theWidth=theHeight=texSize;
        particlesTexSizeChanged=true;
      }
    }
    numFloats = particles.length;
    particleBuffer.position(0).limit(numFloats);
    particleBuffer.put(particles);
    
    for(
      particleBuffer.position(numFloats).limit(particleBuffer.capacity());
      particleBuffer.hasRemaining();
      particleBuffer.put(0f)
    );
    
    particlesChanged=true;
    hasParticles=true;
  }
  
  private int texSize(int i) {
    double fl = Math.floor(Math.sqrt(i));
    return (int) ((fl*fl < i) ? fl+1 : fl);
  }

  public void setVortexData(float[] vortData) {
	  //System.out.println("GpgpuViewer.setVortexData()");
      if (vortData == null || vortData.length == 0) {
        hasVortices = false;
        return;
      }
      if (this.vorts0 == null || this.vorts0.length != vortData.length) {
        this.vorts0 = (float[]) vortData.clone();
        int texSize = texSize((vortData.length-1)/(3*4));
        if (texSize != vortexTextureHeight) {
          System.out.println("[setVortexData] new vortex tex size="+texSize+" data.length="+vortData.length);
          vortexTextureWidth=vortexTextureHeight=texSize;
          vortexTexSizeChanged=recompilePrograms=true;
        }
        vortexDataChanged=true;
      } else {
        boolean changed=false;
        for (int i = 0; i < vorts0.length; i++) {
          if (!changed) {
            if (vorts0[i] != vortData[i]) {
              changed=true;
              i--;
            }
          } else vorts0[i] = vortData[i];
        }
        vortexDataChanged = changed;
      }
      doIntegrate=hasVortices=true;
  }

  public static void dumpData(float[] data) {
    for (int i = 0; i < data.length; i++)
      System.out.print(data[i] + ", ");
    System.out.println();
  }

  void dumpData(FloatBuffer data) {
    for (int i = 0; i < data.capacity(); i++)
      System.out.print(data.get(i) + ", ");
    System.out.println();
  }
  
  private void checkBuf(GL gl) {
    String res = checkFrameBufferStatus(gl);
    if (!res.equals("OK"))
      System.out.println(res);
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
    
  public int getStatsInterval() {
    	return statsInterval;
  }
    
  public void setStatsInterval(int statsInterval) {
    	this.statsInterval = statsInterval;
  }
    
  public void setRo(double ro) {
    this.ro = ro;
  }

  public boolean isForthOrder() {
    return forthOrder;
  }
  
  public void setForthOrder(boolean forthOrder) {
    this.forthOrder=forthOrder;
    orderChanged=true;
  }

  public boolean isReadData() {
  	return readData;
  }
  
  public void setReadData(boolean readData) {
  	this.readData = readData;
  }
  
  public int getParticleTexSize() {
    return theWidth;
  }
  
  public void renderPoints(JOGLRenderer jr) {
    if (!hasValidVBO) return;
    GL gl = jr.globalGL;
    
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER, vbos[0]);
    gl.glVertexPointer(4, GL.GL_FLOAT, 0, (Buffer) null);
    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER, 0);  
    
    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
    gl.glDrawArrays(GL.GL_POINTS, 0, theWidth*theWidth);
    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
  }
  
}
