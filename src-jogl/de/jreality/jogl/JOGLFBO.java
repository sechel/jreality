package de.jreality.jogl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;

import com.sun.opengl.util.ImageUtil;

import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.util.ImageUtility;

/**
 * Parts of this implementation are not pretty. Particularly the case when the framebuffer is used to
 * generate a texture directly.  It abuses methods in the {@link Texture2D} interface to pass
 * around JOGL-specific information, in particular the texture ID which the JOGLFBO instance
 * generates when it binds its rendered output to a texture.  It expects that the
 * methods {@link Texture2D#setSource0Alpha(Integer)} and {@link Texture2D#setSource1Alpha(Integer)}
 * (and their <i>get</i> counterparts) are not otherwise used.  The first is used below to set 
 * a magic value (23) which means that the value contained in the second should be interpreted
 * as the designated texture ID. 
@author gunn
 *
 */
public class JOGLFBO {

	BufferedImage image;
	Buffer buffer;
	Texture2D tex;
	boolean asTexture = false,
			dispose = false;
	int width = 512, height = 512, samples = 4;
	int[] fbo = {-1}, rbuffer = {-1}, cbuffer = {-1}, txt = {-1}, fboN = {-1}, cbufferN = {-1}, maxrbuffer = {-1};
	boolean haveCheckedForMaxRBS = false;
	
	public JOGLFBO() {
		update();
	}
	
	public JOGLFBO(int width, int height)	{
		this.width = width;
		this.height = height;
		update();
	}
	boolean dirty = true;
	public void setSize(Dimension dim)	{
		if (width == dim.width && height == dim.height) return;
		width = dim.width; height = dim.height;
		// this number doesn't seem to be exactly right.  On my ATI device, I get a 
		// value of 16384, but even when samples = 1 I can't seem to exceed 4096.
		dirty = true;
		update();
	}

	public void update()	{
		checkSize();
		if (image == null
				|| image.getWidth() != width
				|| image.getHeight() != height) {
			image = new BufferedImage(width, height,
					BufferedImage.TYPE_4BYTE_ABGR); // TYPE_3BYTE_BGR); //
			buffer = ByteBuffer.wrap(((DataBufferByte) image.getRaster()
							.getDataBuffer()).getData());
			System.err.println("Allocating BI of size "+width+":"+height);
		}
	}
	public BufferedImage getImage() {
		return image;
	}

	public void setTexture(Texture2D t)	{
		if (tex  == t) return;
		tex = t;
		if (asTexture) {
			tex.setSource0Alpha(23);
			tex.setImage(null);
		} else {
			tex.setSource0Alpha(Texture2D.SOURCE0_ALPHA_DEFAULT);
		}
	}

	public Texture2D getTexture2D() {
		return tex;
	}
	
	protected void preRender(GL gl)	{
		if (!haveCheckedForMaxRBS) {
			gl.glGetIntegerv(GL.GL_MAX_RENDERBUFFER_SIZE_EXT, maxrbuffer, 0);
			haveCheckedForMaxRBS = true;
			System.err.println("max render buffer size = "+maxrbuffer[0]);
			checkSize();
			update();
		}
		if (dirty) {
			dispose(gl);
			dirty = false;
		}
		if (fbo[0] == -1)	{
			gl.glGenFramebuffersEXT(1, fbo, 0);
			gl.glGenRenderbuffersEXT(1, rbuffer, 0);
			gl.glGenRenderbuffersEXT(1, cbuffer, 0);
			gl.glGenTextures(1, txt,0);
			gl.glGenFramebuffersEXT(1, fboN, 0);
			gl.glGenRenderbuffersEXT(1, cbufferN, 0);
			System.err.println("creating fbo "+fbo[0]);
		}
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, rbuffer[0]);
		if (!asTexture) gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, samples, GL.GL_DEPTH_COMPONENT32, width, height);
		else gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT, GL.GL_DEPTH_COMPONENT, width, height);
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, rbuffer[0]);

		if (!asTexture)	{
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, cbuffer[0]);
			gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, samples, GL.GL_RGBA8, width, height);
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_RENDERBUFFER_EXT, cbuffer[0]);			
		} else {
			gl.glBindTexture(GL.GL_TEXTURE_2D, txt[0]);
		    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
			gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, 
					GL.GL_COLOR_ATTACHMENT0_EXT, 
					GL.GL_TEXTURE_2D, 
					txt[0],  0);
//		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_GENERATE_MIPMAP, GL.GL_TRUE);
		    gl.glGenerateMipmapEXT(GL.GL_TEXTURE_2D);		
		}
		int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		if (status != GL.GL_FRAMEBUFFER_COMPLETE_EXT)	{
			System.err.println("Error in fbo: "+String.format("%d", status));
		}
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);
	}

	protected void postRender(GL gl) {
		if (!asTexture)	{ 
			image = new BufferedImage(width, height,
					BufferedImage.TYPE_4BYTE_ABGR); // TYPE_3BYTE_BGR); //
			buffer = ByteBuffer.wrap(((DataBufferByte) image.getRaster()
							.getDataBuffer()).getData());
			gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, fboN[0]);
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, cbufferN[0]);
			gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,  GL.GL_RGBA8, width, height);
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_RENDERBUFFER_EXT, cbufferN[0]);			
			// Blit the multisampled FBO to the normal FBO
			gl.glBlitFramebufferEXT(0, 0, width, height, 0, 0, width, height, GL.GL_COLOR_BUFFER_BIT, GL.GL_LINEAR);
			//Bind the normal FBO for reading
			gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, fboN[0]);
			// Read the pixels into the buffer of the BufferedImage
			gl.glReadPixels(0, 0, width, height,
					GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
//			System.err.println("reading pixels");
			image = ImageUtility.rearrangeChannels(null, image);
			ImageUtil.flipImageVertically(image);
			if (tex != null) {
//				if (tex.getImage().getImage() != image) 
					tex.setImage(new ImageData(image));
				tex.setSource0Alpha(Texture2D.SOURCE0_ALPHA_DEFAULT);
			}
		} else {
			if (tex != null) tex.setSource1Alpha(txt[0]);
			tex.setSource0Alpha(23);
		}
		if (dispose) dispose(gl);

		// restore the size of the interactive window
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, 0);		
	}
	
	protected void dispose(GL gl) {
		gl.glDeleteFramebuffersEXT(1, fbo, 0);
		gl.glDeleteRenderbuffersEXT(1, rbuffer, 0);
		gl.glDeleteTextures(1, txt, 0);
		gl.glDeleteRenderbuffersEXT(1, cbuffer, 0);
		gl.glDeleteRenderbuffersEXT(1, cbufferN, 0);
		gl.glDeleteFramebuffersEXT(1, fboN, 0);
		fbo[0] = rbuffer[0] = txt[0] = cbuffer[0] = cbuffer[0] = fboN[0] =  -1;
	}

	private void checkSize() {
		if (!haveCheckedForMaxRBS) return;
		if (height*samples > maxrbuffer[0]) height = maxrbuffer[0]/samples;
		if (width*samples > maxrbuffer[0]) width = maxrbuffer[0]/samples;
	}

	public boolean isAsTexture() {
		return asTexture;
	}

	public void setAsTexture(boolean asTexture) {
		if (asTexture == this.asTexture) return;
		this.asTexture = asTexture;
		dirty=true;
		update();
	}

	public boolean isDispose() {
		return dispose;
	}

	public void setDispose(boolean dispose) {
		this.dispose = dispose;
	}


}
