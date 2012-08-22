package de.jreality.jogl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLPbuffer;

import com.sun.opengl.util.ImageUtil;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.Texture2D;
import de.jreality.util.ImageUtility;

public class JOGLOffscreenRenderer {

	transient private GLPbuffer offscreenPBuffer;
	transient private Buffer offscreenBuffer;
	BufferedImage offscreenImage, bi;
	boolean preMultiplied = false;		// not sure about this!
	boolean useColorBuffer = true;
	boolean asTexture = false;
	transient private int tileSizeX=1024, tileSizeY=768,numTiles=4;
	private JOGLRenderer jr;
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
	JOGLFBO joglFBO = null, joglFBOSlow;
	int[] fbo = {-1}, rbuffer = {-1}, cbuffer = {-1}, txt = {-1}, normalFBO = {-1}, normalCBuffer = {-1};
	int imageWidth, imageHeight;
	static Matrix flipY = new Matrix();
	{
		MatrixBuilder.euclidean().scale(1,-1,1).assignTo(flipY);
	}
	public JOGLFBO renderOffscreen(JOGLFBO fbo, Texture2D dst, int imageWidth, int imageHeight) {
		if (fbo == null) joglFBO = new JOGLFBO(imageWidth, imageHeight);
		else joglFBO = fbo;
		// by setting the texture we get the image copied into the right place (in case asTexture = false)
		joglFBO.setTexture(dst);
		joglFBO.setAsTexture(asTexture);
		joglFBO.setSize(new Dimension(imageWidth, imageHeight));
		if (asTexture) {
			dst.setTextureMatrix(flipY);
//			dst.setMipmapMode(false);
		} 
		jr.setTheFBO(joglFBO);
		jr.setFboMode(true);
		jr.theViewer.render();
		jr.setFboMode(false);
		return joglFBO;
	}
	public BufferedImage renderOffscreen(BufferedImage dst, int w, int h, double aa, GLAutoDrawable canvas) {
		return renderOffscreen(dst, w, h, aa, canvas, null);
	}
	public BufferedImage renderOffscreen(BufferedImage dst, int w, int h, double aa, GLAutoDrawable canvas, SceneGraphPath cp) {
		imageHeight = (int) (h/aa);
		imageWidth = (int) (w/aa);
		if (useFBO)	{
			if (joglFBOSlow == null) 
				joglFBOSlow = new JOGLFBO(imageWidth, imageHeight);
			else 
				joglFBOSlow.setSize(new Dimension(imageWidth, imageHeight));
			jr.setTheFBO(joglFBOSlow);
			joglFBOSlow.setAsTexture(false);
			jr.setFboMode(true);
			jr.setAlternateCameraPath(cp);
			jr.theViewer.render();
			//canvas.display();
			dst = joglFBOSlow.getImage();
			jr.setAlternateCameraPath(null);
			jr.setFboMode(false);
		} 
		else {	// use pbuffers
			jr.offscreenMode = true;
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
			dst = ImageUtility.rearrangeChannels(dst, offscreenImage);
			ImageUtil.flipImageVertically(dst);
			// a magic incantation to get the alpha channel to show up correctly
			dst.coerceData(true);
		}
		return dst;
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

	public boolean isAsTexture() {
		return asTexture;
	}

	public void setAsTexture(boolean asTexture) {
		this.asTexture = asTexture;
	}

}
