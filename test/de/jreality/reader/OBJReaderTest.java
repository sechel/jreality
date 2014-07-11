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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;


/**
 *
 * TODO: comment this
 *
 * @author weissman
 *
 */
public class OBJReaderTest {

	@Before
	public void setUp() {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream( new File("test_logging.properties")));
		} catch (IOException e) {
			System.out.println("File test_logging.properties not found.");
			// You can customize log levels in test_logging.properties file.
		} 
	}

	@Ignore("File not committed")@Test
    public void testOBJReader() throws Exception {
        //String fileName = "/home/gollwas/bolt1.obj";
        //String fileName = "/home/gollwas/cube2.obj";
        URL url = this.getClass().getResource("square.obj");
        SceneGraphComponent sgc = new ReaderOBJ().read(url); 
        Assert.assertEquals("sgc 0", sgc.getName());
        Assert.assertEquals("[len=4 storage=double[][3]]", 
        	sgc.getChildComponent(0).getGeometry().getAttributes(Geometry.CATEGORY_VERTEX, Attribute.COORDINATES).toString());
    }
    
//    public void test3DSReader() throws Exception {
//        String fileName = "/home/gollwas/3ds/tetranoid_0_7.3ds";
//        SceneGraphComponent sgc = new Reader3DS().read(new File(fileName)); 
//    }

	@Test
    public void testMTLReader() throws Exception {
        URL url = this.getClass().getResource("vp.mtl");
        List<?> list = ParserMTL.readAppearences(Input.getInput(url));
        Assert.assertEquals("baerFinal", ((Appearance) list.get(0)).getName());
    }
    
    @Test
	public void testReadInvalidFile() throws Exception {
		File testFile = File.createTempFile("test", "obj");
		testFile.deleteOnExit();
		FileWriter w = new FileWriter(testFile);
		//this content produced infinite loops in the reader
		w.write("g");
		w.close();
		ReaderOBJ reader = new ReaderOBJ();
		reader.read(testFile);
	}
    
    
    @Test public void testReadTextureCoordinates_MultipleFalse() throws Exception {
    	String objData = 
    		"v 0 0 0\n" + 
    		"v 0 0 1\n" + 
    		"v 1 1 0\n" +
    		"vt 0 0\n" +
    		"vt 0 1\n" +
    		"vt 1 1\n" +
    		"f 1/1 2/2 3/3";
    	ByteArrayInputStream in = new ByteArrayInputStream(objData.getBytes());
    	ReaderOBJ o = new ReaderOBJ();
    	o.setUseMultipleTexAndNormalCoords(false);
    	IndexedFaceSet g = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(o.read(new Input("Direct String Data", in)));
    	DataList c = g.getVertexAttributes(Attribute.COORDINATES);
    	DataList tc = g.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
    	Assert.assertNotNull(c);
    	Assert.assertNotNull(tc);
    }
    
    @Test public void testReadTextureCoordinates_MultipleTrue() throws Exception {
    	String objData = 
    		"v 0 0 0\n" + 
    		"v 0 0 1\n" + 
    		"v 1 1 0\n" +
    		"vt 0 0\n" +
    		"vt 0 1\n" +
    		"vt 1 1\n" +
    		"f 1/1 2/2 3/3";
    	ByteArrayInputStream in = new ByteArrayInputStream(objData.getBytes());
    	ReaderOBJ o = new ReaderOBJ();
    	o.setUseMultipleTexAndNormalCoords(true);
    	IndexedFaceSet g = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(o.read(new Input("Direct String Data", in)));
    	DataList c = g.getVertexAttributes(Attribute.COORDINATES);
    	DataList tc = g.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
    	Assert.assertNotNull(c);
    	Assert.assertNotNull(tc);
    }

}
