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
		//boolean worked=true;
		System.err.println("Writing to file "+file.getPath());
		if (file.getName().endsWith(".tiff") || file.getName().endsWith(".tif")) {
			try {
				// TODO: !!!
				//worked = ImageIO.write(img, "TIFF", new File(noSuffix+".tiff"));
				Method cm = Class.forName("javax.media.jai.JAI").getMethod("create", new Class[]{String.class, RenderedImage.class, Object.class, Object.class});
				cm.invoke(null, new Object[]{"filestore", img, file.getPath(), "tiff"});
			} catch(Throwable e) {
//				//worked=false;
//				LoggingSystem.getLogger(this).log(Level.CONFIG, "could not write TIFF: "+file.getPath(), e);
				e.printStackTrace();
			}
		} else {
			//if (!worked)
			try {
				String suffix = getFileSuffix(file);
				System.err.println("suffix is "+suffix);
				if (suffix != "")
				    if (!ImageIO.write(img, getFileSuffix(file), file)) {
					    LoggingSystem.getLogger(ImageUtility.class).log(Level.WARNING,"Error writing file using ImageIO (unsupported file format?)");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getFileSuffix(File file) {
		int lastDot = file.getName().lastIndexOf('.');
		if (lastDot == -1) return "png";
		return file.getName().substring(lastDot+1);
	}

}
