/*
 * Created on Jun 14, 2004
 *
 */
package de.jreality.jogl;

import net.java.games.jogl.GL;
import net.java.games.jogl.GLDrawable;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.CameraUtility;

/**
 * @author Charles Gunn
 *
 */
public class SkyBox extends SceneGraphComponent {
	double stretch = 40.0;

	static private int[][] cubeIndices = {{0,1,2,3}};

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
	
	String[] faceNames = {"back","front","down","up","left","right"};
	String[] texNameSuffixes = {"rt","lf","up", "dn","bk","ft"};
	Texture2D[] imagesIn = new Texture2D[6], imagesOut = new Texture2D[6];
	EffectiveAppearance parentEap = EffectiveAppearance.create();
	EffectiveAppearance children[] = new EffectiveAppearance[6];
	
	public SkyBox(ImageData[] ft) {
		super();
		
		// TODO  check validity of parameters		
		Appearance ap = new Appearance();
		parentEap = parentEap.create(ap);
		
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
		ap.setAttribute(CommonAttributes.AT_INFINITY,true);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
		ap.setAttribute(CommonAttributes.FACE_DRAW,true);
		ap.setAttribute(CommonAttributes.EDGE_DRAW,false);
		setAppearance(ap);
		
		Transformation tt = new Transformation();
//		tt.setRotation(Math.PI, 1d, 0d, 0d);	
		setTransformation(tt);

//		
		for (int i = 0; i<6; ++i)	{
			SceneGraphComponent sgc = new SceneGraphComponent();
			ap = new Appearance();
			sgc.setAppearance(ap);
			imagesIn[i] = TextureUtility.createTexture(ap, "polygonShader", ft[i]);
			imagesIn[i].setRepeatS(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
			imagesIn[i].setRepeatT(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
			children[i] = parentEap.create(ap);
			imagesOut[i] = (Texture2D) AttributeEntityUtility.createAttributeEntity(Texture2D.class, ShaderUtility.nameSpace("polygonShader","texture2d"), children[i]);
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
			ifsf.setFaceCount(cubeIndices.length);
			ifsf.setVertexCount(cubeVerts3[i].length);
			ifsf.setVertexCoordinates(cubeVerts3[i]);
			ifsf.setFaceIndices(cubeIndices);
			ifsf.setVertexTextureCoordinates(texCoords);
			ifsf.setGenerateEdgesFromFaces(false);
			ifsf.setGenerateFaceNormals(false);
			ifsf.setGenerateVertexNormals(false);
			//IndexedFaceSet face = IndexedFaceSetUtility.createIndexedFaceSetFrom(cubeIndices, cubeVerts3[i], null, null, texCoords, null, null);
			//GeometryUtility.calculateAndSetNormals(face);
			//face.buildEdgesFromFaces();
			ifsf.update();
			IndexedFaceSet face = ifsf.getIndexedFaceSet();
		
			//GeometryUtility.calculateAndSetFaceNormals(face);
			sgc.setGeometry(face);
			addChild(sgc);
		}
	}
	
	public void render(GLDrawable gd, JOGLRenderer glr)	{
		GL gl = gd.getGL();
		gl.glDepthMask(false);
		gl.glDepthFunc(GL.GL_NEVER);
		gl.glPushAttrib(GL.GL_ENABLE_BIT);
		gl.glDisable(GL.GL_BLEND);
		gl.glDisable(GL.GL_DEPTH_TEST);
		gl.glDisable(GL.GL_LIGHTING);
	    gl.glActiveTexture(0);
	    gl.glEnable(GL.GL_TEXTURE_2D);
	    float[] white = {1f,1f,1f,1f};
//	    gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, white);
	    gl.glColor4fv( white);
	    gl.glPushMatrix();
	    
	    double[] w2c = glr.context.getWorldToCamera();
	    gl.glLoadTransposeMatrixd(P3.extractOrientationMatrix(null, w2c, Pn.originP3, Pn.EUCLIDEAN));
		for (int i = 0; i<6; ++i)	{
		    Texture2DLoaderJOGL.render(gd, imagesOut[i]);
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
