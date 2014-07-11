package de.jreality.reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.util.Input;

public class OBJReaderGroupTest {

	final static double delta = 1E-10;
	
	@Test public void testOneGroup() throws Exception {
    	String testGroup = "testGroup";
		String objData = 
    		"g "+ testGroup +"\n"+
    		"v 0 0 0\n" + 
    		"v 0 0 1\n" + 
    		"v 1 1 0\n" +
    		"vt 0 0\n" +
    		"vt 0 1\n" +
    		"vt 1 1\n" +
    		"f 1/1 2/2 3/3";
    	SceneGraphComponent root = parseString(objData);
    	SceneGraphComponent child = root.getChildComponent(0);
    	Assert.assertTrue(testGroup.equals(child.getName()));
		IndexedFaceSet g = (IndexedFaceSet)child.getGeometry();
    	
		double[][] vertices = {{0,0,0},{0,0,1},{1,1,0}};
    	TestUtils.testVertexCoordinates(vertices, g, delta);
    	double[][] texture = {{0,0},{0,1},{1,1}};
    	TestUtils.testTextureCoordinates(texture,g,delta);
    	DataList tc = g.getVertexAttributes(Attribute.TEXTURE_COORDINATES);
    	Assert.assertNotNull(tc);
    }

	private SceneGraphComponent parseString(String objData) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(objData.getBytes());
		Input input = new Input("Direct String Data", stream);
		ReaderOBJ o = new ReaderOBJ();
        SceneGraphComponent root = o.read(input);
		return root;
	}
}
