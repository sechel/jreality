/*
 * Created on Jul 14, 2004
 *
 */
package de.jreality.worlds;

import de.jreality.geometry.CatenoidHelicoid;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.Torus;
import de.jreality.geometry.TubeUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Sphere;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.util.MatrixBuilder;
import de.jreality.util.Pn;

/**
 * @author weissman
 *
 */
public class StandardDemo extends AbstractJOGLLoadableScene {

	public boolean addBackPlane() {
		return true;
	}
	public boolean isEncompass() {
		return true;
	}
	/* (non-Javadoc)
	 * @see de.jreality.jogl.WorldMaker#makeWorld()
	 */
	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setName("theWorld");
		root.setTransformation(new Transformation());
		CatenoidHelicoid globeSet=new CatenoidHelicoid(40);
		//Torus globeSet = new Torus(2.3, 1.5, 20, 30);
		GeometryUtility.calculateAndSetNormals(globeSet);
		globeSet.setName("CatHel1");
		globeSet.buildEdgesFromFaces();
	   double scaleVal = 1.5;
		SceneGraphComponent globeNode1= new SceneGraphComponent();
		globeNode1.setName("Comp1");
		MatrixBuilder.euclidian().translate(0,0,scaleVal).scale(.3).assignTo(globeNode1);
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,false);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,false);
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureEnabled",true);
//	   double[] vec = {1d, 1.5d, 1d};
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureFile","test/de/jreality/examples/resources/grid256rgba.png");
//	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+"textureMatrix",P3.makeStretchMatrix(null, vec));
	   globeNode1.setAppearance(ap1);
	   //rootAp = ap1;
		globeNode1.setGeometry(globeSet);
  
  
		// 2.
	   CatenoidHelicoid catHel=new CatenoidHelicoid(20);
	   catHel.setName("CatHel2");
	   GeometryUtility.calculateAndSetNormals(catHel);
	   catHel.buildEdgesFromFaces();
		SceneGraphComponent globeNode2= new SceneGraphComponent();
		globeNode2.setName("Comp1");
		Transformation gt= new Transformation();
		gt.setTranslation(scaleVal, scaleVal, scaleVal);
		gt.setStretch(.3);
		globeNode2.setTransformation(gt);
		globeNode2.setGeometry(catHel);
		//globeNode2.setGeometry(globeSet);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.red);
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
	   GeometryUtility.calculateAndSetNormals(catHel);
	   catHel.buildEdgesFromFaces();
		SceneGraphComponent globeNode3= new SceneGraphComponent();
		gt= new Transformation();
		gt.setTranslation(-scaleVal, -scaleVal, scaleVal);
		gt.setStretch(.3);
		globeNode3.setTransformation(gt);
		globeNode3.setGeometry(catHel);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,2.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,true);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE_PATTERN,0x1c47);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   globeNode3.setAppearance(ap1);

	   IndexedLineSet torus1 = IndexedLineSetUtility.discreteTorusKnot(1.0, .4,4,5,400);
	   double[][] pts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
	   QuadMeshShape tube = TubeUtility.makeTubeAsIFS(pts, .2d, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN,0);
	   GeometryUtility.calculateAndSetNormals(tube);
	   SceneGraphComponent globeNode4= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(0, 0, 0.0);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   //gt.setStretch(.3);
	   globeNode4.setTransformation(gt);
	   //globeNode4.setGeometry(torus1);
	   globeNode4.setGeometry(tube);
	   ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLUE);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH,1.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   ap1.setAttribute(CommonAttributes.FACE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LIGHTING_ENABLED,true);
	   ap1.setAttribute(CommonAttributes.SMOOTH_SHADING,true);
	   ap1.setAttribute(CommonAttributes.NORMALS_DRAW,true);
	   ap1.setAttribute(CommonAttributes.LINE_WIDTH, 1.0);
	   ap1.setAttribute(CommonAttributes.LINE_STIPPLE,false);
	   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,false);
	   //globeNode4.setAppearance(ap1);
 
	   Torus torus= new Torus(2.3, 1.5, 20, 30);
	   GeometryUtility.calculateAndSetNormals(torus);
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
		
	   torus= new Torus(2.3, 1.5, 20, 20);
	   GeometryUtility.calculateAndSetNormals(torus);
	   SceneGraphComponent globeNode6= new SceneGraphComponent();
	   gt= new Transformation();
	   gt.setTranslation(-scaleVal,-scaleVal,0.0);
	   //gt.setRotation( Math.PI/2.0,1.0, 0.0, 0.0);
	   gt.setStretch(.3);
	   globeNode6.setTransformation(gt);
	   globeNode6.setGeometry(new Sphere());
	   globeNode6.setGeometry(torus);
	   globeNode6.setGeometry(IndexedFaceSetUtility.implode(torus, -.35));
	   SceneGraphComponent s1;
//	try {
//		s1 = Parser3DS.readFromFile(config.getDataFile("3ds", "space011.3ds"));
//		globeNode6.addChild(s1);
//	} catch (FileNotFoundException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//

		root.addChild(globeNode1);
		root.addChild(globeNode3);
	   root.addChild(globeNode4);
	   root.addChild(globeNode5);	  
	   root.addChild(globeNode6);	
	   // add the transparent node LAST
		root.addChild(globeNode2);
	   //viewer.removeBackPlane();  
	  return root;
	}

}
