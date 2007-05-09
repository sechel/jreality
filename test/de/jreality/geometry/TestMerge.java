package de.jreality.geometry;

import java.awt.Color;

import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestMerge {

	static ViewerApp vApp ;
	static SceneGraphComponent root= new SceneGraphComponent();

	public static void main(String[] args) {
		IndexedFaceSet ico= Primitives.sharedIcosahedron;
		IndexedFaceSet ico2= Primitives.sharedIcosahedron;
		IndexedFaceSetUtility.assignSmoothVertexNormals(ico2, 20);
		IndexedFaceSet box= Primitives.box(10, .5, .5, true);
		IndexedFaceSet box2= Primitives.box(10, .6, 0.4, true);
		IndexedFaceSet zyl= Primitives.cylinder(20,1,0,.5,5);

		SceneGraphComponent root= new SceneGraphComponent();
		Appearance app=new Appearance();
		app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(255,255,0));
		app.setAttribute(CommonAttributes.VERTEX_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(0,255,255));
		root.setAppearance(app);

		SceneGraphComponent A= new SceneGraphComponent();
		SceneGraphComponent B= new SceneGraphComponent();

		SceneGraphComponent C= new SceneGraphComponent();
		SceneGraphComponent A1= new SceneGraphComponent();
		MatrixBuilder.euclidean().translate(0,1,0).assignTo(A1);
		SceneGraphComponent A11= new SceneGraphComponent();
		MatrixBuilder.euclidean().rotate(Math.PI/2,0,0,1 ).assignTo(A11);
		SceneGraphComponent B1= new SceneGraphComponent();
		SceneGraphComponent B2= new SceneGraphComponent();
		Appearance app2=new Appearance();
		app2.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, new Color(255,0,255));
		B2.setAppearance(app2);

		root.addChild(A); 	A.addChild(A1); A1.addChild(A11);
		root.addChild(B); 	B.addChild(B1);
		B.addChild(B2);
		root.addChild(C);

		A1.setGeometry(box);
		A11.setGeometry(box2);
		B1.setGeometry(zyl);
		B2.setGeometry(ico);
		//C.setGeometry(ico2);

		IndexedFaceSet[] list= new IndexedFaceSet[]{ico};
		//PointSet[] list= new PointSet[]{box,box2,zyl,ico2};
		//IndexedLineSet[] list= new IndexedLineSet[]{box,box2,zyl,ico2};
		//IndexedFaceSet i=mergeIndexedFaceSets(list,new Attribute[]{Attribute.COLORS},new double[][][]{{{0,1,1}}},null,null,null,null );

		GeometryMergeFactory t= new GeometryMergeFactory();
		//t.respectFaces=false;
		//t.generateFaceNormals=false;
		//t.generateVertexNormals=false;
		//t.respectEdges=false;
		
		IndexedFaceSet i=t.mergeGeometrySets(root);
		//PointSet i=t.mergeIndexedLineSets(list);
		//PointSet i=t.mergeIndexedFaceSets(list);
		//System.out.println("Report:"+i);
		vApp.display(i);
	}

}
