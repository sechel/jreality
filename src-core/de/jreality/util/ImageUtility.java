package de.jreality.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.beans.Statement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.imageio.ImageIO;


public class ImageUtility {

	private ImageUtility() {}

	public static void writeBufferedImage(File file, BufferedImage img) {
		String suffix = getFileSuffix(file);
		System.err.println("Suffix is "+suffix);
		if ("tiff".equals(suffix) || "tif".equals(suffix)) {
			try {
				  Class encParamClass = Class.forName("com.sun.media.jai.codec.TIFFEncodeParam");
				  
			      Object encodeParam = encParamClass.newInstance();
			      Object compField = encParamClass.getField("COMPRESSION_DEFLATE").get(null);
			      
			      new Statement(encodeParam, "setCompression", new Object[]{compField}).execute();
			      new Statement(encodeParam, "setDeflateLevel", new Object[]{9}).execute();
			      
			      ParameterBlock pb = new ParameterBlock();
			      pb.addSource(img);
			      pb.add(new FileOutputStream(file));
			      pb.add("tiff");
			      pb.add(encodeParam);
			      
				  new Statement(Class.forName("javax.media.jai.JAI"), "create", new Object[]{"encode", pb}).execute();
			} catch(Throwable e) {
				throw new RuntimeException("need JAI for tiff writing", e);
			}
		} else {
			try {
				if (suffix != "")
				    if (!ImageIO.write(img, getFileSuffix(file), file)) {
					    LoggingSystem.getLogger(ImageUtility.class).log(Level.WARNING,"Error writing file using ImageIO (unsupported file format?)");
				}
			} catch (IOException e) {
				throw new RuntimeException("image writing failed", e);
			}
		}
	}

	public static BufferedImage resizeToPowerOfTwo(BufferedImage input)	{
		BufferedImage output;
		int w = input.getWidth();
		int h = input.getHeight();
		int w2 = 1;
		while (w2 < w)	w2 *= 2;
		int h2 = 1;
		while (h2 < h) h2 *= 2;
		System.err.println("input type is "+input.getType());
		output = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) output.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		output.getGraphics().drawImage(input, 0, 0, w2, h2, null);
		return output;
	}
	private static String getFileSuffix(File file) {
		int lastDot = file.getName().lastIndexOf('.');
		if (lastDot == -1) return "png";
		return file.getName().substring(lastDot+1);
	}

}
