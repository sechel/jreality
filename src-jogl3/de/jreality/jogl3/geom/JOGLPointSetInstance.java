package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.shader.PointShader;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLPointSetInstance extends JOGLGeometryInstance {
	
	GLShader pointShader = GLShader.defaultPointShader;
	
	public JOGLPointSetInstance(PointSet node) {
		super(node);
	}

	@Override
	public void render(JOGLRenderState state) {
		if(eap == null)
			return;
		JOGLPointSetEntity pse = (JOGLPointSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER, CommonAttributes.VERTEX_DRAW), CommonAttributes.VERTEX_DRAW_DEFAULT);
		if(visible)
			PointShader.render(pse, pointSetUniforms, pointShader, state);
			//PointShader.render(state.getGL(), pse.getVertexVBO(), Rn.convertDoubleToFloatArray(state.getModelViewMatrix()), Rn.convertDoubleToFloatArray(state.getProjectionMatrix()));
	}

	public LinkedList<GlUniform> pointSetUniforms = new LinkedList<GlUniform>();
	public GlTexture pointTexture = new GlTexture();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		pointSetUniforms = new LinkedList<GlUniform>();
		pointShader = updateAppearance(GLShader.defaultPointShader, sgp, gl, pointSetUniforms, pointTexture, CommonAttributes.POINT_SHADER);
	}
}