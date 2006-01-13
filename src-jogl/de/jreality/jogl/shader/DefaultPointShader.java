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
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;

/**
 * @author Charles Gunn
 *
 */
public class DefaultPointShader  implements PointShader {
	double pointSize = 1.0;
	float[] pointAttenuation = {1.0f, .00f, 0.0f};
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
	static byte[] defaultSphereTexture = new byte[128 * 128 * 4];
	static Appearance a=new Appearance();
	static Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", a, true);
	static {
		for (int i = 0; i<128; ++i)	{
			for (int j = 0; j< 128; ++j)	{
				int I = 4*(i*128+j);
				int sq = (i-64)*(i-64) + (j-64)*(j-64);
				//sq = i*i + j*j;
				if (sq < 4096)	
					{defaultSphereTexture[I] =  defaultSphereTexture[I+1] = defaultSphereTexture[I+2] = defaultSphereTexture[I+3] = (byte) (255- Math.abs(sq/16.0)); }
				else
					{defaultSphereTexture[I] =  defaultSphereTexture[I+1] = defaultSphereTexture[I+2] = defaultSphereTexture[I+3]  = 0;  }
			}
		}
		tex.setImage(new ImageData(defaultSphereTexture, 128, 128));
	}
	public void render(JOGLRenderer jr) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
//		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
//		}
		
//		if (sphereDraw)	{
//		} else {
		if (!sphereDraw)	{
			lighting = false;
			gl.glPointSize((float) getPointSize());
			gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation);
			gl.glEnable(GL.GL_POINT_SPRITE_ARB);
		    gl.glActiveTexture(0);
			gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
			Texture2DLoaderJOGL.render(theCanvas, tex);
			gl.glEnable(GL.GL_TEXTURE_2D);
		} else
		polygonShader.render(jr);
		
		//if (jr.openGLState.lighting != lighting)	{
			jr.openGLState.lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
		//}

//		if (jr.openGLState.transparencyEnabled)	{
//			gl.glDepthMask(true);
//			gl.glDisable(GL.GL_BLEND);			
//		}
		
	}

	public void postRender(JOGLRenderer jr) {
		polygonShader.postRender(jr);
		if (!sphereDraw)	{
			GL gl = jr.globalGL;
			jr.globalGL.glDisable(GL.GL_POINT_SPRITE_ARB);
		    gl.glActiveTexture(0);
			gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_FALSE);
			gl.glDisable(GL.GL_TEXTURE_2D);
		}
	}

	public boolean providesProxyGeometry() {		
		return sphereDraw;
	}
	
	public int proxyGeometryFor(Geometry original, JOGLRenderer jr, int sig, boolean useDisplayLists) {
		GL gl = 	jr.globalGL;
		if (original instanceof PointSet)	{
			PointSet ps = (PointSet) original;
			DataList vertices = ps.getVertexAttributes(Attribute.COORDINATES);
			DataList vertexColors = ps.getVertexAttributes(Attribute.COLORS);
			//JOGLConfiguration.theLog.log(Level.INFO,"VC is "+vertexColors);
			int colorLength = 0;
			if (vertexColors != null) colorLength = GeometryUtility.getVectorLength(vertexColors);
			DoubleArray da;
			int n = ps.getNumPoints();
			int resolution = 1;
			if (jr.openGLState.levelOfDetail == 0.0) resolution = 0;
			int dlist = JOGLSphereHelper.getSphereDLists(resolution, jr);
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
	
	public Shader getPolygonShader() {
		return polygonShader;
	}


}
