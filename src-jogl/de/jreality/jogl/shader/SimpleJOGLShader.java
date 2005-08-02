/*
 * Created on Nov 24, 2004
 *
 */
package de.jreality.jogl.shader;

import java.io.IOException;

/**
 * @author gunn
 *
 */
public class SimpleJOGLShader extends AbstractJOGLShader {

	/**
	 * @param c
	 */
	public SimpleJOGLShader(String VSFileName, String FSFileName) {
		super();
		try {
			if (VSFileName != null)	{
				vertexSource  = new String[1];
				vertexSource[0] = loadTextFromFile(VSFileName);				
			}
			if (FSFileName != null)	{
				fragmentSource = new String[1];
				fragmentSource[0] = loadTextFromFile(FSFileName);				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
