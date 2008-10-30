package de.jreality.geometry;

import junit.framework.TestCase;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.data.Attribute;
import de.jreality.ui.viewerapp.ViewerApp;

public class TestIFSFactory extends TestCase {

	public void testFaceColors()	{
		IndexedFaceSetFactory borromeanRectFactory = null;
		double[][] jitterbugEdgeVerts = new double[][] {{0,0,0,1},{1,0,0,1},{1,1,0,1},{0,1,0,1}};
		int[][] jitterbugSegmentIndices1 = {{0,1},{2,3}}; //,{{{0,1,2,3,0,1},{4,5,6,7,4,5},{8,9,10,11,8,9}};
		int[][] jitterbugFaceIndices = {{0,1,2,3}};
		double[][] borromeanRectColors = {{1.0, 1.0, 200/255.0, 1}};

				borromeanRectFactory = new IndexedFaceSetFactory();
				borromeanRectFactory.setVertexCount(jitterbugEdgeVerts.length);
				borromeanRectFactory.setVertexCoordinates(jitterbugEdgeVerts);	
				borromeanRectFactory.setFaceCount(1);
				borromeanRectFactory.setFaceIndices(jitterbugFaceIndices);	
				borromeanRectFactory.setFaceColors(new double[][]{{1,0,0}});
				borromeanRectFactory.setGenerateFaceNormals(true);
				borromeanRectFactory.setEdgeCount(jitterbugSegmentIndices1.length);
				borromeanRectFactory.setEdgeIndices(jitterbugSegmentIndices1);
				borromeanRectFactory.update();
				borromeanRectFactory.getIndexedLineSet();			
				//System.err.println("Alpha channel is "+borromeanRectFactory.getIndexedFaceSet().getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][3]);
				ViewerApp.display(borromeanRectFactory.getIndexedFaceSet());
				// now we try to change the alpha channel of the face color
				// just to be safe, we don't use the old array but create a new one.
				borromeanRectFactory.setFaceColors(new double[][]{{0,1,0}});
				borromeanRectFactory.update();
				//System.err.println("Alpha channel is "+borromeanRectFactory.getIndexedFaceSet().getFaceAttributes(Attribute.COLORS).toDoubleArrayArray(null)[0][3]);
				
				ViewerApp.display(borromeanRectFactory.getIndexedFaceSet());
	}
	
	public static  void main( String [] arg ) {
		new TestIFSFactory().testFaceColors();
	}
}
