package de.jreality.jogl3.geom;

import java.awt.Color;
import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.GLShader;
import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.GLShader.ShaderVar;
import de.jreality.jogl3.shader.PolygonShader;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

public class JOGLFaceSetInstance extends JOGLLineSetInstance {

	//GLShader polygonShader = new DefaultPolygonShader();
	GLShader polygonShader = GLShader.defaultPolygonShader;
	public JOGLFaceSetInstance(IndexedFaceSet node) {
		super(node);
	}
	
	@Override
	public void render(JOGLRenderState state) {
		super.render(state);
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(visible)
			PolygonShader.render(fse, faceSetUniforms, faceTexture, polygonShader, state);
	}
	
	public LinkedList<GlUniform> faceSetUniforms = new LinkedList<GlUniform>();
	public GlTexture faceTexture = new GlTexture();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
//		System.out.println("UpdateAppearance");
		super.updateAppearance(sgp, gl);
		JOGLFaceSetEntity entity = (JOGLFaceSetEntity)this.getEntity();
		IndexedFaceSet fs = (IndexedFaceSet)entity.getNode();
		polygonShader = updateAppearance(sgp, gl, faceSetUniforms, faceTexture, CommonAttributes.POLYGON_SHADER);
	}

}
