/*
 * Created on Jun 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;


/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkyBoxTexture {
	Texture2DJOGL[] faceTextures;
	private GL gl;
	private GLU glu;
	/**
	 * 
	 */
	public SkyBoxTexture(Texture2DJOGL[] faces) {
		super();
		faceTextures = faces;
	}

	static int[] box_constants = {GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X,GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
		GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};
	
	public void bindSkyBox(GLDrawable drawable) 
	{ 
		gl = drawable.getGL();
		glu = drawable.getGLU();
		
		gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
		gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
		gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
		gl.glEnable(GL.GL_TEXTURE_GEN_S);
		gl.glEnable(GL.GL_TEXTURE_GEN_T);
		gl.glEnable(GL.GL_TEXTURE_GEN_R);
		gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
		for (int i = 0; i<6; ++i)	{
			gl.glTexImage2D(box_constants[i], 0, GL.GL_RGBA,		
			 faceTextures[i].getImageWidth(), 
			 faceTextures[i].getImageHeight(), 
			  0, 
			  GL.GL_RGBA, 
			  GL.GL_UNSIGNED_BYTE, 
			  faceTextures[i].getByteBuffer()); 
		}

	}

}
