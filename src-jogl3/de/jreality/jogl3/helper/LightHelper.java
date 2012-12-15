package de.jreality.jogl3.helper;

import java.nio.FloatBuffer;

import javax.media.opengl.GL3;

import de.jreality.jogl3.light.JOGLDirectionalLightEntity;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.jogl3.light.JOGLPointLightEntity;
import de.jreality.jogl3.light.JOGLPointLightInstance;
import de.jreality.jogl3.light.JOGLSpotLightEntity;
import de.jreality.jogl3.light.JOGLSpotLightInstance;

public class LightHelper {

	public static int getTextureID(){
		return textureID;
	}
	
	private static int textureID;
	
	public static void initLightTexture(GL3 gl){
		int[] textures = new int[1];
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glGenTextures(1, textures, 0);
		textureID=textures[0];
	}

	public static void loadGlobalLightTexture(JOGLLightCollection lc, GL3 gl) {
		//texture id
		
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		
		gl.glBindTexture(gl.GL_TEXTURE_2D, textureID);
		
		int width = lc.directionalLights.size()*2+lc.pointLights.size()*3+lc.spotLights.size()*5;

		float[] data = new float[width*4];
		int i = 0;
		for(JOGLDirectionalLightInstance d : lc.directionalLights){
			JOGLDirectionalLightEntity dl = (JOGLDirectionalLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//direction of the light
			data[i+4] = (float)d.trafo[2];
			data[i+5] = (float)d.trafo[6];
			data[i+6] = (float)d.trafo[10];
			data[i+7] = (float)d.trafo[14];//?needed?
			
			i+=8;
		}
		for(JOGLPointLightInstance d : lc.pointLights){
			JOGLPointLightEntity dl = (JOGLPointLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//position of the light
			data[i+4] = (float)d.trafo[3];
			data[i+5] = (float)d.trafo[7];
			data[i+6] = (float)d.trafo[11];
			data[i+7] = (float)d.trafo[15];
			
			//attenuation
			data[i+8] = (float)dl.A0;
			data[i+9] = (float)dl.A1;
			data[i+10] = (float)dl.A2;
			
			i+=12;
		}
		for(JOGLSpotLightInstance d : lc.spotLights){
			JOGLSpotLightEntity dl = (JOGLSpotLightEntity)d.getEntity();
			
			data[i+0] = dl.getColor()[0]*(float)dl.getIntensity();
			data[i+1] = dl.getColor()[1]*(float)dl.getIntensity();
			data[i+2] = dl.getColor()[2]*(float)dl.getIntensity();
			data[i+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			//direction
			data[i+4] = (float)d.trafo[2];
			data[i+5] = (float)d.trafo[6];
			data[i+6] = (float)d.trafo[10];
			data[i+7] = (float)d.trafo[14];
			
			//position
			data[i+8] = (float)d.trafo[3];
			data[i+9] = (float)d.trafo[7];
			data[i+10] = (float)d.trafo[11];
			data[i+11] = (float)d.trafo[15];
			
			data[i+12] = (float)dl.coneAngle;
			data[i+13] = (float)dl.coneAngleDelta;
			data[i+14] = (float)dl.distribution;
			
			//attenuation
			data[i+16] = (float)dl.A0;
			data[i+17] = (float)dl.A1;
			data[i+18] = (float)dl.A2;
			
			i+=20;
		}
		
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST); 
	    gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
	    
	    gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA32F, 
    	width, 1, 0, gl.GL_RGBA,
	    gl.GL_FLOAT, FloatBuffer.wrap(data));
	}
}
