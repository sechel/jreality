package de.jreality.jogl3.shader;

import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING_DEFAULT;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL3;

import de.jreality.jogl.shader.ShadedSphereImage;
import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.GLvboFaceIndices;
import de.jreality.jogl3.GLvboNormals;
import de.jreality.jogl3.GLvboVertex;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.GLShader.ShaderVar;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlTexture;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.light.JOGLDirectionalLightEntity;
import de.jreality.jogl3.light.JOGLDirectionalLightInstance;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;

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
		JOGLLightCollection lc = state.getLights();
		
		int numDirLights = lc.directionalLights.size();
		float[] directionalLightColors = new float[numDirLights*4];
		double[] directionalLightDirections = new double[numDirLights * 3];
		for(int i = 0; i < numDirLights; i++){
			JOGLDirectionalLightEntity dl = (JOGLDirectionalLightEntity) lc.directionalLights.get(i).getEntity();
			directionalLightColors[i*4+0] = dl.getColor()[0]*(float)dl.getIntensity();
			directionalLightColors[i*4+1] = dl.getColor()[1]*(float)dl.getIntensity();
			directionalLightColors[i*4+2] = dl.getColor()[2]*(float)dl.getIntensity();
			directionalLightColors[i*4+3] = dl.getColor()[3]*(float)dl.getIntensity();
			
			JOGLDirectionalLightInstance li = lc.directionalLights.get(i);
			//System.out.println("name = " + li.getNode().getName());
			directionalLightDirections[i*3+0] = li.trafo[2];
			directionalLightDirections[i*3+1] = li.trafo[6];
			directionalLightDirections[i*3+2] = li.trafo[10];
			//System.out.println("dir Light: " + directionalLightDirections[i*3+0] + ", " + directionalLightDirections[i*3+1] + ", " + directionalLightDirections[i*3+2]);
		}

			shader.useShader(gl);
			
        	//matrices
        	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "projection"), 1, true, projection, 0);
        	gl.glUniformMatrix4fv(gl.glGetUniformLocation(shader.shaderprogram, "modelview"), 1, true, modelview, 0);
        	
			//directional lights
        	//TODO change to texture
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "numDirLights"), numDirLights);
			gl.glUniform4fv(gl.glGetUniformLocation(shader.shaderprogram, "directionalLightColors"), numDirLights, directionalLightColors, 0);
			gl.glUniform3fv(gl.glGetUniformLocation(shader.shaderprogram, "directionalLightDirections"), numDirLights, Rn.convertDoubleToFloatArray(directionalLightDirections), 0);
			
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
