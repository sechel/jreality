package de.jreality.jogl3.shader;

import java.util.HashMap;

import javax.media.opengl.GL3;

import de.jreality.jogl3.glsl.GLShader;
import de.jreality.shader.ImageData;

public class ShaderVarHash {
	private static HashMap<GLShader, HashMap<String, Integer>> variables = new HashMap<GLShader, HashMap<String, Integer>>();
	
	public static void bindUniform(GLShader s, String name, int value, GL3 gl){
		HashMap<String, Integer> shaders;
		shaders = variables.get(s);
		
		if(shaders == null){
			System.out.println("Int: creating new shader hash map");
			shaders = new HashMap<String, Integer>();
			variables.put(s, shaders);
		}
		Integer i = shaders.get(name);
		if(i == null){
			System.out.println("Int: creating new shader-uniform pair in hash map");
			i = gl.glGetUniformLocation(s.shaderprogram, name);
			shaders.put(name, i);
		}
		gl.glUniform1i(i, value);
	}
	
	public static void bindUniform(GLShader s, String name, float value, GL3 gl){
		HashMap<String, Integer> shaders;
		shaders = variables.get(s);
		
		if(shaders == null){
			System.out.println("Float: creating new shader hash map");
			shaders = new HashMap<String, Integer>();
			variables.put(s, shaders);
		}
		Integer i = shaders.get(name);
		if(i == null){
			System.out.println("Float: creating new shader-uniform pair in hash map");
			i = gl.glGetUniformLocation(s.shaderprogram, name);
			shaders.put(name, i);
		}
		gl.glUniform1f(i, value);
	}
	
	public static void bindUniform(GLShader s, String name, float[] value, GL3 gl){
		HashMap<String, Integer> shaders;
		shaders = variables.get(s);
		
		if(shaders == null){
			System.out.println("Float[]: creating new shader hash map");
			shaders = new HashMap<String, Integer>();
			variables.put(s, shaders);
		}
		Integer i = shaders.get(name);
		if(i == null){
			System.out.println("Float[]: creating new shader-uniform pair in hash map");
			i = gl.glGetUniformLocation(s.shaderprogram, name);
			shaders.put(name, i);
		}
		gl.glUniform4fv(i, 1, value, 0);
	}
	
	public static void bindUniformMatrix(GLShader s, String name, float[] value, GL3 gl){
		HashMap<String, Integer> shaders;
		shaders = variables.get(s);
		
		if(shaders == null){
			System.out.println("Matrix: creating new shader hash map");
			shaders = new HashMap<String, Integer>();
			variables.put(s, shaders);
		}
		Integer i = shaders.get(name);
		if(i == null){
			System.out.println("Matrix: creating new shader-uniform pair in hash map");
			i = gl.glGetUniformLocation(s.shaderprogram, name);
			shaders.put(name, i);
		}
		gl.glUniformMatrix4fv(i, 1, true, value, 0);
	}
	
}
