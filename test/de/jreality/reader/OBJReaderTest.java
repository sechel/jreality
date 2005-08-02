/*
 * Created on 28-Feb-2005
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

import java.io.File;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;

import junit.framework.TestCase;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class OBJReaderTest extends TestCase {

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(OBJReaderTest.class);
    }
    
    public void testOBJReader() throws Exception {
        //String fileName = "/home/gollwas/bolt1.obj";
        //String fileName = "/home/gollwas/cube2.obj";
        String fileName = "/home/gollwas/obj/square.obj";
        SceneGraphComponent sgc = new ReaderOBJ().read(new File(fileName)); 
        System.out.println(sgc);
    }
    
    public void testASEReader() throws Exception {
        String fileName = "/home/gollwas/d3/models/sfb288/wenteBubble.ASE";
        SceneGraphComponent sgc = new ReaderASE().read(new File(fileName)); 
    }

//    public void test3DSReader() throws Exception {
//        String fileName = "/home/gollwas/3ds/tetranoid_0_7.3ds";
//        SceneGraphComponent sgc = new Reader3DS().read(new File(fileName)); 
//    }

    public void testMTLReader() throws Exception {
        String fileName = "/home/gollwas/Buddy-Mesh.mtl";
        ParserMTL.readAppearences(Input.getInput(new File(fileName)));
    }

}
