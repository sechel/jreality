/*
 * Created on Apr 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import de.jreality.jogl.JOGLRendererNew;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultGeometryShader  implements Shader {
		
		boolean faceDraw = true, 
		 	vertexDraw = false, 
		 	edgeDraw = true;
		// these should be more general shaders, but since we only have one type of each ...
		public PolygonShader polygonShader;
		public LineShader lineShader;
		public PointShader pointShader;
		/**
		 * 
		 */
		public DefaultGeometryShader() {
			super();
			polygonShader = new DefaultPolygonShader();
			lineShader = new DefaultLineShader();
			pointShader = new DefaultPointShader();
		}
		
	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute(CommonAttributes.FACE_DRAW,true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW,true);
		ap.setAttribute(CommonAttributes.VERTEX_DRAW,false);
		if (polygonShader == null) polygonShader = new DefaultPolygonShader();
		polygonShader.setDefaultValues(ap);
		if (lineShader == null) lineShader = new DefaultLineShader();
		lineShader.setDefaultValues(ap);
		if (pointShader == null) pointShader = new DefaultPointShader();
		pointShader.setDefaultValues(ap);
		
	}
	
	public static DefaultGeometryShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		DefaultGeometryShader dgs = new DefaultGeometryShader();
		dgs.setFromEffectiveAppearance(eap, name);
		return dgs;
	}
	
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		String geomShaderName = "";		// at a later date this may be a field;
		vertexDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.VERTEX_DRAW), vertexDraw);
		edgeDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.EDGE_DRAW), edgeDraw);
		faceDraw = eap.getAttribute(NameSpace.name(geomShaderName, CommonAttributes.FACE_DRAW), faceDraw);
		
		if(faceDraw) {
	        polygonShader =ShaderLookup.getPolygonShaderAttr(eap, geomShaderName, CommonAttributes.POLYGON_SHADER);
	    } else {
	    		polygonShader = null;
	    }
	    if(edgeDraw) {
	    		lineShader =ShaderLookup.getLineShaderAttr(eap, geomShaderName, CommonAttributes.LINE_SHADER);
	    } else {
	        	lineShader = null;
	    }
	       
	    if(vertexDraw) {
	        pointShader=ShaderLookup.getPointShaderAttr(eap, geomShaderName, CommonAttributes.POINT_SHADER);
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
		public void render(JOGLRendererNew jr) {
		}


}
