/*
 * Created on May 12, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.examples.jogl;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.jogl.GL;
import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.Torus;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.DiscreteSpaceCurve;
import de.jreality.jogl.InteractiveViewerDemo;
import de.jreality.jogl.SkyBox;
import de.jreality.jogl.shader.DefaultMaterialShader;
import de.jreality.jogl.shader.Texture2DJOGL;
import de.jreality.jogl.shader.Texture2DLoaderJOGL;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.CameraUtility;
import de.jreality.util.P3;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TestSkyboxDemo extends InteractiveViewerDemo {

	static String resourceDir = "/gunn/";
	static {
		String foo = System.getProperty("resourceDir");
		if (foo != null)	resourceDir  = foo;
	}
	//static String resourceDir = "/Users/gunn/Library/Textures/";
	/**
	 * 
	 */
	public TestSkyboxDemo() {
		super();
	}

	static double[][] square = {{-1,-1,0},{1,-1,0},{1,1,0},{-1,1,0}};
	static double[][] texc = {{0,0},{1,0},{1,1},{0,1}};

	public boolean encompass()	{ return false; }
	public boolean addBackPlane()	{ return false; }
	public SceneGraphComponent makeWorld() {

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
		float[] bgc = {.3f, .3f, .5f, 1f};
		//viewer.getSceneRoot().getAppearance().setAttribute("backgroundColor", bgc);
		IndexedFaceSet globeSet=new CatenoidHelicoid(40);
		//Torus globeSet = new Torus(2.3, 1.5, 20, 30);
		//globeSet.calculateNormals();
		globeSet.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateFaceNormals(globeSet)));
		globeSet.setName("CatHel1");
		//globeSet.buildEdgesFromFaces();
	   double scaleVal = 1.5;
		SceneGraphComponent globeNode1= new SceneGraphComponent();
		globeNode1.setName("Comp1");
		Transformation gt= new Transformation();
		gt.setTranslation(pos[0]);
		gt.setStretch(.3);
		globeNode1.setTransformation(gt);
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.RED);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.BLACK);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
	   double[] vec = {1d, 1.5d, 1d};
	   Texture2D tex2d = null;
	   //try {
		//tex2d = new Texture2D(resourceDir+"grid256rgba.png");
	//} catch (MalformedURLException e1) {
		// TODO Auto-generated catch block
		//e1.printStackTrace();
	//}
	   //ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.TEXTURE2D, tex2d);
	   //tex2d.setTextureMatrix( new Transformation(P3.makeStretchMatrix(null, vec)));
	   globeNode1.setAppearance(ap1);
	   //rootAp = ap1;
		globeNode1.setGeometry(globeSet);
  
  
		// 2.
	   CatenoidHelicoid catHel=new CatenoidHelicoid(20);
	   catHel.setName("CatHel2");
	   catHel.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly( GeometryUtility.calculateFaceNormals(catHel)));
	   //catHel.buildEdgesFromFaces();
		SceneGraphComponent globeNode2= new SceneGraphComponent();
		globeNode2.setName("Comp1");
		gt= new Transformation();
		gt.setTranslation(pos[1]);
		gt.setStretch(.3);
		globeNode2.setTransformation(gt);
		globeNode2.setGeometry(catHel);
		//globeNode2.setGeometry(globeSet);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.PURPLE);
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
		gt= new Transformation();
		gt.setTranslation(pos[2]);
		gt.setStretch(.3);
		globeNode3.setTransformation(gt);
		globeNode3.setGeometry(catHel);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.WHITE);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,2.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
	   ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   globeNode3.setAppearance(ap1);

	   DiscreteSpaceCurve torus1 = DiscreteSpaceCurve.discreteTorusKnot(1.0, .4,4,5,400);
	   double[][] pts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	   QuadMeshShape tube = TubeUtility.makeTubeAsIFS(pts, .2d, null, TubeUtility.PARALLEL);
	   tube.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateFaceNormals(tube)));
	   tube.setVertexAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateVertexNormals(tube)));
	   SceneGraphComponent globeNode4= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(pos[3]);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   //gt.setStretch(.3);
	   globeNode4.setTransformation(gt);
	   //globeNode4.setGeometry(torus1);
	   globeNode4.setGeometry(tube);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.BLUE);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.BLACK);
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
 
	   Torus torus= new Torus(2.3, 1.5, 20, 30);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode5= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(pos[4]);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   gt.setStretch(.3);
	   globeNode5.setTransformation(gt);
	   globeNode5.setGeometry(torus);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, DefaultMaterialShader.YELLOW);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,true);
	   ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.NORMAL_SCALE,1.0);
	   globeNode5.setAppearance(ap1);
		
	   torus= new Torus(2.3, 1.5, 20, 20);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode6= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(pos[5]);
	   gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   gt.setStretch(.3);
	   globeNode6.setTransformation(gt);
	   //globeNode6.setGeometry(torus);
	   globeNode6.setGeometry(GeometryUtility.implode(torus, -.35));
	   //SceneGraphComponent s1 = Parser3DS.readFromFile("/homes/geometer/gunn/tmp/read3DS/models/space011.3ds");
	   //globeNode6.addChild(s1);


		root.addChild(globeNode1);
		root.addChild(globeNode2);
		root.addChild(globeNode3);
	   root.addChild(globeNode4);
	   root.addChild(globeNode5);	  
	   root.addChild(globeNode6);	

		SceneGraphComponent sbkit = new SceneGraphComponent();
		sbkit.setName("sbkit");
		ap1 = new Appearance();
		sbkit.setAppearance(ap1);

		double[] stretch = {1.0, 1.0, 0.0, 1.0};
		ap1.setAttribute("normalScale",0.05);
		ap1.setAttribute(CommonAttributes.EDGE_DRAW, false);
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
		//ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureMatrix",P3.makeStretchMatrix(null, stretch));
		java.awt.Color[] colors = {java.awt.Color.WHITE,java.awt.Color.BLUE, java.awt.Color.GRAY, java.awt.Color.YELLOW, java.awt.Color.RED};

		//String[] texNameSuffixes = {"bk","ft","dn","up","lf","rt"};
		String[] texNameSuffixes = {"rt","lf","up", "dn","bk","ft"};
		Texture2D[] faceTex = new Texture2D[6];
		for (int i = 0; i<6; ++i)	{
			try {
				//BufferedImage image = Texture2D.loadImage(resourceDir+ "desertstorm/desertstorm_"+texNameSuffixes[i]+".JPG");
				//faceTex[i] = new Texture2D(image);
				faceTex[i] = new Texture2D(resourceDir+ "/desertstorm/desertstorm_"+texNameSuffixes[i]+".JPG");
				faceTex[i].setRepeatS(Texture2D.GL_CLAMP_TO_EDGE);
				faceTex[i].setRepeatT(Texture2D.GL_CLAMP_TO_EDGE);
				faceTex[i].setMinFilter(Texture2D.GL_LINEAR_MIPMAP_LINEAR);
				//faceTex[i].setMagFilter(GL.GL_NEAREST);
				//faceTex[i].setMinFilter(GL.GL_NEAREST);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		SkyBox sb = new SkyBox(faceTex, viewer.getCameraPath());
		//sb.getTransformation().setTranslation(0.0d, 0.0d, -4.0d);
		sb.getTransformation().setStretch(100.0);
		CameraUtility.getCamera(viewer).setFar(500.0);
		sbkit.addChild(sb);
		
		root.addChild(sbkit);
		
		viewer.getCameraPath().getLastComponent().getTransformation().setTranslation(0d,0d,0d);
		
		return root;
	}
	
	public boolean isEncompass() {return false;}
 
   public static void main(String argv[])	{
	   TestSkyboxDemo test = new TestSkyboxDemo();
	   Logger.getLogger("de.jreality").setLevel(Level.INFO);
	   Logger.getLogger("").getHandlers()[0].setLevel(Level.INFO);
	   if (argv != null && argv.length > 0)	{
		   Logger.getLogger("de.jreality").log(Level.INFO, "arguments are {0}",argv[0]);
	   }
	   test.begin();
   }
}

