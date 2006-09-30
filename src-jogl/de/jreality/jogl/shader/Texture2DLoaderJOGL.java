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


package de.jreality.jogl.shader;

import java.awt.Dimension;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.LoggingSystem;

/**
 * Manages mapping Texture2D and CubeMap to GL textures and loading. Needs the following improvements:
 *  - mapping from WeakReference<ImageData> to multiple GL objects
 *  - garbage collection for cube map textures.
 *  
 *   @author Charles Gunn, Steffen Weissmann
 */
public class Texture2DLoaderJOGL {
	private static WeakHashMap<GL, WeakHashMap<ImageData, Integer>> lookupTextures = new WeakHashMap<GL, WeakHashMap<ImageData,Integer>>();
	private static WeakHashMap<GL, WeakHashMap<ImageData, Integer>> lookupCubemaps = new WeakHashMap<GL, WeakHashMap<ImageData,Integer>>();

  private static ReferenceQueue<ImageData> refQueue = new ReferenceQueue<ImageData>();
  private static IdentityHashMap<WeakReference<ImageData>, Integer> refToID = new IdentityHashMap<WeakReference<ImageData>, Integer>();
  private static IdentityHashMap<WeakReference<ImageData>, GL> refToGL = new IdentityHashMap<WeakReference<ImageData>, GL>();
  private static IdentityHashMap<WeakReference<ImageData>, Dimension> refToDim = new IdentityHashMap<WeakReference<ImageData>, Dimension>();
  
  private static final boolean REPLACE_TEXTURES = true;
  
	private Texture2DLoaderJOGL() {
	}

	private static int createTextureID(GL gl) 
	{ 
	   int[] tmp = new int[1]; 
	   gl.glGenTextures(1, tmp, 0);
	   return tmp[0]; 
	} 
       
    private static WeakHashMap<ImageData, Integer> getTextureTableForGL(GL gl)	{
      WeakHashMap<ImageData, Integer> ht = lookupTextures.get(gl);
  		if (ht == null)	{
    			ht = new WeakHashMap<ImageData, Integer>();
    			lookupTextures.put(gl, ht);
      } 
  		return ht;
  }

    private static WeakHashMap<ImageData, Integer> getCubeMapTableForGL(GL gl)	{
      WeakHashMap<ImageData, Integer> ht = lookupCubemaps.get(gl);
  		if (ht == null)	{
    			ht = new WeakHashMap<ImageData, Integer>();
    			lookupCubemaps.put(gl, ht);
      } 
  		return ht;
  }

    /******************* new Textures *******************/
  
    public static void render(GL gl, Texture2D tex) {
      render(gl, tex, true);
    }
    public static void render(GL gl, Texture2D tex, boolean mipmapped) {
      if (tex.getImage() == null) return;
        //  render(drawable, tex, 0);
    //}
    //public static void render(GLCanvas drawable, Texture2D tex, int level) {
    boolean first = true;

    boolean replace = false;
    
    GLU glu = new GLU(); //drawable.getGLU();
    
    WeakHashMap<ImageData, Integer> ht = getTextureTableForGL(gl);

    Integer texid = (Integer) ht.get(tex.getImage());
    int textureID = -1;
    if (texid != null) {
      first = false;
      textureID = texid.intValue();
    } else {
      Dimension dim = new Dimension(tex.getImage().getWidth(), tex.getImage().getHeight());
      { // delete garbage collected textures or reuse if possible
        for (Object ref=refQueue.poll(); ref != null; ref=refQueue.poll()) {
          Integer id = (Integer) refToID.remove(ref);
          if (id == null) throw new Error();
          GL g = (GL) refToGL.remove(ref);
          Dimension d = (Dimension) refToDim.remove(ref);
          if (REPLACE_TEXTURES && g == gl && dim.equals(d) && !replace) {
            // replace texture
            LoggingSystem.getLogger(Texture2DLoaderJOGL.class).fine("replacing texture...");
            textureID = id.intValue();
            replace = true;
            first = false;
          } else {
            LoggingSystem.getLogger(Texture2DLoaderJOGL.class).fine("deleted texture...");
            g.glDeleteTextures(1, new int[]{id.intValue()},0);
          }
       }
        LoggingSystem.getLogger(Texture2DLoaderJOGL.class).fine("creating texture... ");
      }
      // create the texture ID for this texture
      if (textureID == -1) textureID = createTextureID(gl);
      Integer id = new Integer(textureID);
      ht.put(tex.getImage(), id);
      // register reference for refQueue
      WeakReference<ImageData> ref = new WeakReference<ImageData>(tex.getImage(), refQueue);
      refToID.put(ref, id);
      refToGL.put(ref, gl);
      refToDim.put(ref, new Dimension(tex.getImage().getWidth(), tex.getImage().getHeight()));
    }

    gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
    int srcPixelFormat = GL.GL_RGBA;
    handleTextureParameters(tex, gl);

    // create either a series of mipmaps of a single texture image based on
    // what's loaded
    if (first || replace) {
    	byte[] data = tex.getImage().getByteArray();
        if (mipmapped) {
          glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, 
        		  GL.GL_RGBA, 
        		  tex.getImage().getWidth(),
              tex.getImage().getHeight(), 
              srcPixelFormat, 
              GL.GL_UNSIGNED_BYTE, 
              ByteBuffer.wrap(data));
 //         System.err.println("Creating mipmaps");
        } else {
          gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, //GL.GL_RGBA,
                                                                          // //tex.getPixelFormat(),
              tex.getImage().getWidth(), tex.getImage().getHeight(), 0, srcPixelFormat,
              GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(data));
        }
    }
    
/*    if (replace) {
      // write data into the tex with id = textureID
      // what aboud mipmapped textures?
      throw new Error("not implemented");
    }*/
    
  } 

  public static void render(JOGLRenderer jr, CubeMap ref) {
//  public static void render(GL gl, CubeMap ref, double[] c2w) {
    boolean first = true;
    boolean mipmapped = true;
    GL gl = jr.getGL();
    GLU glu = new GLU();
    WeakHashMap<ImageData, Integer> ht = getCubeMapTableForGL(gl);
    
    Integer texid = (Integer) ht.get(ref.getTop());
    int textureID;
    if (texid != null) {
      first = false;
      textureID = texid.intValue();
    } else {
      // create the texture ID for this texture 
      textureID = createTextureID(gl); 
      ht.put(ref.getTop(), new Integer(textureID));
    }
    gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureID); 
    

    int srcPixelFormat =  GL.GL_RGBA;
    double[] c2w = jr.getContext().getCameraToWorld();
    c2w[3] = c2w[7] = c2w[11] = 0.0;
    
//    ref.setTextureMatrix(c2w);
//    handleTextureParameters(ref, gl);

    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, Texture2D.GL_CLAMP_TO_EDGE); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, Texture2D.GL_CLAMP_TO_EDGE); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, Texture2D.GL_LINEAR_MIPMAP_LINEAR); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, Texture2D.GL_LINEAR);

    float[] texcolor = ref.getBlendColor().getRGBComponents(null);
    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor, 0);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, Texture2D.GL_COMBINE);
    
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, Texture2D.COMBINE_MODE_DEFAULT);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_TEXTURE);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND0_RGB, GL.GL_SRC_COLOR);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE1_RGB, GL.GL_PREVIOUS);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND1_RGB, GL.GL_SRC_COLOR);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE2_RGB, GL.GL_CONSTANT);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND2_RGB, GL.GL_SRC_ALPHA);

    gl.glMatrixMode(GL.GL_TEXTURE);
    gl.glLoadTransposeMatrixd(c2w, 0);
    gl.glMatrixMode(GL.GL_MODELVIEW);       
    
    gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_R, Texture2D.GL_CLAMP_TO_EDGE); 
    
    gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
    gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
    gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
    gl.glEnable(GL.GL_TEXTURE_GEN_S);
    gl.glEnable(GL.GL_TEXTURE_GEN_T);
    gl.glEnable(GL.GL_TEXTURE_GEN_R);
    gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);

    // create either a series of mipmaps of a single texture image based on what's loaded 
    if (first)  {
      ImageData[] faces = new ImageData[6];
      faces[0]=ref.getBack();
      faces[1]=ref.getFront();
      faces[2]=ref.getBottom();
      faces[3]=ref.getTop();
      faces[4]=ref.getLeft();
      faces[5]=ref.getRight();
      for (int i = 0; i<6; ++i)   {
        byte[] data = faces[i].getByteArray();
        int width = faces[i].getWidth();
        int height = faces[i].getHeight();
         if (mipmapped) 
          glu.gluBuild2DMipmaps(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 
                      GL.GL_RGBA, 
                      width,
                      height, 
                      srcPixelFormat, 
                      GL.GL_UNSIGNED_BYTE, 
                      ByteBuffer.wrap(data)); 
        else    
          gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 
                  0, 
                  GL.GL_COMPRESSED_RGBA, //GL.GL_RGBA, //tex.getPixelFormat(), 
                width, 
                height, 
                  0, 
                  srcPixelFormat, 
                  GL.GL_UNSIGNED_BYTE, 
                  ByteBuffer.wrap(data) ); 
        
      
          }
        }
  }

  private static FloatBuffer maxAnisotropy;
  private static boolean canFilterAnisotropic=true;
  
  private static void handleTextureParameters(Texture2D tex, GL gl) {
    
    // TODO: maybe this should move to jogl configuration?
    if (canFilterAnisotropic)  {
      if (maxAnisotropy == null) {
        if (gl.glGetString(GL.GL_EXTENSIONS).contains("GL_EXT_texture_filter_anisotropic")) {
          maxAnisotropy = FloatBuffer.allocate(1);
          gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy.get(0));
        } else {
          canFilterAnisotropic = false;
        }
      } else {
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy.get(0));
      }
    }
    
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, tex.getRepeatS()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, tex.getRepeatT()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());

    float[] texcolor = tex.getBlendColor().getRGBComponents(null);
    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor, 0);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, tex.getApplyMode());
    
    if (tex.getApplyMode() == Texture2D.GL_COMBINE) 
    {
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, tex.getCombineMode());
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE0_RGB, GL.GL_TEXTURE);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND0_RGB, GL.GL_SRC_COLOR);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE1_RGB, GL.GL_PREVIOUS);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND1_RGB, GL.GL_SRC_COLOR);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SOURCE2_RGB, GL.GL_CONSTANT);
      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_OPERAND2_RGB, GL.GL_SRC_ALPHA);
      
    }    
    gl.glMatrixMode(GL.GL_TEXTURE);
    gl.glLoadTransposeMatrixd(tex.getTextureMatrix().getArray(),0);
    gl.glMatrixMode(GL.GL_MODELVIEW);       
  }

	/**
	 * 
	 */
	public static void deleteAllTextures(GL gl) {
        WeakHashMap ht = (WeakHashMap) lookupTextures.get(gl);
		if (ht == null) return;
		Collection vals = ht.values();
		Iterator it = vals.iterator();
		while (it.hasNext())	{
			Object obj = it.next();
			if (obj == null || ! (obj instanceof Integer)) continue;
			int[] list = new int[1];
			list[0] = ((Integer) obj).intValue();
			gl.glDeleteTextures(1, list, 0);
		}
		ht.clear();
	}

}


