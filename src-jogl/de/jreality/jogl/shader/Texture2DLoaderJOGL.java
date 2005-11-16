package de.jreality.jogl.shader;

import java.awt.Dimension;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.shader.ImageData;
import de.jreality.shader.CubeMap;
import de.jreality.shader.Texture2D;
import de.jreality.util.LoggingSystem;

/**
 * A utility class to load textures for JOGL
 * Picked up from java.net.games.jogl forum 2.6.4 (gunn)
 * 
 * TODO remove gl calls
 * 
 * @author Kevin Glass
 */
public class Texture2DLoaderJOGL {
	private static WeakHashMap lookupFromGL = new WeakHashMap();

  private static ReferenceQueue refQueue = new ReferenceQueue();
  private static IdentityHashMap refToID = new IdentityHashMap();
  private static IdentityHashMap refToGL = new IdentityHashMap();
  private static IdentityHashMap refToDim = new IdentityHashMap();
  
  private static final boolean REPLACE_TEXTURES = true;
  
	private Texture2DLoaderJOGL() {
	}

	private static int createTextureID(GL gl) 
	{ 
	   int[] tmp = new int[1]; 
	   gl.glGenTextures(1, tmp);
	   return tmp[0]; 
	} 
       
    private static WeakHashMap getHashTableForGL(GL gl)	{
      WeakHashMap ht = (WeakHashMap) lookupFromGL.get(gl);
  		if (ht == null)	{
    			ht = new WeakHashMap();
    			lookupFromGL.put(gl, ht);
      } 
  		return ht;
  }
  /**
	 * @param theCanvas
	 * @param tex
	 */
	public static void render(GLDrawable drawable, de.jreality.scene.Texture2D tex) {
//        render(drawable, tex, 0);
//    }
//  public static void render(GLCanvas drawable, Texture2D tex, int level) {
			boolean first = true;
			boolean mipmapped = true;
			GL gl = drawable.getGL();
			GLU glu = drawable.getGLU();
			WeakHashMap ht = getHashTableForGL(gl);
			Integer texid = (Integer) ht.get(tex);
			int textureID;
			if (texid != null)	{
				first = false;
				textureID = texid.intValue();
			} else {
				// create the texture ID for this texture 
				textureID = createTextureID(gl);
				Integer id = new Integer(textureID);
				ht.put(tex, id);
				JOGLConfiguration.theLog.log(Level.INFO, "Creating new (old) texture2d "+tex);
			}
 
//			gl.glActiveTexture(GL.GL_TEXTURE0+level);
			gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); 			
			int srcPixelFormat =  GL.GL_RGBA;
			handleTextureParameters(tex, gl);

			byte[] data = tex.getByteArray();
			
			// create either a series of mipmaps of a single texture image based on what's loaded 
			if (first) 
				if (mipmapped) 
					glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, 
											GL.GL_RGBA, 
											tex.getWidth(), 
								  			tex.getHeight(), 
										  srcPixelFormat, 
										  GL.GL_UNSIGNED_BYTE, 
										  data); 
				else		
					gl.glTexImage2D(GL.GL_TEXTURE_2D, 
								  0, 
								  GL.GL_COMPRESSED_RGBA_ARB, //GL.GL_RGBA, //tex.getPixelFormat(), 
								tex.getWidth(), 
								tex.getHeight(), 
								  0, 
								  srcPixelFormat, 
								  GL.GL_UNSIGNED_BYTE, 
								  data ); 
	} 

	/**
	 * @param tex
	 * @param textureID
	 */
  private static void handleTextureParameters(de.jreality.scene.Texture2D tex, GL gl) {
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, tex.getRepeatS()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, tex.getRepeatT()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());

    // TODO make this a field in Texture2D
    float[] texcolor = tex.getBlendColor().getRGBComponents(null);
    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor);
    gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, tex.getApplyMode());
    
    if (tex.getApplyMode() == de.jreality.scene.Texture2D.GL_COMBINE) 
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
    //gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
    if (tex.getTextureTransformation() != null) {
      gl.glMatrixMode(GL.GL_TEXTURE);
      gl.glLoadTransposeMatrixd(tex.getTextureMatrix());
      gl.glMatrixMode(GL.GL_MODELVIEW);       
    }
  }

//	public static void render(JOGLRenderer jr, de.jreality.scene.ReflectionMap ref) {
//		GLDrawable drawable = jr.getCanvas();
//		boolean first = true;
//		boolean mipmapped = true;
//		GL gl = drawable.getGL();
//		GLU glu = drawable.getGLU();
//		WeakHashMap ht = getHashTableForGL(gl);
//		
//		Integer texid = (Integer) ht.get(ref);
//		int textureID;
//		if (texid != null)	{
//			first = false;
//			textureID = texid.intValue();
//		} else {
//			// create the texture ID for this texture 
//			textureID = createTextureID(gl); 
//			ht.put(ref, new Integer(textureID));
//		}
//		gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureID); 
//		//if (!first) return;
//		
//		int srcPixelFormat =  GL.GL_RGBA;
//		
//		double[] c2w = jr.getContext().getCameraToWorld();
//		c2w[3] = c2w[7] = c2w[11] = 0.0;
//		ref.setTextureMatrix(c2w);
//		handleTextureParameters(ref, gl);
//
//		gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
//		gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
//		gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
//		gl.glEnable(GL.GL_TEXTURE_GEN_S);
//		gl.glEnable(GL.GL_TEXTURE_GEN_T);
//		gl.glEnable(GL.GL_TEXTURE_GEN_R);
//		gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
//		// create either a series of mipmaps of a single texture image based on what's loaded 
//		if (first) 	{
//      de.jreality.scene.Texture2D[] faces = ref.getFaceTextures();
//			for (int i = 0; i<6; ++i)		{
//				byte[] data = faces[i].getByteArray();
//				int width = faces[i].getWidth();
//				int height = faces[i].getHeight();
//				if (mipmapped) 
//					glu.gluBuild2DMipmaps(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 
//											GL.GL_RGBA, 
//											width,
//								  		height, 
//										  srcPixelFormat, 
//										  GL.GL_UNSIGNED_BYTE, 
//										  data); 
//				else		
//					gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 
//								  0, 
//								  GL.GL_COMPRESSED_RGBA_ARB, //GL.GL_RGBA, //tex.getPixelFormat(), 
//								width, 
//								height, 
//								  0, 
//								  srcPixelFormat, 
//								  GL.GL_UNSIGNED_BYTE, 
//								  data ); 
//				
//			
//					}
//				}
//	}

   /******************* new Textures *******************/
  
  public static void render(GLDrawable drawable, Texture2D tex) {
    //  render(drawable, tex, 0);
    //}
    //public static void render(GLCanvas drawable, Texture2D tex, int level) {
    boolean first = true;
    boolean mipmapped = true;
    boolean replace = false;
    
    GL gl = drawable.getGL();
    GLU glu = drawable.getGLU();
    
    WeakHashMap ht = getHashTableForGL(gl);

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
            g.glDeleteTextures(1, new int[]{id.intValue()});
          }
       }
        LoggingSystem.getLogger(Texture2DLoaderJOGL.class).info("creating texture... ");
      }
      // create the texture ID for this texture
      if (textureID == -1) textureID = createTextureID(gl);
      Integer id = new Integer(textureID);
      ht.put(tex.getImage(), id);
      // register reference for refQueue
      WeakReference ref = new WeakReference(tex.getImage(), refQueue);
      refToID.put(ref, id);
      refToGL.put(ref, gl);
      refToDim.put(ref, new Dimension(tex.getImage().getWidth(), tex.getImage().getHeight()));
    }

    //gl.glActiveTexture(GL.GL_TEXTURE0+level);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureID);
    int srcPixelFormat = GL.GL_RGBA;
    handleTextureParameters(tex, gl);

    byte[] data = tex.getImage().getByteArray();

    // create either a series of mipmaps of a single texture image based on
    // what's loaded
    if (first || replace) {
        if (mipmapped) {
          glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, GL.GL_RGBA, tex.getImage().getWidth(),
              tex.getImage().getHeight(), srcPixelFormat, GL.GL_UNSIGNED_BYTE, data);
        } else {
          gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, //GL.GL_RGBA,
                                                                          // //tex.getPixelFormat(),
              tex.getImage().getWidth(), tex.getImage().getHeight(), 0, srcPixelFormat,
              GL.GL_UNSIGNED_BYTE, data);
        }
    }
    
/*    if (replace) {
      // write data into the tex with id = textureID
      // what aboud mipmapped textures?
      throw new Error("not implemented");
    }*/
    
  } 

  public static void render(JOGLRenderer jr, CubeMap ref) {
    GLDrawable drawable = jr.getCanvas();
    boolean first = true;
    boolean mipmapped = true;
    GL gl = drawable.getGL();
    GLU glu = drawable.getGLU();
    WeakHashMap ht = getHashTableForGL(gl);
    
    Integer texid = (Integer) ht.get(ref);
    int textureID;
    if (texid != null)  {
      first = false;
      textureID = texid.intValue();
    } else {
      // create the texture ID for this texture 
      textureID = createTextureID(gl); 
      ht.put(ref, new Integer(textureID));
    }
    gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, textureID); 
    //if (!first) return;
    
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
    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor);
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
    gl.glLoadTransposeMatrixd(c2w);
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
                      data); 
        else    
          gl.glTexImage2D(GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X+i, 
                  0, 
                  GL.GL_COMPRESSED_RGBA_ARB, //GL.GL_RGBA, //tex.getPixelFormat(), 
                width, 
                height, 
                  0, 
                  srcPixelFormat, 
                  GL.GL_UNSIGNED_BYTE, 
                  data ); 
        
      
          }
        }
  }

  private static void handleTextureParameters(Texture2D tex, GL gl) {
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, tex.getRepeatS()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, tex.getRepeatT()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());

    float[] texcolor = tex.getBlendColor().getRGBComponents(null);
    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor);
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
    gl.glLoadTransposeMatrixd(tex.getTextureMatrix().getArray());
    gl.glMatrixMode(GL.GL_MODELVIEW);       
  }

	/**
	 * 
	 */
	public static void deleteAllTextures(GL gl) {
        WeakHashMap ht = (WeakHashMap) lookupFromGL.get(gl);
		if (ht == null) return;
		Collection vals = ht.values();
		Iterator it = vals.iterator();
		while (it.hasNext())	{
			Object obj = it.next();
			if (obj == null || ! (obj instanceof Integer)) continue;
			int[] list = new int[1];
			list[0] = ((Integer) obj).intValue();
			gl.glDeleteTextures(1, list);
		}
		ht.clear();
	}

	/**
	 * @param tex2d
	 */
	public static void deleteTexture(Object tex2d) {
		Iterator gls = lookupFromGL.keySet().iterator();
		
		while (gls.hasNext())	{
			Object foo = gls.next();
			if ( !(foo instanceof GL)) throw new Error();
			GL gl = (GL) foo;
			deleteTexture(tex2d, gl);
		}
	}

	 public static void deleteTexture(Object tex, GL gl)  {
	    WeakHashMap ht = (WeakHashMap) lookupFromGL.get(gl);
	    if (ht == null) return;
	    Integer which = (Integer) ht.get(tex);
	    if (which == null) return;
	    int[] list = new int[1];
	    list[0] = which.intValue();
	    JOGLConfiguration.theLog.info("Deleting texture2d ID "+list[0]);
	    gl.glDeleteTextures(1, list);
	  }


}


