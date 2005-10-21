/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRenderer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

/**
 * @author Charles Gunn
 *
 */
public class DefaultGeometryShader  implements Shader {
		
		boolean faceDraw = true, 
		 	vertexDraw = false, 
		 	edgeDraw = true;
		// these should be more general shaders, but since we only have one type of each ...
		public PolygonShader polygonShader;
		public LineShader lineShader;
		public PointShader pointShader;
//		public DefaultPolygonShader polygonShaderNew;
		/**
		 * 
		 */
		public DefaultGeometryShader() {
			super();
//			polygonShader = new DefaultPolygonShader();
//			lineShader = new DefaultLineShader();
//			pointShader = new DefaultPointShader();
		}
			
	public static DefaultGeometryShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		DefaultGeometryShader dgs = new DefaultGeometryShader();
		dgs.setFromEffectiveAppearance(eap, name);
		return dgs;
	}
	
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		String geomShaderName = "";		// at a later date this may be a field;
		vertexDraw = eap.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.VERTEX_DRAW), CommonAttributes.VERTEX_DRAW_DEFAULT);
		edgeDraw = eap.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.EDGE_DRAW), CommonAttributes.EDGE_DRAW_DEFAULT );
		faceDraw = eap.getAttribute(ShaderUtility.nameSpace(geomShaderName, CommonAttributes.FACE_DRAW), CommonAttributes.FACE_DRAW_DEFAULT);
		if(faceDraw) {
//		    if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name,"polygonShader"), eap))
//		        polygonShaderNew = (DefaultPolygonShader) AttributeEntityUtility.createAttributeEntity(DefaultPolygonShader.class, ShaderUtility.nameSpace(name,"polygonShader"), eap);
	        	polygonShader =(PolygonShader) ShaderLookup.getShaderAttr(eap, geomShaderName, CommonAttributes.POLYGON_SHADER);
	    } else {
//	    		polygonShaderNew = null;
	    		polygonShader = null;
	    }
	    if(edgeDraw) {
	    		lineShader =(LineShader) ShaderLookup.getShaderAttr(eap, geomShaderName, CommonAttributes.LINE_SHADER);
	    } else {
	        	lineShader = null;
	    }
	       
	    if(vertexDraw) {
	        pointShader=(PointShader) ShaderLookup.getShaderAttr(eap, geomShaderName, CommonAttributes.POINT_SHADER);
	    } else {
	        pointShader=null;
	    }
	}

		/**
		 * @return
		 */
		public boolean isEdgeDraw() {
			return edgeDraw;
		}

		/**
		 * @return
		 */
		public boolean isFaceDraw() {
			return faceDraw;
		}

		/**
		 * @return
		 */
		public boolean isVertexDraw() {
			return vertexDraw;
		}

		/**
		 * @return
		 */
		public Shader getLineShader() {
			return lineShader;
		}

		/**
		 * @return
		 */
		public Shader getPointShader() {
			return pointShader;
		}

		/**
		 * @return
		 */
		public Shader getPolygonShader() {
			return polygonShader;
		}

		/* (non-Javadoc)
		 * @see de.jreality.jogl.shader.Shader#render(de.jreality.jogl.JOGLRendererNew)
		 */
		public void render(JOGLRenderer jr) {
		}

		public void postRender(JOGLRenderer jr) {
		}

}
