/*
 * Author	gunn
 * Created on Nov 14, 2005
 *
 */
package de.jreality.geometry;

import java.util.logging.Level;

import de.jreality.geometry.TubeUtility.FrameInfo;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.StorageModel;

public class PolygonalTubeFactory extends TubeFactory {
	IndexedFaceSet theTube;
	QuadMeshFactory qmf;
	double[][] theTubeVertices;
	
//	public PolygonalTubeFactory(IndexedLineSet ils) {
//		super(ils);
//	}
//
	public PolygonalTubeFactory(double[][] curve) {
		super(curve);
	}
	

	/**
	 * 
	 * @param polygon
	 * @param radius
	 * @param xsec
	 * @param type
	 * @param closed
	 * @param signature
	 * @return
	 */
	private double[][] polygon2, vals;
	protected  double[][] makeTube(double[][] polygon, double radius, double[][] xsec, int type, boolean closed, int signature, int twists)	{
		int n = polygon.length;
		int vl = xsec[0].length;
		// have to handle the situation here that the first and last points are the same but the closed flag isn't set.
		// We assume for now that the user wants to treat this as a closed curve but we have to ignore the last point
		// Here's how we do that
		boolean autoClosed = false;
		double d = Rn.euclideanDistance(polygon[0], polygon[n-1]);
		autoClosed =  d < 10E-8;
		if (autoClosed)	{
			closed = true;
			n = n-1;
		}
		int realLength = (closed ? n+1 : n)*xsec.length;
		if (vals == null || vals.length != realLength || vals[0].length != vl)
			vals = new double[realLength][vl];

		if (n <= 1) {
			throw new IllegalArgumentException("Can't tube a vertex list of length less than 2");
		}
		
		int usedVerts = closed ? n+3 : n+2;
		if (polygon2 == null || polygon2.length != usedVerts)  
			polygon2 = new double[usedVerts][];
		for (int i = 0; i<n; ++i)	polygon2[i+1] = polygon[i];
		if (closed)	{
			polygon2[0] = polygon[n-1];
			polygon2[n+1] = polygon[0];
			polygon2[n+2] = polygon[1];				
		} else {
			polygon2[0] = Rn.add(null, polygon[0],  Rn.subtract(null, polygon[0], polygon[1]));
			polygon2[n+1] = Rn.add(null, polygon[n-1], Rn.subtract(null, polygon[n-1], polygon[n-2]));
			
		}
		FrameInfo[] frames = makeFrameField(polygon2, type, signature);
		double[] rad = Rn.identityMatrix(4);
		rad[0] = rad[5] = radius;
		int nn = frames.length;
		for (int i = 0; i<nn; ++i)	{
			// scale normal vector
			double sangle = Math.sin( frames[i].theta/2.0);
			double factor = 1.0;
			if (sangle != 0) factor = 1.0/sangle;
			rad[0] = radius *factor;
			double[] zrot = P3.makeRotationMatrixZ(null,frames[i].phi+ twists*2*Math.PI*frames[i].length);
			double[] scaledFrame = Rn.times(null, frames[i].frame, Rn.times(null, rad, zrot));
			//LoggingSystem.getLogger().log(Level.FINE,"Theta is "+frames[i].theta);
			int m = xsec.length;
			for (int j = 0; j < m; ++j) {
				int p = j; //m - j - 1;
				Rn.matrixTimesVector(vals[(i) * m + j], scaledFrame, xsec[p]);
			}
		}
		return vals;
	}
	
	 public void update() {
		theTubeVertices = makeTube(theCurve, radius, crossSection, frameFieldType, closedCurve, signature, twists);
		qmf = new QuadMeshFactory();
		qmf.setSignature(signature);
		qmf.setULineCount(crossSection.length);
		qmf.setVLineCount(theTubeVertices.length/crossSection.length);
		qmf.setClosedInUDirection(true);
		qmf.setClosedInVDirection(closedCurve);
		
		//signature, crossSection.length, theTubeVertices.length/crossSection.length, true,closedCurve);
		qmf.setVertexCoordinates(theTubeVertices);
		qmf.setGenerateFaceNormals(true);
		qmf.setGenerateVertexNormals(true);
		if (generateTextureCoordinates)	{
			if (!arcLengthTextureCoordinates) qmf.setGenerateTextureCoordinates(true);
			else {
				qmf.setVertexTextureCoordinates(arcLengthTextureCoordinates(theCurve, crossSection, signature));
			}
		}
		qmf.setGenerateTextureCoordinates(generateTextureCoordinates && !arcLengthTextureCoordinates);
		qmf.update();
		theTube = qmf.getIndexedFaceSet();
		if (vertexColors != null || edgeColors != null)	{
//		 	DataList theCurveAsILSEdgeColors = theCurveAsILS.getEdgeAttributes(Attribute.COLORS);
//		 	DataList theCurveAsILSVertexColors = theCurveAsILS.getVertexAttributes(Attribute.COLORS);
//		 	int theCurveAsILSNumVerts = theCurveAsILS.getNumPoints();
//		 	int theCurveAsILSNumEdges = theCurveAsILS.getNumEdges();
		 	int numVerts = theTube.getNumPoints();
		 	int numFaces = theTube.getNumFaces();
		 	// transfer the colors
		 	int xsLength = crossSection.length;
		 	if (edgeColors != null)	{
		 		int colorLength = edgeColors[0].length;
		 		double[][] faceColors = new double[numFaces][colorLength];
		 		int lim = numFaces/crossSection.length;
		 		for (int j = 0; j<lim; ++j)	{
		 			for (int k = 0; k<xsLength; ++k)	{
		 				for (int m = 0; m<colorLength; ++m)	{
		 					faceColors[j*xsLength+k][m] = edgeColors[j%edgeColors.length][m];
		 				}
		 			}
		 		}
		 		theLogger.log(Level.FINER,"Setting Face colors");
		 		theTube.setFaceAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(colorLength).createReadOnly(faceColors));
		 	}
		 	if (vertexColorsEnabled && vertexColors != null)	{
		 		int colorLength = vertexColors[0].length;
		 		double[][] vertColors = new double[numVerts][colorLength];
		 		int realNumVerts = numVerts/xsLength;
		 		for (int j = 0; j<realNumVerts; ++j)	{
		 			for (int k = 0; k<xsLength; ++k)	{
		 				for (int m = 0; m<colorLength; ++m)	{
		 					vertColors[j*xsLength+k][m] = vertexColors[j%vertexColors.length][m];
		 				}
		 			}
		 		}
		 		theLogger.log(Level.FINER,"Setting vertex colors");
		 		theTube.setVertexAttributes(Attribute.COLORS, StorageModel.DOUBLE_ARRAY.array(colorLength).createReadOnly(vertColors));
		 	}
		}
	}
	
	private double[][] arcLengthTextureCoordinates(double[][] theCurve, double[][] crossSection, int signature) {
			
			final int vLineCount = theCurve.length;
			final int uLineCount = crossSection.length;
			double[][] textureCoordinates = new double[uLineCount*vLineCount][2];
			int vLength = theCurve[0].length;			// 3 or 4?
			// create a list of v-parameter values parametrized by arc-length
			double[] lengths = new double[vLineCount];
			lengths[0] = 0.0;
			for (int i = 1; i<vLineCount; ++i)	{
				if (vLength == 3)		lengths[i] = lengths[i-1] + Rn.euclideanDistance(theCurve[i], theCurve[i-1]);
				else lengths[i] = lengths[i-1] + Pn.distanceBetween(theCurve[i], theCurve[i-1], signature);
			}
			final double dv= 1.0 / (vLineCount - 1);
			final double du= 1.0 / (uLineCount - 1);
			
			double v=0;
			double curveLength = lengths[vLineCount-1];
			for(int iv=0, firstIndexInULine=0;
			iv < vLineCount;
			iv++,  firstIndexInULine+=uLineCount) {
				double u=0;
				for(int iu=0; iu < uLineCount; iu++, u+=du) {
					final int indexOfUV=firstIndexInULine + iu;
					textureCoordinates[indexOfUV][0] = u;
					textureCoordinates[indexOfUV][1] = lengths[iv] / curveLength;
				}
			}
					
			return textureCoordinates;
		}

	public IndexedFaceSet getTube()	{
		return theTube;
	}


}
