package de.jreality.jogl3.shader;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.GLShader.ShaderVar;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlTexture;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.light.JOGLDirectionalLightEntity;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.math.Rn;
import de.jreality.shader.EffectiveAppearance;

public class PolygonShader{
	
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
	
	public static void render(JOGLFaceSetEntity fse, LinkedList<GlUniform> c, GlTexture tex, GLShader shader, JOGLRenderState state){
		
		//TODO replace by fsi or renderState
		//GLShader shader = GLShader.defaultPolygonShader;
		GL3 gl = state.getGL();
		
		gl.glDisable(gl.GL_BLEND);
		//gl.glBlendEquationSeparate(gl.GL_FUNC_ADD, gl.GL_FUNC_ADD);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFuncSeparate(0x0302, 0x0303, gl.GL_ZERO, gl.GL_ZERO);
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] modelview = Rn.convertDoubleToFloatArray(state.getModelViewMatrix());
		
			shader.useShader(gl);
			
        	//matrices
        	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "projection"), 1, true, projection, 0);
        	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "modelview"), 1, true, modelview, 0);
        	
			//global lights in a texture
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "globalLights"), 0);
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "numGlobalDirLights"), state.numGlobalDirLights);
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "numGlobalPointLights"), state.numGlobalPointLights);
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "numGlobalSpotLights"), state.numGlobalSpotLights);
			
			
			//bind shader uniforms
			//TODOhave to set default values here for shader uniforms not present in the appearance
			for(GlUniform u : c){
				u.bindToShader(shader, gl);
			}
			tex.bind(shader, gl);
			//TODO all the other types
			
        	//bind vbos to corresponding shader variables
        	List<ShaderVar> l = shader.vertexAttributes;
        	for(ShaderVar v : l){
        		GLVBO vbo = fse.getVBO(v.getName());
        		if(vbo != null){
        			//System.out.println(v.getName());
        			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 1);
        			gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo.getID());
                	gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, v.getName()), 4, vbo.getType(), false, 0, 0);
                	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
        		}else{
        			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "has_" + v.getName()), 0);
        		}
        	}
        	
        	//new way to do lights
			gl.glEnable(gl.GL_TEXTURE_2D);
			gl.glActiveTexture(gl.GL_TEXTURE0);
			gl.glBindTexture(gl.GL_TEXTURE_2D, state.lightTex);
			
        	//actual draw command
        	gl.glDrawArrays(gl.GL_TRIANGLES, 0, fse.getVBO("vertex_coordinates").getLength()/4);
        	
        	//disable all vbos
        	for(ShaderVar v : l){
        		GLVBO vbo = fse.getVBO(v.getName());
        		if(vbo != null){
        			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, v.getName()));
        		}
        	}
        	
			shader.dontUseShader(gl);
	}
}
