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


package de.jreality.reader;

import java.io.File;

import junit.framework.TestCase;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;


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
