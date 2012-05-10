package de.jreality.jogl;

import java.awt.Dimension;

import javax.media.opengl.GL;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.Texture2D;


/**
 * This class is experimental.  It is designed to be used as the source of a 2d texture
 * which is active in another viewer, called here the <i>main</i> viewer.  
 * I don't really understand the OpenGL code;
 * I copied some code off an OpenGL tutorial somewhere and hacked at it until it worked.
 * 
 * The main methods here are {@link #setSize(Dimension)}, to set the size of the texture image
 * created, and {@link #setTexture2D(Texture2D)}, to set the instance of Texture2D whose
 * image data is to be replaced by the rendered image of this viewer.
 * 
 * The implementation is not pretty.  It abuses methods in the {@link Texture2D} interface to pass
 * around JOGL-specific information, in particular the texture ID which the JOGLFBOViewer
 * generates when it binds its rendered output to a texture.  It expects that the
 * methods {@link Texture2D#setSource0Alpha(Integer)} and {@link Texture2D#setSource1Alpha(Integer)}
 * (and their <i>get</i> counterparts) are not otherwise used.  The first is used below to set 
 * a magic value (23) which means that the value contained in the second should be interpreted
 * as the designated texture ID. 
 * 
 * For working example, see <code>de.jreality.tutorial.viewer.JOGLFBOTextureExample</code> in the jReality tutorial.
 * 
 * @author Charles Gunn
 *
 */public class JOGLFBOViewer  extends Viewer  {

	int  width = -1, height = -1;
	int[] fbo = {-1}, rbuffer = {-1}, txt = {-1};
	Texture2D tex;
	boolean init = false, dirty = true;
	
	public JOGLFBOViewer(SceneGraphComponent sgc)	{
		this(null, sgc);
	
	}
	public JOGLFBOViewer(SceneGraphPath cameraPath,
			SceneGraphComponent sceneRoot) {
		super(cameraPath, sceneRoot);
		renderer = new JOGLRenderer(this);
	}

	public void setSize(Dimension dim)	{
		width = dim.width; height = dim.height;
		dirty = true;
	}
	
	public int renderFBOTexture(int w, int h) 	{
		GL gl = drawable.getGL();
		if (w != width || h != height)	{
			width = w;
			height = h;
		}
		renderer.fboMode = true;
		drawable.display();
		renderer.fboMode = false;
		System.err.println("Rendered fbo texture "+txt[0]);
		return txt[0];
	}

	protected void render(GL gl)	{
		if (!init)	{
			renderer.init(gl);
			renderer.fboViewer = this;
			renderer.width = width;
			renderer.height = height;
			if (tex != null) tex.setSource1Alpha(txt[0]);
			init = true;
		}
		if (dirty) {
			dispose(gl);
			dirty = false;
		}
		renderer.fboMode = true;
		renderer.display(gl);
		renderer.fboMode = false;		
		if (tex != null) tex.setSource1Alpha(txt[0]);
	}
	
	protected void preRender(GL gl)	{
		if (fbo[0] == -1) {
			System.err.println("setting up frame buffer");
			gl.glGenFramebuffersEXT(1, fbo, 0);
			gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);
			gl.glGenRenderbuffersEXT(1, rbuffer, 0);
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, rbuffer[0]);
			gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT, width, height);
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, rbuffer[0]);

			gl.glGenTextures(1, txt,0);
			System.err.println("joglfbo tex = "+txt[0]);
			gl.glBindTexture(GL.GL_TEXTURE_2D, txt[0]);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);//_MIPMAP_LINEAR);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
			gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, 
					GL.GL_COLOR_ATTACHMENT0_EXT, 
					GL.GL_TEXTURE_2D, 
					txt[0],  0);
//		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_FALSE);
			// funny artifacts with mipmapping
//		    gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);		
			int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
			if (status != GL.GL_FRAMEBUFFER_COMPLETE_EXT)	{
				System.err.println("Error in fbo: "+String.format("%04x", status));
			}
		}
//		System.err.println("prerender");
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);
		renderer.myglViewport(0, 0, width, height);
	}
	
	protected void postRender(GL gl) {
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);		
//		System.err.println("postrender");
	}
	
	private void dispose(GL gl) {
		gl.glDeleteFramebuffersEXT(1, fbo, 0);
		gl.glDeleteRenderbuffersEXT(1, rbuffer, 0);
		fbo[0] = rbuffer[0] = -1;
	}
	
	public void setTexture2D(Texture2D tex)	{
		this.tex = tex;
		tex.setSource0Alpha(23);
		tex.setImage(null);
		//tex2d.setMipmapMode(false);
	}
	
}
