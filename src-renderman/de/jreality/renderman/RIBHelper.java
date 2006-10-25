package de.jreality.renderman;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import de.jreality.math.Rn;
import de.jreality.renderman.shader.DefaultPolygonShader;
import de.jreality.shader.ImageData;
import de.jreality.util.LoggingSystem;

public class RIBHelper {

	public static void writeShader(String name, String shaderName ) {
		try {
		    File file = new File(name);
		    System.out.println("writing in  "+name);
		    file = new File(file.getParent(),shaderName);
		    System.out.println("checking on "+file+" exists "+file.exists());
		    if(!file.exists()) {
		    	OutputStream os = new FileOutputStream(file);
		    	InputStream is = DefaultPolygonShader.class.getResourceAsStream(shaderName);
		    
		    	int c = 0;
		    	while((c =is.read())!=-1) {
		    		os.write(c);
		    	}
		    	os.close();
		    	is.close();
		    }
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	/**
	 * @param cam
	 * @return
	 */
	public static float[] fTranspose(double[] mat) {
	    float[] tmat = new float[16];
	    for (int i = 0; i < 4; i++) 
	        for (int j = 0;j<4;j++){
	            tmat[i + 4*j] = (float) mat[j+4*i];
	        }
	    return tmat;
	}

	public static String str(String name) {
	    return "\""+name+"\"";
	}

	/**
	 * @param w2
	 * @param map
	 */
	public static void writeMap(PrintWriter w2, Map map) {
	    if(map!=null) {
	    Set keys = map.keySet();
	        for (Iterator key = keys.iterator(); key.hasNext();) {
	            String element = (String) key.next();
	            w2.print("\""+ element+"\" ");
	            RIBHelper.writeObject(w2,map.get(element));
	        }
	    }
	    w2.println("");
	}

	/**
	 * @param w2
	 * @param object
	 */
	public static void writeObject(PrintWriter w2, Object object) {
	    if(object instanceof double[]) {
	    	object = Rn.convertDoubleToFloatArray((double[]) object);
	    }
	    if(object instanceof float[]) {
	        float[] f = (float[]) object;
	        w2.print("[");
	        for (int i = 0; i < f.length; i++) {
	            w2.print(f[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	    if(object instanceof int[]) {
	        int[] f = (int[]) object;
	        w2.print("[");
	        for (int i = 0; i < f.length; i++) {
	            w2.print(f[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	    if(object instanceof Color) {
	        w2.print("[");
	        float[] rgb = ((Color)object).getRGBComponents(null);
	        for (int i = 0; i < 3; i++) {
	            w2.print(rgb[i]+" ");
	        }
	        w2.print("]");
	        return;
	    }
	
	    if(object instanceof String) {
	        w2.print("\""+object+"\"");
	        return;
	    }
	    //if(object instanceof Float)
	    w2.print(" "+object+" ");
	}
  
  
  public static void writeTexture(ImageData data, String noSuffix, boolean transparencyEnabled){
    
    BufferedImage img;
	  for (Iterator iter = ImageIO.getImageWritersByMIMEType("image/tiff"); iter.hasNext(); ) {
	   System.err.println("Writer: "+((ImageWriter) iter.next()).getClass().getName());
	  }
	        
	  if (true) {
	   // TODO temporary as long as ImageData does not return a propper BufferedImage
	   byte[] byteArray = data.getByteArray();
	   int dataHeight = data.getHeight();
	   int dataWidth = data.getWidth();
	   img = new BufferedImage(dataWidth, dataHeight,
	   BufferedImage.TYPE_INT_ARGB);
	   WritableRaster raster = img.getRaster();
	   int[] pix = new int[4];
         for (int y = 0, ptr = 0; y < dataHeight; y++) {
           for (int x = 0; x < dataWidth; x++, ptr += 4) {
             if (transparencyEnabled) pix[3] = byteArray[ptr + 3];
             else pix[3] = (byte) 255;
             pix[0] = byteArray[ptr];
             pix[1] = byteArray[ptr + 1];
             pix[2] = byteArray[ptr + 2];
             raster.setPixel(x, y, pix);
           }
         }                      
	  } else {
	   img = (BufferedImage) data.getImage();
	  }
	  // force alpha channel to be "pre-multiplied"
	  img.coerceData(true);
	
	  boolean worked=true;
		try {
					// TODO: !!!
					//worked = ImageIO.write(img, "TIFF", new File(noSuffix+".tiff"));
		  Method cm = Class.forName("javax.media.jai.JAI").getMethod("create", new Class[]{String.class, RenderedImage.class, Object.class, Object.class});
		  cm.invoke(null, new Object[]{"filestore", img, noSuffix+".tiff", "tiff"});
	//				Statement stm = new Statement(, "create", new Object[]{"filestore", img, noSuffix+".tiff", "tiff"});
	//				stm.execute();
		} catch(Throwable e) {
		worked=false;
		LoggingSystem.getLogger(RIBVisitor.class).log(Level.CONFIG, "could not write TIFF: "+noSuffix+".tiff", e);
		}
	  if (!worked) {
	    try {
			 worked =ImageIO.write(img, "PNG", new File(noSuffix+".png"));
	    } catch (IOException e) {
					// TODO Auto-generated catch block
	      e.printStackTrace();
			}
	    if (!worked) 
	     LoggingSystem.getLogger(RIBVisitor.class).log(Level.CONFIG, "could not write PNG: {0}.png", noSuffix);
	  }
	}
  

}
