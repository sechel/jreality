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
	// on my mac, the only value for the following array that seems to "work" is {1,0,0}.  WHY?
	float[] pointAttenuation = {1.0f, .0f, 0.00000f};
	double	pointRadius = .1;		
	Color diffuseColor = java.awt.Color.RED;
	float[] diffuseColorAsFloat;
	float[] specularColorAsFloat = {0f,1f,1f,1f};		// for texturing point sprite to simulate sphere
	boolean sphereDraw = false, lighting = true;
	PolygonShader polygonShader = null;
	Appearance a=new Appearance();
	Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", a, true);
  static Texture2D currentTex;
	double specularExponent = 60.0;
	
	/**
	 * 
	 */
	public DefaultPointShader() {
		super();
	}

	public void setFromEffectiveAppearance(EffectiveAppearance eap, String name)	{
		sphereDraw = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.SPHERES_DRAW), CommonAttributes.SPHERES_DRAW_DEFAULT);
		lightDirection = (double[]) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHT_DIRECTION),lightDirection);
		lighting = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.LIGHTING_ENABLED), true);
		pointSize = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_SIZE), CommonAttributes.POINT_SIZE_DEFAULT);
		pointRadius = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POINT_RADIUS),CommonAttributes.POINT_RADIUS_DEFAULT);
		diffuseColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.DIFFUSE_COLOR), CommonAttributes.POINT_DIFFUSE_COLOR_DEFAULT);	
		double t = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.TRANSPARENCY), CommonAttributes.TRANSPARENCY_DEFAULT );
		diffuseColor = ShaderUtility.combineDiffuseColorWithTransparency(diffuseColor, t);
		diffuseColorAsFloat = diffuseColor.getRGBComponents(null);
		polygonShader = (PolygonShader) ShaderLookup.getShaderAttr(eap, name, "polygonShader");

		if (!sphereDraw)	{
	      if (AttributeEntityUtility.hasAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap))
	    	  currentTex = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace(name, "pointSprite"), eap);
	      else {
	  			Rn.normalize(lightDirection, lightDirection);
	  			specularColor = (Color) eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_COLOR), CommonAttributes.SPECULAR_COLOR_DEFAULT);
	  			specularColorAsFloat = specularColor.getRGBComponents(null);
	  			specularExponent = eap.getAttribute(ShaderUtility.nameSpace(name,CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SPECULAR_EXPONENT), CommonAttributes.SPECULAR_EXPONENT_DEFAULT);
	  			setupTexture();
	  			currentTex=tex;
	      }
	  }
	}


	byte[] sphereTex;
	double[] lightDirection = {1,-1,2};
	
	private void setupTexture() {
		int I = 0, II = 0;
		double[] reflected = new double[3];
		if (sphereTex != null) return;
		//System.out.println("specular color is "+specularColor.toString());
		//if (sphereTex == null) 
			sphereTex = new byte[textureSize * textureSize * 4];
		for (int i = 0; i<textureSize; ++i)	{
			for (int j = 0; j< textureSize; ++j)	{
				if (sphereVertices[I][0] != -1)	{	
					double diffuse = Rn.innerProduct(lightDirection, sphereVertices[I]);
					if (diffuse < 0) diffuse = 0;
					if (diffuse > 1.0) diffuse =1.0;
					double z = sphereVertices[I][2];
					reflected[0] = 2*sphereVertices[I][0]*z;
					reflected[1] = 2*sphereVertices[I][1]*z;
					reflected[2] = 2*z*z-1;
					double specular = Rn.innerProduct(lightDirection, reflected);
					if (specular < 0.0) specular = 0.0;
					if (specular > 1.0) specular = 1.0;
					specular = Math.pow(specular, specularExponent);
					for (int k = 0; k<3; ++k)	{
						double f = (diffuse * diffuseColorAsFloat[k] + specular * specularColorAsFloat[k]);
						if (f < 0) f = 0;
						if (f > 1) f = 1;
						sphereTex[II+k] =  (byte) (255 * f); 
					}
					sphereTex[II+3] = sphereVertices[I][2] < .1 ? (byte) (2550*sphereVertices[I][2]) : -128;
				}
				else	{
					sphereTex[II] =  sphereTex[II+1] = sphereTex[II+2] = sphereTex[II+3]  = 0;  
					}
				II += 4;
				I++;
				}
			}
			ImageData id = new ImageData(sphereTex, textureSize, textureSize) ;
			tex.setImage(id);
			tex.setApplyMode(Texture2D.GL_REPLACE);
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
	static final int textureSize = 128;
	static double[][] sphereVertices = new double[textureSize * textureSize][3];
	private Color specularColor;
	static {
		double x,y,z;
		int I = 0;
		for (int i = 0; i<textureSize; ++i)	{
			y = 2*(i+.5)/textureSize - 1.0;
			for (int j = 0; j< textureSize; ++j)	{
				x = 2*(j+.5)/textureSize - 1.0;
				double dsq = x*x+y*y;
				if (dsq <= 1.0)	{	
					z = Math.sqrt(1.0-dsq);
					sphereVertices[I][0] = x; sphereVertices[I][1] = y; sphereVertices[I][2] = z;
					}
				else sphereVertices[I][0] = sphereVertices[I][1] = sphereVertices[I][2] = -1;
				I++;
			}
		}
	}
	public void render(JOGLRenderer jr) {
		GLDrawable theCanvas = jr.getCanvas();
		GL gl = theCanvas.getGL();
//		if (!(OpenGLState.equals(diffuseColorAsFloat, jr.openGLState.diffuseColor, (float) 10E-5))) {
			gl.glColor4fv( diffuseColorAsFloat);
			System.arraycopy(diffuseColorAsFloat, 0, jr.openGLState.diffuseColor, 0, 4);
//		}
		
		if (!sphereDraw)	{
			lighting = false;
			gl.glPointSize((float) getPointSize());
      try {
        gl.glPointParameterfv(GL.GL_POINT_DISTANCE_ATTENUATION, pointAttenuation);
      } catch (Exception e){
      //TODO: i dont know - got error on ati radeon 9800
      }
			 //gl.glPointParameterf(GL.GL_POINT_DISTANCE_ATTENUATION, ))
			gl.glEnable(GL.GL_POINT_SMOOTH);
			gl.glEnable(GL.GL_POINT_SPRITE_ARB);
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, GL.GL_TRUE);
			Texture2DLoaderJOGL.render(theCanvas, currentTex);
			gl.glEnable(GL.GL_TEXTURE_2D);
		} else
		polygonShader.render(jr);
		
			jr.openGLState.lighting = lighting;
			if (lighting) gl.glEnable(GL.GL_LIGHTING);
			else gl.glDisable(GL.GL_LIGHTING);
		
	}

	public void postRender(JOGLRenderer jr) {
		polygonShader.postRender(jr);
		if (!sphereDraw)	{
			GL gl = jr.globalGL;
			jr.globalGL.glDisable(GL.GL_POINT_SPRITE_ARB);
			gl.glActiveTexture(GL.GL_TEXTURE0);
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

	public TextShader getTextShader() {
		// TODO Auto-generated method stub
		return null;
	}


}
