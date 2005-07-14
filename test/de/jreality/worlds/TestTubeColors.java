/*
 * Author	gunn
 * Created on May 4, 2005
 *
 */
package de.jreality.worlds;

import de.jreality.scene.CommonAttributes;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.util.SceneGraphUtilities;

/**
 * @author gunn
 *
 */
public class TestTubeColors extends AbstractJOGLLoadableScene {
	SceneGraphComponent icokit;
		/*	
		*/
	    
		public SceneGraphComponent makeWorld()	{
			SceneGraphComponent theWorld = SceneGraphUtilities.createFullSceneGraphComponent();
			double[][] verts = new double[5][3];
			int order = 5;
			for (int  i =0; i<order; ++i)	{
				double angle = 2 * Math.PI * i/((double) order);
				verts[i][0] = Math.cos(angle);
				verts[i][1] = Math.sin(angle);
				verts[i][2] = 0.0;
			}
			double[][] colors = {{1,1,1},{1,1,0},{0,1,1},{1,0,1},{0,1,0}};
			int[][] findices = {{0,1,2,3,4}};
			int[][] indices = {{0,1},{1,2},{2,3},{3,4},{4,0}};
			IndexedLineSet ils = new IndexedLineSet(5,5);
			ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
			ils.setEdgeCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(indices));
			ils.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
			IndexedFaceSet ifs = new IndexedFaceSet(5,1);
			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(verts));
			ifs.setFaceCountAndAttributes(Attribute.INDICES, StorageModel.INT_ARRAY_ARRAY.createReadOnly(findices));
			ifs.buildEdgesFromFaces();
			ifs.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(3).createReadOnly(colors));
			theWorld.getAppearance().setAttribute(CommonAttributes.FACE_DRAW,true);

			theWorld.setGeometry( ils);
			
			return theWorld;
		}
	
		public boolean isEncompass() {
			return true;
		}
			

}
