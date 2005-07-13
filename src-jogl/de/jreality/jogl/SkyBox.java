/*
 * Created on Jun 14, 2004
 *
 */
package de.jreality.jogl;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Texture2D;
import de.jreality.scene.Transformation;
import de.jreality.shader.ShaderFactory;
import de.jreality.util.ImageData;

/**
 * @author Charles Gunn
 *
 */
public class SkyBox extends SceneGraphComponent {
	Texture2D[] faceTextures;
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
	
	public SkyBox(Texture2D[] ft) {
		super();
		
		// TODO  check validity of parameters
		faceTextures = ft;
		
		Appearance ap = new Appearance();
		
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
			ap.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, ft[i]);
			sgc.setAppearance(ap);
			IndexedFaceSet face = IndexedFaceSetUtility.createIndexedFaceSetFrom(cubeIndices, cubeVerts3[i], null, null, texCoords, null, null);
			face.buildEdgesFromFaces();
			//GeometryUtility.calculateAndSetFaceNormals(face);
			sgc.setGeometry(face);
			addChild(sgc);
		}
	}
	public SkyBox(ImageData[] ft) {
		super();
		
		// TODO  check validity of parameters		
		Appearance ap = new Appearance();
		
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
			de.jreality.shader.Texture2D tex = ShaderFactory.createTexture(ap, "polygonShader", ft[i]);
      tex.setRepeatS(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
      tex.setRepeatT(de.jreality.shader.Texture2D.GL_CLAMP_TO_EDGE);
			IndexedFaceSet face = IndexedFaceSetUtility.createIndexedFaceSetFrom(cubeIndices, cubeVerts3[i], null, null, texCoords, null, null);
			GeometryUtility.calculateAndSetNormals(face);
			face.buildEdgesFromFaces();
			//GeometryUtility.calculateAndSetFaceNormals(face);
			sgc.setGeometry(face);
			addChild(sgc);
		}
	}
	
}
