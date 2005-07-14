/*
 * Created on Jun 23, 2004
 *
  */
package de.jreality.jogl;

import java.util.logging.Level;

import de.jreality.util.Rn;

/**
 * @author gunn
 *
 */
public class BezierCurve {
	double[][] controlPoints;
	public BezierCurve(double[][] cp)	{
		super();
		if (cp.length % 3 != 1)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Array length must be for form 3n + 1");
			return;
		}
		controlPoints = cp;
	}
	
	static double[] leftSplit = {1,0,0,0,.5,.5,0,0,.25,.5,.25,0,.125,.375,.375,.125};
	static double[] rightSplit = {.125,.375,.375,.125,0,.25,.5,.25,0,0,.5,.5,0,0,0,1};
	public double[][] refine()	{
		return refine(controlPoints);
	}
	
	public static double[][] refine(double[][] controlPoints)	{
		int n = controlPoints.length;
		if (n % 3 != 1)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Array length must be for form 3n + 1");
			return null;
		}
		int vectorLength = controlPoints[0].length;
		double[][] vals = new double[2*n-1][vectorLength];
		for (int i = 0; i<vectorLength; ++i)	{
			int outCount = 0;
			for (int inCount = 0; inCount < n-1; inCount += 3)	{
				double[] icp = new double[4];
				double[] ocp = new double[4];
				for (int j = 0; j<4; ++j)		icp[j] = controlPoints[inCount+j][i];
				Rn.matrixTimesVector(ocp, leftSplit, icp);
				for (int j = 0; j<4; ++j)		vals[outCount+j][i] = ocp[j];
				outCount += 3;
				Rn.matrixTimesVector(ocp, rightSplit, icp);
				for (int j = 0; j<4; ++j)		vals[outCount+j][i] = ocp[j];
				outCount += 3;
			}
		} 
		return vals;		
	}
	
	public static double[][][] refineU(double[][][] controlPoints)	{
		int uDim = controlPoints.length;
		int vDim = controlPoints[0].length;
		
		if (uDim % 3 != 1 || vDim % 3 != 1)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Array length must be for form 3n + 1");
			return null;
		}
		int vectorLength = controlPoints[0][0].length;
		double[][][] vals = new double[2*uDim-1][vDim][vectorLength];
		for (int k = 0; k< vDim; ++k)	{
			for (int i = 0; i<vectorLength; ++i)	{
				int outCount = 0;
				for (int inCount = 0; inCount < uDim-1; inCount += 3)	{
					double[] icp = new double[4];
					double[] ocp = new double[4];
					for (int j = 0; j<4; ++j)		icp[j] = controlPoints[inCount+j][k][i];
					Rn.matrixTimesVector(ocp, leftSplit, icp);
					for (int j = 0; j<4; ++j)		vals[outCount+j][k][i] = ocp[j];
					outCount += 3;
					Rn.matrixTimesVector(ocp, rightSplit, icp);
					for (int j = 0; j<4; ++j)		vals[outCount+j][k][i] = ocp[j];
					outCount += 3;
				}
			} 
		}
		return vals;		
	}
	public static double[][][] refineV(double[][][] controlPoints)	{
		int uDim = controlPoints.length;
		int vDim = controlPoints[0].length;
		
		if (uDim % 3 != 1 || vDim % 3 != 1)	{
			JOGLConfiguration.getLogger().log(Level.WARNING,"Array length must be for form 3n + 1");
			return null;
		}
		int vectorLength = controlPoints[0][0].length;
		double[][][] vals = new double[uDim][2*vDim-1][vectorLength];
		for (int k = 0; k< uDim; ++k)	{
			for (int i = 0; i<vectorLength; ++i)	{
				int outCount = 0;
				for (int inCount = 0; inCount < vDim-1; inCount += 3)	{
					double[] icp = new double[4];
					double[] ocp = new double[4];
					for (int j = 0; j<4; ++j)		icp[j] = controlPoints[k][inCount+j][i];
					Rn.matrixTimesVector(ocp, leftSplit, icp);
					for (int j = 0; j<4; ++j)		vals[k][outCount+j][i] = ocp[j];
					outCount += 3;
					Rn.matrixTimesVector(ocp, rightSplit, icp);
					for (int j = 0; j<4; ++j)		vals[k][outCount+j][i] = ocp[j];
					outCount += 3;
				}
			} 
		}
		return vals;		
	}
}
