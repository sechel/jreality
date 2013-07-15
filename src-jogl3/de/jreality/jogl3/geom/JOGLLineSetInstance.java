package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.shader.LineShader;
import de.jreality.jogl3.shader.TubesLineShader;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLLineSetInstance extends JOGLPointSetInstance {

	//GLShader lineShader = GLShader.defaultLineShader;
	GLShader linePolygonShader = GLShader.defaultPolygonLineShader;
	GLShader lineShader = GLShader.defaultLineShader;
	
	public JOGLLineSetInstance(IndexedLineSet node) {
		super(node);
	}
	public void render(JOGLRenderState state, int width, int height) {
		if(eap==null)
			return;
		super.render(state, width, height);
		JOGLLineSetEntity lse = (JOGLLineSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.EDGE_DRAW), CommonAttributes.EDGE_DRAW_DEFAULT);
		if(visible){
			boolean tubesDraw = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.TUBES_DRAW), CommonAttributes.TUBES_DRAW_DEFAULT);
			if(tubesDraw)
				TubesLineShader.render(lse, lineSetPolygonUniforms, lineReflMap, linePolygonShader, state, width, height);
			else{
				float lineWidth = (float)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.LINE_SHADER, CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
	        	LineShader.render(lse, lineSetUniforms, lineShader, state, lineWidth);
			}
		}
	}
	@Override
	public void renderDepth(JOGLRenderState state, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOneLayer(JOGLRenderState state, int width, int height, float alpha) {
		// TODO Auto-generated method stub
		
	}
	public LinkedList<GlUniform> lineSetUniforms = new LinkedList<GlUniform>();
	public LinkedList<GlUniform> lineSetPolygonUniforms = new LinkedList<GlUniform>();
	public GlTexture lineTexture = new GlTexture();
	public GlReflectionMap lineReflMap = new GlReflectionMap();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		super.updateAppearance(sgp, gl);
		lineSetUniforms = new LinkedList<GlUniform>();
		lineSetPolygonUniforms = new LinkedList<GlUniform>();
		
		linePolygonShader = updateAppearance(GLShader.defaultPolygonLineShader, sgp, gl, lineSetPolygonUniforms, lineTexture, lineReflMap, "lineShader.polygonShader");
		
		lineShader = updateAppearance(GLShader.defaultLineShader, sgp, gl, lineSetUniforms, lineTexture, new GlReflectionMap(), CommonAttributes.LINE_SHADER);
		
	}
}
