/*
 * Created on Feb 13, 2004
 *
 */

package de.jreality.math;

import java.util.logging.Level;

import de.jreality.util.LoggingSystem;

/**
 * Library class for holding static methods for geometry of the real projective plane.
 * 
 * @author gunn
 *
 */
final public class P2 {

	/**
	 * 
	 */
	private P2() {
		super();
	}

	private static double[] perpendicularBisector(double[] dst, double[] p1, double[]p2)	{
		if (p1.length != 3 || p2.length != 3)	{
			throw new IllegalArgumentException("Input points must be homogeneous vectors");
		}
		if (dst == null) dst = new double[3];
		double[] avg = new double[3];
		Rn.add(avg,p1,p2);
		Rn.times(avg, .5, avg);
		double[] line = new double[3];
		lineFromPoints(line, p1, p2);
		dst[0] = -line[1];
		dst[1] = line[0];
		dst[2] = -(dst[0]*avg[0] + dst[1]*avg[1]);
		return dst;
	}
	
	public static double[] perpendicularBisector(double[] dst, double[] p1, double[]p2, int signature)	{
		if (p1.length != 3 || p2.length != 3)	{
			throw new IllegalArgumentException("Input points must be homogeneous vectors");
		}
		if (signature == Pn.EUCLIDEAN) return perpendicularBisector(dst, p1, p2);
		if (dst == null) dst = new double[3];
		double[] midpoint = new double[3];
		Pn.linearInterpolation(midpoint,p1,p2, .5, signature);
		double[] line = lineFromPoints(null, p1, p2);
		double[] polarM = Pn.polarize(null, midpoint, signature);
		double[] pb = pointFromLines(null, polarM, line);
		Pn.polarize(dst, pb, signature);
		if (Rn.innerProduct(dst,p1) < 0)	Rn.times(dst, -1.0, dst);
		return dst;
	}
	
	public static double[] pointFromLines(double[] point, double[] l1, double[] l2)	{
		if (l1.length < 3 || l2.length < 3)	{
			throw new IllegalArgumentException("Input arrays too short");
		}
		if (point == null) point = new double[3];	
		point[0] = l1[1]*l2[2] - l1[2]*l2[1];
		point[1] = l1[2]*l2[0] - l1[0]*l2[2];
		point[2] = l1[0]*l2[1] - l1[1]*l2[0];
		return point;
	}
	
	public static double[] lineFromPoints(double[] line, double[] p1, double[] p2)	{
		return pointFromLines(line, p1, p2);
	}
		
	public static boolean polygonContainsPoint(double[][] polygon, double[] point)	{
		if (point.length != 3)	{
			throw new IllegalArgumentException("Input point must be homogeneous vector");
		}
		double sign = 0.0;
		int n = polygon.length, j;
		double[] p1 = new double[3];
		double[] p2 = new double[3];
		double[] tmp;
		p1[2] = p2[2] = 1.0;
		p1[0] = polygon[0][0]; p1[1] = polygon[0][1];
		for (int i = 0; i<n; ++i)	{
			j = (i+1) % n;
			p2[0] = polygon[j][0]; p2[1] = polygon[j][1];
			double[] line = lineFromPoints(null, p1, p2);
			double ip = Rn.innerProduct(line, point);
			if (sign == 0.0) sign = ip;
			else if (sign * ip < 0.0) return false;
			tmp = p1;
			p1 = p2;
			p2 = tmp;
			//System.arraycopy(p2,0,p1,0,3);
		}
		return true;
	}
	
	public static boolean isConvex(double[][] polygon)	{
		int n = polygon.length, j;
		double sign = 0.0;
		double[][] diffs = new double[n][polygon[0].length];
		for (int i = 0; i<n; ++i)	{
			j = (i+1) % n;
			Rn.subtract(diffs[i], polygon[j], polygon[i]);
			Rn.normalize(diffs[i], diffs[i]);
		}
		double[] p1 = new double[3];
		double[] p2 = new double[3];
		double[] tmp = new double[3];
		p1[2] = p2[2] = 1.0;
		p1[0] = polygon[0][0]; p1[1] = polygon[0][1];
		for (int i = 0; i<n; ++i)	{
			j = (i+1) % n;
			Rn.crossProduct(tmp, diffs[i],diffs[j]);
			if (sign == 0.0)	sign = tmp[2];
			else if (sign * tmp[2] < 0.0) return false;
		}
		
		return true;
	}
	/**
	 * The assumption is that the line is specified in such a way that vertices to be cut away
	 * have a negative inner product with the line coordinates.
	 * @param polygon
	 * @param line
	 * @return
	 */
	public static double[][] chopConvexPolygonWithLine(double[][] polygon, double[] line)	{
		if (line.length != 3 )	{
			throw new IllegalArgumentException("Input line must be homogeneous vectors");
		}
		if (polygon == null) return null;
		int n = polygon.length;
		
		double[] center = new double[3];
		Rn.average(center, polygon);
		boolean noNegative = true;
		
		double[] vals = new double[n];
		int count = 0;
		for (int i = 0; i<n; ++i)	{
			vals[i] = Rn.innerProduct(line, polygon[i]);
			if (vals[i] >= 0) count++;
			else noNegative = false;	
			}
		if (count == 0)		{
			LoggingSystem.getLogger(P2.class).log(Level.FINE, "chopConvexPolygonWithLine: nothing left");
			return null;
		} else if (count == n || noNegative)	{
			return polygon;
		}
		double[][] newPolygon = new double[count+2][3];
		double[] tmp = new double[3];
		count = 0;
		for (int i = 0; i<n; ++i)	{
			if (vals[i] >= 0) 	System.arraycopy(polygon[i],0,newPolygon[count++],0,3);
			if (count >= newPolygon.length) break;
			if (vals[i] * vals[(i+1)%n] < 0)	{
				double[] edge = new double[3];
				lineFromPoints(edge, polygon[i], polygon[(i+1)%n]);
				pointFromLines(tmp,edge,line);
				Pn.dehomogenize(newPolygon[count],tmp);
				count++;
			} 
			if (count >= newPolygon.length) break;
		}
		if (count != newPolygon.length) {
			double[][] newPolygon2 = new double[count][];
			System.arraycopy(newPolygon, 0, newPolygon2,0,count);
			return newPolygon2;
		}
		return newPolygon;
	}
	
	public static double[] projectP3ToP2(double[] vec3, double[] vec4)	{
		double[] dst;
		if (vec3 == null)	dst = new double[3];
		else dst = vec3;
		dst[0] = vec4[0];
		dst[1] = vec4[1];
		dst[2] = vec4[3];
		return dst;
	}
	
	public static double[] imbedP2InP3(double[] vec4, double[] vec3)	{
		double[] dst;
		if (vec4 == null)	dst = new double[4];
		else dst = vec4;
		dst[0] = vec3[0];
		dst[1] = vec3[1];
		dst[2] = 0.0;
		dst[3] = vec3[2];
		return dst;
	}

}
