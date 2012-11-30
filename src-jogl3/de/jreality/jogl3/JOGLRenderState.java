package de.jreality.jogl3;

import javax.media.opengl.GL3;

import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.jogl3.light.JOGLLightInstance;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLSpotLightInstance;
import de.jreality.jogl3.light.JOGLPointLightInstance;
import de.jreality.math.Rn;

public class JOGLRenderState {

	GL3 gl;
	
	final double[] modelViewMatrix=Rn.identityMatrix(4);
	final double[] projectionMatrix=Rn.identityMatrix(4);
	public boolean appearanceUpToDate = true;
	public int screenSize = 0;
	public float screenSizeInScene = 1;

	private JOGLLightCollection localLights = null;
	public int numGlobalDirLights = 0;
	public int numGlobalPointLights = 0;
	public int numGlobalSpotLights = 0;
	
	public int lightTex = 0;
	
	//copy the references to the light instances to a new light collection
	public JOGLLightCollection copyLocalLights(){
		JOGLLightCollection ret = new JOGLLightCollection(null);
		for(JOGLSpotLightInstance s : this.localLights.spotLights)
			ret.spotLights.add(s);
		for(JOGLPointLightInstance p : this.localLights.pointLights)
			ret.pointLights.add(p);
		for(JOGLDirectionalLightInstance d : this.localLights.directionalLights)
			ret.directionalLights.add(d);
		return ret;
	}
	
	public void addLocalLight(JOGLLightInstance	l){
		if(l instanceof JOGLDirectionalLightInstance)
			localLights.directionalLights.add((JOGLDirectionalLightInstance) l);
		else if(l instanceof JOGLPointLightInstance){
			if((JOGLPointLightInstance)l instanceof JOGLSpotLightInstance)
				localLights.spotLights.add((JOGLSpotLightInstance) l);
			else
				localLights.pointLights.add((JOGLPointLightInstance) l);
		}
	}
	
	public double[] getModelViewMatrix() {
		return modelViewMatrix;
	}
	
	public double[] getProjectionMatrix() {
		return projectionMatrix;
	}

	public JOGLRenderState(GL3 gl, double[] inverseCameraMatrix, double[] projection, int lightTex, int numGlobalDirLights, int numGlobalPointLights, int numGlobalSpotLights, int screenSize, float screenSizeInScene) {
		localLights = new JOGLLightCollection(null);
		this.screenSize = screenSize;
		this.screenSizeInScene = screenSizeInScene;
		this.lightTex = lightTex;
		this.numGlobalDirLights = numGlobalDirLights;
		this.numGlobalPointLights = numGlobalPointLights;
		this.numGlobalSpotLights = numGlobalSpotLights;
		System.arraycopy(inverseCameraMatrix, 0, modelViewMatrix, 0, 16);
		System.arraycopy(projection, 0, projectionMatrix, 0, 16);
		//System.arraycopy(inverseCameraMatrix, 0, projectionMatrix, 0, 16);
		this.gl = gl;
	}
	
	public JOGLRenderState(JOGLRenderState parentState, double[] matrix) {
		this.numGlobalDirLights = parentState.numGlobalDirLights;
		this.numGlobalPointLights = parentState.numGlobalPointLights;
		this.numGlobalSpotLights = parentState.numGlobalSpotLights;
		localLights = parentState.copyLocalLights();
		screenSize = parentState.screenSize;
		screenSizeInScene = parentState.screenSizeInScene;
		this.lightTex = parentState.lightTex;
		System.arraycopy(parentState.getProjectionMatrix(), 0, projectionMatrix, 0, 16);
		if (matrix != null) Rn.times(modelViewMatrix, parentState.getModelViewMatrix(), matrix);
		else System.arraycopy(parentState.getModelViewMatrix(), 0, modelViewMatrix, 0, 16);
		gl = parentState.getGL();
	}

	public GL3 getGL() {
		return gl;
	}	
	
}
