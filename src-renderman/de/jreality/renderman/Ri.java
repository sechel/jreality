/*
 * Created on 06.05.2004
 *
 * This file is part of the de.jreality.renderman package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.renderman;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    public static  void end() {
        w.close();
    }
    
    public static void option(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//       option(name, tokens, values);
        w.print("Option "+str(name)+" ");
        writeMap(w, map);
        
    }
//    public static  void option(String name, String[] tokens, Object[] values);
    
    
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
    
    public static void surface(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        surface(name, tokens, values);
        w.print("Surface "+str(name)+" ");
        writeMap(w,map);
    }
//    public static  void surface(String name, String[] tokens, Object[] values);
    
    public static void imager(String name, Map map) {
//        String[] tokens = keysFromMap(map);
//        Object[] values = valuesFromMap(map, tokens);
//        imager(name, tokens, values);
        w.print("Imager "+str(name)+" ");
        writeMap(w,map);
    }
//    public static  void imager(String name, String[] tokens, Object[] values);
    
    public static  void color(float[] color) {
        w.print("Color ");
        writeObject(w,color);
        w.println("");
    }
    
    public static  void opacity(float[] color) {
        w.print("Opacity ");
        writeObject(w,color);
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
        w.print("Points "+" "+npoints+" ");
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
    
}
