/*
 * Created on May 7, 2004
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
import de.jreality.util.Rn;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultPolygonShader implements PolygonShader {

	boolean		smoothShading = false; 		// interpolate shaded values between vertices
	Color diffuseColor = DefaultVertexShader.RED; //java.awt.Color.RED;
	Texture2D texture2D;
	public Shader vertexShader = null;
	AbstractJOGLShader glShader = null;
	static double[] idmat = Rn.identityMatrix(4);
	/**
		 * 
		 */
		public DefaultPolygonShader() {
			super();
			vertexShader = new DefaultVertexShader();
		}

		
	public void setDefaultValues(Appearance ap)	{
		ap.setAttribute("textureEnabled",false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,false);
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.RED);
	}
	
	public static DefaultPolygonShader createFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		DefaultPolygonShader dgs = new DefaultPolygonShader();
		dgs.setFromEffectiveAppearance(eap, name);
		return dgs;
	}
	
	static int count = 0;
	public void  setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		smoothShading = eap.getAttribute(NameSpace.name(name,CommonAttributes.SMOOTH_SHADING), smoothShading);	
		diffuseColor = (Color) eap.getAttribute(NameSpace.name(name,CommonAttributes.DIFFUSE_COLOR), diffuseColor);
		double alpha = diffuseColor.getAlpha();
		double alpha2 = eap.getAttribute(NameSpace.name(name,CommonAttributes.TRANSPARENCY), alpha );
		if (alpha != alpha2)	{
			float[] f = getDiffuseColorAsFloat();
			f[3] = (float) alpha2;
			diffuseColor = new Color(f[0], f[1], f[2], f[3]);
		}
		Object foo = eap.getAttribute(NameSpace.name(name,"texture2d"), null, Texture2D.class);
		if (foo instanceof Texture2D)	texture2D = (Texture2D) foo;
		vertexShader = ShaderLookup.getVertexShaderAttr(eap, name, "vertexShader");
	
		//TODO this is a hack. 
		if (eap.getAttribute(NameSpace.name(name,"useGLShader"), false) == true)	{
			Object obj =  eap.getAttribute(NameSpace.name(name,"GLShader"), null, AbstractJOGLShader.class);
			if (obj instanceof AbstractJOGLShader) {
				glShader = (AbstractJOGLShader) obj;
			}
		} else glShader = null;
	
	}

		/**
		 * @return
		 */
		public boolean isSmoothShading() {
			return smoothShading;
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
		//return cc.getComponents(null);
		}
	/**
	 * @return
	 */
	public Texture2D getTexture2D() {
		return texture2D;
	}

	public void render(JOGLRendererNew jr)	{
		GLCanvas theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
		if (isSmoothShading()) 	{
			gl.glShadeModel(GL.GL_SMOOTH);	
		}
		else		{
			gl.glShadeModel(GL.GL_FLAT);		
		}
		//System.out.println("Smooth shading is: "+isSmoothShading());
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, getDiffuseColorAsFloat());
		gl.glColor4fv( getDiffuseColorAsFloat());
		float[] testcolor = {.3f, .5f, .7f, .5f};
		gl.glMaterialfv(GL.GL_BACK, GL.GL_DIFFUSE, testcolor);
		gl.glDisable(GL.GL_TEXTURE_2D);
		if (texture2D != null)	{
			Texture2DLoaderJOGL tl = Texture2DLoaderJOGL.FactoryLoader;
			
			tl.bindTexture2D(theCanvas, texture2D);
			int[] res = new int[1];
			gl.glGetTexParameteriv(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_RESIDENT, res);
			//System.out.println("Texture is resident: "+res[0]);
			if (res[0] == 0)	{ jr.texResident = false; }
			gl.glEnable(GL.GL_TEXTURE_2D);
		} 
		if (glShader != null) glShader.activate(theCanvas);
		vertexShader.render(jr);
	}


	public void setSmoothShading(boolean b) {
		smoothShading = b;
	}
	
	/**
	 * @param diffuseColor2
	 */
	public void setDiffuseColor(Color diffuseColor2) {
		diffuseColor = diffuseColor2;
		
	}


	/**
	 * @return
	 */
	public boolean isTextureEnabled() {
		// TODO Auto-generated method stub
		return false;
	}
}
