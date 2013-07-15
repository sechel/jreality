package de.jreality.jogl3.geom;

import java.util.LinkedList;

import javax.media.opengl.GL3;

import de.jreality.jogl3.JOGLRenderState;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.shader.PointShader;
import de.jreality.jogl3.shader.SpherePointShader;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.ShaderUtility;

public class JOGLPointSetInstance extends JOGLGeometryInstance {
	
	GLShader pointSphereShader = GLShader.defaultPointSphereShader;
	GLShader pointShader = GLShader.defaultPointShader;
	
	public JOGLPointSetInstance(PointSet node) {
		super(node);
	}

	@Override
	public void render(JOGLRenderState state, int width, int height) {
		if(eap == null)
			return;
		JOGLPointSetEntity pse = (JOGLPointSetEntity) getEntity();
		boolean visible = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER, CommonAttributes.VERTEX_DRAW), CommonAttributes.VERTEX_DRAW_DEFAULT);
		if(visible){
			boolean spheresDraw = (boolean)eap.getAttribute(ShaderUtility.nameSpace(CommonAttributes.POINT_SHADER, CommonAttributes.SPHERES_DRAW), CommonAttributes.SPHERES_DRAW_DEFAULT);
			if(spheresDraw)
				SpherePointShader.render(pse, pointSetPolygonUniforms, pointReflMap, pointSphereShader, state);
			else{
				PointShader.render(pse, pointSetUniforms, pointShader, state);
			}
		}
	}
	@Override
	public void renderDepth(JOGLRenderState state, int width, int height) {
		
	}

	@Override
	public void addOneLayer(JOGLRenderState state, int width, int height, float alpha) {
		
	}
	
	public LinkedList<GlUniform> pointSetUniforms = new LinkedList<GlUniform>();
	public LinkedList<GlUniform> pointSetPolygonUniforms = new LinkedList<GlUniform>();
	public GlTexture pointTexture = new GlTexture();
	public GlReflectionMap pointReflMap = new GlReflectionMap();
	@Override
	public void updateAppearance(SceneGraphPath sgp, GL3 gl) {
		pointSetUniforms = new LinkedList<GlUniform>();
		
		//pointSphereShader = updateAppearance(GLShader.defaultPointShader, sgp, gl, pointSetPolygonUniforms, pointTexture, new GlReflectionMap(), CommonAttributes.POINT_SHADER);
		pointSphereShader = updateAppearance(GLShader.defaultPointSphereShader, sgp, gl, pointSetPolygonUniforms, pointTexture, pointReflMap, "pointShader.polygonShader");
		
		pointShader = updateAppearance(GLShader.defaultPointShader, sgp, gl, pointSetUniforms, pointTexture, new GlReflectionMap(), CommonAttributes.POINT_SHADER);
	}
}