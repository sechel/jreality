/*
 * Author	gunn
 * Created on Apr 25, 2005
 *
 */
package de.jreality.geometry;

import java.awt.Dimension;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;

/**
 * @author gunn
 *
 */
public class QuadMeshUtility {

	private QuadMeshUtility() {}


	public static IndexedFaceSet representBezierPatchMeshAsQuadMesh(BezierPatchMesh bpm)	{
		return representBezierPatchMeshAsQuadMesh(null, bpm);
	}
	
	public static IndexedFaceSet representBezierPatchMeshAsQuadMesh(IndexedFaceSet existing, BezierPatchMesh bpm)	{
		double[][][] thePoints = bpm.getControlPoints();
		//if (qmpatch == null) 
		AbstractQuadMeshFactory qmf = new AbstractQuadMeshFactory(existing, Pn.EUCLIDEAN, thePoints[0].length, thePoints.length, false, false);
        double[] verts1d = Rn.convertArray3DToArray1D(thePoints);
        qmf.setVertexCoordinates(verts1d);
        qmf.setGenerateFaceNormals(true);
        qmf.setGenerateVertexNormals(true);
        qmf.update();
//		qmpatch.setVertexCountAndAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(thePoints[0][0].length).createReadOnly(verts1d));
//		GeometryUtility.calculateAndSetNormals(qmpatch);
		existing = qmf.getIndexedFaceSet();
		return existing;
	}

	/**
	 * @deprecated
	 */
	 public static double[][] extractUParameterCurve(double[][] curve, QuadMeshShape qms, int which)	{
		return extractParameterCurve(curve, qms, which, 0);
	}

		/**
		 * @deprecated
		 */
	public static double[][] extractVParameterCurve(double[][] curve, QuadMeshShape qms, int which)	{
		return extractParameterCurve(curve, qms, which, 1);
	}

	/**
	 * @deprecated
	 */
	public static double[][] extractParameterCurve(double[][] curve, QuadMeshShape qms, int which, int type)	{
		return extractParameterCurve(curve, qms, qms.getMaxU(), qms.getMaxV(), which, type);
	}
	public static double[][] extractUParameterCurve(double[][] curve, IndexedFaceSet ifs, int which)	{
		return extractParameterCurve(curve, ifs, which, 0);
	}

	public static double[][] extractVParameterCurve(double[][] curve, IndexedFaceSet ifs, int which)	{
		return extractParameterCurve(curve, ifs, which, 1);
	}

	public static double[][] extractParameterCurve(double[][] curve, IndexedFaceSet qms, int which, int type)	{
		Dimension dim = (Dimension) qms.getGeometryAttributes(GeometryUtility.QUAD_MESH_SHAPE);
		return extractParameterCurve(curve, qms, dim.width, dim.height, which, type);
	}
	// extract a curve for a given fixed u-value 
	public static double[][] extractParameterCurve(double[][] curve, IndexedFaceSet ifs, int u, int v, int which, int type)	{
		DataList verts = ifs.getVertexAttributes(Attribute.COORDINATES);
//		int u = qms.getMaxU();
//		int v = qms.getMaxV();
		boolean closedU = false; //qms.isClosedInUDirection();
		boolean closedV = false; //qms.isClosedInVDirection();
		int numverts = u*v;
		int lim = 0, begin = 0, stride = 0, modulo;
		if (type == 0)	{
			lim = (closedV) ? v+1 : v;
			begin = which;
			stride = u;
			modulo = numverts;
		} else {
			lim = (closedU) ? u+1 : u;
			begin = which * u;
			stride = 1;
			modulo = u;
		}
		int n = GeometryUtility.getVectorLength(verts);
		if (curve == null || curve.length != lim || curve[0].length != n)	 curve = new double[lim][n];
		int m, i;
		for (i = 0, m = 0; i<lim; ++i, m += stride)	{
			int xx = begin + (m % modulo);
			DoubleArray da = verts.item(xx).toDoubleArray();
			for (int j = 0; j < n; ++j)	{
				curve[i][j] = da.getValueAt(j);				
			}
		}
		return curve;
	}

}
