package de.jreality.jogl3.shader;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public class GLVBOInt extends GLVBO{
	
	public GLVBOInt(GL3 gl, int[] vertdata, String name){
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
