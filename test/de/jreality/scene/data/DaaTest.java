/*
 * Created on 16-Jan-2005
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
package de.jreality.scene.data;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import junit.framework.TestCase;

public class DaaTest extends TestCase {

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(DaaTest.class);
    }

    public void testNormal() throws Exception {
        int runCre=0, runAc=0, 
        runSer=0, runOv=0, runTB=100,
        completeRuns=5;
        CatenoidHelicoid ch = new CatenoidHelicoid(50);
        double[][] data = GeometryUtility.calculateVertexNormals(ch);
        DaaNormal daan = new DaaNormal(data);
        DaaInlined daai = new DaaInlined(data);
        DaaInlinedNIO daaib = new DaaInlinedNIO(data);
        assertEquals(daan, daai);
        assertEquals(daan, daaib);
        ByteBuffer bb = ByteBuffer.allocateDirect(1024*1024).order(ByteOrder.nativeOrder());
        for (int i = 0; i < completeRuns; i++) {
            runCreate(data, runCre);
            runOverwrite(daan, daaib, runOv);
            runOverwrite(daai, daaib, runOv);
            runOverwrite(daaib, daaib, runOv);
            runAccess(daan, runAc);
            runAccess(daai, runAc);
            runAccess(daaib, runAc);
//            runSerialize(daan, runSer);
//            runSerialize(daai, runSer);
//            runSerialize(daaib, runSer);
            runToByteBuffer(daan, bb, runTB);
            runToByteBuffer(daai, bb, runTB);
            runToByteBuffer(daaib, bb, runTB);
        }
    }
    
    private void runToByteBuffer(Daa array, ByteBuffer bb, int runs) {
        if (runs == 0) return;
        System.out.println(array.getClass().getName());
        long cts=0;
        long s,t;
        for (int i = 0; i < runs; i++) {
            s = System.currentTimeMillis();
            array.toByteBuffer(bb);
            t = System.currentTimeMillis() - s;
            cts+=t;
            bb.clear();
        }
        System.out.println("\ttoByteBuffer="+(cts/((double)runs)));
    }
    
    private void runCreate(double[][] array, int runs) {
        if (runs == 0) return;
        long tN=0,tI=0,tIN=0;
        long st, t;
        st = System.currentTimeMillis();
        for (int run = 0; run < runs; run++) {
            new DaaInlinedNIO(array);
        }
        tIN=System.currentTimeMillis()-st;
        st = System.currentTimeMillis();
        for (int run = 0; run < runs; run++) {
           new DaaNormal(array);          
        }
        tN=System.currentTimeMillis()-st;
        st = System.currentTimeMillis();
        for (int run = 0; run < runs; run++) {
           new DaaInlined(array);          
        }
        tI=System.currentTimeMillis()-st;
        System.out.println("creation normal: "+tN+" ["+tN/((double)runs)+"]");
        System.out.println("creation inlined: "+tI+" ["+tI/((double)runs)+"]");
        System.out.println("creation inlinedNIO: "+tIN+" ["+tIN/((double)runs)+"]");
    }

    private void runAccess(Daa array, int runs) {
        if (runs == 0) return;
       System.out.println(array.getClass().getName());
       Random r = new Random();
       long tLa=0,tVa=0,tIt=0;
       long st;
      for (int run = 0; run < runs; run++) { // test getLength() access
            
            final int[] rands = new int[10000]; // 100000 random accesses
            int[] ind=new int[rands.length];

            // create random indices
            for (int i = 0; i < rands.length; i++)
                rands[i] = r.nextInt(array.getLength());
                        
            // check getLengthAt performance
            st = System.currentTimeMillis();
            for (int i = 0; i < rands.length; i++) {
                ind[i] = array.getLengthAt(rands[i]);
            }
            tLa += System.currentTimeMillis()-st;
            
            // create random indices per sub-array
            for (int i = 0; i < rands.length; i++)
                ind[i] = r.nextInt(ind[i]);

            // check getValueAt performance
            st = System.currentTimeMillis();
            for (int j = 0; j < rands.length; j++) {
                double k = array.getValueAt(rands[j], ind[j]);
            }
            tVa += System.currentTimeMillis()-st;
            
            // check iteration performance
            double k;
            st = System.currentTimeMillis();
            for (int i = 0; i < array.getLength(); i++)
                for (int j = 0; j < array.getLengthAt(i); j++)
                    k = array.getValueAt(i,j);
            tIt += System.currentTimeMillis()-st;

        }
        System.out.println("\tgetLengthAt: "+tLa+" ms ["+(tLa/(double)runs)+"]");
        System.out.println("\tgetValueAt: "+tVa+" ms ["+(tVa/(double)runs)+"]");
        System.out.println("\titerate: "+tIt+" ms ["+(tIt/(double)runs)+"]");
    }

    public void runSerialize(Daa array, int runs) throws Exception {
        if (runs == 0) return;
        long cts=0, ctd=0;
        System.out.println(array.getClass().getName());
        for (int i = 0; i < runs; i++) {
            ByteArrayOutputStream bb = new ByteArrayOutputStream(10000);
            ObjectOutputStream os = new ObjectOutputStream(bb);
            long s = System.currentTimeMillis();
            os.writeObject(array);
            long t = System.currentTimeMillis() - s;
            cts+=t;
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(bb.toByteArray()));
            s = System.currentTimeMillis();
            Daa dls2 = (Daa) is.readObject();
            t = System.currentTimeMillis() - s;
            ctd+=t;
            assertEquals(array, dls2);
        }
        System.out.println("\tserial="+(cts/((double)runs))+" deser="+(ctd/((double)runs)));
    }
    public void runOverwrite(Daa target, Daa src, int runs) throws Exception {
        if (runs == 0) return;
        System.out.println(target.getClass().getName());
        long cts=0;
        long s,t;
        for (int i = 0; i < runs; i++) {
            s = System.currentTimeMillis();
            target.overwriteData(src);
            t = System.currentTimeMillis() - s;
            cts+=t;
        }
        System.out.println("\toverwrite="+(cts/((double)runs)));
    }
}
