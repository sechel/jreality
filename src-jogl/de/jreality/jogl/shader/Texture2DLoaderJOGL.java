package de.jreality.jogl.shader;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import net.java.games.jogl.*;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.ReflectionMap;
import de.jreality.scene.Texture2D;
import de.jreality.scene.Texture3D;

/**
 * A utility class to load textures for JOGL
 * Picked up from java.net.games.jogl forum 2.6.4 (gunn)
 * 
 * TODO remove gl calls
 * 
 * @author Kevin Glass
 */
public class Texture2DLoaderJOGL {
	private GL gl;
	private GLU glu;
	public static final Texture2DLoaderJOGL FactoryLoader = new Texture2DLoaderJOGL();
	static Hashtable ht = new Hashtable();
	
	private Texture2DLoaderJOGL() {
		super();
	}

	private static int createTextureID(GL gl) 
	{ 
	   int[] tmp = new int[1]; 
	   gl.glGenTextures(1, tmp);
	   return tmp[0]; 
	} 

	public static int getID(Texture2D tex)	{
		Integer texid = (Integer) ht.get(tex);
		if (texid == null) return -1;
		return texid.intValue();
	}
        
  /**
	 * @param theCanvas
	 * @param tex
	 */
	public static void render(GLCanvas drawable, Texture2D tex) {
        render(drawable, tex, 0);
    }
  public static void render(GLCanvas drawable, Texture2D tex, int level) {
			boolean first = true;
			boolean mipmapped = true;
			GL gl = drawable.getGL();
			GLU glu = drawable.getGLU();
			
			Integer texid = (Integer) ht.get(tex);
			int textureID;
			if (texid != null)	{
				first = false;
				textureID = texid.intValue();
			} else {
				// create the texture ID for this texture 
				textureID = createTextureID(gl); 
				ht.put(tex, new Integer(textureID));
			}
      gl.glActiveTexture(GL.GL_TEXTURE0+level);
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
	private static void handleTextureParameters(Texture3D tex, GL gl) {
		Texture2D foo = (Texture2D) tex;
		handleTextureParameters(foo, gl);
		gl.glTexParameteri(GL.GL_TEXTURE_CUBE_MAP, GL.GL_TEXTURE_WRAP_R, tex.getRepeatT()); 
	}
	/**
	 * @param tex
	 * @param textureID
	 */
	private static void handleTextureParameters(Texture2D tex, GL gl) {
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, tex.getRepeatS()); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, tex.getRepeatT()); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());

		// TODO make this a field in Texture2D
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
    
		//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
		if (tex.getTextureTransformation() != null) {
			gl.glMatrixMode(GL.GL_TEXTURE);
			gl.glLoadTransposeMatrixd(tex.getTextureMatrix());
			gl.glMatrixMode(GL.GL_MODELVIEW);				
		}
	}

	public static void render(JOGLRenderer jr, ReflectionMap ref) {
		GLCanvas drawable = jr.getCanvas();
		boolean first = true;
		boolean mipmapped = true;
		GL gl = drawable.getGL();
		GLU glu = drawable.getGLU();
		
		Integer texid = (Integer) ht.get(ref);
		int textureID;
		if (texid != null)	{
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
		ref.setTextureMatrix(c2w);
		handleTextureParameters(ref, gl);

		gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
		gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
		gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
		gl.glEnable(GL.GL_TEXTURE_GEN_S);
		gl.glEnable(GL.GL_TEXTURE_GEN_T);
		gl.glEnable(GL.GL_TEXTURE_GEN_R);
		gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
		// create either a series of mipmaps of a single texture image based on what's loaded 
		if (first) 	{
			Texture2D[] faces = ref.getFaceTextures();
			for (int i = 0; i<6; ++i)		{
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

	public void deleteTexture(Texture2D tex)	{
		Integer which = (Integer) ht.get(tex);
		if (which == null) return;
		int[] list = new int[1];
		list[0] = which.intValue();
		gl.glDeleteTextures(1, list);
	}
}


