/*
 * Created on 07.09.2004
 *
 * This file is part of the de.jreality.reader package.
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

import java.io.*;
import java.util.logging.Logger;

import de.jreality.geometry.GeometryUtility;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.LoggingSystem;

/**
 * A very rough parsing of pov files that have been written with jScene.
 * 
 * @version 1.0
 * @author timh
 *
 * @deprecated use ReaderPOV instead
 */
public class PovReader {

    final boolean useCylinders;
    private static int UNIT_DISK_DETAIL = 16;
    public PovReader() {
        this(true);
    }
    
    public PovReader(boolean useCylinders) {
        super();
        this.useCylinders =useCylinders;
    }

    public static SceneGraphComponent readFromFile( String fileName) {
        return readFromFile(fileName, true);
    }
        public static SceneGraphComponent readFromFile( String fileName, boolean useCylinders) {
        
        SceneGraphComponent result = null;
        try {
            FileInputStream inputStream = null;
            inputStream = new FileInputStream( fileName );
            result = load(inputStream,useCylinders);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param inputStream
     * @return
     */
    private static SceneGraphComponent load(FileInputStream inputStream, boolean useCylinders) {
        Reader r = new BufferedReader(new InputStreamReader(inputStream));
        SceneGraphComponent disk=new SceneGraphComponent();
        if(useCylinders){
            disk.setGeometry(new Cylinder());           
        } else {
            disk.setGeometry(new UnitDisk(UNIT_DISK_DETAIL));
        }
        Transformation t =new Transformation();
        t.setStretch(1,1,.02);
        disk.setTransformation(t);
        
        StreamTokenizer st = new StreamTokenizer(r);
        
        st.ordinaryChar('{');
        st.ordinaryChar('}');
        st.parseNumbers();

        
        SceneGraphComponent root = new SceneGraphComponent();
        SceneGraphComponent current= null;
        int bc = 0;
        int oc =0;
        LoggingSystem.getLogger(PovReader.class).fine("start.");
        try {
            while(st.ttype != StreamTokenizer.TT_EOF) {
                st.nextToken();
                //System.out.println("next");
                if(st.ttype ==StreamTokenizer.TT_WORD && st.sval.equals("object")) {
                    while (st.ttype !='{')
                        st.nextToken();
                    oc =bc;
                    current =new SceneGraphComponent();
                    //System.out.println("found object!");
                }
                if(st.ttype =='{') bc++;
                if(st.ttype =='}') bc--;
                //System.out.println("bc "+bc);
                if(bc == oc&& current !=null) {
                    root.addChild(current);
                    current = null;
                    //System.out.println("did object");
                }
                if(st.ttype ==StreamTokenizer.TT_WORD && st.sval.equals("Disk")&& current !=null) {
                        current.addChild(disk);
                        //System.out.println("found Disk!");
                    }
                    
                    if(st.ttype ==StreamTokenizer.TT_WORD && st.sval.equals("matrix")&& current !=null) {
                        current.setTransformation(readMatrix(st));
                        //System.out.println("found matrix!");
                    }
                    
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LoggingSystem.getLogger(PovReader.class).fine("made "+root.getChildComponentCount()+" components");
        LoggingSystem.getLogger(PovReader.class).fine("done.");
        return root;
    }

    /**
     * @param st
     * @return
     */
    private static Transformation readMatrix(StreamTokenizer st) throws IOException {
        double[] d =new double[12];
        double[] m =new double[16];
//        while(st.ttype !=StreamTokenizer.TT_WORD && !st.sval.equals("<"))
//            st.nextToken();
        for(int i = 0;i<12;i++) {
            int b =0;
            while(st.ttype !=StreamTokenizer.TT_NUMBER&& b<40) {
                st.nextToken();
                b++;
                //System.out.println("nonumber");
            }
            if(b==40) LoggingSystem.getLogger(PovReader.class).fine("Error number "+i+" was aborted due to recursion.");
            d[i] =st.nval;
            //System.out.println("number "+st.nval);
            st.nextToken();
            if(st.ttype== StreamTokenizer.TT_WORD && st.sval.startsWith("E")){
                int exp = Integer.parseInt(st.sval.substring(1));
                d[i] *= Math.pow(10,exp);
                st.nextToken();
            }
        }
//        while(!(st.ttype ==StreamTokenizer.TT_WORD && st.sval.equals(">"))) {
//            System.out.println("no >");
//            st.nextToken();
//        }
        for(int j =0;j<4;j++)
            for(int i =0;i<3;i++)
                m[j+4*i] =d[i+3*j];
//        m[0+4*0] = 1;
//        m[1+4*1] = 1;
//        m[2+4*2] = 1;
        
//        m[3+4*0] += 100*(Math.random()-.5);
//        m[3+4*1] += 100*(Math.random()-.5);
//        m[3+4*2] += 100*(Math.random()-.5);
        m[3+4*3] = 1;
        Transformation t =new Transformation();
        t.setMatrix(m);
        //print(m);
        return t;
    }
    
    private static class UnitDisk extends IndexedFaceSet {
    public UnitDisk(int detail)  {
        super();
        
        double r= 1;
        double[] vertices = new double[detail * 3 + 3];
        double[] normals  = new double[detail * 3 + 3];
        //int[][] faces =new int[detail][3];
        int[] faces =new int[detail*3];
        compute(vertices,normals, faces);
        
        setVertexCountAndAttributes(Attribute.COORDINATES,
                StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(vertices));
        //setVertexAttributes(Attribute.NORMALS,
        //        StorageModel.DOUBLE_ARRAY.inlined(3).createReadOnly(normals));
        setFaceCountAndAttributes(Attribute.INDICES,
                StorageModel.INT_ARRAY.inlined(3).createReadOnly( faces));
        buildEdgesFromFaces();
        GeometryUtility.calculateAndSetFaceNormals(this);
        GeometryUtility.calculateAndSetVertexNormals(this);
    }

    private void compute(double[] vertices, double[] normals, int[] faces) {
        // The disk:
        int k =UNIT_DISK_DETAIL;
        for (int i = 0 ; i < k ; i++) {
            
            faces[3*i+0] = (i);
            faces[3*i+1] = ( ((i+1)%k));
            faces[3*i+2] = (k);


            double theta = 2 * Math.PI * i / k;
            double cosT = Math.cos(theta);
            double sinT = Math.sin(theta);

            int pos = 3*(i);           
            vertices[pos+0] = cosT;
            vertices[pos+1] = sinT;
            vertices[pos+2] =0;
            normals[pos+0] =0 ;
            normals[pos+1] =0 ;
            normals[pos+2] = 1;
        }
        vertices[3*(k)] = 0;
        vertices[3*(k)+1] = 0;
        vertices[3*(k)+2] = 0;
        
        normals[3*(k)] = 0;
        normals[3*(k)+1] = 0;
        normals[3*(k)+2] = 1;

        }
    
    }
    
//    public static void print(double[] d) {
//        for (int i= 0; i < 4; i++) {
//            System.out.println("| "+d[0+4*i]+"\t "+d[1+4*i]+"\t "
//                    +d[2+4*i]+"\t "+d[3+4*i]+ "\t |");
//        }
//    }
}
