package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.geom.JOGLGeometryInstance.GlUniform;
import de.jreality.jogl3.shader.LineShader;
import de.jreality.jogl3.shader.TubesLineShader;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLLineSetInstance extends JOGLPointSetInstance {

	//GLShader lineShader = GLShader.defaultLineShader;
	GLShader lineShader = GLShader.defaultPolygonLineShader;
	
	public JOGLLineSetInstance(IndexedLineSet node) {
		super(node);
	}
	public void render(JOGLRenderState state) {
		if(eap==null)
			return;
		super.render(state);
		JOGLLineSetEntity lse = (JOGLLineSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.EDGE_DRAW), CommonAttributes.EDGE_DRAW_DEFAULT);
		if(visible)
			TubesLineShader.render(lse, lineSetUniforms, lineShader, state);
	}
	
	public LinkedList<GlUniform> lineSetUniforms = new LinkedList<GlUniform>();
	public GlTexture lineTexture = new GlTexture();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		super.updateAppearance(sgp, gl);
		lineSetUniforms = new LinkedList<GlUniform>();
		lineShader = updateAppearance(sgp, gl, lineSetUniforms, lineTexture, CommonAttributes.LINE_SHADER);
		lineShader = updateAppearance(sgp, gl, lineSetUniforms, lineTexture, "lineShader.polygonShader");
	}
}
