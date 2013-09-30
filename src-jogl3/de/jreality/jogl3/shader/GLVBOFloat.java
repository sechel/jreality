package de.jreality.jogl3.shader;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

public class GLVBOFloat extends GLVBO{
	
	private float[] data;
	
	public float[] getData(){
		return data;
	}
	/**
	 * 
	 * @param gl
	 * @param subdata
	 * @param begin
	 * @param length The length in Bytes, i.e. 4 for a single float.
	 */
	public void updateSubData(GL3 gl, float[] subdata, int begin, int length){
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, index);
		gl.glBufferSubData(gl.GL_ARRAY_BUFFER, begin, length, FloatBuffer.wrap(subdata));
	}
	
	public void updateData(GL3 gl, float[] data){
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, index);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*data.length, FloatBuffer.wrap(data), gl.GL_STATIC_READ);
	}
	
	public GLVBOFloat(GL3 gl, float[] vertdata, String name){
		this(gl, vertdata, name, 4);
	}
	public GLVBOFloat(GL3 gl, float[] vertdata, String name, int arraySize){
		data = vertdata;
		this.arraySize = arraySize;
		this.name = name;
		int[] vertindex = new int[1];
		gl.glGenBuffers(1, vertindex, 0);
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vertindex[0]);
		gl.glBufferData(gl.GL_ARRAY_BUFFER, 4*vertdata.length, FloatBuffer.wrap(vertdata), gl.GL_STATIC_READ);
		index = vertindex[0];
		length = vertdata.length;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return GL3.GL_FLOAT;
	}
}
