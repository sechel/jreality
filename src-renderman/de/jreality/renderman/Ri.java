/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.renderman;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Rn;
import de.jreality.renderman.shader.RendermanShader;

/**
 * 
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class Ri {

    /**
     * 
     */
    public Ri() {
        super();
    }

    public static final int BEZIER_BASIS = 0;
    public static final int BSPLINE_BASIS = 1;
    public static final int CATMULL_ROM_BASIS = 2;
    public static final int HERMITE_BASIS = 3;
    public static final int POWER_BASIS = 4;
    private static PrintWriter w;
    private static int lightCount;
    
    public static  void begin(String name) {
        try {
             w =new PrintWriter(new FileWriter(new File(name)));
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        lightCount=0;
    }
    public static  void begin(File file) {
        try {
            w =new PrintWriter(new FileWriter(file));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        lightCount=0;
    }
    public static  void end() {
        w.close();
    }
    
    public static void verbatim(String s)	{
    	w.println(s);
    }
    public static void comment(String s) {
        String[] ss = s.split("\\n");
        for (int i = 0; i < ss.length; i++) {
            w.println("# "+ss[i]);
        }
    }
    public static void option(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//       option(name, tokens, values);
        w.print("Option "+str(name)+" ");
        writeMap(w, map);
        
    }
//    public static  void option(String name, String[] tokens, Object[] values);
    
    public static void attribute(String name, Map map) {
//      String[] tokens = keysFromMap(map);
//      Object[] values = valuesFromMap(map, tokens);
//     option(name, tokens, values);
      w.print("Attribute "+str(name)+" ");
      writeMap(w, map);
      
  }
//  public static  void attribute(String name, String[] tokens, Object[] values);

    /**
     * @param name
     * @return
     */
    private static String str(String name) {
        return "\""+name+"\"";
    }
    /**
     * @param w2
     * @param map
     */
    private static void writeMap(PrintWriter w2, Map map) {
        if(map!=null) {
        Set keys = map.keySet();
            for (Iterator key = keys.iterator(); key.hasNext();) {
                String element = (String) key.next();
                w2.print("\""+ element+"\" ");
                writeObject(w2,map.get(element));
            }
        }
        w.println("");
    }
    /**
     * @param w2
     * @param object
     */
    private static void writeObject(PrintWriter w2, Object object) {
        //TODO:This will be tricky...
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
    public static void display(String name, String type, String mode, Map map ) {
//        String[]  tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        display(name, type, mode, tokens, values);
        w.print("Display "+str(name)+" "+str(type)+" "+str(mode)+" ");
        writeMap(w,map);
    }
//    public static  void display(String name, String type, String mode, String[] tokens, Object[] values );
    
    public static  void format(int xresolution, int yresolution, float pixelaspectratio) {
        w.println("Format "+xresolution+" "+yresolution+" "+pixelaspectratio);
    }
    
    public static  void shadingRate(float rate) {
        w.println("ShadingRate "+rate);
    }
    
	public static void clipping(double near, double far) {
		w.println("Clipping "+near+" "+far);
	}
   public static void projection(String name, Map map) {
//        String[]  tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        projection(name, tokens, values);
        w.print("Projection "+str(name)+" ");
        writeMap(w,map);
    }
//    public static  void projection(String name,String[] tokens, Object[] values);
    
    public static int lightSource(String name, Map map) {
//        String[]  tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        return lightSource(name, tokens, values);
        lightCount++;
        w.print("LightSource "+str(name)+" "+lightCount+" ");
        writeMap(w,map);
        return lightCount;
    }
//    public static  int lightSource(String name, String[] tokens, Object[] values);
    
    public static  void worldBegin() {
        w.println("WorldBegin");
    }
    public static  void worldEnd() {
        w.println("WorldEnd");
    }
    public static  void frameBegin(int n) {
        w.println("FrameBegin "+n);
    }
    public static  void frameEnd() {
        w.println("FrameEnd");
    }
    public static  void attributeBegin() {
        w.println("AttributeBegin");
    }
    public static  void attributeEnd() {
        w.println("AttributeEnd");
    }
    public static  void transformBegin() {
        w.println("TransformBegin");
    }
    public static  void transformEnd() {
        w.println("TransformEnd");
    }
    public static  void archiveBegin(String name) {
        w.println("ArchiveBegin " +str(name));
    }
    public static  void archiveEnd() {
        w.println("ArchiveEnd");
    }
	public static void readArchive(String foo) {
		w.println("ReadArchive "+str(foo));
	}

    public static void surface(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        surface(name, tokens, values);
        w.print("Surface "+str(name)+" ");
        writeMap(w,map);
    }
//    public static  void surface(String name, String[] tokens, Object[] values);
    
    public static void displacement(String name, Map map) {
//      String[] tokens = keysFromMap(map);
//      Object[] values = valuesFromMap(map, tokens);
//      surface(name, tokens, values);
      w.print("Displacement "+str(name)+" ");
      writeMap(w,map);
  }
//  public static  void displacement(String name, String[] tokens, Object[] values);

    
    public static void imager(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        imager(name, tokens, values);
        w.print("Imager "+str(name)+" ");
        writeMap(w,map);
    }
//    public static  void imager(String name, String[] tokens, Object[] values);
 
    public static  void color(Color color) {
        float[] cc = color.getRGBComponents(null);
        color(cc[0], cc[1], cc[2]);
        if (cc.length == 4) opacity(cc[3]);
    }
    
    public static  void color(double[] color) {
    	color((float) color[0], (float) color[1], (float) color[2]);
    	if (color.length == 4) opacity((float) color[3]);
    }
    
   public static void color(float r, float g, float b){
    	color( new float[]{r,g,b});
    }
    public static  void color(float[] color) {
        w.print("Color ");
        writeObject(w,color);
        w.println("");
       if (color.length == 4) opacity(color[3]);
    }
    
    public static  void opacity(float[] color) {
        w.print("Opacity ");
        writeObject(w,color);
        w.println("");
    }
    
    public static  void opacity(float color) {
        w.print("Opacity ");
        float[] opa = {color, color, color};
        writeObject(w, opa);
        w.println("");
    }
    
   public static  void concatTransform(float[] transform) {
        w.print("ConcatTransform ");
        writeObject(w,transform);
        w.println("");
    }
    public static  void transform(float[] transform) {
        w.print("Transform ");
        writeObject(w,transform);
        w.println("");
    }
    public static  void identity() {
        w.println("Identity");
    }
    
    
    public static void sphere(float radius, float zmin, float zmax, float thetamax, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        sphere(radius, zmin,zmax, thetamax, tokens,values);
        w.print("Sphere "+radius+" "+zmin+" "+zmax+" "+thetamax+" ");
        writeMap(w,map);
    }
    
//    public static  void sphere(float radius, float zmin, float zmax, float thetamax, String[] tokens, Object[] values) ;

    public static void cylinder(float radius, float zmin, float zmax, float thetamax, Map map) {
        w.print("Cylinder "+radius+" "+zmin+" "+zmax+" "+thetamax+" ");
        writeMap(w,map);
    }
    
    public static void disk(float z, float radius, float thetamax, Map map) {
        w.print("Disk "+z+" "+radius+" "+thetamax+" ");
        writeMap(w,map);
    }
    
    public static void clippingPlane(float x, float y, float z, float nx, float ny, float nz) {
        w.print("ClippingPlane "+x+" "+y+" "+z+" "+nx+" "+ny+" "+nz+"\n");
    }

    public static void points(int npoints, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        points(npoints,tokens,values);
//        w.print("Points "+" "+npoints+" ");
        w.print("Points ");
        writeMap(w,map);
    }
//    public static  void points(int npoints, String[] tokens, Object[] values) ;
    
    public static void pointsPolygons(int npolys,int[] nvertices,int[] vertices,Map map) {
        w.print("PointsPolygons ");
        writeObject(w,nvertices);
        w.print(" ");
        writeObject(w,vertices);
        w.print(" ");
        writeMap(w,map);
    }
    
    public static void curves(String type, int ncurves,int[] nvertices, String wrap, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        curves(type,ncurves,nvertices,wrap,tokens,values);
        w.print("Curves "+str(type)+" "+ncurves+" ");
        writeObject(w,nvertices);
        w.print(" "+wrap+" ");
        writeMap(w,map);
    }
//    public static  void curves(String type, int ncurves,int[] nvertices, String wrap, String[] tokens, Object[] values);
    
    public static  void basis(int ubasis, int ustep, int vbasis, int vstep) {
        w.print("Basis "+ubasis+" "+ustep+" "+vbasis+" "+vstep);
    }
    //
    // utility methods:
    //
    private static String[] keysFromMap(Map map) {
        if(map == null) return new String[0];
        Object[] oa = map.keySet().toArray();
        String[] sa =new String[oa.length];
        for (int i = 0; i < sa.length; i++) {
            sa[i] =(String) oa[i];
        }
        return sa;
    }
    private static Object[] valuesFromMap(Map map, String[] tokens) {
        if(map == null) return new Object[0];
        Object[] values = new Object[map.size()];
        for (int i = 0; i < tokens.length; i++) {
            values[i] = map.get(tokens[i]);
            
        }
        return values;
    }
    
    private static KeyValArrays tokensValuesFromMap(Map map) {
        KeyValArrays ret = new KeyValArrays();
        if(map == null) {
            ret.tokens = new String[0];
            ret.values = new Object[0];
            return ret;
        }
        else {
            ret.tokens = (String[]) map.keySet().toArray();
            ret.values = new Object[map.size()];
            for (int i = 0; i < ret.tokens.length; i++) {
                ret.values[i] = map.get(ret.tokens[i]);
            }
            return ret;
        }
    }
    static final class KeyValArrays {
        String[] tokens;
        Object[] values;
    }
	public static void shader(RendermanShader sh) {
        w.print(sh.getType()+" "+str(sh.getName())+" ");
        writeMap(w,sh.getAttributes());

		
	}
    
}
