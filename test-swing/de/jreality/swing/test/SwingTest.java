/*
 * Created on 11.02.2006
 *
 * This file is part of the de.jreality.swing.test package.
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
package de.jreality.swing.test;

import java.io.File;
import java.io.IOException;

import javax.swing.JButton;

import de.jreality.examples.CatenoidHelicoid;
import de.jreality.examples.PaintComponent;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.swing.JRJComponent;
import de.jreality.ui.viewerapp.ViewerApp;
public class SwingTest {


    /**
     * @param args
     */
    public static void main(String[] args) {
       PaintComponent pc = new PaintComponent();

       CatenoidHelicoid catenoid = new CatenoidHelicoid(50);
       catenoid.setAlpha(Math.PI/2.-0.3);
       
       SceneGraphComponent catComp= new SceneGraphComponent();
       Transformation gt= new Transformation();

       catComp.setTransformation(gt);
       catComp.setGeometry(catenoid);
       SceneGraphComponent c;
//    try {
//        c = de.jreality.reader.Readers.read(new File(args[0]));
//       catComp.setGeometry(c.getChildComponent(0).getGeometry());
//    } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//       try {
//        catComp = 
//               de.jreality.reader.Readers.read(new File("/home/timh/jRdata/testData3D/obj/baer10000_punched_vt.obj"));
//    } catch (IOException e) {
//        e.printStackTrace();
//    }
//        catComp.getChildComponent(0).setAppearance(null);
       // AABBPickSystem does this authomatically now:
       //PickUtility.assignFaceAABBTree(catenoid);
   
       
       JRJComponent jrj = new JRJComponent();
       jrj.add(new JButton("my button"));
       
       catComp.addTool(jrj.getTool());
       
    System.out.print("setting appearance ");
       catComp.setAppearance(jrj.getAppearance());
       System.out.println("done");
       ViewerApp.display(catComp);
       
    }

}
