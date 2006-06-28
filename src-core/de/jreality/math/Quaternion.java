/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.math;

import java.io.Serializable;



/**
 * <p>
 * A simple quaternion class.  Some instance methods, mostly static methods.
 * </p><p>
 * The generic calling convention is <code>public static Quaternion method(Quaternion result, Quaternion q1, Quaternion q2)</code>
 * where if <i>result</i> is null, a new instance is created and the result is returned in it. 
 * @author Charles Gunn
  */
final public class Quaternion implements Cloneable, Serializable {
	public double re, x, y, z;
	/**
	 * No transforms allowed
	 */
	public Quaternion() {
		this(1d, 0d, 0d, 0d);
		// TODO Auto-generated constructor stub
	}
	
	public Quaternion(Quaternion nq) {
		this(nq.re, nq.x, nq.y, nq.z);
		// TODO Auto-generated constructor stub
	}
	
	public Quaternion(double r, double dx, double dy, double dz)	{
		super();
		re = r;
		x = dx;
		y = dy;
		z = dz;
	}
	
	public String toString()	{
		return "re: "+Double.toString(re)+
		"i: "+Double.toString(x)+
		"j: "+Double.toString(y)+
		"k: "+Double.toString(z);
	}
	
	public Object clone()	{
		try {
			Quaternion copy = (Quaternion) super.clone();
			copy.re = re;
			copy.x = x;
			copy.y = y;
			copy.z = z;
			return copy;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void setValue(double r, double dx, double dy, double dz)	{
		re = r;
		x = dx;
		y = dy;
		z = dz;
	}
	static Quaternion INFINITE_QUATERNION = new Quaternion(Double.POSITIVE_INFINITY, 0.0, 0.0, 0.0);
	
	// static methods start here
	public static Quaternion copy(Quaternion dst, Quaternion src)	{
		if (dst == null) return new Quaternion(src);
		dst.re = src.re;
		dst.x = src.x;
		dst.y = src.y;
		dst.z = src.z;
		return dst;
	}
	
	public static double[] IJK(double[] dst, Quaternion q)	{
		// assert dim checks
		dst[0] = q.x;
		dst[1] = q.y;
		dst[2] = q.z;
		return dst;
	}
	
	public static boolean equals(Quaternion a, Quaternion b, double tol)	{
		Quaternion tmp = new Quaternion();
		subtract(tmp, a, b);
		double ll = length(tmp);
		return ll < tol;
	}

	public static boolean equalsRotation(Quaternion a, Quaternion b, double tol)	{
		Quaternion tmp = new Quaternion();
		return (equals(a,b,tol) || equals(times(tmp,-1.0,a),b,tol));
	}

	public static Quaternion add(Quaternion dst, Quaternion a, Quaternion b)	{
		if (dst == null) dst = new Quaternion();
		if (a == null || b == null) {
			return dst;} 
		dst.re = a.re + b.re;
		dst.x = a.x + b.x;
		dst.y = a.y + b.y;
		dst.z = a.z + b.z;
		return dst;
	}
	
	public static Quaternion negate(Quaternion dst, Quaternion src)	{
		if (dst == null) dst = new Quaternion();
		if (src == null) {
			return dst;} 
		dst.re = -src.re;
		dst.x = -src.x;
		dst.y = -src.y;
		dst.z = -src.z;
		return dst;
	}
	
	public static Quaternion conjugate(Quaternion dst, Quaternion src)	{
		if (dst == null) dst = new Quaternion();
		if (src == null) {
			return dst;} 
		dst.re = src.re;
		dst.x = -src.x;
		dst.y = -src.y;
		dst.z = -src.z;
		return dst;
	}
	
	public static Quaternion subtract(Quaternion dst, Quaternion a, Quaternion b)	{
		if (dst == null) dst = new Quaternion();
		if (a == null || b == null) {
			return dst;} 
		dst.re = a.re - b.re;
		dst.x = a.x - b.x;
		dst.y = a.y - b.y;
		dst.z = a.z - b.z;
		return dst;
	}

	public static Quaternion times(Quaternion dst, double s, Quaternion src)	{
		if (dst == null) dst = new Quaternion();
		dst.re = s * src.re;
		dst.x = s * src.x;
		dst.y = s * src.y;
		dst.z = s * src.z;
		return dst;
	}

	public static Quaternion times(Quaternion dst, Quaternion a, Quaternion b)	{
		// check to see if dst = a or dst = b
		if (dst == null) dst = new Quaternion();
		if (a == null || b == null) {
			return dst;} 
		dst.re = a.re * b.re - a.x*b.x - a.y * b.y - a.z*b.z;
		dst.x =  a.re * b.x + b.re*a.x + a.y * b.z - a.z*b.y;
		dst.y =  a.re * b.y - a.x *b.z + b.re* a.y + a.z*b.x;
		dst.z =  a.re * b.z + a.x * b.y - a.y* b.x + b.re*a.z ;
		return dst;
	}
	
	public static double innerProduct(Quaternion a, Quaternion b)	{
		return (a.re*b.re + a.x*b.x + a.y*b.y + a.z*b.z);
	}
	
	public static double lengthSquared(Quaternion q)	{
		return innerProduct(q,q);
	}

	public static double length(Quaternion q)	{
		return Math.sqrt(lengthSquared(q));
	}
	
	public static Quaternion invert(Quaternion dst, Quaternion src)	{
		Quaternion tmp = new Quaternion();
		double ll = lengthSquared(src);
		if (ll == 0.0)	{
			dst = (Quaternion) INFINITE_QUATERNION.clone();
		} else {		// q^-1 = q * (q bar)/<q,q>
			ll = 1.0/ll;
			conjugate(tmp, src);
			times(dst, ll, tmp);
			//times(dst, tmp, src);
		}
		return dst;
	}
	
	public static Quaternion divide(Quaternion dst, Quaternion a, Quaternion b) {
		Quaternion tmp = new Quaternion();
		invert(tmp, b);
		return times(dst, a, tmp);
	}

	public static Quaternion star(Quaternion dst, Quaternion src)	{
		Quaternion tmp = new Quaternion();
		return conjugate(dst, invert(tmp, src));
	}
	
	public static Quaternion normalize(Quaternion dst, Quaternion src)	{
		double ll = length(src);
		if (ll == 0) dst = (Quaternion) src.clone();
		else {
			ll = 1.0/ll;
			times(dst, ll, src);
		}
		return dst;
	}
	
	// 4 different unit quaternions represent the same rotation:
	// we choose the one such that the real part and the i component are positive
	public static Quaternion normalizeRotation(Quaternion dst, Quaternion src)	{
		normalize(dst, src);
		//if (dst.x < 0)	times(dst, -1.0, dst);
		//if (dst.re < 0)	times(dst, -1.0, dst);
		return dst;
	}

	public static Quaternion makeRotationQuaternionAngle(Quaternion q, double angle, double[] axis)	{
		double [] tmp = (double[] ) axis.clone();
		double cos = Math.cos(angle/2.0);
		double sin = Math.sin(angle/2.0);
		Rn.normalize(tmp, axis);
		Rn.times(tmp, sin, tmp);
		q.setValue(cos, tmp[0], tmp[1], tmp[2]);
		return normalizeRotation(q,q);
	}
	
	public static Quaternion makeRotationQuaternionCos(Quaternion q, double cos, double[] axis)	{
		return makeRotationQuaternionAngle(q, 2*Math.acos(cos), axis);
	}
	
	public static Quaternion rotationMatrixToQuaternion(Quaternion q, double[] mat)		{
		// assert dim checks
		int n = Rn.sqrt(mat.length);
		double d = Rn.determinant(mat);
		double[] m = null;
		if (d < 0)	{
			double[] mtmp = new double[9];
			Rn.times(mtmp, -1.0, mat);
			m = mtmp;
		} else
			m = mat;
			
		q.x = Math.sqrt(1 - m[2*n+2] - m[n+1] + m[0])/2;
		if ( q.x > .001 ) {
			q.y = (m[1] + m[n]) / (4 * q.x);
			q.z = (m[2] + m[2*n]) / (4 * q.x);
			q.re = (m[2*n+1] - m[n+2]) / (4 * q.x);
		} else {
			q.y = Math.sqrt(1 - m[2*n+2] + m[n+1] - m[0])/2;
			if ( q.y  > .001) {
				q.x = (m[1] + m[n]) / (4 * q.y);
				q.z = (m[n+2] + m[2*n+1]) / (4 * q.y);
				q.re = (m[2] - m[2*n]) / (4 * q.y);
			} else {
				q.z = Math.sqrt(1 + m[2*n+2] - m[n+1] - m[0])/2;
				if ( q.z  > .001) {
					q.x = (m[2] + m[2*n]) / (4 * q.z);
					q.y = (m[n+2] + m[2*n+1]) / (4 * q.z);
					q.re = (m[n] - m[1]) / (4 * q.z);
				} else {
					q.setValue(1.0, 0.0, 0.0, 0.0);
				}
			}
		}
		// normalize the quaternion to have positive real part
		normalizeRotation(q,q);
		return q;
	}
	
	public static double[] quaternionToRotationMatrix( double[] rot, Quaternion qt)	{		
		// assert dim checks
		if (rot == null) rot = new double[16];
		double[] axis = new double[3];
		Quaternion q = new Quaternion();
		/* this is supposed to be a unit quaternion */
		//if ( Math.abs(d-1.0) > .001) 
			//ooError(OE_MISC, OE_WARNING, "Non-unit quaternion with length %G for rotation\n", sqrt(d));
		normalizeRotation(q,qt);
		
		if (1.0 - Math.abs(q.re) < 10E-16)	{
			Rn.setIdentityMatrix(rot);
			return rot;
		}
		IJK(axis, q);
		Rn.normalize(axis, axis);
		double angle = 2 *  Math.acos(q.re);
		/* fprintf(stderr,"angle is %f\n",angle); */
		return P3.makeRotationMatrix(rot, axis, angle);
	}

	/**
	 * @param object
	 * @param rot1
	 * @param rot2
	 * @param s
	 * @return
	 */
	public static Quaternion linearInterpolation(Quaternion dst, Quaternion rot1, Quaternion rot2, double s) {
		if (dst ==null) dst = new Quaternion();
		double[] r1 = rot1.asDouble();
		double[] r2 = rot2.asDouble();
		if (Rn.innerProduct(r1, r2) < 0) Rn.times(r2, -1.0, r2);
		double[] val = Pn.linearInterpolation(null, r1, r2, s, Pn.ELLIPTIC);
		dst.setValue(val[0], val[1], val[2], val[3]);
		return dst;
	}

	public double[] asDouble() { return asDouble(null); }
	
	/**
	 * @return
	 */
	public double[] asDouble(double[] val) {
		if (val == null) val = new double[4];
		val[0] = re;
		val[1] = x;
		val[2] = y;
		val[3] = z;
		return val;
	}
  
    /**
     * one of a million ways to interprete euler angles.
     * 
     * copied from: 
     * http://www.euclideanspace.com/maths/geometry/rotations/conversions/eulerToQuaternion/index.htm
     * 
     * @param heading  // y-axis
     * @param attitude // z-axis
     * @param bank     // x-axis
     * 
     * @return the Quaternion representing the rotation from euler angles 
     */
  public static Quaternion fromEulerAngles(double heading, double attitude, double bank) {
      // Assuming the angles are in radians.
      double c1 = Math.cos(heading/2);
      double s1 = Math.sin(heading/2);
      double c2 = Math.cos(attitude/2);
      double s2 = Math.sin(attitude/2);
      double c3 = Math.cos(bank/2);
      double s3 = Math.sin(bank/2);
      double c1c2 = c1*c2;
      double s1s2 = s1*s2;
      return new Quaternion(c1c2*c3 - s1s2*s3,
           c1c2*s3 + s1s2*c3,
           s1*c2*c3 + c1*s2*s3,
           c1*s2*c3 - s1*c2*s3);
   
  }


}
