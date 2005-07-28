/*
 * Created on Nov 24, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.Geometry;
import de.jreality.util.EffectiveAppearance;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import net.java.games.jogl.GLDrawable;
import net.java.games.jogl.GLU;

/**
 * A class to handle common tasks related to openGL shading language shaders.
 * @author gunn
 *
 */
public abstract class AbstractJOGLShader implements PolygonShader {
	String[] vertexSource, fragmentSource;
	int program = -1;
	static String resourceDir = "./";
	static {
		String foo = System.getProperty("jreality.jogl.resourceDir");
		if (foo != null) resourceDir = foo;
	}
	/**
	 * 
	 */
	public AbstractJOGLShader() {
		super();
	}

	public void setupShader(GLDrawable theCanvas)	{
		if (theCanvas == null)	{
			return;
		}
		GL gl = theCanvas.getGL();
		GLU glu = theCanvas.getGLU();
		
		int[] status = new int[1];
		program = gl.glCreateProgramObjectARB();
		if (vertexSource != null && vertexSource.length > 0)	{
			int vertexHandle = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
			gl.glShaderSourceARB(vertexHandle, 1, vertexSource, (int[]) null);
			gl.glCompileShaderARB(vertexHandle);
			printOpenGLError();		
			gl.glGetObjectParameterivARB(vertexHandle, GL.GL_OBJECT_COMPILE_STATUS_ARB, status);
			printInfoLog(vertexHandle, theCanvas);
			if (status[0] == 0) return;
			gl.glAttachObjectARB(program, vertexHandle);			
		}
		
		if (fragmentSource != null && fragmentSource.length > 0)	{
			int fragmentHandle = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
			gl.glShaderSourceARB(fragmentHandle, 1, fragmentSource, (int[] ) null);
			gl.glCompileShaderARB(fragmentHandle);
			printOpenGLError();
			gl.glGetObjectParameterivARB(fragmentHandle, GL.GL_OBJECT_COMPILE_STATUS_ARB, status);
			printInfoLog(fragmentHandle, theCanvas);
			if (status[0] == 0) return;
			gl.glAttachObjectARB(program, fragmentHandle);
		}
		
		gl.glLinkProgramARB(program);
		printOpenGLError();
		gl.glGetObjectParameterivARB(program, GL.GL_OBJECT_LINK_STATUS_ARB, status);
		printInfoLog(program, theCanvas);
		if (status[0] == 0) {
			program = -1;
			return;
		}
	}
	
	public void render(JOGLRenderer jr) {
		GL gl = jr.getCanvas().getGL();
		activate(jr.getCanvas());
	}
	
	public void activate(GLDrawable theCanvas)	{
		GL gl = theCanvas.getGL();
		if (program == -1)	{
			setupShader(theCanvas);
			if (program == -1) return;
		}
		gl.glUseProgramObjectARB(program);
		JOGLConfiguration.theLog.log(Level.FINE,"Setting GLSL program to "+program);
	}
	
	public void deactivate(GLDrawable theCanvas)	{
		GL gl = theCanvas.getGL();
		// TODO how to deactivate a shader?
	}
	private void printInfoLog(int objectHandle, GLDrawable theCanvas)	{
		GL gl = theCanvas.getGL();
		
		int[] logLength = new int[1];
		int[] charsWritten = new int[1];
		byte[] infoLog;
		
		printOpenGLError();
		gl.glGetObjectParameterivARB(objectHandle, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, logLength);
		printOpenGLError();
		
		if (logLength[0] > 0)	{
			infoLog = new byte[logLength[0]];
			gl.glGetInfoLogARB(objectHandle, logLength[0], charsWritten, infoLog );
			StringBuffer foo = new StringBuffer(charsWritten[0]);
			
			for (int i = 0; i< charsWritten[0]; ++i)	foo.append((char) infoLog[i]);
			JOGLConfiguration.theLog.log(Level.INFO,"Info Log: "+foo.toString());
		}
		printOpenGLError();
	}
	
	public static void printOpenGLError()	{
		
	}
	
	public static String loadTextFromFile(String fn)	throws IOException {
		String filename;
		if (fn.charAt(0) == '/') filename = fn;
		else filename = resourceDir + fn;
		FileReader fr = new FileReader(filename);
		StringBuffer sb = new StringBuffer(1024);
		char[] array = new char[100];
		int length = fr.read(array);
		while(length != -1)	{
			sb.append(array,0,length);
			length = fr.read(array);
		}
		JOGLConfiguration.theLog.log(Level.FINEST,"Read "+sb.toString());
		return sb.toString();
	}
	
	public void postRender(JOGLRenderer jr) {
	
	}
	
//	public void render(JOGLRenderer jr) {
//	}
//	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name) {
		

	}
	//
     // Get the location of a uniform variable
     //
     static
     int getUniLoc(int program, String name, GL gl)
     {
         int loc;
    
         loc = gl.glGetUniformLocationARB(program, name);
    
       if (loc == -1)
             JOGLConfiguration.theLog.log(Level.WARNING,"No such uniform named"+ name);
    
         printOpenGLError();  // Check for OpenGL errors
         return loc;
     }
    

	public void setFrontBack(int f) {
		// TODO Auto-generated method stub

	}
	public boolean providesProxyGeometry() {
		// TODO Auto-generated method stub
		return false;
	}
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr,
			int signature) {
		// TODO Auto-generated method stub
		return 0;
	}
}
