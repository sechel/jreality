/*
 * Created on Jun 4, 2004
 *
 */
package de.jreality.examples.jsyzygy;


import de.jreality.geometry.*;
import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.Torus;
import de.jreality.geometry.WingedEdge;
import de.jreality.jogl.DiscreteSpaceCurve;
import de.jreality.jsyzygy.JsyzygyViewer;
import de.jreality.scene.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;



/**
 * @author gollwas
 *
 */
public class ImplodeShaderDemo { 	
    
    public static SceneGraphComponent generateScene(SceneGraphComponent root) {

	Appearance ap1 = new Appearance();
	root.setAppearance(ap1);
	root.getAppearance().setAttribute("normalScale",0.05);
	// root.getAppearance().setAttribute("faceShader.diffuseColor", java.awt.Color.GREEN);
	root.getAppearance().setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.green);
	for (int i = 0; i< 5; ++i)	{
		Torus torus= new Torus(0.5, 0.3, 20, 30);
	    //CatenoidHelicoid torus = new CatenoidHelicoid(20);
	    torus.buildEdgesFromFaces();
	    torus.setName("torus"+i);
		//torus.calculateNormals();
		torus.setFaceAttributes(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(GeometryUtility.calculateFaceNormals(torus)));
		//torus.setVertexAttributes(Attribute.NORMALS, new DataList(StorageModel.DOUBLE_ARRAY.array(3), GeometryUtility.calculateVertexNormals(torus)));
		SceneGraphComponent globeNode = new SceneGraphComponent();
		globeNode.setName("comp"+i);
		Transformation gt= new Transformation();
		gt.setTranslation(-5.0 + 8.0* i, 0, 0.0);
		gt.setStretch(5);
		globeNode.setTransformation(gt);
		if (i!=0) globeNode.setGeometry(GeometryUtility.implode(torus, -.9 + .4 * i));
		else globeNode.setGeometry(GeometryUtility.truncate(torus));
		ap1 = new Appearance();
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER + "." + CommonAttributes.DIFFUSE_COLOR, new java.awt.Color(255-(5*i),10*i,20*i));
		ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, ((i%2)==0));
		globeNode.setAppearance(ap1);
		root.addChild(globeNode);
	}
	return root;
    }
    

    public static SceneGraphComponent generateScene1(SceneGraphComponent r)	{
		WingedEdge oloid = new WingedEdge(3.0d);
		int num = 50;
		for (int i = 0; i<=num; ++i)  	{
			double angle = 2.0 * Math.PI * ( i/((double) num));
			double[] plane = {Math.cos(angle), Math.sin(angle), .5 * Math.cos(2*angle), -1d};
			oloid.cutWithPlane(plane);
		} 
		oloid.update();
		SceneGraphComponent oloidkit = new SceneGraphComponent();
		//oloidkit.addChild(oloid);
		oloidkit.setGeometry(oloid);
		
		SceneGraphComponent theWorld = new SceneGraphComponent();
		theWorld.setName("world");
		Transformation tt = new Transformation();
		tt.setRotation(Math.PI/3.0,1d,0d,0d);
		tt.setTranslation(1d,1d,0d);
		//theWorld.addTransform(tt);
		theWorld.addChild(oloidkit);
		theWorld.setAppearance(new Appearance());
		r.addChild(theWorld);
		return r;
	}
    
	public static SceneGraphComponent generateScene2(SceneGraphComponent root) {
		float[] bgc = {.3f, .3f, .5f, 1f};
		IndexedFaceSet globeSet=new CatenoidHelicoid(40);
		globeSet.setName("CatHel1");
		globeSet.buildEdgesFromFaces();
	   double scaleVal = 1.5;
		SceneGraphComponent globeNode1= new SceneGraphComponent();
		globeNode1.setName("Comp1");
		Transformation gt= new Transformation();
		gt.setTranslation(0, 0, scaleVal);
		gt.setStretch(.3);
		globeNode1.setTransformation(gt);
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING,true);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
//	   double[] vec = {1d, 1.5d, 1d};
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureFile","/gunn/grid256rgba.jpg");
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureMatrix",P3.makeStretchMatrix(null, vec));
	   globeNode1.setAppearance(ap1);
	   //rootAp = ap1;
		globeNode1.setGeometry(globeSet);
  
  
		// 2.
	   globeSet=new CatenoidHelicoid(20);
	   globeSet.setName("CatHel2");
	   globeSet.buildEdgesFromFaces();
		SceneGraphComponent globeNode2= new SceneGraphComponent();
		globeNode2.setName("Comp1");
		gt= new Transformation();
		gt.setTranslation(scaleVal, scaleVal, scaleVal);
		gt.setStretch(.3);
		globeNode2.setTransformation(gt);
		globeNode2.setGeometry(globeSet);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.red);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,2.0);
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new java.awt.Color(.2f, .5f, .5f, 1f));
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,true);
	   ap1.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,true);
	   ap1.setAttribute(CommonAttributes.NORMAL_SCALE, 1.0);
	   globeNode2.setAppearance(ap1);
  
 
	   globeSet=new CatenoidHelicoid(20);
	   globeSet.setName("CatHel3");
	   globeSet.buildEdgesFromFaces();
		SceneGraphComponent globeNode3= new SceneGraphComponent();
		gt= new Transformation();
		gt.setTranslation(-scaleVal, -scaleVal, scaleVal);
		gt.setStretch(.3);
		globeNode3.setTransformation(gt);
		globeNode3.setGeometry(globeSet);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,2.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
	   ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   globeNode3.setAppearance(ap1);

	   //Torus torus= new Torus(2.3, 1.5, 20, 30);
	   //torus.calculateNormals();
	   //Torus torus= new Torus(2.3, 1.5, 20, 30);
	   //torus.calculateNormals();
	   DiscreteSpaceCurve torus1 = DiscreteSpaceCurve.discreteTorusKnot(1.0, .4,3,5,100);
	   SceneGraphComponent globeNode4= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(0, 0, 0.0);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   //gt.setStretch(.3);
	   globeNode4.setTransformation(gt);
	   globeNode4.setGeometry(torus1);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.GREEN);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,1.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,true);
	   ap1.setAttribute(CommonAttributes.SPECULAR_EXPONENT,200.0);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,false);
	   ap1.setAttribute(CommonAttributes.NORMAL_SCALE,-1.0);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH, 2.0);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   globeNode4.setAppearance(ap1);
 
	   Torus torus= new Torus(2.3, 1.5, 20, 30);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode5= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(scaleVal,scaleVal,0.0);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   gt.setStretch(.3);
	   globeNode5.setTransformation(gt);
	   globeNode5.setGeometry(torus);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.YELLOW);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,true);
	   ap1.setAttribute(CommonAttributes.ANTIALIASING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.NORMAL_SCALE,1.0);
	   globeNode5.setAppearance(ap1);
		
	   torus= new Torus(2.3, 1.5, 20, 30);
	   torus.calculateNormals();
	   SceneGraphComponent globeNode6= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(-scaleVal,-scaleVal,0.0);
	   gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   gt.setStretch(.3);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
	   globeNode6.setTransformation(gt);
	   globeNode6.setGeometry(torus);
	   globeNode6.setAppearance(ap1);

		root.addChild(globeNode1);
		root.addChild(globeNode2);
		root.addChild(globeNode3);
	   root.addChild(globeNode4);
	   root.addChild(globeNode5);	  
	   root.addChild(globeNode6);	  
	  return root;
	}
	 

	public static void main(String argv[])	{
	    JsyzygyViewer viewer = JsyzygyViewer.createInstance();
	    final ImplodeShaderDemo gd = new ImplodeShaderDemo();
	    viewer.makeSimpleWand(Wand.makeWandComponent(new SceneGraphComponent()));
	    viewer.initSomeLight();
	    SceneGraphComponent myRoot = new SceneGraphComponent();
	    int demoID = 0;
	    try {demoID = Integer.parseInt(argv[0]);} catch (Exception e) {}
	    switch (demoID) {
    	case 0:
    	    ImplodeShaderDemo.generateScene(myRoot);
    	    break;
    	case 1:
    		ImplodeShaderDemo.generateScene1(myRoot);
    	    break;
    	case 2:
    		ImplodeShaderDemo.generateScene2(myRoot);
    	    break;
    	default:
    		ImplodeShaderDemo.generateScene(myRoot);
    	    break;
		}
	    viewer.addSceneGraphComponent(myRoot);
	}
}
