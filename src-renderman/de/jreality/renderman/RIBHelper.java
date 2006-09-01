package de.jreality.renderman;

import java.awt.Color;
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

import de.jreality.math.Rn;
import de.jreality.renderman.shader.DefaultPolygonShader;

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

}
