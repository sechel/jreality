/*
 * Created on Jun 2, 2004
 *
 */
package de.jreality.jogl.shader;

/**
 * @author Charles Gunn
 *
 */

//package tools;

import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Hashtable;

import javax.imageio.ImageIO;

/**
 * A utility class to load textures for JOGL
 *
 * @author Kevin Glass
 */
public abstract class Texture2DLoader {
	protected HashMap table = new HashMap();
//	private ColorModel glAlphaColorModel;
//	private ColorModel glColorModel;

	public Texture2DLoader() {
//		glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
//											new int[] {8,8,8,8},
//											true,
//											false,
//											Transparency.TRANSLUCENT,
//											DataBuffer.TYPE_BYTE);
//                                        
//		glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
//											new int[] {8,8,8,0},
//											false,
//											false,
//											Transparency.OPAQUE,
//											DataBuffer.TYPE_BYTE);
	}

//	public Texture2DJOGL getTexture2D(String name,String resourceName) throws IOException {
//
//		return getTexture2D(name,resourceName,
//						 GL.GL_RGBA,     // dst pixel format
//						 GL.GL_LINEAR, // min filter (unused)
//						 GL.GL_LINEAR, // mag filter (unused)
//						 GL.GL_REPEAT,  // wrap?
//						 false,
//						 true); // mipmap?
//	}
//
//	public Texture2DJOGL getTexture2D(String name, 
//							  String resourceName, 
//							  int dstPixelFormat, 
//							  int minFilter, 
//							  int magFilter, 
//							  int wrap, 
//							  boolean mipmapped,
//							  boolean powerOfTwo) throws IOException 
//	{ 
//		Texture2DJOGL texture = (Texture2DJOGL) table.get(name);
//    
//		if (texture != null)		return texture;
//    
//		System.out.println("Loading texture "+resourceName);
//		 texture = new Texture2DJOGL(name, resourceName); 
// 
//		BufferedImage bufferedImage = loadImage(resourceName); 
//		texture.setBufferedImage(bufferedImage); 
//      
//		// Getting the real Width/Height of the Texture in the Memory
//		int realWidth = bufferedImage.getWidth(); 
//		int realHeight = bufferedImage.getHeight();
//		if (powerOfTwo)	{
//			realWidth = get2Fold(bufferedImage.getWidth()); 
//			realHeight = get2Fold(bufferedImage.getHeight());
//		}
//    
//		texture.setWidth(realWidth);
//		texture.setHeight(realHeight);
//		texture.setWrapMode(wrap);
//		texture.setMinFilter(minFilter);
//		texture.setMagFilter(magFilter);
//    
//		ByteBuffer textureBuffer = convertImageData(bufferedImage,texture); 
//    
//		texture.setByteBuffer(textureBuffer); 
//		table.put(name,texture);
//    
//		return texture;
//		
//	}
   private int get2Fold(int fold) {
	  int ret = 2;
	  while (ret < fold) {
		 ret *= 2;
	  }
	  return ret;
   }

//   	private BufferedImage loadImage(String ref) throws IOException 
//	{ 
//	  File file = new File(ref);
//        
//		if (!file.isFile()) {
//			throw new IOException("Cannot find: "+ref);
//		}
//        
//		BufferedImage bufferedImage = ImageIO.read(new BufferedInputStream(
//		   new FileInputStream(file))
//		); 
// 
//		return bufferedImage;
//	} 
//	
//	private ByteBuffer convertImageData(BufferedImage bufferedImage) throws IOException 
//	{ 
//		ByteBuffer imageBuffer = null; 
//		WritableRaster raster;
//		BufferedImage texImage;
//        
//		int texWidth = get2Fold(bufferedImage.getWidth());
//		int texHeight = get2Fold(bufferedImage.getHeight());
//        
//		if (bufferedImage.getColorModel().hasAlpha()) {
//			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
//			texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable());
//		} else {
//			raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
//			texImage = new BufferedImage(glColorModel,raster,false,new Hashtable());
//		}
//        
//		Graphics g = texImage.getGraphics();
//		g.drawImage(bufferedImage,0,0,null);
//        
//		byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData(); 
//
//		imageBuffer = ByteBuffer.allocateDirect(data.length); 
//		imageBuffer.order(ByteOrder.nativeOrder()); 
//		imageBuffer.put(data, 0, data.length); 
//
//		return imageBuffer; 
//	} 
//    
	}


