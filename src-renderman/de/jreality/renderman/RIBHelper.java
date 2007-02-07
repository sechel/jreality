package de.jreality.renderman;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.beans.Statement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

import de.jreality.math.Rn;
import de.jreality.renderman.shader.DefaultPolygonShader;
import de.jreality.renderman.shader.RendermanShader;
import de.jreality.renderman.shader.TwoSidePolygonShader;
import de.jreality.scene.Appearance;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.PolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.LoggingSystem;

public class RIBHelper {

	public static RendermanShader processPolygonShader(PolygonShader ps, RIBVisitor ribv, String name)	{
		RendermanShader rs = null;
		Color Cs = null;
		double transparency = 0.0;
		if (ps instanceof de.jreality.shader.DefaultPolygonShader)	{
//			System.err.println("processing defaultpolygonshader");
			de.jreality.shader.DefaultPolygonShader dps = (de.jreality.shader.DefaultPolygonShader) ps;
			DefaultPolygonShader rdps = new DefaultPolygonShader(dps);
			rdps.setFromEffectiveAppearance(ribv, ribv.eAppearance, name);
			rs = rdps;
			Cs = dps.getDiffuseColor();
			transparency = (float)dps.getTransparency().floatValue();
			ribv.smooth = dps.getSmoothShading();
		} 
		else if (ps instanceof de.jreality.shader.TwoSidePolygonShader)	{
//			System.err.println("processing twosidepolygonshader");
			de.jreality.shader.TwoSidePolygonShader dps = (de.jreality.shader.TwoSidePolygonShader) ps;
			TwoSidePolygonShader rdps = new TwoSidePolygonShader(dps);
			rdps.setFromEffectiveAppearance(ribv, ribv.eAppearance, name);
			rs = rdps;
			de.jreality.shader.DefaultPolygonShader dpss = ((de.jreality.shader.DefaultPolygonShader)dps.getFront());
			Cs = dpss.getDiffuseColor();
			transparency = (float)dpss.getTransparency().floatValue();
			// TODO figure out how to read out a reasonable "smooth" value from this shader
		}
		else {
			LoggingSystem.getLogger(ShaderUtility.class).warning("Unknown shader class "+ps.getClass());
		}
		float[] csos = extractCsOs(Cs, (!(ribv.handlingProxyGeometry && ribv.opaqueTubes) && ribv.transparencyEnabled) ? transparency : 0f);
		ribv.ri.color(csos);
		ribv.ri.shader(rs);
		
		return rs;
	}
	protected static float[] extractCsOs(Color color, double transparency)	{
		float[] csos = new float[4];
		float colorAlpha = 1.0f;
		if (color != Appearance.INHERITED) {
			float[] c = ((Color) color).getRGBComponents(null);
			if (c.length == 4)
				colorAlpha = c[3];
			csos[0] = c[0];
			csos[1] = c[1];
			csos[2] = c[2];
		}

		csos[3] = 1f - (float) transparency;
		// TODO remove this if we decide finally to not allow transparency control via alpha channel of Color
		csos[3] *= colorAlpha;
		return csos;
	}

	public static void writeShader(String name, String shaderName ) {
		try {
		    File file = new File(name);
		    LoggingSystem.getLogger(RIBHelper.class).fine("writing in  "+name);
		    file = new File(file.getParent(),shaderName);
		    LoggingSystem.getLogger(RIBHelper.class).fine("checking on "+file+" exists "+file.exists());
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
             pix[0] = byteArray[ptr];
             pix[1] = byteArray[ptr + 1];
             pix[2] = byteArray[ptr + 2];
             pix[3] = byteArray[ptr + 3]; 
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
		  Class encParamClass = Class.forName("com.sun.media.jai.codec.TIFFEncodeParam");
		  
      Object encodeParam = encParamClass.newInstance();
      Object compField = encParamClass.getField("COMPRESSION_DEFLATE").get(null);
      
      new Statement(encodeParam, "setCompression", new Object[]{compField}).execute();
      //encodeParam.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
      new Statement(encodeParam, "setDeflateLevel", new Object[]{9}).execute();
      //encodeParam.setDeflateLevel(9);
      
      ParameterBlock pb = new ParameterBlock();
      pb.addSource(img);
      pb.add(new FileOutputStream(noSuffix+".tiff"));
      pb.add("tiff");
      pb.add(encodeParam);
      
	  new Statement(Class.forName("javax.media.jai.JAI"), "create", new Object[]{"encode", pb}).execute();
      //JAI.create("encode", pb);

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
