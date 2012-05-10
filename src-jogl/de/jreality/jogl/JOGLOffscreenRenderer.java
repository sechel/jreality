package de.jreality.jogl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.ImageUtil;

import de.jreality.util.ImageUtility;

public class JOGLOffscreenRenderer {

	transient private GLPbuffer offscreenPBuffer;
	transient private Buffer offscreenBuffer;

	transient private int tileSizeX=1024, tileSizeY=768,numTiles=4, lastWidth = -1, lastHeight = -1;
	private JOGLRenderer jr;
	
	public JOGLOffscreenRenderer(JOGLRenderer jr)	{
		this.jr = jr;
	}
	
	public void renderOffscreen(int imageWidth, int imageHeight, File file, GLAutoDrawable canvas) {
		BufferedImage img = renderOffscreen(imageWidth, imageHeight, canvas);
		ImageUtility.writeBufferedImage(file, img);
	}

	BufferedImage offscreenImage;
	boolean preMultiplied = false;		// not sure about this!
	private javax.swing.Timer followTimer;
	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, GLAutoDrawable canvas) {
		return renderOffscreen(imageWidth, imageHeight, 1.0, canvas);
	}
	public BufferedImage renderOffscreen(int imageWidth, int imageHeight, double aa, GLAutoDrawable canvas) {
		if (!GLDrawableFactory.getFactory().canCreateGLPbuffer()) {
			JOGLConfiguration.getLogger().log(Level.WARNING,"PBuffers not supported");
			return null;
		}
		double oldaa = jr.renderingState.globalAntiAliasingFactor;
		jr.renderingState.globalAntiAliasingFactor = aa;
		System.err.println("setting global aa factor to "+aa);
		jr.lightsChanged = true;
		numTiles = Math.max(imageWidth/1024, imageHeight/1024);
		if (numTiles == 0) numTiles = 1;
		tileSizeX = (imageWidth/numTiles);
		tileSizeY = (imageHeight/numTiles);
		tileSizeX = 4 * (tileSizeX/4);
		tileSizeY = 4 * (tileSizeY/4);
		imageWidth = (tileSizeX) * numTiles;
		imageHeight = (tileSizeY) * numTiles;
		System.err.println("Tile size x = "+tileSizeX);
		System.err.println("Tile sizey = "+tileSizeY);
		System.err.println("Image size = "+imageWidth+":"+imageHeight);
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(false);
		caps.setAlphaBits(8);
		if (offscreenPBuffer == null ||
				lastWidth != tileSizeX ||
				lastHeight != tileSizeY ) {
			System.err.println("Allocating new pbuffer");
			lastWidth = tileSizeX;
			lastHeight = tileSizeY;
			if (offscreenPBuffer != null)
				offscreenPBuffer.destroy();
			offscreenPBuffer = GLDrawableFactory.getFactory().createGLPbuffer(
					caps, null,
					tileSizeX, tileSizeY,
					canvas.getContext());		
		}
		offscreenImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR); //TYPE_3BYTE_BGR); //
		offscreenBuffer = ByteBuffer.wrap(((DataBufferByte) offscreenImage.getRaster().getDataBuffer()).getData());
		jr.offscreenMode = true;
		jr.lightListDirty = true;
//		offscreenPBuffer.setGL(new DebugGL(offscreenPBuffer.getGL()));
		canvas.display();
		jr.renderingState.globalAntiAliasingFactor = oldaa;
		BufferedImage bi = ImageUtility.rearrangeChannels(offscreenImage);
		ImageUtil.flipImageVertically(bi);
		// a magic incantation to get the alpha channel to show up correctly
		bi.coerceData(true);
//		offscreenPBuffer.getContext().destroy();
//		offscreenPBuffer.destroy();
//		offscreenPBuffer = null;
		return bi;
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
