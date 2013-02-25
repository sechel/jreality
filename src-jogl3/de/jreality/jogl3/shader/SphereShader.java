package de.jreality.jogl3.shader;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlTexture;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLSphereEntity;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.jogl3.helper.LightHelper;
import de.jreality.jogl3.light.JOGLDirectionalLightEntity;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.math.Rn;
import de.jreality.shader.EffectiveAppearance;

public class SphereShader{
	
	public static void printEffectiveAppearance(EffectiveAppearance eap, String name) {
		System.out.println("start");
		//System.out.println(((IndexedFaceSet)(fse.getNode())).getName());
		
		//eap.getApp().getAttributes().keySet()
		for( Object o : eap.getApp().getAttributes().keySet()){
			String s = (String)o;
			eap.getApp().getAttribute(s);
			System.out.println(s + " " + eap.getApp().getAttribute(s).getClass());
		}
		System.out.println("stop");
	}
	
	public static void render(JOGLSphereEntity se, LinkedList<GlUniform> c, GlTexture tex, GLShader shader, JOGLRenderState state){
		
		//GLShader shader = GLShader.defaultPolygonShader;
		GL3 gl = state.getGL();
		
		state.getLightHelper().loadLocalLightTexture(state.getLocalLightCollection(), gl);
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] modelview = Rn.convertDoubleToFloatArray(state.getModelViewMatrix());
		shader.useShader(gl);
		
    	//matrices
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "projection"), 1, true, projection, 0);
    	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "modelview"), 1, true, modelview, 0);
    	
		//global lights in a texture
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_globalLights"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalDirLights"), state.getLightHelper().getNumGlobalDirLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalPointLights"), state.getLightHelper().getNumGlobalPointLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numGlobalSpotLights"), state.getLightHelper().getNumGlobalSpotLights());
		
		//local lights in a texture
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_localLights"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalDirLights"), state.getLightHelper().getNumLocalDirLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalPointLights"), state.getLightHelper().getNumLocalPointLights());
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "sys_numLocalSpotLights"), state.getLightHelper().getNumLocalSpotLights());
		
		
		//bind shader uniforms
		//TODOhave to set default values here for shader uniforms not present in the appearance
		for(GlUniform u : c){
			u.bindToShader(shader, gl);
		}

		//tex.bind(shader, gl);
		//TODO all the other types
		
		GLVBO sphereVBO = state.getSphereHelper().getSphereVBO(gl, 20);
		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, sphereVBO.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, "vertex_coordinates"), sphereVBO.getElementSize(), sphereVBO.getType(), false, 0, 0);
    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "vertex_coordinates"));
    	
    	//new way to do lights
		state.getLightHelper().bindGlobalLightTexture(gl);
		
    	//actual draw command
    	gl.glDrawArrays(gl.GL_TRIANGLES, 0, sphereVBO.getLength()/4);
    	
    	//disable all vbos
    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, "vertex_coordinates"));
    	
		shader.dontUseShader(gl);
	}
}
