/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.worlds;

import java.awt.Color;

import de.jreality.geometry.GeometryUtility;
import de.jreality.geometry.Primitives;
import de.jreality.geometry.WingedEdge;
import de.jreality.scene.Appearance;
import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.Rn;
import de.jreality.util.SceneGraphUtilities;
import discreteGroup.DiscreteGroupSceneGraphRepresentation;
import discreteGroup.DiscreteGroupUtility;
import discreteGroup.TriangleGroup;


/**
 * @author gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SoccerBall extends AbstractLoadableScene {
	SceneGraphComponent icokit;
	boolean tryFlatten = true;
	/**
	 * 
	 */
	public SoccerBall() {
		super();
	}

	
		/*	
		*/
	
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtilities.createFullSceneGraphComponent("soccerball");
			TriangleGroup tg = TriangleGroup.instanceOfGroup("*235");
			TriangleGroup.prepareArchimedeanSolid(tg, "*235", "110");
			IndexedFaceSet foo = (IndexedFaceSet) DiscreteGroupUtility.getSplitFundamentalRegion(tg);
			double[][] verts = foo.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			System.out.println("Verts are \n"+Rn.toString(verts));
			IndexedFaceSet hexifs = new IndexedFaceSet();
			IndexedFaceSet pentifs = new IndexedFaceSet();
			int[][] hexind = new int[][]{{0,3,1},{0,4,3}};
			int[][] pentind = new int[][]{{0,2,4}};
			GeometryUtility.setIndexedFaceSetFrom(hexifs, hexind, verts,null,null,null,null,null);
			GeometryUtility.calculateAndSetFaceNormals(hexifs);
			GeometryUtility.setIndexedFaceSetFrom(pentifs, pentind, verts, null, null, null,null,null);
			GeometryUtility.calculateAndSetFaceNormals(pentifs);
			pentifs.buildEdgesFromFaces();
			hexifs.buildEdgesFromFaces();
			for (int i = 0; i<3; ++i)	{
				pentifs = GeometryUtility.binaryRefine(pentifs);
				pentifs.buildEdgesFromFaces();
				hexifs = GeometryUtility.binaryRefine(hexifs);
				hexifs.buildEdgesFromFaces();
			}
			verts = pentifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			int vlength = GeometryUtility.getVectorLength(pentifs);
			Rn.normalize(verts, verts);
			pentifs.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			pentifs.setVertexAttributes(Attribute.NORMALS,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			verts = hexifs.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			vlength = GeometryUtility.getVectorLength(hexifs);
			Rn.normalize(verts, verts);
			hexifs.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			hexifs.setVertexAttributes(Attribute.NORMALS,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));

			SceneGraphComponent pentsgc = SceneGraphUtilities.createFullSceneGraphComponent("pentagon");
			pentsgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			pentsgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.WHITE);
			pentsgc.setGeometry(pentifs);

			SceneGraphComponent hexsgc = SceneGraphUtilities.createFullSceneGraphComponent("hexagon");
			hexsgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
			hexsgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.GREEN);
			hexsgc.setGeometry(hexifs);

			int[][] tubeind = new int[][]{{1,0},{0,4}};
			IndexedLineSet tubeils = new IndexedLineSet();
			GeometryUtility.setIndexedLineSetFrom(tubeils, tubeind, verts, null, null);
			tubeils = GeometryUtility.refine(tubeils, 20);
			verts = tubeils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
			vlength = GeometryUtility.getVectorLength(tubeils);
			Rn.normalize(verts, verts);
			tubeils.setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
			
			SceneGraphComponent tubesgc = SceneGraphUtilities.createFullSceneGraphComponent("tubes");
			tubesgc.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, true);
			tubesgc.getAppearance().setAttribute(CommonAttributes.TUBES_DRAW, true);
			tubesgc.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR,Color.BLUE);
			tubesgc.setGeometry(tubeils);

			SceneGraphComponent fundDomain = SceneGraphUtilities.createFullSceneGraphComponent("FD");
			fundDomain.addChild(pentsgc);
			fundDomain.addChild(hexsgc);
			fundDomain.addChild(tubesgc);
			DiscreteGroupSceneGraphRepresentation sgr = new DiscreteGroupSceneGraphRepresentation(tg);
			sgr.setWorldNode(fundDomain);
			SceneGraphComponent DGRepn =  sgr.getRepresentationRoot();
			theWorld.addChild(DGRepn);

			
//			for (int i = 0; i<4; ++i)	{
//				if (i == 0)		spheres[0] = Primitives.icosahedron();
//				else {
//					spheres[i] = GeometryUtility.binaryRefine(spheres[i-1]);
//					double[][] verts = spheres[i].getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
//					int vlength = GeometryUtility.getVectorLength(spheres[i]);
//					Rn.normalize(verts, verts);
//					spheres[i].setVertexAttributes(Attribute.COORDINATES,  StorageModel.DOUBLE_ARRAY.array(vlength).createReadOnly(verts));
//				}
//				spheres[i].setVertexAttributes(Attribute.NORMALS, spheres[i].getVertexAttributes(Attribute.COORDINATES)); 
//				GeometryUtility.calculateAndSetFaceNormals(spheres[i]);
//				spheres[i].buildEdgesFromFaces();
//				
//				DataList vv = spheres[i].getVertexAttributes(Attribute.COORDINATES);
//				int ll = spheres[i].getNumPoints();
//				double[][] vc = new double[ll][4];
//				for (int j=0; j<ll; ++j)	{
//					DoubleArray v = vv.item(j).toDoubleArray();
//					vc[j][0] = .3+.7*v.getValueAt(0);
//					vc[j][1] = .3+.7*v.getValueAt(1);
//					vc[j][2] = .3+.7*v.getValueAt(2);
//					vc[j][3] = 0.5d;		// alpha component
//				}
//				spheres[i].setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(4).createReadOnly(vc));
//
//				icokit = SceneGraphUtilities.createFullSceneGraphComponent("sphere"+i);
//				icokit.setTransformation(new Transformation());
//				icokit.setGeometry(spheres[i]);
//				icokit.getTransformation().setTranslation(-1.5 + i, 0, 0);
//				icokit.getTransformation().setStretch(.5);
//				if (i == 0) icokit.getAppearance().setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
//				if (i == 1) icokit.getAppearance().setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
//				theRow.addChild(icokit);
//
//			}
//				
//			theWorld.addChild(theRow);
//			theWorld.addChild(newRow);
			return theWorld;
		}
	
		public boolean isEncompass() {return true;}
		public boolean addBackPlane() {return true; }
	
}
