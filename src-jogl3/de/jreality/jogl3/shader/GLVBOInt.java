package de.jreality.jogl3.shader;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public class GLVBOInt extends GLVBO{
	
	private int[] data;
	
	public int[] getData(){
		return data;
	}
	/**
	 * 
	 * @param gl
	 * @param subdata
	 * @param begin
	 * @param length The length in Bytes, i.e. 4 for a single float.
	 */
	public void updateSubData(GL3 gl, int[] subdata, int begin, int length){
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, index);
		gl.glBufferSubData(gl.GL_ARRAY_BUFFER, begin, length, IntBuffer.wrap(subdata));
	}
	public void updateData(GL3 gl, int[] data){
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, index);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*data.length, IntBuffer.wrap(data), gl.GL_STATIC_READ);
	}
	public GLVBOInt(GL3 gl, int[] vertdata, String name, int arraySize){
		this(gl, vertdata, name);
		this.arraySize = arraySize;
	}
	public GLVBOInt(GL3 gl, int[] vertdata, String name){
		data = vertdata;
		this.name = name;
		int[] vertindex = new int[1];
		gl.glGenBuffers(1, vertindex, 0);
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vertindex[0]);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*vertdata.length, IntBuffer.wrap(vertdata), gl.GL_STATIC_DRAW);
		index = vertindex[0];
		length = vertdata.length;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return GL3.GL_INT;
	}
}
