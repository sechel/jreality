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
import de.jreality.jogl.ElementBinding;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLRendererHelper;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Texture2D;
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
	double	tubeRadius = 0.05,
		 	lineWidth = 1.0,
			depthFudgeFactor = 0.9999d;			// in pixels
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
		depthFudgeFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.DEPTH_FUDGE_FACTOR), depthFudgeFactor);
		lineStipple = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_WIDTH), CommonAttributes.LINE_WIDTH_DEFAULT);
		lineFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.LINE_DIFFUSE_COLOR_DEFAULT);
		double transp = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		setDiffuseColor( ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, transp));
		polygonShader = ShaderLookup.getPolygonShaderAttr(eap, name, "polygonShader");
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
		//System.out.println("Setting diffuse color to: "+getDiffuseColor().toString());
	
		gl.glLineWidth((float) getLineWidth());
		if (isLineStipple()) {
			gl.glEnable(GL.GL_LINE_STIPPLE);
			gl.glLineStipple(getLineFactor(), (short) getLineStipplePattern());
		} 
		else gl.glDisable(GL.GL_LINE_STIPPLE);
		//TODO set this correctly when tube-drawing is supported
		if (tubeDraw) {
			polygonShader.render(jr);
			gl.glEnable(GL.GL_LIGHTING);
		}
		else gl.glDisable(GL.GL_LIGHTING);
		gl.glDepthRange(0.0d, depthFudgeFactor);
	}

	public boolean providesProxyGeometry() {		
		if (tubeDraw) return true;
		return false;
	}
	
	public int proxyGeometryFor(Geometry original, GL gl) {
		// TODO handle quadmesh differently
		if ( !(original instanceof IndexedLineSet)) return -1;
		if (tubeDraw && original instanceof IndexedLineSet)	{
			int dlist =  createTubesOnEdgesAsDL((IndexedLineSet) original, tubeRadius, 1.0, gl);
			System.out.println("Creating tubes with radius "+tubeRadius);
			return dlist;
		}
		return -1;
	}
	
	static double[][] xSection = {{1,0,0}, {.707, .707, 0}, {0,1,0},{-.707, .707, 0},{-1,0,0},{-.707, -.707, 0},{0,-1,0},{.707, -.707, 0}};
	private static double[][] urTubeVerts;
	static QuadMeshShape urTube;
	static int urTubeLength;
	static {
	    int n = xSection.length;
		urTube = new QuadMeshShape(n, 2, true, false);
		urTubeLength = n;
		urTubeVerts = new  double[2*n][3];
		for (int i = 0; i<2; ++i){
			for (int j = 0; j<n; ++j)	{
			    int q = n - j - 1;
			    System.arraycopy(xSection[j], 0, urTubeVerts[i*n+q],0,3);
			    if (i==1) urTubeVerts[i*n+q][2] = 1.0;
			}
		}
		urTube.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(urTubeVerts[0].length).createReadOnly(urTubeVerts));
		GeometryUtility.calculateAndSetNormals(urTube);
	}
	
	public int createTubesOnEdgesAsDL(IndexedLineSet ils, double rad,  double alpha, GL gl)	{
		int n = ils.getNumEdges();
		DataList vertices = ils.getVertexAttributes(Attribute.COORDINATES);
		
		int tubeDL = gl.glGenLists(1);
		gl.glNewList(tubeDL, GL.GL_COMPILE);
		JOGLRendererHelper.drawFaces(urTube, gl, false, true, alpha);
		gl.glEndList();
		
		int nextDL = gl.glGenLists(1);
		gl.glNewList(nextDL, GL.GL_COMPILE);
		for (int i = 0; i<n; ++i)	{
			int[] ed = ils.getEdgeAttributes(Attribute.INDICES).item(i).toIntArray(null);
			int m = ed.length;
			for (int j = 0; j<m-1; ++j)	{
				int k = ed[j];
				double[] p1 = vertices.item(k).toDoubleArray(null);	
				k = ed[j+1];
				double[] p2 = vertices.item(k).toDoubleArray(null);	
				SceneGraphComponent cc = TubeUtility.makeTubeAsIFS(p1, p2, rad, null);
				gl.glPushMatrix();
				gl.glMultTransposeMatrixd(cc.getTransformation().getMatrix());
				gl.glCallList(tubeDL);
				gl.glPopMatrix();
			}
		}
		gl.glEndList();
		return nextDL;
	}
	

}
