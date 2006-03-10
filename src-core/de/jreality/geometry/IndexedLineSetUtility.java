/*
 * Author	gunn
 * Created on Apr 25, 2005
 *
 */
package de.jreality.geometry;

import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.Scene;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.IntArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;

/**
 * @author gunn
 *
 */
public class IndexedLineSetUtility {

	/**
	 * 
	 */
	private IndexedLineSetUtility() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static IndexedLineSet refine(IndexedLineSet ils, int n)	{
		int[][] indices = ils.getEdgeAttributes(Attribute.INDICES).toIntArrayArray(null);
		for (int i=0; i<indices.length; ++i)	{
			if (indices[i].length != 2) {
				throw new IllegalArgumentException("Edge array can have only 2 points per curve");
			}
		}
		double[][] verts = ils.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		int numEdges = ils.getNumEdges();
		int veclength = verts[0].length;
		double[][] newVerts = new double[n*numEdges][veclength];
		int[][] newIndices = new int[numEdges][n];
		IndexedLineSet newils = new IndexedLineSet(n*numEdges, numEdges);
		for (int i = 0; i<numEdges; ++i)	{
			int i0 = indices[i][0];
			int i1 = indices[i][1];
			double[] p0 = verts[i0];
			double[] p1 = verts[i1];
			for (int j = 0; j<n; ++j)	{
				double t = (j)/(n-1.0);
				double s = 1.0 - t;
				newVerts[i*n+j] = Rn.linearCombination(null, s, p0, t, p1);
				newIndices[i][j] = i*n+j;
			}
		}
		IndexedFaceSetUtility.setIndexedLineSetFrom(newils, newIndices, newVerts, null, null);
		return newils;
	}

	/**
	 * @deprecated
	 *  @param ifs
	 * @param indices
	 * @param verts
	 * @param vcolors
	 * @param ecolors
	 * @return
	 */public static IndexedLineSet setIndexedLineSetFrom(IndexedLineSet ifs, int[][] indices, 
		double[][] verts, 
		double[][] vcolors, 
		double[][] ecolors)  
	{
		if (indices != null)	{
			ifs.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(indices));
		}
		if (verts != null)	{
			int vectorLength = verts[0].length;
			ifs.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(verts));
		}
		if (vcolors != null)	{
			int vectorLength = vcolors[0].length;
			ifs.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vcolors));
		}
		if (ecolors != null)	{
			int vectorLength = ecolors[0].length;
			ifs.setEdgeAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(ecolors));
		}
		return ifs;
	}

	/**
	 * @param curve
	 * @param ils
	 * @param i
	 * @return
	 */
	public static double[][] extractCurve(double[][] curve, IndexedLineSet ils, int i) {
		DataList verts = ils.getVertexAttributes(Attribute.COORDINATES);
		DataList indices = ils.getEdgeAttributes(Attribute.INDICES);
		IntArray thisEdge = indices.item(i).toIntArray();
		int n = thisEdge.getLength();
		double[][] output = null;
		if (curve == null || curve.length != n) output = new double[n][];
		else output = curve;
		for (int j = 0; j<n; ++j)	{
			int which = thisEdge.getValueAt(j);
			output[j] = verts.item(which).toDoubleArray(null);
		}
		return output;
	}

	
	public static IndexedLineSet createCurveFromPoints(double[][] points, boolean closed)	{
		return createCurveFromPoints(null, points,  closed);
	}
		
	/**
	 * @param points
	 * @param closed
	 * @return
	 */
	public static IndexedLineSet createCurveFromPoints(IndexedLineSet g, final double[][] points, boolean closed)	{
		int n = points.length;
		int size = (closed) ? n+1 : n;
		if (g==null) g = new IndexedLineSet(n,1);
		final IndexedLineSet ils = g;
		// TODO replace this with different call if IndexedLineSet exists.
		final int[][] ind = new int[1][size];
		for (int i = 0; i<size ; ++i)	{
			ind[0][i] = (i%n);
		}
		//if (closed) ind[0][n] = 0;
		final int vectorLength = points[0].length;
		
		Scene.executeWriter(ils, new Runnable () {
			public void run() {
				ils.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(ind));
				ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createWritableDataList(points));
				}
		});
		return g;
	}

	public static IndexedLineSet createCurveFromPoints( double[] points, int fiber, boolean closed)	{
		return createCurveFromPoints(null, points, fiber, closed);
	}

	public static IndexedLineSet createCurveFromPoints(IndexedLineSet g, final double[] points, final int fiber, final int[][] indices)	{
		int n = points.length/fiber;
		if (g == null) g = new IndexedLineSet(n,indices.length);
		final IndexedLineSet ils = g;
		Scene.executeWriter(ils, new Runnable () {
			public void run() {
				ils.setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(indices));
				ils.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(fiber).createWritableDataList(points));
				}
		});
		return g;
		
	}

	public static IndexedLineSet createCurveFromPoints(IndexedLineSet g, final double[] points, int fiber, boolean closed)	{
		int n = points.length/fiber;
		int size = (closed) ? n+1 : n;
		// TODO replace this with different call if IndexedLineSet exists.
		final int[][] ind = new int[1][size];
		for (int i = 0; i<size ; ++i)	{
			ind[0][i] = (i%n);
		}
		return createCurveFromPoints(g, points, fiber, ind);
	}

	/**
	 * @deprecated
	 * @param R
	 * @param r
	 * @param n
	 * @param m
	 * @param nPts
	 * @return
	 */public static IndexedLineSet discreteTorusKnot(double R, double r, int n, int m, int nPts)	{
		double[][] vertices = new double[nPts][3];
		for (int i = 0; i<nPts; ++i)	{
			double angle = ( i * 2.0 * Math.PI)/ nPts;
			double a = m * angle, A = n * angle;
			double C = Math.cos(A),				S = Math.sin(A);
			double c = r*Math.cos(a), 			s = r*Math.sin(a);
			
			vertices[i][0] = C * (R + c);
			vertices[i][1] = s;
			vertices[i][2] = S * (R+c);
		}
		return createCurveFromPoints(vertices, true);
	}
	

}
