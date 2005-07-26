/*
 * Created on May 12, 2004
 *
 */
package de.jreality.worlds;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JMenuBar;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.Torus;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.SkyBox;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.ReflectionMap;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.shader.ShaderFactory;
import de.jreality.shader.Texture2D;
import de.jreality.util.CameraUtility;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Matrix;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.P3;
import de.jreality.util.Pn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class JOGLSkyBox extends AbstractJOGLLoadableScene {

	static double[][] square = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};

	public boolean encompass()	{ return false; }
	ConfigurationAttributes config = null;
	String configResourceDir = "/net/MathVis/data/config/";
	
	public boolean addBackPlane()	{ return false; }
  
  public SceneGraphComponent makeWorld() {
    SceneGraphComponent root = makeScene();
    ReflectionMap rm = ReflectionMap.reflectionMapFactory(
        "textures/desertstorm/desertstorm_",
        new String[]{"rt","lf","up", "dn","bk","ft"},
        "JPG");
    SkyBox sb = new SkyBox(rm.getFaceTextures());
    sbkit.addChild(sb);
    sb.getTransformation().setStretch(100.0);
  
    root.addChild(sbkit);
    return root;

  }
  
	public SceneGraphComponent makeScene() {

		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("theWorld");
		root.setTransformation(new Transformation());
				
		double[][] pos = new double[6][3];
		for (int i = 0; i<6; ++i)	{
			double angle = i*Math.PI * 2.0/(6.0);
			pos[i][0] = 4 * Math.cos(angle);
			pos[i][1] = 0.0;
			pos[i][2] = 4 * Math.sin(angle);
		}
		IndexedFaceSet globeSet=new CatenoidHelicoid(40);
		globeSet.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateFaceNormals(globeSet)));
		globeSet.setName("CatHel1");
		SceneGraphComponent globeNode1= new SceneGraphComponent();
		globeNode1.setName("Comp1");
		MatrixBuilder.euclidian()
      .translate(pos[0])
      .scale(.3)
      .assignTo(globeNode1);
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
	   //ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
	   double[] vec = {1d, 1.5d, 1d};
	   Texture2D tex2d = null;
	   try {
	      tex2d = ShaderFactory.createTexture(ap1, "", "textures/grid256rgba.png");
        ShaderFactory.createReflectionMap(
            ap1,
            "polygonShader",
            "textures/desertstorm/desertstorm_",
            new String[]{"rt","lf","up", "dn","bk","ft"},
            ".JPG");
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	 	   	//tex2d = new Texture2D(Readers.getInput("textures/grid256rgba.png"));
	   //ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE_2D, tex2d);
	   //ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"reflectionMap", refm);
	   tex2d.setTextureMatrix( new Matrix(P3.makeStretchMatrix(null, vec)));
	   globeNode1.setAppearance(ap1);
	   //rootAp = ap1;
		globeNode1.setGeometry(globeSet);
  
  
		// 2.
	   CatenoidHelicoid catHel=new CatenoidHelicoid(20);
	   catHel.setName("CatHel2");
	   catHel.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly( GeometryUtility.calculateFaceNormals(catHel)));
	   catHel.buildEdgesFromFaces();
		SceneGraphComponent globeNode2= new SceneGraphComponent();
		globeNode2.setName("Comp1");
    MatrixBuilder.euclidian()
    .translate(pos[1])
    .scale(.3)
    .assignTo(globeNode2);
		globeNode2.setGeometry(catHel);
		//globeNode2.setGeometry(globeSet);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(255,0,255));
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,2.0);
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR,new java.awt.Color(.2f, .5f, .5f, 1f));
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,true);
	   ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,true);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,true);
	   ap1.setAttribute(CommonAttributes.NORMAL_SCALE, 1.0);
	   globeNode2.setAppearance(ap1);
  
 
	   catHel=new CatenoidHelicoid(20);
	   catHel.setName("CatHel3");
	   catHel.buildEdgesFromFaces();
		SceneGraphComponent globeNode3= new SceneGraphComponent();
    MatrixBuilder.euclidian()
    .translate(pos[2])
    .scale(.3)
    .assignTo(globeNode3);
		globeNode3.setGeometry(catHel);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(200, 150, 0));
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW,true);
//	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,true);
//	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
//	   ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   globeNode3.setAppearance(ap1);

	   IndexedLineSet torus1 = IndexedLineSetUtility.discreteTorusKnot(1.0, .4,4,5,400);
	   double[][] pts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	   QuadMeshShape tube = TubeUtility.makeTubeAsIFS(pts, .2d, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN,0);
	   tube.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateFaceNormals(tube)));
	   tube.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateVertexNormals(tube)));
	   SceneGraphComponent globeNode4= new SceneGraphComponent();
      MatrixBuilder.euclidian()
      .translate(pos[3])
      .assignTo(globeNode4);
	   //globeNode4.setGeometry(torus1);
	   globeNode4.setGeometry(tube);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,java.awt.Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,1.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   //ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,true);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,false);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH, 1.0);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   //globeNode4.setAppearance(ap1);
 
	   Torus torus= new Torus(2.3, 1.5, 40,60);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode5= new SceneGraphComponent();
     globeNode5.setName("TorusWithReflectionMap");
    MatrixBuilder.euclidian()
    .translate(pos[4])
    .scale(.3)
    .assignTo(globeNode5);
	   globeNode5.setGeometry(torus); //SphereHelper.spheres[4]); //torus);
	   
	   ap1 = new Appearance();
		try {
      ShaderFactory.createReflectionMap(
          ap1,
          "polygonShader",
          "textures/desertstorm/desertstorm_",
          new String[]{"rt","lf","up", "dn","bk","ft"},
          ".JPG");
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.YELLOW);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
	    globeNode5.setAppearance(ap1);
		
	   torus= new Torus(2.3, 1.5, 20, 20);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode6= new SceneGraphComponent();
     MatrixBuilder.euclidian()
       .translate(pos[5])
       .rotate(Math.PI/2.0,1.0, 0.0, 0.0)
       .scale(0.3)
       .assignTo(globeNode6);
     //globeNode6.setGeometry(torus);
	   globeNode6.setGeometry(IndexedFaceSetUtility.implode(torus, -.35));
	   //SceneGraphComponent s1 = Parser3DS.readFromFile("/homes/geometer/gunn/tmp/read3DS/models/space011.3ds");
	   //globeNode6.addChild(s1);


		sbkit =SceneGraphUtilities.createFullSceneGraphComponent("skybox");
		ap1 = sbkit.getAppearance();
		ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);

		root.addChild(globeNode1);
		root.addChild(globeNode2);
		root.addChild(globeNode3);
	   root.addChild(globeNode4);
	   root.addChild(globeNode5);	  
	   root.addChild(globeNode6);	

		
		return root;
	}
	
	public boolean isEncompass() {return false;}
 
	SceneGraphComponent sbkit;
	public void customize(JMenuBar menuBar, Viewer viewer) {
		//sb.getTransformation().setTranslation(0.0d, 0.0d, -4.0d);
		CameraUtility.getCamera(viewer).setFar(500.0);
	}
}

