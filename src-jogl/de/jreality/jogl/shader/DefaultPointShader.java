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
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.ShaderUtility;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultPointShader  implements PointShader {
	double pointSize = 1.0;
	double	pointRadius = .1;		
	Color diffuseColor = java.awt.Color.RED;
	float[] diffuseColorAsFloat;
	boolean sphereDraw = false;
	PolygonShader polygonShader = null;
	/**
	 * 
	 */
	public DefaultPointShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		sphereDraw = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPHERES_DRAW), CommonAttributes.SPHERES_DRAW_DEFAULT);
		pointSize = eap.getAttribute(NameSpace.name(name,CommonAttributes.POINT_SIZE), CommonAttributes.POINT_SIZE_DEFAULT);
		pointRadius = eap.getAttribute(NameSpace.name(name,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
		Color diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.POINT_DIFFUSE_COLOR_DEFAULT);	
		double t = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, t);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		polygonShader = ShaderLookup.getPolygonShaderAttr(eap, name, "polygonShader");
		//polygonShader.setDiffuseColor(diffuseColor);
		//polygonShader.setSmoothShading(true);
	}


	/**
	 * @return
	 */
	public boolean isSphereDraw() {
		return sphereDraw;
	}

	/**
	 * @return
	 */
	public double getPointSize() {
		return pointSize;
	}

	/**
	 * @return
	 */
	public double getPointRadius() {
		return pointRadius;
	}

	public float[] getDiffuseColorAsFloat() {
		return diffuseColorAsFloat;
	}

	public Color getDiffuseColor() {
		return diffuseColor;
	}
	/**
	 * @param globalHandle
	 * @param jpc
	 */
	public void render(JOGLRenderer jr) {
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glPointSize((float) getPointSize());
		gl.glColor4fv(getDiffuseColorAsFloat());
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, getDiffuseColorAsFloat());
		if (sphereDraw)	{
			polygonShader.render(jr);
			gl.glEnable(GL.GL_LIGHTING);
		}
		else gl.glDisable(GL.GL_LIGHTING);
		
	}

	public boolean providesProxyGeometry() {		
		return sphereDraw;
	}
	
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig) {
		GL gl = 	jr.globalGL;
		// TODO handle quadmesh differently
		if (sphereDraw && original instanceof PointSet)	{
			PointSet ps = (PointSet) original;
			DataList vertices = ps.getVertexAttributes(Attribute.COORDINATES);
			DataList vertexColors = ps.getVertexAttributes(Attribute.COLORS);
			int colorLength = 0;
			if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
			DoubleArray da;
			int n = ps.getNumPoints();
			int nextDL = gl.glGenLists(1);
			int dlist = JOGLSphereHelper.getSphereDLists(1, gl);
			gl.glNewList(nextDL, GL.GL_COMPILE);
			double[] mat = Rn.identityMatrix(4);
			double[] scale = Rn.identityMatrix(4);
			scale[0] = scale[5] = scale[10] = pointRadius;
			//System.out.println("Signature is "+sig);
			//sig = Pn.EUCLIDEAN;
			boolean pickMode = jr.isPickMode();
			if (pickMode) gl.glPushName(JOGLPickAction.GEOMETRY_POINT);
			for (int i = 0; i< n; ++i)	{
				da = vertices.item(i).toDoubleArray();	
				gl.glPushMatrix();
				
				P3.makeTranslationMatrix(mat, da.toDoubleArray(null),sig);
				Rn.times(mat, mat, scale);
				gl.glMultTransposeMatrixd(mat);
				if (vertexColors != null)	{
					da = vertexColors.item(i).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), 1.0);
					} else if (colorLength == 4) 	{
						gl.glColor4d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2), 1.0*da.getValueAt(3));
					} 
				}
				if (pickMode) gl.glPushName(i);
				gl.glCallList(dlist);
				if (pickMode) gl.glPopName();
				gl.glPopMatrix();
			}
			if (pickMode) gl.glPopName();
			gl.glEndList();
			//System.out.println("Creating spheres with radius "+pointRadius);
			return nextDL;
		}
		return -1;
	}
	
	public Shader getPolygonShader() {
		return polygonShader;
	}

}
