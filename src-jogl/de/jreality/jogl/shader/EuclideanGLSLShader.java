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

public class EuclideanGLSLShader extends StandardGLSLShader {
	static String shaderLocation = "de/jreality/jogl/shader/resources/euclidean.vert";

	@Override
	String getShaderLocation() {
		// TODO Auto-generated method stub
		return shaderLocation;
	}
	

}
