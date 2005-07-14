/*
 * Created on May 8, 2005
 *
 * This file is part of the jReality package.
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
package de.jreality.reader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Vector;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;


/**
 *
 * Simple parser for polymake files
 *
 * @author weissman
 *
 */
public class ReaderPOLY extends AbstractReader {

  public void setInput(Input input) throws IOException {
    super.setInput(input);
    root = parse(input.getInputStream());
  }
  
  static SceneGraphComponent parse(InputStream is) {
    InputStreamReader r = new InputStreamReader(is);
    LineNumberReader lr = new LineNumberReader(r);
    SceneGraphComponent root = new SceneGraphComponent();

    Vector v=null;
    HashMap map = new HashMap();
    
    String line; 
    try {
        while((line= lr.readLine()) !=null) {
            line = line.trim();
            if(line.equals("")) continue;
            if(Character.isUpperCase(line.charAt(0))) {
                LoggingSystem.getLogger(ReaderPOLY.class).finer(" make entry "+line);
                v = new Vector();
                map.put(line,v);
            } else if (v != null&& !line.equals("")) {
                v.add(line);
            }
        }
    } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    // now we have all the data but still mostly unparsed
    int o = 0;
    Vector vData = ((Vector)map.get("GEOMETRIC_REALIZATION"));
    int n = vData.size();
     double[] vertices = new double[3*n];
     for(int i = 0; i<n;i++) {
         String str = (String) vData.get(i);
         String[] vals = str.split("[\\s\\{\\}/]");
         LoggingSystem.getLogger(ReaderPOLY.class).finer("vals length "+vals.length);
         vertices[3*i  ] = Integer.parseInt(vals[0])/(double)Integer.parseInt(vals[1]);
         vertices[3*i+1] = Integer.parseInt(vals[2])/(double)Integer.parseInt(vals[3]);
         vertices[3*i+2] = Integer.parseInt(vals[4])/(double)Integer.parseInt(vals[5]);
     }
     
     vData = ((Vector)map.get("FACETS"));
     n = vData.size();
     int[][] faces = new int[n][3];
     for(int i = 0; i<n;i++) {
         String str = (String) vData.get(i);
         String[] vals = str.split("[\\s\\{\\}/]");
         LoggingSystem.getLogger(ReaderPOLY.class).finer("face vals length "+vals.length);
         if(vals.length>3 ) o = 1;
         else o = 0;
         faces[i][0] = Integer.parseInt(vals[o+0]);
         faces[i][1] = Integer.parseInt(vals[o+1]);
         faces[i][2] = Integer.parseInt(vals[o+2]);
     }
     
     IndexedFaceSet ifs = new IndexedFaceSet();
     
     ifs.setVertexCountAndAttributes(Attribute.COORDINATES,StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(vertices));
     //ifs.setFaceCountAndAttributes(Attribute.INDICES,new IntArrayArray.Array(faces));
     ifs.setFaceCountAndAttributes(Attribute.INDICES,StorageModel.INT_ARRAY_ARRAY.createReadOnly(faces));
     GeometryUtility.calculateAndSetFaceNormals(ifs);
     GeometryUtility.calculateAndSetVertexNormals(ifs);
     ifs.buildEdgesFromFaces();
     root.setGeometry(ifs);
//     System.out.println("we return "+root+" with geometry "+root.getGeometry());
    return root;
}
}
