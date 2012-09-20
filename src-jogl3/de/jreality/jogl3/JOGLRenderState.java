package de.jreality.jogl3;

import javax.media.opengl.GL3;

import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.math.Rn;

public class JOGLRenderState {

	GL3 gl;
	
	final double[] modelViewMatrix=Rn.identityMatrix(4);
	final double[] projectionMatrix=Rn.identityMatrix(4);
	public boolean appearanceUpToDate = true;
	public int screenSize = 0;
	public float screenSizeInScene = 1;

	private JOGLLightCollection lights = null;
	
	public double[] getModelViewMatrix() {
		return modelViewMatrix;
	}
	
	public double[] getProjectionMatrix() {
		return projectionMatrix;
	}

	public JOGLRenderState(GL3 gl, double[] inverseCameraMatrix, double[] projection, JOGLLightCollection c, int screenSize, float screenSizeInScene) {
		this.screenSize = screenSize;
		this.screenSizeInScene = screenSizeInScene;
		lights = c;
		System.arraycopy(inverseCameraMatrix, 0, modelViewMatrix, 0, 16);
		System.arraycopy(projection, 0, projectionMatrix, 0, 16);
		//System.arraycopy(inverseCameraMatrix, 0, projectionMatrix, 0, 16);
		this.gl = gl;
	}
	
	public JOGLRenderState(JOGLRenderState parentState, double[] matrix) {
		lights = parentState.getLights();
		screenSize = parentState.screenSize;
		screenSizeInScene = parentState.screenSizeInScene;
		
		System.arraycopy(parentState.getProjectionMatrix(), 0, projectionMatrix, 0, 16);
		if (matrix != null) Rn.times(modelViewMatrix, parentState.getModelViewMatrix(), matrix);
		else System.arraycopy(parentState.getModelViewMatrix(), 0, modelViewMatrix, 0, 16);
		gl = parentState.getGL();
	}

	public JOGLLightCollection getLights() {
		// TODO Auto-generated method stub
		return lights;
	}

	public GL3 getGL() {
		return gl;
	}	
	
}
