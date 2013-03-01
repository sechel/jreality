package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.shader.PointShader;
import de.jreality.jogl3.shader.SphereShader;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Sphere;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLSphereInstance extends JOGLGeometryInstance {
	
	GLShader sphereShader = GLShader.defaultSphereShader;
	
	public JOGLSphereInstance(Sphere node) {
		super(node);
	}

	@Override
	public void render(JOGLRenderState state) {
		if(eap == null)
			return;
		JOGLSphereEntity se = (JOGLSphereEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(visible)
			SphereShader.render(se, sphereUniforms, polygonTexture, sphereShader, state);
			//PointShader.render(state.getGL(), pse.getVertexVBO(), Rn.convertDoubleToFloatArray(state.getModelViewMatrix()), Rn.convertDoubleToFloatArray(state.getProjectionMatrix()));
	}

	public LinkedList<GlUniform> sphereUniforms = new LinkedList<GlUniform>();
	public GlTexture polygonTexture = new GlTexture();
	public GlReflectionMap polygonReflMap = new GlReflectionMap();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		sphereUniforms = new LinkedList<GlUniform>();
		sphereShader = updateAppearance(GLShader.defaultSphereShader, sgp, gl, sphereUniforms, polygonTexture, polygonReflMap, CommonAttributes.POLYGON_SHADER);
	}
}