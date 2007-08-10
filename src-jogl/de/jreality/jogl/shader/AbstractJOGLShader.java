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


package de.jreality.jogl.shader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;

/**
 * A class to handle common tasks related to openGL shading language shaders.
 * @author gunn
 *
 */
public abstract class AbstractJOGLShader extends AbstractPrimitiveShader implements PolygonShader {
	String[] vertexSource, fragmentSource;
	int program = -1;
	static String resourceDir = "./resources/";

	public void setupShader(GL gl)	{
		GLU glu = new GLU();
		
		int[] status = new int[1];
		program = gl.glCreateProgramObjectARB();
		if (vertexSource != null && vertexSource.length > 0)	{
			int vertexHandle = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
			gl.glShaderSourceARB(vertexHandle, 1, vertexSource, (int[]) null, 0);
			gl.glCompileShaderARB(vertexHandle);
			printOpenGLError();		
			gl.glGetObjectParameterivARB(vertexHandle, GL.GL_OBJECT_COMPILE_STATUS_ARB, status, 0);
			printInfoLog(vertexHandle, gl);
			if (status[0] == 0) return;
			gl.glAttachObjectARB(program, vertexHandle);			
		}
		
		if (fragmentSource != null && fragmentSource.length > 0)	{
			int fragmentHandle = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
			gl.glShaderSourceARB(fragmentHandle, 1, fragmentSource, (int[] ) null, 0);
			gl.glCompileShaderARB(fragmentHandle);
			printOpenGLError();
			gl.glGetObjectParameterivARB(fragmentHandle, GL.GL_OBJECT_COMPILE_STATUS_ARB, status, 0);
			printInfoLog(fragmentHandle, gl);
			if (status[0] == 0) return;
			gl.glAttachObjectARB(program, fragmentHandle);
		}
		
		gl.glLinkProgramARB(program);
		printOpenGLError();
		gl.glGetObjectParameterivARB(program, GL.GL_OBJECT_LINK_STATUS_ARB, status, 0);
		printInfoLog(program, gl);
		if (status[0] == 0) {
			program = -1;
			return;
		}
	}
	
	public void render(JOGLRenderingState jrs) {
		JOGLRenderer jr = jrs.renderer;
		GL gl = jr.getGL();
		activate(jr.getGL());
	}
	
	public void activate(GL gl)	{
		if (program == -1)	{
			setupShader(gl);
			if (program == -1) {
				JOGLConfiguration.theLog.log(Level.WARNING,"Can't  setup OpenGL shader "+vertexSource);
				return;
			}
		}
		gl.glUseProgramObjectARB(program);
		JOGLConfiguration.theLog.log(Level.FINE,"Setting GLSL program to "+program);
		System.err.println("Activating glsl shader");
	}
	
	public void postRender(JOGLRenderingState jrs) {
		JOGLRenderer jr = jrs.renderer;
		deactivate(jr.getGL());
	}
	
	public void deactivate(GL gl)	{
		//TODO fix this
		gl.glUseProgramObjectARB(0);
		LoggingSystem.getLogger(this).log(Level.FINE,"Deactivating GLSL program");
		System.err.println("De-Activating glsl shader");
	}
	private void printInfoLog(int objectHandle, GL gl)	{
		int[] logLength = new int[1];
		int[] charsWritten = new int[1];
		byte[] infoLog;
		
		printOpenGLError();
		gl.glGetObjectParameterivARB(objectHandle, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, logLength, 0);
		printOpenGLError();
		
		if (logLength[0] > 0)	{
			infoLog = new byte[logLength[0]];
			gl.glGetInfoLogARB(objectHandle, logLength[0], IntBuffer.wrap(charsWritten), ByteBuffer.wrap(infoLog) );
			StringBuffer foo = new StringBuffer(charsWritten[0]);
			
			for (int i = 0; i< charsWritten[0]; ++i)	foo.append((char) infoLog[i]);
			JOGLConfiguration.theLog.log(Level.FINE,"Info Log: "+foo.toString());
		}
		printOpenGLError();
	}
	
	public static void printOpenGLError()	{
		
	}
	
	public static String loadTextFromFile(String fn)	throws IOException {
		String filename;
		if (fn.charAt(0) == '/') filename = fn;
		else filename = resourceDir + fn;
    File f = new File(filename);
    Reader fr;
    if (f.exists()) {
      fr = new FileReader(filename);
    } else { // try to read from de.jreality.jogl.shader.resources
      Input in = Input.getInput("de/jreality/jogl/shader/resources/"+fn);
      fr = in.getReader();
    }
    StringBuffer sb = new StringBuffer(1024);
    char[] array = new char[1024];
		int length = fr.read(array);
		while(length != -1)	{
			sb.append(array,0,length);
			length = fr.read(array);
		}
		JOGLConfiguration.theLog.log(Level.FINEST,"Read "+sb.toString());
		return sb.toString();
	}
	
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
             JOGLConfiguration.theLog.log(Level.FINE,"No such uniform named"+ name);
    
         printOpenGLError();  // Check for OpenGL errors
         return loc;
     }
    

	public void setFrontBack(int f) {
		// TODO Auto-generated method stub

	}
}
