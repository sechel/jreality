/*
 * Created on Apr 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLCanvas;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;
import de.jreality.util.Rn;
import de.jreality.util.ShaderUtility;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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
	boolean smoothShading = false;
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
		tubeDraw = eap.getAttribute(NameSpace.name(name, CommonAttributes.TUBES_DRAW), CommonAttributes.TUBES_DRAW_DEFAULT);
		tubeRadius = eap.getAttribute(NameSpace.name(name,CommonAttributes.TUBE_RADIUS),CommonAttributes.TUBE_RADIUS_DEFAULT);
		tubeStyle = eap.getAttribute(NameSpace.name(name,CommonAttributes.TUBE_STYLE),CommonAttributes.TUBE_STYLE_DEFAULT);
		depthFudgeFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.DEPTH_FUDGE_FACTOR), depthFudgeFactor);
		smoothShading = eap.getAttribute(NameSpace.name(name,CommonAttributes.INTERPOLATE_VERTEX_COLORS), CommonAttributes.INTERPOLATE_VERTEX_COLORS_DEFAULT);
		lineStipple = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
		lineFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.LINE_DIFFUSE_COLOR_DEFAULT);
		double transp = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		setDiffuseColor( ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transp));
		polygonShader = ShaderLookup.getPolygonShaderAttr(eap, name, "polygonShader");
		//System.out.println("Line shader is smooth: "+smoothShading);
		//System.out.println("Line shader's polygon shader is smooth: "+(polygonShader.isSmoothShading() ? "true" : "false"));
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
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, getDiffuseColorAsFloat());
		// TODO figure out why I have to use this call too, even though
		// GL_COLOR_MATERIAL is disabled.
		//gl.glDisable(GL.GL_COLOR_MATERIAL);
		gl.glColor4fv(getDiffuseColorAsFloat());
		//System.out.println("LineShader: Setting diffuse color to: "+Rn.toString(getDiffuseColorAsFloat()));
	
		gl.glLineWidth((float) getLineWidth());
		if (isLineStipple()) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineStipple(getLineFactor(), (short) getLineStipplePattern());
		} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);
		if (tubeDraw) {
			polygonShader.render(jr);
		}
		else gl.glDisable(GL.GL_LIGHTING);

		// this little bit of code forces tubes to be opaque: could add
		// transparency-enable flag to the line shader to allow this to be controlled
		gl.glDepthMask(true);
		 gl.glDisable(GL.GL_BLEND);

		gl.glDepthRange(0.0d, depthFudgeFactor);
	}

	public boolean providesProxyGeometry() {		
		if (tubeDraw) return true;
		return false;
	}
	
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig) {
		GL gl = jr.globalGL;
		if ( !(original instanceof IndexedLineSet)) return -1;
		if (tubeDraw && original instanceof IndexedLineSet)	{
			int dlist =  createTubesOnEdgesAsDL((IndexedLineSet) original, tubeRadius, 1.0, gl, sig, jr.isPickMode());
			//System.out.println("Creating tubes with radius "+tubeRadius);
			return dlist;
		}
		return -1;
	}
	
	// TOOD figure out how to share this code with TubeUtility
	static double[][] xSection = {{1,0,0}, {.707, .707, 0}, {0,1,0},{-.707, .707, 0},{-1,0,0},{-.707, -.707, 0},{0,-1,0},{.707, -.707, 0}};
//	
	// TOOD figure out how to clear out local display lists (not returned by the method)!
	int[] tubeDL = null;
	boolean testQMS = true;
	public int createTubesOnEdgesAsDL(IndexedLineSet ils, double rad,  double alpha, GL gl, int sig, boolean pickMode)	{
		int n = ils.getNumEdges();
		DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
		System.out.println("Creating tubes for "+ils.getName());
		if (tubeDL == null)	{
			tubeDL = new int[3];
			for (int i = 0; i<3; ++i)	{
				tubeDL[i] = gl.glGenLists(1);
				gl.glNewList(tubeDL[i], GL.GL_COMPILE);
				JOGLRendererHelper.drawFaces(TubeUtility.urTube[i], gl, polygonShader.isSmoothShading(), alpha );
				gl.glEndList();	
			}
		}
		int nextDL = gl.glGenLists(1);
		gl.glNewList(nextDL, GL.GL_COMPILE);
		//System.out.println("Tube radius is "+tubeRadius);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
		gl.glColorMaterial(DefaultPolygonShader.FRONT_AND_BACK, GL.GL_DIFFUSE);
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
//				//System.out.println("Tube has "+tube.getNumPoints()+" points");
//				JOGLRendererHelper.drawFaces(tube, gl, polygonShader.isSmoothShading(), alpha, pickMode, JOGLPickAction.GEOMETRY_LINE);
//			}
			int count = 0;
			for (int i = 0; i<u; ++i)	{
				curve = GeometryUtility.extractUParameterCurve(curve, qms, i);
				tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, closedV, sig, 0);
				GeometryUtility.calculateAndSetNormals(tube);
				//System.out.println("Tube has "+tube.getNumPoints()+" points");
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, count));
				if (pickMode)	gl.glPushName(count++);
				JOGLRendererHelper.drawFaces(tube, gl, polygonShader.isSmoothShading(), alpha);
				if (pickMode) 	gl.glPopName();
			}
			for (int i = 0; i<v; ++i)	{
				curve = GeometryUtility.extractVParameterCurve(curve, qms, i);
				tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, closedU, sig, 0);
				GeometryUtility.calculateAndSetNormals(tube);
				//System.out.println("Tube has "+tube.getNumPoints()+" points");
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, count));
				if (pickMode)	gl.glPushName(count++);
				JOGLRendererHelper.drawFaces(tube, gl, polygonShader.isSmoothShading(), alpha);
				if (pickMode) 	gl.glPopName();
			}
		} else {
		for (int i = 0; i<n; ++i)	{
			int[] ed = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			if (pickMode)	gl.glPushName(i);
			if (m == 2 || pickMode)	{
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
					gl.glCallList(tubeDL[sig+1]);
					if (pickMode) gl.glPopName();
					gl.glPopMatrix();
					
				}
			}
			else {
				//double[][] curve = GeometryUtility.extractCurve(null, ils, i);
				//System.out.println("curve is "+Rn.toString(curve));
				//QuadMeshShape tube = TubeUtility.makeTubeAsIFS(curve, rad, null, tubeStyle, false, sig);
				//QuadMeshShape tube = TubeUtility.makeTubeAsIFS(ils, i, smoothShading, rad, null, tubeStyle, false, sig, 0);
				QuadMeshShape tube = TubeUtility.makeTubeAsIFS(ils, i, false, rad, null, tubeStyle, false, sig, 0);
				//tube.setGeometryAttributes(PROXY_FOR_EDGE, new ProxyTubeIdentifier(ils, i));
				GeometryUtility.calculateAndSetNormals(tube);
				JOGLRendererHelper.drawFaces(tube, gl,  polygonShader.isSmoothShading(), alpha);
			}
			if (pickMode) 	gl.glPopName();					
		}
		}
		if (pickMode) gl.glPopName();
		
		gl.glEndList();
		return nextDL;
	}
	

	public boolean isSmoothShading() {
		return smoothShading;
	}
}
