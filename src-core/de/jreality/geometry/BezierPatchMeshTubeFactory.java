/*
 * Author	gunn
 * Created on Nov 14, 2005
 *
 */
package de.jreality.geometry;

import de.jreality.geometry.TubeUtility.FrameInfo;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedLineSet;

public class BezierPatchMeshTubeFactory extends TubeFactory {
	BezierPatchMesh theTube;

//	public BezierPatchMeshTubeFactory(IndexedLineSet ils) {
//		super(ils);
//	}

	public BezierPatchMeshTubeFactory(double[][] curve) {
		super(curve);
	}

	public BezierPatchMesh getTube()	{
		return theTube;
	}
	
	public void update() {
		int n = theCurve.length;
		int vl = crossSection[0].length;
		
		if (theCurve[0].length != 3)	{
			throw new IllegalArgumentException("Points must be dimension 3");
		}
		int usedVerts = closedCurve ? n+3 : n;
		double[][] polygon2 = new double[usedVerts][];
		if (closedCurve)	{
			for (int i = 0; i<n; ++i)	polygon2[i+1] = theCurve[i];
			polygon2[0] = theCurve[n-1];
			polygon2[n+1] = theCurve[0];
			if (closedCurve) polygon2[n+2] = theCurve[1];				
		} else {
			for (int i = 1; i<n-1; ++i)	polygon2[i] = theCurve[i];
			polygon2[0] = theCurve[0]; //Rn.add(null, polygon[0],  Rn.subtract(null, polygon[0], polygon[1]));
			polygon2[n-1] = theCurve[n-1]; //Rn.add(null, polygon[n-1], Rn.subtract(null, polygon[n-1], polygon[n-2]));
		}
		
		double[][] P = new double[2*usedVerts-1][3];
		// insert midpoints of edges as new vertices
		for (int i = 0; i<usedVerts; ++i)	{
			Rn.copy(P[2*i], polygon2[i]);
			if (i< (usedVerts - 1)) Rn.times(P[2*i+1], .5, Rn.add(null, polygon2[i], polygon2[i+1]));
		}
		
		FrameInfo[] frames = makeFrameField(P, frameFieldType, signature);
		int  numBezierNodes = closedCurve ? 3*n + 1 : 3*(n-2) + 1;
		double[][][] vals = new double[numBezierNodes][crossSection.length][vl];
		
//		LoggingSystem.getLogger().log(Level.FINE,"Input curve has "+n+" segments");
//		LoggingSystem.getLogger().log(Level.FINE,"Curve handed over to routines "+P.length+" segments");
//		LoggingSystem.getLogger().log(Level.FINE,"Frame field has "+frames.length+" frames");
//		LoggingSystem.getLogger().log(Level.FINE,"Bezier patch mesh has "+vals.length+" nodes");
		
		double[] scaleMatrix = Rn.identityMatrix(4);
		double radcoord = radius;
		if (signature == Pn.ELLIPTIC)		radcoord = Math.tan(radius);
		else if (signature == Pn.HYPERBOLIC) 	radcoord = Pn.tanh(radius);
		scaleMatrix[5] = radcoord;
		int m = crossSection.length;
		double[] rotateNBMatrix = new double[16];
		//n = closed ? 2 * usedVerts - 1  :  2 * usedVerts - 3;
		int nn = closedCurve? frames.length-2 : frames.length;
		for (int i = 0,outCount = 0; i<nn; ++i)	{
			int index = i;
			double sangle = Math.sin(frames[index].theta/2.0);
			scaleMatrix[0] = radcoord * ((sangle == 0) ? 1.0 : 1.0/sangle);
			P3.makeRotationMatrixZ(rotateNBMatrix, frames[index].phi);
			double[] scaledFrame = Rn.times(null, frames[index].frame, Rn.times(null,scaleMatrix, rotateNBMatrix));
			for (int j = 0; j<m; ++j)	{
				int p = m - j - 1;
				Rn.matrixTimesVector(vals[outCount][j], scaledFrame, crossSection[p]);
			}
			if (i%2 == 1)	{		
				outCount++;
				//LoggingSystem.getLogger().log(Level.FINE,"Theta is "+frames[index].theta);
			}
			outCount++;
		}
		int k = vals.length;
		int ai = 0;
		for (int i = 0; i<k; ++i)		{
			if ((i%3) != 1) continue;		// midpoints
			int next = i+2;
			int prev = i-1;
			double a = Math.abs(frames[ai].theta/Math.PI);
			// This quadratic function is chosen so that the resulting spline surface
			// is a cylinder when angle = pi,
			// is close to a circular arc for angle = pi/2
			double p = .4379 * a*a - .1046 * a + .3333;
			//p = p * Math.sin(angles[ai]/2);
			p *= .8;
			//p = 1.0;
			double ip = 1.0 - p;
			ai += 2;
			
			for (int j = 0; j<m; ++j) {
				Rn.linearCombination(vals[i+1][j], ip, vals[next][j], p, vals[i][j]);
				Rn.linearCombination(vals[i][j], ip, vals[prev][j], p, vals[i][j]);
			}
		}
		theTube = new BezierPatchMesh(2, 3, vals);
	}
	
}
