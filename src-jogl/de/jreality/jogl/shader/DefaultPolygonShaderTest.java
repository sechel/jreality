/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import junit.framework.TestCase;


/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultPolygonShaderTest extends TestCase {

	/**
	 * Constructor for DefaultPolygonShaderTest.
	 * @param arg0
	 */
	public DefaultPolygonShaderTest(String arg0) {
		super(arg0);
	}
	
	public void testDiffuseColor()	{
		DefaultPolygonShader dps = new DefaultPolygonShader();
		float[] dc = dps.getDiffuseColorAsFloat();
		for (int i = 0; i<4; ++i)	{
			System.out.print(dc[i]+" ");
		}
	}

}
