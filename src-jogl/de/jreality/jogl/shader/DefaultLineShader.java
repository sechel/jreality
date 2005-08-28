/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;
import java.util.logging.Level;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.geometry.*;
import de.jreality.jogl.JOGLConfiguration;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultLineShader implements LineShader  {
	public static Attribute PROXY_FOR_EDGE = Attribute.attributeForName("proxyForEdge");
	public class ProxyTubeIdentifier	{
		public IndexedLineSet originalGeometry;
		public int edgeNumber;
		public ProxyTubeIdentifier(IndexedLineSet ils, int en)	{
			super();
			originalGeometry = ils;
			edgeNumber = en;
		}
	}
	
	int 	tubeStyle = TubeUtility.PARALLEL;
	double	tubeRadius = 0.05,
		 	lineWidth = 1.0,
			depthFudgeFactor = 0.9999d;			// in pixels
	boolean interpolateVertexColors = false, lighting;
	int	lineFactor = 1;
	int 	lineStipplePattern = 0x1c47; 
	 
	boolean lineStipple = false;
	boolean tubeDraw = false;
			
	Color diffuseColor = java.awt.Color.BLACK;
	private PolygonShader polygonShader;
	 
		/**
		 * 
		 */
	public DefaultLineShader() {
			super();
		}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		tubeDraw = eap.getAttribute(ShaderUtility.nameSpace(name, CommonAttributes.TUBES_DRAW), CommonAttributes.TUBES_DRAW_DEFAULT);
		tubeRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
		tubeStyle = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TUBE_STYLE),CommonAttributes.TUBE_STYLE_DEFAULT);
		depthFudgeFactor = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DEPTH_FUDGE_FACTOR), depthFudgeFactor);
		interpolateVertexColors = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.INTERPOLATE_VERTEX_COLORS), CommonAttributes.INTERPOLATE_VERTEX_COLORS_DEFAULT);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		lineStipple = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
		lineFactor = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.LINE_DIFFUSE_COLOR_DEFAULT);
		double transp = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		setDiffuseColor( ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transp));
		polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");
		//JOGLConfiguration.theLog.log(Level.FINE,"Line shader is smooth: "+smoothShading);
		//JOGLConfiguration.theLog.log(Level.FINE,"Line shader's polygon shader is smooth: "+(polygonShader.isSmoothShading() ? "true" : "false"));
		//polygonShader.setDiffuseColor(diffuseColor);
	}

	public double getDepthFudgeFactor() {
		return depthFudgeFactor;
	}
	/**
	 * @return
	 */
	public double getLineWidth() {
		return lineWidth;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @return
	 */
	public boolean isLineStipple() {
		return lineStipple;
	}

	/**
	 * @return
	 */
	public int getLineStipplePattern() {
		return lineStipplePattern;
	}

	/**
	 * @return
	 */
	public int getLineFactor() {
		return lineFactor;
	}

	/**
	 * @return
	 */
	public boolean isTubeDraw() {
		return tubeDraw;
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}
	float[] diffuseColorAsFloat;
	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}

	public void setDiffuseColor(Color diffuseColor2) {
		diffuseColor = diffuseColor2;
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
	}

	public double getTubeRadius() {
		return tubeRadius;
	}
	public void render(JOGLRenderer jr)	{
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, diffuseColorAsFloat);
		// TODO figure out why I have to use this call too, even though
		// GL_COLOR_MATERIAL is disabled.
		//gl.glDisable(GL.GL_COLOR_MATERIAL);
		//gl.glColor4fv(getDiffuseColorAsFloat());
		//if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
		//}
		//JOGLConfiguration.theLog.log(Level.FINE,"LineShader: Setting diffuse color to: "+Rn.toString(getDiffuseColorAsFloat()));
	
		gl.glLineWidth((float) getLineWidth());
		if (isLineStipple()) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineStipple(getLineFactor(), (short) getLineStipplePattern());
		} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);

//		boolean lighting = false;
		if (tubeDraw)	{
			polygonShader.render(jr);
			//lighting = true;
		} else lighting = false;
//		if (jr.openGLState.lighting != lighting)	{
		//else {
			jr.openGLState.lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
			
		//}

		// this little bit of code forces tubes to be opaque: could add
		// transparency-enable flag to the line shader to allow this to be controlled
		gl.glDepthMask(true);
		gl.glDisable(GL.GL_BLEND);

		if (!tubeDraw) gl.glDepthRange(0.0d, depthFudgeFactor);
	}

	public boolean providesProxyGeometry() {		
		if (tubeDraw) return true;
		return false;
	}
	
	public int proxyGeometryFor(final Geometry original, final JOGLRenderer jr, final int sig, final boolean useDisplayLists) {
		if ( !(original instanceof IndexedLineSet)) return -1;
		if (tubeDraw && original instanceof IndexedLineSet)	{
      final int[] dlist = new int[1];
      Scene.executeReader(original, new Runnable() {
        public void run() {
    			dlist[0] = createTubesOnEdgesAsDL((IndexedLineSet) original, tubeRadius, 1.0, jr, sig, jr.isPickMode(), useDisplayLists);
			    //JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes with radius "+tubeRadius);
        }
      });
			return dlist[0];
		}
		return -1;
	}
	
	// TODO figure out how to share this code with TubeUtility
	static double[][] xSection = {{1,0,0}, {.707, .707, 0}, {0,1,0},{-.707, .707, 0},{-1,0,0},{-.707, -.707, 0},{0,-1,0},{.707, -.707, 0}};
//	
	// TOOD figure out how to clear out local display lists (not returned by the method)!
	int[] tubeDL = null;
	boolean testQMS = true;
	boolean smoothShading = true;		// force tubes to be smooth shaded ?
	public int createTubesOnEdgesAsDL(IndexedLineSet ils, double rad,  double alpha, JOGLRenderer jr, int sig, boolean pickMode, boolean useDisplayLists)	{
		GL gl = jr.globalGL;
		
		int n = ils.getNumEdges();
		DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
		if (ils.getNumPoints() <= 1) return -1;
		JOGLConfiguration.theLog.log(Level.FINE,"Creating tubes for "+ils.getName());
		if (tubeDL == null)	{
			tubeDL = new int[3];
			for (int i = 0; i<3; ++i)	{
				tubeDL[i] = gl.glGenLists(1);
				gl.glNewList(tubeDL[i], GL.GL_COMPILE);
				JOGLRendererHelper.drawFaces(TubeUtility.urTube[i], jr, smoothShading, alpha );
				gl.glEndList();	
			}
		}
		int nextDL = -1;
		if (useDisplayLists) {
			nextDL = gl.glGenLists(1);
			gl.glNewList(nextDL, GL.GL_COMPILE);
		}
		//JOGLConfiguration.theLog.log(Level.FINE,"Tube radius is "+tubeRadius);
		//gl.glEnable(GL.GL_COLOR_MATERIAL);
		//gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK, GL.GL_DIFFUSE);
		if (pickMode)	gl.glPushName(JOGLPickAction.GEOMETRY_LINE);
		if (!pickMode && testQMS && ils instanceof QuadMeshShape)	{
			QuadMeshShape qms = (QuadMeshShape) ils;
			int u = qms.getMaxU();
			int v = qms.getMaxV();
			boolean closedU = qms.isClosedInUDirection();
			boolean closedV = qms.isClosedInVDirection();
			double[][] curve = null;
			IndexedFaceSet tube = null;
//			int numEdges = qms.getNumEdges();
//			for (int i = 0; i<numEdges; ++i)	{
//				curve = GeometryUtility.extractCurve(curve, qms, i);
//				tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, false, sig, 0);
//				GeometryUtility.calculateAndSetNormals(tube);
//				//JOGLConfiguration.theLog.log(Level.FINE,"Tube has "+tube.getNumPoints()+" points");
//				JOGLRendererHelper.drawFaces(tube, gl, polygonShader.isSmoothShading(), alpha, pickMode, JOGLPickAction.GEOMETRY_LINE);
//			}
			int count = 0;
			for (int i = 0; i<u; ++i)	{
				curve = QuadMeshUtility.extractUParameterCurve(curve, qms, i);
				tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, closedV, sig, 0);
				GeometryUtility.calculateAndSetNormals(tube);
				//JOGLConfiguration.theLog.log(Level.FINE,"Tube has "+tube.getNumPoints()+" points");
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, count));
				if (pickMode)	gl.glPushName(count++);
				JOGLRendererHelper.drawFaces(tube, jr, smoothShading, alpha);
				if (pickMode) 	gl.glPopName();
			}
			for (int i = 0; i<v; ++i)	{
				curve = QuadMeshUtility.extractVParameterCurve(curve, qms, i);
				tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, closedU, sig, 0);
				GeometryUtility.calculateAndSetNormals(tube);
				//JOGLConfiguration.theLog.log(Level.FINE,"Tube has "+tube.getNumPoints()+" points");
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, count));
				if (pickMode)	gl.glPushName(count++);
				JOGLRendererHelper.drawFaces(tube, jr, smoothShading, alpha);
				if (pickMode) 	gl.glPopName();
			}
		} else {
		for (int i = 0; i<n; ++i)	{
			int[] ed = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			DataList edgec =  ils.getEdgeAttributes(Attribute.COLORS);
			int m = ed.length;
			if (pickMode)	gl.glPushName(i);
			if (m == 2 || pickMode)	{
				double[] edgecolor = null;
				int clength = 3;
				if (edgec != null) {
					edgecolor = edgec.item(i).toDoubleArray(null);
					clength = edgecolor.length;
				}
				
				for (int j = 0; j<m-1; ++j)	{
					int k = ed[j];
					double[] p1 = vertices.item(k).toDoubleArray(null);	
					k = ed[j+1];
					double[] p2 = vertices.item(k).toDoubleArray(null);	
					SceneGraphComponent cc = TubeUtility.tubeOneEdge(p1, p2, rad, null, sig);
					//cc.getGeometry().setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, i));
					gl.glPushMatrix();
					gl.glMultTransposeMatrixd(cc.getTransformation().getMatrix());
					if (pickMode) gl.glPushName(j);
					if (edgec != null) 
						if (clength == 3) gl.glColor3dv(edgecolor);
						else gl.glColor4dv(edgecolor);
					gl.glCallList(tubeDL[sig+1]);
					if (pickMode) gl.glPopName();
					gl.glPopMatrix();
	
				}
			}
			else {
				//double[][] curve = GeometryUtility.extractCurve(null, ils, i);
				//JOGLConfiguration.theLog.log(Level.FINE,"curve is "+Rn.toString(curve));
				//QuadMeshShape tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, false, sig);
				//QuadMeshShape tube = TubeUtility.makeTubeAsIFS(ils, i, smoothShading, rad, null, tubeStyle, false, sig, 0);
				QuadMeshShape tube = TubeUtility.makeTubeAsIFS(ils, i, false, rad, null, tubeStyle, false, sig, 0);
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, i));
				if (tube != null)	{
					GeometryUtility.calculateAndSetNormals(tube);
					JOGLRendererHelper.drawFaces(tube, jr,  smoothShading, alpha);					
				}
			}
			if (pickMode) 	gl.glPopName();					
		}
		}
		if (pickMode) gl.glPopName();
		
		if (useDisplayLists) gl.glEndList();
		return nextDL;
	}
	
	public void postRender(JOGLRenderer jr) {
		if (!tubeDraw) jr.getCanvas().getGL().glDepthRange(0.0d, 1d);
	}

	public boolean isSmoothShading() {
		return interpolateVertexColors;
	}

}
