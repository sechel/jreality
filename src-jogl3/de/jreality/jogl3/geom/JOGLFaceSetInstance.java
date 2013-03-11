package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.helper.TransparencyHelper;
import de.jreality.jogl3.shader.PolygonShader;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLFaceSetInstance extends JOGLLineSetInstance {

	//GLShader polygonShader = new DefaultPolygonShader();
	GLShader polygonShader = GLShader.defaultPolygonShader;
	GLShader polygonShaderDepth = TransparencyHelper.depth;
	GLShader polygonShaderTransp = TransparencyHelper.transp;
	public JOGLFaceSetInstance(IndexedFaceSet node) {
		super(node);
	}
	
	@Override
	public void render(JOGLRenderState state, int width, int height) {
		if(eap==null)
			return;
		super.render(state, width, height);
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		boolean transparencyEnabled = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.TRANSPARENCY_ENABLED), false);
		if(visible && !transparencyEnabled)
			PolygonShader.render(fse, faceSetUniforms, faceTexture, reflMap, polygonShader, state);
	}

	@Override
	public void renderDepth(JOGLRenderState state, int width, int height) {
		if(eap==null)
			return;
		super.renderDepth(state, width, height);
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(visible)
			PolygonShader.renderDepth(fse, polygonShaderDepth, state, width, height);
	}

	@Override
	public void addOneLayer(JOGLRenderState state, int width, int height, float alpha) {
		if(eap==null)
			return;
		super.addOneLayer(state, width, height, alpha);
		JOGLFaceSetEntity fse = (JOGLFaceSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POLYGON_SHADER, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(visible)
			PolygonShader.addOneLayer(fse, faceSetUniforms, faceTexture, reflMap, polygonShaderTransp, state, width, height, alpha);
	}
	
	public LinkedList<GlUniform> faceSetUniforms = new LinkedList<GlUniform>();
	public GlTexture faceTexture = new GlTexture();
	public GlReflectionMap reflMap = new GlReflectionMap();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		super.updateAppearance(sgp, gl);
//		JOGLFaceSetEntity entity = (JOGLFaceSetEntity)this.getEntity();
//		IndexedFaceSet fs = (IndexedFaceSet)entity.getNode();
		faceSetUniforms = new LinkedList<GlUniform>();
		polygonShader = updateAppearance(GLShader.defaultPolygonShader, sgp, gl, faceSetUniforms, faceTexture, reflMap, CommonAttributes.POLYGON_SHADER);
	}

}
