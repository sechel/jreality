/*
 * Created on May 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.jogl;

import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StorageModel;

/**
 * @author Charles Gunn
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DiscreteSpaceCurve extends IndexedLineSet {
	boolean closed = false;
	/**
	 * 
	 */
	public static double[][] square = {{1d,1d,0d},{1d,-1d,0d},{-1d,-1d,0d},{-1d,1d,0d}};
	
	public DiscreteSpaceCurve()	{
		this(square, true); 
	}
	
	public DiscreteSpaceCurve(double[][] vertices, boolean closed) {
		super(vertices.length, 1);
		int vectorLength = vertices[0].length;
		setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.array(vectorLength).createReadOnly(vertices));
		int numPoints = getNumPoints();
		setClosed(closed);
		int extra = closed? 1 : 0;
		int[][] ind = new int[1][numPoints+extra];
		for (int i = 0 ; i<numPoints; ++i) {
			ind[0][i] = i;
		}
		if (closed) ind[0][numPoints] = 0;
		setEdgeCountAndAttributes(Attribute.INDICES, new IntArrayArray.Array(ind));
	}

	public static DiscreteSpaceCurve discreteTorusKnot(double R, double r, int n, int m, int nPts)	{
		double[][] vertices = new double[nPts][3];
		for (int i = 0; i<nPts; ++i)	{
			double angle = ( i * 2.0 * Math.PI)/ nPts;
			double a = m * angle, A = n * angle;
			double C = Math.cos(A),			S = Math.sin(A);
			double c = r*Math.cos(a), 			s = r*Math.sin(a);
			
			vertices[i][0] = C * (R + c);
			vertices[i][1] = s;
			vertices[i][2] = S * (R+c);
		}
		return new DiscreteSpaceCurve(vertices, true);
	}

	public Geometry makeTube(double radius)	{
		// this is your assignment: fill in this method
		return null;
	}
	/**
	 * @return
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @param b
	 */
	public void setClosed(boolean b) {
		closed = b;
	}

}
