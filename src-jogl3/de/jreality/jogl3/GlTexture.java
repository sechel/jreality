package de.jreality.jogl3;

import javax.media.opengl.GL3;

import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.shader.ShaderVarHash;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.shader.Texture2D;

public class GlTexture{
	boolean hasTexture = false;
	public GlTexture(){
		
	}
	private Texture2D tex = null;
	public void setTexture(Texture2D tex){
		this.tex = tex;
		hasTexture = true;
	}
	public void removeTexture(){
		hasTexture = false;
	}
	public void bind(GLShader shader, GL3 gl){
		if(hasTexture){
			//GL_TEXTURE0 and GL_TEXTURE1 reserved for lights.
			Texture2DLoader.load(gl, tex, gl.GL_TEXTURE8);
			ShaderVarHash.bindUniform(shader, "image", 8, gl);
			ShaderVarHash.bindUniform(shader, "has_Tex", 1, gl);
		}else{
			ShaderVarHash.bindUniform(shader, "has_Tex", 0, gl);
		}
	}
}
