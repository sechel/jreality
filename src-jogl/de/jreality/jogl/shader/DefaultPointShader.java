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
public class DefaultPointShader  implements PointShader {
	double pointSize = 1.0;
	double	pointRadius = 2.0;		// break into facets about this many pixels big
	Color diffuseColor = java.awt.Color.RED;
	boolean sphereDraw = false;
	PolygonShader polygonShader = null;
	/**
	 * 
	 */
	public DefaultPointShader() {
		super();
	}

	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW,false);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE,1);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,0.2);
		ap.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.RED);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		sphereDraw = eap.getAttribute(NameSpace.name(name,CommonAttributes.SPHERES_DRAW), false);
		pointSize = eap.getAttribute(NameSpace.name(name,CommonAttributes.POINT_SIZE), pointSize);
		pointRadius = eap.getAttribute(NameSpace.name(name,CommonAttributes.POINT_RADIUS),pointRadius);
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), diffuseColor);	
		double alpha = diffuseColor.getAlpha();
		double alpha2 = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), alpha );
		if (alpha != alpha2)	{
			float[] f = getDiffuseColorAsFloat();
			f[3] = (float) alpha2;
			diffuseColor = new Color(f[0], f[1], f[2], f[3]);
		}
		polygonShader = ShaderLookup.getPolygonShaderAttr(eap, name, "polygonShader");
		polygonShader.setDiffuseColor(diffuseColor);
		polygonShader.setSmoothShading(true);
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

	public void setDiffuseColor(Color dc) {
		diffuseColor = dc;
	}
	/**
	 * @return
	 */
	public Color getDiffuseColor() {
		return diffuseColor;
	}

	public float[] getDiffuseColorAsFloat() {
		return ColorToFloat(diffuseColor);
	}

	private float[] ColorToFloat(Color cc)	{
		return cc.getRGBComponents(null);
		}

	/**
	 * @param globalHandle
	 * @param jpc
	 */
	public void render(JOGLRendererNew jr) {
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

	public Shader getPolygonShader() {
		return polygonShader;
	}
}
