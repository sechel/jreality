package de.jreality.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.imageio.ImageIO;


public class ImageUtility {

	private ImageUtility() {}
	
	public static void writeBufferedImage(File file, BufferedImage img) {
		String suffix = getFileSuffix(file);
		if ("tiff".equals(suffix) || "tif".equals(suffix)) {
			try {
				Method cm = Class.forName("javax.media.jai.JAI").getMethod("create", new Class[]{String.class, RenderedImage.class, Object.class, Object.class});
				cm.invoke(null, new Object[]{"filestore", img, file.getPath(), "tiff"});
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

	private static String getFileSuffix(File file) {
		int lastDot = file.getName().lastIndexOf('.');
		if (lastDot == -1) return "png";
		return file.getName().substring(lastDot+1);
	}

}
