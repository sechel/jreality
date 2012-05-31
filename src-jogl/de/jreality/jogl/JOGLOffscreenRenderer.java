package de.jreality.jogl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.ImageUtil;

import de.jreality.util.ImageUtility;

public class JOGLOffscreenRenderer {

	transient private GLPbuffer offscreenPBuffer;
	transient private Buffer offscreenBuffer;
	BufferedImage offscreenImage, bi;
	boolean preMultiplied = false;		// not sure about this!
	boolean useColorBuffer = true;
	transient private int tileSizeX=1024, tileSizeY=768,numTiles=4;
	private JOGLRenderer jr;
	transient private int samples = 1;
	int[] maxrbuffer = new int[4];
	
	public JOGLOffscreenRenderer(JOGLRenderer jr)	{
		this.jr = jr;
	}
	
	public void renderOffscreen(int imageWidth, int imageHeight, File file, GLAutoDrawable canvas) {
		BufferedImage img = renderOffscreen(imageWidth, imageHeight, canvas);
		ImageUtility.writeBufferedImage(file, img);
	}

	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, GLAutoDrawable canvas) {
		return renderOffscreen(imageWidth, imageHeight, 1.0, canvas);
	}
	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, double aa, GLAutoDrawable canvas) {
		return renderOffscreen(null, imageWidth, imageHeight, aa, canvas);
	}
	HashMap<Long, GLPbuffer> pbuffers = new HashMap<Long, GLPbuffer>();
	Boolean useFBO = true;
	int[] fbo = {-1, -1, -1}, rbuffer = {-1}, cbuffer = {-1}, txt = {-1};
	int[] normalFBO = {-1}, normalCBuffer = {-1};
	int imageWidth, imageHeight;
	int ow, oh;
	public BufferedImage renderOffscreen(BufferedImage dst, int imageWidth, int imageHeight, double aa, GLAutoDrawable canvas) {
		this.imageHeight = (int) (imageHeight/aa);
		this.imageWidth = (int) (imageWidth/aa);
		samples = (int) aa;
		jr.offscreenMode = true;
		if (useFBO)	{
			jr.theViewer.render();
		} 
		else {	// use pbuffers
			if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
				JOGLConfiguration.getLogger().log(Level.WARNING,"PBuffers not supported");
				return null;
			}

			double oldaa = jr.renderingState.globalAntiAliasingFactor;
			jr.renderingState.globalAntiAliasingFactor = aa;
			// System.err.println("setting global aa factor to "+aa);
			jr.lightsChanged = true;
			numTiles = Math.max(imageWidth / 1024, imageHeight / 1024);
			if (numTiles == 0)
				numTiles = 1;
			tileSizeX = (imageWidth / numTiles);
			tileSizeY = (imageHeight / numTiles);
			tileSizeX = 4 * (tileSizeX / 4);
			tileSizeY = 4 * (tileSizeY / 4);
			imageWidth = (tileSizeX) * numTiles;
			imageHeight = (tileSizeY) * numTiles;
			// System.err.println("Tile size x = "+tileSizeX);
			// System.err.println("Tile sizey = "+tileSizeY);
			// System.err.println("Image size = "+imageWidth+":"+imageHeight);
			long hashkey = 16384 * tileSizeY + tileSizeX;
			offscreenPBuffer = pbuffers.get(hashkey);
			if (offscreenPBuffer == null) {
				// if (offscreenPBuffer == null ||
				// lastWidth != tileSizeX ||
				// lastHeight != tileSizeY ) {
				System.err.println("Allocating new pbuffer");
				GLCapabilities caps = new GLCapabilities();
				caps.setDoubleBuffered(false);
				caps.setAlphaBits(8);
				// if (offscreenPBuffer != null)
				// offscreenPBuffer.destroy();
				offscreenPBuffer = GLDrawableFactory.getFactory()
						.createGLPbuffer(caps, null, tileSizeX, tileSizeY,
								canvas.getContext());
				pbuffers.put(hashkey, offscreenPBuffer);
			} else {
				jr.renderingState.clearColorBuffer = true;
			}
			if (offscreenImage == null
					|| offscreenImage.getWidth() != imageHeight
					|| offscreenImage.getHeight() != imageHeight) {
				offscreenImage = new BufferedImage(imageWidth, imageHeight,
						BufferedImage.TYPE_4BYTE_ABGR); // TYPE_3BYTE_BGR); //
				offscreenBuffer = ByteBuffer
						.wrap(((DataBufferByte) offscreenImage.getRaster()
								.getDataBuffer()).getData());
			}
			jr.lightListDirty = true;
			// offscreenPBuffer.setGL(new DebugGL(offscreenPBuffer.getGL()));
			// System.err.println("Calling canvas.display()");
			canvas.display();
			jr.renderingState.globalAntiAliasingFactor = oldaa;
		}
		dst = ImageUtility.rearrangeChannels(dst, offscreenImage);
		ImageUtil.flipImageVertically(dst);
		// a magic incantation to get the alpha channel to show up correctly
		dst.coerceData(true);
//		offscreenPBuffer.getContext().destroy();
//		offscreenPBuffer.destroy();
//		offscreenPBuffer = null;
		return dst;
	}

	boolean haveCheckedForMaxRBS = false;
	// these are the callbacks which are used when useFBO is true.
	// they are called by the JOGLRenderer instance in the top-level render() method.
	// We have to do it this way, since this is the only way to render to an active
	// gl context.
	protected void preRenderOffscreen(GL gl)	{
		if (!haveCheckedForMaxRBS) {
			jr.globalGL.glGetIntegerv(GL.GL_MAX_RENDERBUFFER_SIZE_EXT, maxrbuffer, 0);
			haveCheckedForMaxRBS = true;
			System.err.println("max render buffer size = "+maxrbuffer[0]);
		}
		// this number doesn't seem to be exactly right.  On my ATI device, I get a 
		// value of 16384, but even when samples = 1 I can't seem to exceed 4096.
		if (imageHeight*samples > maxrbuffer[0]) imageHeight = maxrbuffer[0]/samples;
		if (imageWidth*samples > maxrbuffer[0]) imageWidth = maxrbuffer[0]/samples;
		if (offscreenImage == null
				|| offscreenImage.getWidth() != imageHeight
				|| offscreenImage.getHeight() != imageHeight) {
			offscreenImage = new BufferedImage(imageWidth, imageHeight,
					BufferedImage.TYPE_4BYTE_ABGR); // TYPE_3BYTE_BGR); //
			offscreenBuffer = ByteBuffer
					.wrap(((DataBufferByte) offscreenImage.getRaster()
							.getDataBuffer()).getData());
		}
		if (fbo[0] == -1) gl.glGenFramebuffersEXT(1, fbo, 0);
//		System.err.println("fbo = "+fbo[0]);
		gl.glBindFramebufferEXT(GL.GL_FRAMEBUFFER_EXT, fbo[0]);
		if (rbuffer[0] == -1) gl.glGenRenderbuffersEXT(1, rbuffer, 0);
//		System.err.println("rbuffer = "+rbuffer[0]);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, rbuffer[0]);
		gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, samples, GL.GL_DEPTH_COMPONENT, imageWidth, imageHeight);
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_DEPTH_ATTACHMENT_EXT, GL.GL_RENDERBUFFER_EXT, rbuffer[0]);
		if (useColorBuffer)	{
			if (cbuffer[0] == -1) gl.glGenRenderbuffersEXT(1, cbuffer, 0);
			gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, cbuffer[0]);
			gl.glRenderbufferStorageMultisampleEXT(GL.GL_RENDERBUFFER_EXT, samples, GL.GL_RGBA8, imageWidth, imageHeight);
			gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_RENDERBUFFER_EXT, cbuffer[0]);			
		} else {
			if (txt[0] == -1) gl.glGenTextures(1, txt,0);
//			System.err.println("joglfbo tex = "+txt[0]);
			gl.glBindTexture(GL.GL_TEXTURE_2D, txt[0]);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);//_MIPMAP_LINEAR);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
		    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
		    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8,  imageWidth, imageHeight, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);
			gl.glFramebufferTexture2DEXT(GL.GL_FRAMEBUFFER_EXT, 
					GL.GL_COLOR_ATTACHMENT0_EXT, 
					GL.GL_TEXTURE_2D, 
					txt[0],  0);
		}
		int status = gl.glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
		if (status != GL.GL_FRAMEBUFFER_COMPLETE_EXT)	{
			System.err.println("Error in fbo: "+String.format("%d", status));
		}
		// this is a bit tricky: the size of the  view port has to be set here;
		// trying to do so earlier (before the pre...() method) resulted it its
		// getting overwritten somewhere by the viewport of the interactive window
		ow = jr.width;
		oh = jr.height;
		jr.width = this.imageWidth;
		jr.height = this.imageHeight;
		jr.myglViewport(0, 0, this.imageWidth, this.imageHeight);
//		System.err.println("Rendering image "+this.imageWidth+" x "+this.imageHeight);
	}
	
	protected void postRenderOffscreen(GL gl)	{
		// Bind the multisampled FBO for reading
//		gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, fbo[0]);
		// Create and bind a normal (not multi-sampled) FBO for drawing
		if (normalFBO[0] == -1) gl.glGenFramebuffersEXT(1, normalFBO, 0);
		gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, normalFBO[0]);
		if (normalCBuffer[0] == -1) gl.glGenRenderbuffersEXT(1, normalCBuffer, 0);
		gl.glBindRenderbufferEXT(GL.GL_RENDERBUFFER_EXT, normalCBuffer[0]);
		gl.glRenderbufferStorageEXT(GL.GL_RENDERBUFFER_EXT,  GL.GL_RGBA8, imageWidth, imageHeight);
		gl.glFramebufferRenderbufferEXT(GL.GL_FRAMEBUFFER_EXT, GL.GL_COLOR_ATTACHMENT0_EXT, GL.GL_RENDERBUFFER_EXT, normalCBuffer[0]);			
		// Blit the multisampled FBO to the normal FBO
		gl.glBlitFramebufferEXT(0, 0, imageWidth, imageHeight, 0, 0, imageWidth, imageHeight, GL.GL_COLOR_BUFFER_BIT, GL.GL_LINEAR);
		//Bind the normal FBO for reading
		gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, normalFBO[0]);
		// Read the pixels into the buffer of the BufferedImage
		gl.glReadPixels(0, 0,imageWidth, imageHeight,
				GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, offscreenBuffer);
		gl.glBindFramebufferEXT(GL.GL_DRAW_FRAMEBUFFER_EXT, 0);		
		gl.glBindFramebufferEXT(GL.GL_READ_FRAMEBUFFER_EXT, 0);	
		// restore the size of the interactive window
		jr.width = ow; jr.height = oh;
		jr.myglViewport(0, 0, ow, oh);
	}
	public GLPbuffer getOffscreenPBuffer() {
		return offscreenPBuffer;
	}

	public Buffer getOffscreenBuffer() {
		return offscreenBuffer;
	}

	public int getNumTiles() {
		return numTiles;
	}

	public int getTileSizeX() {
		return tileSizeX;
	}

	public int getTileSizeY() {
		return tileSizeY;
	}

}
