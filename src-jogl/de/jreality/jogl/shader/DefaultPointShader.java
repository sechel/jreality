/*
 * Created on Apr 29, 2004
 *
 */
package de.jreality.jogl.shader;

import java.awt.Color;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.geometry.GeometryUtility;
import de.jreality.jogl.JOGLRenderer;
import de.jreality.jogl.JOGLSphereHelper;
import de.jreality.jogl.OpenGLState;
import de.jreality.jogl.pick.JOGLPickAction;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPointShader  implements PointShader {
	double pointSize = 1.0;
	double	pointRadius = .1;		
	Color diffuseColor = java.awt.Color.RED;
	float[] diffuseColorAsFloat;
	boolean sphereDraw = false, lighting = true;
	PolygonShader polygonShader = null;
	/**
	 * 
	 */
	public DefaultPointShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		sphereDraw = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPHERES_DRAW), CommonAttributes.SPHERES_DRAW_DEFAULT);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		pointSize = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_SIZE), CommonAttributes.POINT_SIZE_DEFAULT);
		pointRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.POINT_DIFFUSE_COLOR_DEFAULT);	
		double t = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, t);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");
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
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		gl.glPointSize((float) getPointSize());
		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
		}
		
		if (sphereDraw)	{
			polygonShader.render(jr);
		} else lighting = false;
		
		//if (jr.openGLState.lighting != lighting)	{
			jr.openGLState.lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
		//}

		if (jr.openGLState.transparencyEnabled)	{
			gl.glDepthMask(true);
			gl.glDisable(GL.GL_BLEND);			
		}
		
	}

	public boolean providesProxyGeometry() {		
		return sphereDraw;
	}
	
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig, boolean useDisplayLists) {
		GL gl = 	jr.globalGL;
		// TODO handle quadmesh differently
		if (original instanceof PointSet)	{
			PointSet ps = (PointSet) original;
			DataList vertices = ps.getVertexAttributes(Attribute.COORDINATES);
			DataList vertexColors = ps.getVertexAttributes(Attribute.COLORS);
			//JOGLConfiguration.theLog.log(Level.INFO,"VC is "+vertexColors);
			int colorLength = 0;
			if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
			DoubleArray da;
			int n = ps.getNumPoints();
			int dlist = JOGLSphereHelper.getSphereDLists(1, jr);
			int nextDL = -1;
			if (useDisplayLists)	{
				nextDL = gl.glGenLists(1);
				gl.glNewList(nextDL, GL.GL_COMPILE);				
			}
			double[] mat = Rn.identityMatrix(4);
			double[] scale = Rn.identityMatrix(4);
			scale[0] = scale[5] = scale[10] = pointRadius;
			int length = n;
			//JOGLConfiguration.theLog.log(Level.INFO,"Signature is "+sig);
			//sig = Pn.EUCLIDEAN;
			boolean pickMode = jr.isPickMode();
			if (pickMode) gl.glPushName(JOGLPickAction.GEOMETRY_POINT);
			for (int i = 0; i< length; ++i)	{
				double[] transVec = null;
				gl.glPushMatrix();
				transVec =  vertices.item(i).toDoubleArray().toDoubleArray(null);	
				P3.makeTranslationMatrix(mat, transVec,sig);
				Rn.times(mat, mat, scale);
				gl.glMultTransposeMatrixd(mat);
				if (vertexColors != null)	{
					da = vertexColors.item(i).toDoubleArray();
					if (colorLength == 3) 	{
						gl.glColor3d(da.getValueAt(0), da.getValueAt(1), da.getValueAt(2));
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
			if (useDisplayLists) gl.glEndList();
			return nextDL;
		}
		return -1;
	}
	
	public void postRender(JOGLRenderer jr) {
	}

	public Shader getPolygonShader() {
		return polygonShader;
	}


}
