/*
 * Created on Jun 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import net.java.games.jogl.GL;

	/**
 * A texture to be bound within JOGl
 *
 * @author Kevin Glass
 */
public class Texture2DJOGL {
	private String name; 
	private String resourceName;
	private int textureID;
	private int height;
	private int width;
	private int texWidth;
	private int texHeight;
	private float widthRatio;
	private float heightRatio;
	private int wrapMode = GL.GL_REPEAT;
	private int minFilter;
	private int magFilter;
	double[] textureMatrix;

	private BufferedImage bufferedImage;
	private ByteBuffer byteBuffer;

	public Texture2DJOGL(String name, String resourceName) {
		this.name = name;
		this.resourceName = resourceName;
		textureMatrix = null;
		textureID = -1;		// not yet bound
	}

	public void setBufferedImage(BufferedImage buffer) {
		this.bufferedImage = buffer;
	}

	public BufferedImage getBufferedImage() {
	   return bufferedImage;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getImageHeight() {
		return height;
	}

	public int getImageWidth() {
		return width;
	}

	public float getHeight() {
		return heightRatio;
	}

	public float getWidth() {
		return widthRatio;
	}

	public void setTextureHeight(int texHeight) {
		this.texHeight = texHeight;
		setHeight();
	}

	public void setTextureWidth(int texWidth) {
		this.texWidth = texWidth;
		setWidth();
	}

	private void setHeight() {
		if (texHeight != 0) {
			heightRatio = ((float) height)/texHeight;
		}
	}

	private void setWidth() {
		if (texWidth != 0) {
			widthRatio = ((float) width)/texWidth;
		}
	}
	/**
	 * @return
	 */
	public int getTextureID() {
		return textureID;
	}

	/**
	 * @param i
	 */
	public void setTextureID(int i) {
		textureID = i;
	}

	/**
	 * @param textureBuffer
	 */
	public void setByteBuffer(ByteBuffer textureBuffer) {
		// TODO Auto-generated method stub
		byteBuffer = textureBuffer;
	}

	/**
	 * @return
	 */
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	/**
	 * @return
	 */
	public int getMagFilter() {
		return magFilter;
	}

	/**
	 * @return
	 */
	public int getMinFilter() {
		return minFilter;
	}

	/**
	 * @return
	 */
	public int getWrapMode() {
		return wrapMode;
	}

	/**
	 * @param i
	 */
	public void setMagFilter(int i) {
		magFilter = i;
	}

	/**
	 * @param i
	 */
	public void setMinFilter(int i) {
		minFilter = i;
	}

	/**
	 * @param b
	 */
	public void setWrapMode(int i) {
		wrapMode = i;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public double[] getTextureMatrix() {
		return textureMatrix;
	}

	/**
	 * @param ds
	 */
	public void setTextureMatrix(double[] ds) {
		if (ds == null || ds.length != 16) return;
		textureMatrix = ds;
	}

}
