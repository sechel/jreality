package de.jreality.jogl.shader;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import net.java.games.jogl.*;
import de.jreality.scene.Texture2D;

/**
 * A utility class to load textures for JOGL
 * Picked up from java.net.games.jogl forum 2.6.4 (gunn)
 * 
 * TODO remove gl calls
 * 
 * @author Kevin Glass
 */
public class Texture2DLoaderJOGL extends Texture2DLoader {
	private GL gl;
	private GLU glu;
	public static final Texture2DLoaderJOGL FactoryLoader = new Texture2DLoaderJOGL();
	Hashtable ht = new Hashtable();
	
	private Texture2DLoaderJOGL() {
		super();
	}

	private int createTextureID() 
	{ 
	   int[] tmp = new int[1]; 
	   gl.glGenTextures(1, tmp);
	   return tmp[0]; 
	} 

	public void bindTexture2D(GLDrawable drawable,
								String name) throws IOException 
	{ 
		Texture2DJOGL tex = (Texture2DJOGL) table.get(name);
    
		if (tex == null)		{
			System.out.println("Unknown texture2D "+name);
			return;
		}
		System.out.println("Binding texture"+name);
		bindTexture2D(drawable, tex);
	}
	
	public void bindTexture2D(GLDrawable drawable,
								Texture2DJOGL tex) 
	{ 
		boolean first = true;
		this.gl = drawable.getGL();
		this.glu = drawable.getGLU();
		
		int textureID = tex.getTextureID();
		if (textureID != -1)	{		// already processed
			first = false;
		} else {
			// create the texture ID for this texture 
			textureID = createTextureID(); 
			tex.setTextureID(textureID);
			// bind this texture 
		}
		gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); 
		
		BufferedImage bufferedImage = tex.getBufferedImage();
		int srcPixelFormat = 0;
    
		if (bufferedImage.getColorModel().hasAlpha()) {
			srcPixelFormat = GL.GL_RGBA;
		} else {
			srcPixelFormat = GL.GL_RGB;
		}
    
		int wrapMode = tex.getWrapMode();

		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, wrapMode); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, wrapMode); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());
		gl.glTexParameterf(textureID, GL.GL_TEXTURE_PRIORITY, (float) 1.0);
		//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
		gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
		//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadTransposeMatrixd(tex.getTextureMatrix());
		gl.glMatrixMode(GL.GL_MODELVIEW);
		// create either a series of mipmaps of a single texture image based on what's loaded 
		/*if (mipmapped) 
		{ 
			glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, 
								  dstPixelFormat, 
									tex.getImageWidth(), 
						  			tex.getImageHeight(), 
								  srcPixelFormat, 
								  GL.GL_UNSIGNED_BYTE, 
								  bufferedImage); 
		} 
		else 
		{ 
		*/	

		if (first) gl.glTexImage2D(GL.GL_TEXTURE_2D, 
						  0, 
						  GL.GL_RGBA, //tex.getPixelFormat(), 
						tex.getImageWidth(), 
						tex.getImageHeight(), 
						  0, 
						  srcPixelFormat, 
						  GL.GL_UNSIGNED_BYTE, 
						  tex.getByteBuffer() ); 
		//} 
 
 		
	}

	public int getID(Texture2D tex)	{
		Integer texid = (Integer) ht.get(tex);
		if (texid == null) return -1;
		else return texid.intValue();
	}
	/**
	 * @param theCanvas
	 * @param tex
	 */
	public void bindTexture2D(GLCanvas drawable, Texture2D tex) {
	
			boolean first = true;
			boolean mipmapped = true;
			this.gl = drawable.getGL();
			this.glu = drawable.getGLU();
			
			Integer texid = (Integer) ht.get(tex);
			int textureID;
			if (texid != null)	{
				first = false;
				textureID = texid.intValue();
			} else {
				// create the texture ID for this texture 
				textureID = createTextureID(); 
				ht.put(tex, new Integer(textureID));
			}
			gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); 
			//if (!first) return;
			
			Image bufferedImage = tex.getImage();
			int srcPixelFormat = 0;
	    
			//if (bufferedImage.getColorModel().hasAlpha()) {
				srcPixelFormat = GL.GL_RGBA;
			//} else {
				//srcPixelFormat = GL.GL_RGB;
			//}
	    
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, tex.getRepeatS()); 
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, tex.getRepeatT()); 
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, tex.getMinFilter()); 
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, tex.getMagFilter());
			gl.glTexParameterf(textureID, GL.GL_TEXTURE_PRIORITY, (float) 1.0);

			float[] texcolor = {.4f, .6f, .3f, .5f};
			gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, texcolor);

			//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
			gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, tex.getApplyMode());
			//gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
			if (tex.getTextureMatrix() != null) {
				gl.glMatrixMode(GL.GL_TEXTURE);
				gl.glLoadTransposeMatrixd(tex.getTextureMatrix().getMatrix());
				gl.glMatrixMode(GL.GL_MODELVIEW);				
			}
			
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
	
	public void deleteTexture(Texture2D tex)	{
		Integer which = (Integer) ht.get(tex);
		if (which == null) return;
		int[] list = new int[1];
		list[0] = which.intValue();
		gl.glDeleteTextures(1, list);
	}

}


