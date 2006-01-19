/*
 * Created on Jun 14, 2004
 *
 */
package de.jreality.jogl;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CubeMap;
import de.jreality.shader.ImageData;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;

/**
 * @author Charles Gunn
 *
 */
class JOGLSkyBox {
	double stretch = 40.0;

  // TODO straighten out nomenclature on faces
	static private double[][][] cubeVerts3 =  
		{
			 {{1,1,1}, {1,1,-1}, {1, -1, -1}, {1, -1, 1}},		// right
			 { {-1, 1, -1}, {-1, 1, 1},{-1,-1,1}, {-1,-1,-1}}, // left
			 { {-1, 1,-1},  {1, 1,-1},{1, 1,1}, {-1, 1,1}},		// 	up
			 {  {-1,-1,1},{1,-1,1},{1,-1,-1}, {-1,-1,-1}},		// down
			 {{-1,1,1}, {1,1,1}, {1,-1,1},{-1,-1,1}},		// back
			 {   {1,1,-1},{-1,1,-1}, {-1,-1,-1},{1,-1,-1}}			// front
		 };	
	
  // TODO figure out texture coordinates 
	static private double[][] texCoords = {{0,0},{1,0},{1,1},{0,1}};
	
	static Appearance a=new Appearance();
	static Texture2D tex=(Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, "", a, true);
  static {
    tex.setRepeatS(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
    tex.setRepeatT(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
  }

  static void render(GLDrawable gd, JOGLRenderer glr, CubeMap cm)	{
    ImageData[] imgs=TextureUtility.getCubeMapImages(cm);
    tex.setBlendColor(cm.getBlendColor());
		GL gl = gd.getGL();
		gl.glDepthMask(false);
		gl.glDepthFunc(GL.GL_NEVER);
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
    gl.glActiveTexture(GL.GL_TEXTURE0);
    gl.glEnable(GL.GL_TEXTURE_2D);
    float[] white = {1f,1f,1f,1f};
//	    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, white);
    gl.glColor4fv( white);
    gl.glPushMatrix();
	    
	    double[] w2c = glr.context.getWorldToCamera();
      
	    gl.glLoadTransposeMatrixd(P3.extractOrientationMatrix(null, w2c, Pn.originP3, Pn.EUCLIDEAN));
		for (int i = 0; i<6; ++i)	{
			tex.setImage(imgs[i]);
		    Texture2DLoaderJOGL.render(gd, tex);
			gl.glBegin(GL.GL_POLYGON);
				for (int j = 0; j<4; ++j)	{
					gl.glTexCoord2dv(texCoords[j]);
					gl.glVertex3dv(cubeVerts3[i][j]);
				}
			gl.glEnd();
		}
	    gl.glPopMatrix();
		gl.glDepthFunc(GL.GL_LESS);
		gl.glPopAttrib();
		gl.glDepthMask(true);
	}
	
}
