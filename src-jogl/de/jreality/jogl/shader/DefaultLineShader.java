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

import de.jreality.jogl.ElementBinding;
import de.jreality.jogl.JOGLRendererNew;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.Texture2D;
import de.jreality.util.EffectiveAppearance;
import de.jreality.util.NameSpace;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultLineShader implements LineShader  {
		double	tubeRadius = 1.0,
			 	lineWidth = 0.05,
				depthFudgeFactor = 0.9999d;			// in pixels
		 int	lineFactor = 1;
		 int 	lineStipplePattern = 0x1c47; 
		 
		 boolean
			lineStipple = false;
	Color diffuseColor = java.awt.Color.BLACK;
	 
		/**
		 * 
		 */
	public DefaultLineShader() {
			super();
		}

			
	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR,1);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,false);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS,.1);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1.0);
		ap.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.BLACK);
	}
	
	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		tubeDraw = eap.getAttribute(NameSpace.name(name, CommonAttributes.TUBES_DRAW), false);
		tubeRadius = eap.getAttribute(NameSpace.name(name,CommonAttributes.TUBE_RADIUS),tubeRadius);
		depthFudgeFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.DEPTH_FUDGE_FACTOR), depthFudgeFactor);
		lineStipple = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE), lineStipple);
		lineWidth = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_WIDTH), lineWidth);
		lineFactor = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_FACTOR),lineFactor);
		lineStipplePattern = eap.getAttribute(NameSpace.name(name,CommonAttributes.LINE_STIPPLE_PATTERN),lineStipplePattern);
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), java.awt.Color.BLACK);
		double alpha = diffuseColor.getAlpha();
		double alpha2 = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), alpha );
		if (alpha != alpha2)	{
			float[] f = getDiffuseColorAsFloat();
			f[3] = (float) alpha2;
			diffuseColor = new Color(f[0], f[1], f[2], f[3]);
		}
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

	boolean tubeDraw = false;
	
	public void render(JOGLRendererNew jr)	{
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
		if (tubeDraw) gl.glEnable(GL.GL_LIGHTING);
		else gl.glDisable(GL.GL_LIGHTING);
		gl.glDepthRange(0.0d, depthFudgeFactor);
	}
		public double getTubeRadius() {
			return tubeRadius;
		}
}
