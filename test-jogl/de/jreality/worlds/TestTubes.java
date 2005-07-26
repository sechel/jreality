 /*
 * Created on May 12, 2004
 *
 */
package de.jreality.worlds;
import java.awt.Color;

import de.jreality.geometry.BezierPatchMesh;
import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.QuadMeshShape;
import de.jreality.geometry.QuadMeshUtility;
import de.jreality.geometry.TubeUtility;
import de.jreality.jogl.LevelOfDetailComponent;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.ConfigurationAttributes;
import de.jreality.util.Pn;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author Charles Gunn
 *
 */
public class TestTubes extends AbstractJOGLLoadableScene {

	static double x = 1;
	static double[][] circle =  {{x, 0, 0, 1},	 {x, 1,0, 1}, {0,2, 0, 2}, {-x, 1,0, 1}, {-x, 0, 0, 1},{-x, -1,0, 1},{0,-2, 0, 2},{x, -1,0, 1},{x, 0, 0, 1}};
	/*
	 { 
 			{ 0.5,     0.0, 0.0, 1.0},      
 			{ 0.5,     0.27614 ,  0.0, 1.0},
 			{ 0.27614,      0.5, 0.0, 1.0}, 
 			{ 0.0,       0.5,  0.0, 1.0}, 
 			{ -0.27614,      0.5, 0.0, 1.0}, 
 			{ -0.5,     0.27614 ,  0.0, 1.0},     
 			{ -0.5,     0.0, 0.0, 1.0},      
	 		{ -0.5,     -0.27614 ,  0.0, 1.0},     
 			{ -0.27614 ,     -0.5, 0.0, 1.0}, 
 			{ 0.0,       -0.5,  0.0, 1.0}, 
 			{ 0.27614 ,     -0.5, 0.0, 1.0}, 
 			{ 0.5,     -0.27614 ,  0.0, 1.0},     
 			{ 0.5,     0.0, 0.0, 1.0}
	};
	*/
	
	static double octant[][][] = {
			{{0, 0, 1, 1},		{0, 1, 1, 1},		{0, 2, 0, 2}}, 
			{{1, 0, 1, 1},		{1, 1, 1, 1},		{2, 2, 0, 2}}, 
			{{2, 0, 0, 2},		{2 ,0, 0, 2},		{4, 0, 0, 4}}};

	static double[][] form = {{1,1,1}, {1,-1,1},{1,-1,-1},{1,1,-1},{-1,1,-1},{-1,-1,-1},{-1,-1,1},{-1,1,1}};
	static double[][] form2 = {{1,1,1}, {1,0,1}, {1,-1,1},{1,-1,0},{1,-1,-1},{1,0,-1},{1,1,-1},{0,1,-1},
			{-1,1,-1},{-1,0,-1},{-1,-1,-1},{-1,-1,0},{-1,-1,1},{-1,0,1},{-1,1,1}};

	static double[][] otherDirection	= {{1,0,0,0},{0,0,1,0},{0,0,2,0},{1,0,3,0}};
	
	static double[][][] patch;
	static double[] lodLevels = {.1,.2, .4, .8};
	static 
	{
		patch = new double[circle.length][otherDirection.length][4];
		for (int i = 0; i<circle.length; ++i)	{
			for (int j = 0; j<otherDirection.length; ++j)	{
				for (int k = 0; k<4; ++k)	
					patch[i][j][k] = circle[i][k] + otherDirection[j][k];
			}
		}
	}

	public SceneGraphComponent makeWorld() {
		SceneGraphComponent root = new SceneGraphComponent();
		root.setTransformation(new Transformation());
		root.setAppearance(new Appearance());
		root.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);

		double[][] rod = new double[8][3];
		for (int i = 0; i<8; ++i)	{
			rod[i][0] = .1*i;
			rod[i][1] = rod[i][2] = 0.0;
		}
		boolean doIco = true, doKnot = true, doBez = true, doBox = true, doHyp = false;
		
		if (doHyp)	{
			TubeUtility.makeTubeAsIFS(rod, .04,null,TubeUtility.FRENET,false, Pn.HYPERBOLIC, 0);
		}
		
		double[][] square = {{1,1,0},{-1,1,0},{-1,-1,0},{1,-1,0}};		
		double[][] profile = {{0,0,0}, {0,.1,0},{1,.1,0},{1,.2,0},{1.4,0,0}};
		double[][] profile2 = {{1,.2,0}, {.2, .2,0}, {0,.4,0}, {-.2, .2, 0},{-1,.2,0}, {-1,-.2,0},{-.2, -.2,0}, {0,-.4,0}, {.2, -.2, 0},{1,-.2,0}};
		  IndexedFaceSet arrow = Primitives.surfaceOfRevolutionAsIFS(profile, 24, Math.PI * 2);
	   //torus1.addGeometryListener(torus1);
	   //pts = square;
	   double[][] pts = form2;
	   
	   //QuadMeshShape torust = TubeUtility.makeTubeAsIFS(tpts, .2,  null, TubeUtility.PARALLEL, false);
	   //GeometryUtility.calculateAndSetNormals(torust);
	   if (doKnot)	{
		   SceneGraphComponent torussgc = SceneGraphUtilities.createFullSceneGraphComponent("torus knot");
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, 
		   		new Color(120,0,  120));
		   //torussgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.SMOOTH_SHADING, true);
		   torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .06);
		   //torussgc.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.SMOOTH_SHADING, false);
		   IndexedLineSet torus1 = IndexedLineSetUtility.discreteTorusKnot(1,.25, 2, 9, 250);//		   double[][] verts = new double[250][3];
//		   double f = .5;
//		   double g = .2;
//		   for (int i = 0; i<250; ++i)	{
//		   	double t =  (i-125)/125.0;
//		   	double e = 1.0/(1.01+t);
//		   	double c = Math.cos(e);
//		   	double s = Math.sin(e);
//		   	
//		   	verts[i][0] = c;
//		   	verts[i][1] = s;
//		   	verts[i][2] = t;
//		   }
//		   IndexedLineSet ils = GeometryUtility.createCurveFromPoints(verts, false);
		   int size = 16;
		   double scale = 1;
		   double[][] mysection = new double[size][3];
		   for (int i = 0; i<size; ++i)	{
		   		double angle = (i/(size-1.0)) * Math.PI * 2;
		   		mysection[i][0] = scale * Math.cos(angle)  *(1.5+Math.cos(4*angle));
		   		mysection[i][1] = scale *  Math.sin(angle)  *(1.5+Math.cos(4*angle));
		   		mysection[i][2] = 0.0;
		   }
		   IndexedLineSet ils = torus1;
		   colorByAngle(ils, new double[] {1,0,0}, new double[] {0,1,0});
		   double[][] tpts = torus1.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		   //QuadMeshShape torus1Tubes = TubeUtility.makeTubeAsIFS(tpts, .04, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN);
		   QuadMeshShape torus1Tubes = TubeUtility.makeTubeAsIFS(torus1, 0, true, .04, mysection, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN, 6);
		   GeometryUtility.calculateAndSetNormals(torus1Tubes);
		   torussgc.setGeometry(torus1Tubes); //ils);
		   torussgc.getTransformation().setStretch(.9);
		   root.addChild(torussgc);	   	
	   }
	   
	   SceneGraphComponent globeNode= SceneGraphUtilities.createFullSceneGraphComponent("container");
	   if (doBox)	{
		   SceneGraphComponent globeNode2= SceneGraphUtilities.createFullSceneGraphComponent("curve");
		   Appearance ap1 = globeNode2.getAppearance();
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,new Color(240, 100, 0));
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, true);
		   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBE_RADIUS, .03);
		   ap1.setAttribute(CommonAttributes.FACE_DRAW,false);
		   ap1.setAttribute(CommonAttributes.EDGE_DRAW,true);
		   ap1.setAttribute(CommonAttributes.VERTEX_DRAW,true);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.SPHERES_DRAW,true);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_RADIUS,.06);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.POINT_SIZE, 3.0);
		   ap1.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.RED);
		   //QuadMeshShape qms = TubeUtility.makeTubeAsIFS(form, .04, null, TubeUtility.PARALLEL, true, Pn.EUCLIDEAN);
		   //GeometryUtility.calculateAndSetNormals(qms);	   	
		   IndexedLineSet croxl = IndexedFaceSetUtility.createCurveFromPoints(form, true);
		   globeNode2.setGeometry(croxl);
		   globeNode.addChild(globeNode2);
	   }
	   
	   //SceneGraphComponent globeNode4= SceneGraphUtilities.createFullSceneGraphComponent("patch");
	   SceneGraphComponent globeNode4 = SceneGraphUtilities.createFullSceneGraphComponent("a node ");
	   globeNode4.setTransformation(new Transformation());
	   Appearance ap1 = new Appearance();
	   ap1.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLUE);
	   ap1.setAttribute(CommonAttributes.SPECULAR_EXPONENT, 100.0);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, Color.BLACK);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
	   ap1.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH,1.0);
	   ap1.setAttribute(CommonAttributes.POINT_RADIUS,3.0);
	   globeNode4.setAppearance(ap1);
	   
	   if (doBez)	{
		   	double[][][] tubePoints = TubeUtility.makeTubeAsBezierPatchMesh(form, .2, circle, TubeUtility.PARALLEL,true, Pn.EUCLIDEAN);
	   		SceneGraphComponent parent = new LevelOfDetailComponent(lodLevels);
	   		for (int i = 0; i<4; ++i)	{ 
	   			BezierPatchMesh bpm = new BezierPatchMesh(2, 3, tubePoints);
		   		for (int j = 1; j<= i; ++j)	bpm.refine();
		   		QuadMeshShape qmpatch = QuadMeshUtility.representBezierPatchMeshAsQuadMesh(bpm);	   
		   		SceneGraphComponent sgc = SceneGraphUtilities.createFullSceneGraphComponent("selection child "+i);
		   		sgc.setGeometry(qmpatch);	   
		   		parent.addChild(sgc);
	   		}
	   		globeNode4.addChild(parent);
	   }

	   if (doIco)	{
		   SceneGraphComponent tubie = TubeUtility.ballAndStick(Primitives.sharedIcosahedron, .10, .05, java.awt.Color.YELLOW, java.awt.Color.GREEN, Pn.EUCLIDEAN); //TubeUtility.createTubesOnEdges(Primitives.sharedIcosahedron, .05); //TubeUtility.makeTubeAsIFS(p1, p2, .3, null);
		   tubie.setTransformation(new Transformation());
		   tubie.getTransformation().setStretch(.5);
		   tubie.setAppearance(new Appearance());
		   globeNode4.addChild(tubie);	   	
	   }
		String[] texNameSuffixes = {"rt","lf","up", "dn","bk","ft"};
		//ReflectionMap refm = ReflectionMap.reflectionMapFactory("/homes/geometer/gunn/Pictures/textures/desertstorm/desertstorm_", texNameSuffixes, "JPG");
		//root.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+"reflectionMap", refm);
		root.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW,false);

	   root.addChild(globeNode);
	   globeNode.addChild(globeNode4);
	  return root;
	}
	
	public void setConfiguration(ConfigurationAttributes config) {
	}

	public int getSignature() {
		return Pn.EUCLIDEAN;
	}

	public boolean addBackPlane() {
		return true;
	}
	public boolean isEncompass() {
		return true;
	}

	public static void colorByAngle(IndexedLineSet ils, double[] color1, double[] color2)	{
		int nPts = ils.getNumPoints();
		double[][] colors = new double[nPts][3];
		double[][] vertices = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		for (int i = 1; i<nPts-1; ++i)	{
			double[] v1 = Rn.subtract(null, vertices[i], vertices[i-1]);
			double[] v2 = Rn.subtract(null, vertices[i], vertices[i+1]);
			//System.out.println("Angle "+i+" is "+angle);
			//double t = Math.abs(angle/Math.PI);
			double t = 10 * Math.sqrt(Math.abs( v1[0]*v1[0] + v1[1] * v1[1]));
			t = t  - ((int) t);
			Rn.linearCombination(colors[i],t, color1, 1-t, color2);
		}
		System.arraycopy(colors[1], 0, colors[0], 0, 3);
		System.arraycopy(colors[nPts-2], 0, colors[nPts-1], 0, 3);
		ils.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
	}

}
