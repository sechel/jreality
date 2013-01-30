package de.jreality.jogl3.helper;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.scene.Appearance;

public class BackgroundHelper {
	
	/**
	 * @param args
	 */
	GLShader backgroundShader;
	private float[] quadVerts = {1, 1, 0, 1,
							1, -1, 0, 1,
							-1, -1, 0, 1,
							-1, -1, 0, 1,
							-1, 1, 0, 1,
							1, 1, 0, 1};
	private float[] colorValues = {
			0.88235295f, 0.88235295f, 0.88235295f, 1.0f,
			1.0f, 0.88235295f, 0.7058824f, 1.0f,
			1.0f, 0.88235295f, 0.7058824f, 1.0f,
			1.0f, 0.88235295f, 0.7058824f, 1.0f,
			0.88235295f, 0.88235295f, 0.88235295f, 1.0f,
			0.88235295f, 0.88235295f, 0.88235295f, 1.0f
	};
	GLVBOFloat quad;
	GLVBOFloat colors;
	static Appearance pseudoAp = new Appearance();
	public void doBackground(GL3 gl){
		
		backgroundShader.useShader(gl);
		
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, quad.getID());
    	gl.glVertexAttribPointer(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_coordinates"), quad.getElementSize(), quad.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_coordinates"));
    	
    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, colors.getID());
    	gl.glVertexAttribPointer(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_colors"), colors.getElementSize(), colors.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_colors"));
    	
    	
    	gl.glDrawArrays(gl.GL_TRIANGLES, 0, quad.getLength()/4);
    	
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_coordinates"));
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(backgroundShader.shaderprogram, "vertex_colors"));
    	backgroundShader.dontUseShader(gl);
	}
	
	public void initializeBackground(GL3 gl){
		quad = new GLVBOFloat(gl, quadVerts, "vertex_coordinates");
		colors = new GLVBOFloat(gl, colorValues, "vertex_colors");
		backgroundShader = new GLShader("background.v", "background.f");
		backgroundShader.init(gl);
	}
}
