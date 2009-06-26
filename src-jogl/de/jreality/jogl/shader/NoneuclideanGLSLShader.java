package de.jreality.jogl.shader;

import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;

import java.io.IOException;

import javax.media.opengl.GL;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRenderingState;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.GlslProgram;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.Input;

public class NoneuclideanGLSLShader {
	boolean		poincareModel = false,
		needsRendered = true;		
	SceneGraphPath poincarePath;
	static GlslProgram glslProgram = null;
	static String shaderLocation = "de/jreality/jogl/shader/resources/noneuclidean.vert";
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		if (glslProgram == null) {
			try {
				Appearance ap = new Appearance();
				glslProgram = new GlslProgram(ap, POLYGON_SHADER, Input.getInput(shaderLocation), null);
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
		poincareModel = eap.getAttribute(ShaderUtility.nameSpace(name,"poincareModel"), false);	
		if (poincareModel) {
			poincarePath =  (SceneGraphPath) eap.getAttribute(ShaderUtility.nameSpace(name,"poincarePath"), new SceneGraphPath());
			if (poincarePath.getLength() == 0) poincarePath=null;
		}
		needsRendered = true;
	}
	public boolean isPoincareModel() {
		return poincareModel;
	}
	public SceneGraphPath getPoincarePath() {
		return poincarePath;
	}
	public  GlslProgram getNoneuclideanShader() {
		return glslProgram;
	}
	
	protected void render(JOGLRenderer jr)	{
		// the only reason we're doing it here is because only now do we know what jrs is
//		System.err.println("writing glsl shader");
		if (needsRendered)  { //return;
			JOGLRenderingState jrs = jr.renderingState;
			glslProgram.setUniform("lightingEnabled", jrs.lighting);
			glslProgram.setUniform("transparencyEnabled", jrs.transparencyEnabled);
			glslProgram.setUniform("transparency", (float) (1.0f - jrs.diffuseColor[3]));
			glslProgram.setUniform("numLights", jrs.numLights);
			glslProgram.setUniform("fogEnabled", jrs.fogEnabled);
			glslProgram.setUniform("hyperbolic", jrs.currentMetric == Pn.HYPERBOLIC);
			glslProgram.setUniform("useNormals4", jrs.normals4d);
			glslProgram.setUniform("poincareModel", poincareModel);
			if (poincarePath != null) {
	    		double[] p2c = Rn.transpose(null, Rn.times(null, jrs.worldToCamera, poincarePath.getMatrix(null))),
				c2p = Rn.inverse(null, p2c);
	//    		System.err.println("c2p = "+Rn.matrixToString(c2p));
	    		glslProgram.setUniform("poincareMatrix", Rn.convertDoubleToFloatArray(c2p));	    			
	    		glslProgram.setUniform("poincareMatrixInv", Rn.convertDoubleToFloatArray(p2c));	    			
			}
	
			needsRendered = false;
		}
    	GlslLoader.render(glslProgram, jr);		
//		noneuclideanInitialized = true;
//
	}
	public void postRender(GL gl) {
		GlslLoader.postRender(glslProgram,gl);
	}

}
