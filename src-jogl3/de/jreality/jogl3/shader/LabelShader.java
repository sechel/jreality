package de.jreality.jogl3.shader;

import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import com.itextpdf.text.log.SysoLogger;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlReflectionMap;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlTexture;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.geom.JOGLGeometryInstance.LabelRenderData;
import de.jreality.jogl3.geom.Label;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.glsl.GLShader.ShaderVar;
import de.jreality.math.Rn;
import de.jreality.shader.Texture2D;

public class LabelShader {

	
	
	public static void render(LabelRenderData labelData, Label[] labels, JOGLRenderState state){
		if(labels == null || labels.length == 0)
			return;
		GL3 gl = state.getGL();
		
		float[] projection = Rn.convertDoubleToFloatArray(state.getProjectionMatrix());
		float[] modelview = Rn.convertDoubleToFloatArray(state.getModelViewMatrix());
		
		GLShader shader = new GLShader("label.v", "label.f");
		shader.init(gl);
		
		
		for(int L = 0; L < labelData.tex.length; L++){
		
			Texture2DLoader.load(gl, labelData.tex[L], gl.GL_TEXTURE2);
			
			shader.useShader(gl);
			
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFuncSeparate(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA,
					GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			
			
			gl.glUniform4fv(gl.glGetUniformLocation(shader.shaderprogram, "xyAlignmentTotalWH"), 1, labelData.xyAlignmentTotalWH[L], 0);
			gl.glUniform4fv(gl.glGetUniformLocation(shader.shaderprogram, "xyzOffsetScale"), 1, labelData.xyzOffsetScale, 0);
			
			gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "tex"), 2);
			ShaderVarHash.bindUniformMatrix(shader, "projection", projection, gl);
			ShaderVarHash.bindUniformMatrix(shader, "modelview", modelview, gl);
			
			GLVBOFloat vbo = new GLVBOFloat(gl, new float[]{1, 1, 0.2f, 1,
															1, 0, 0.2f, 1,
															0, 0, 0.2f, 1,
															0, 0, 0.2f, 1,
															0, 1, 0.2f, 1,
															1, 1, 0.2f, 1}, "vertices");
			
			
			gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo.getID());
	    	gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, vbo.getName()), vbo.getElementSize(), vbo.getType(), false, 0, 0);
	    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, vbo.getName()));
			
	    	
	    	GLVBO p = labelData.points[L];
	    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, p.getID());
			gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, p.getName()), p.getElementSize(), p.getType(), false, 4*p.getElementSize(), 0);
	    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, p.getName()));
	    	//important here: we advance to the next element only after all of tube_coords have been drawn.
	    	gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, p.getName()), 1);
	    	
	    	GLVBO l = labelData.ltwh[L];
	    	gl.glBindBuffer(gl.GL_ARRAY_BUFFER, l.getID());
			gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, l.getName()), l.getElementSize(), l.getType(), false, 4*l.getElementSize(), 0);
	    	gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, l.getName()));
	    	//important here: we advance to the next element only after all of tube_coords have been drawn.
	    	gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, l.getName()), 1);
	    	
	    	
	    	//actual draw command
	    	//gl.glDrawArrays(gl.GL_TRIANGLES, 0, vbo.getLength()/4);
	    	
	    	gl.glDrawArraysInstanced(gl.GL_TRIANGLES, 0, vbo.getLength()/4, p.getLength()/4);
	    	
			
	    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, vbo.getName()));
	    	
	    	gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, p.getName()));
			gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, p.getName()), 0);
			gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, l.getName()));
			gl.glVertexAttribDivisor(gl.glGetAttribLocation(shader.shaderprogram, l.getName()), 0);
			
			shader.dontUseShader(gl);
		}
	}
}
