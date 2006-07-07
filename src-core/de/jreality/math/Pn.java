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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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




/**
 * <p>
 * 	A set of static methods related to real n-dimensional projective space.
 *  This includes a number of methods related to the classical 
 *  homogeneous metric spaces (euclidean, hyperbolic, and elliptic)
 *  which are based on a projective model for these spaces.
 *  <p>
 *  In general, points and vectors are represented in homogeneous coordinates by arrays of length <i>n+1</i>.  
 *  By duality, hyperplanes are represented in the same fashion.
 *	<p>
 *	The methods related to metric geometries generally have a final argument
 * which identifies the geometry.  This can be one of the 3 pre-defined values
 * HYPERBOLIC, EUCLIDEAN, or ELLIPTIC.
 *  <p>
 * @see de.jreality.math.Rn for more on method conventions.
 * @author Charles Gunn
 */
public class Pn {

  
	public static final int HYPERBOLIC = -1;
	public static final int EUCLIDEAN	= 0;
	public static final int ELLIPTIC	= 1;
	public static final int PROJECTIVE	= 2;
	

	public static double[] originP3 = {0.0, 0.0, 0.0, 1.0};
	public static double[] zDirectionP3 = {0.0, 0.0, 1.0, 0.0};
	
  private Pn() {}

	/**
	 * Dehomogenize the src array into the dst array.  Both must be the same length.
	 * The src array is copied to the destination array if the last element of src is 0.0 or 1.0;
	 * otherwise each element of src is divided by the last element of src and written to the destination.
	 * @param dst
	 * @param src
	 * @return	dst
	 */
	public static double[] dehomogenize(double[] dst, double[] src)	{
		// assert dim checks
		int sl = src.length;
		if (dst == null) dst = new double[src.length];
		int dl = dst.length;
		// allow dst array same length or one shorter than source
		if (! (dl == sl || dl +1 == sl))	{
			throw new IllegalArgumentException("Invalid dimensions");
		}
		double last = src[sl-1];
		if (last == 1.0 || last == 0.0) 	{
			System.arraycopy(src,0,dst,0,dl);
			return dst;
		}
		last = 1.0/last;
		for (int i = 0; i<dl; ++i)		dst[i] = last * src[i];
		if (dl == sl) dst[dl-1] = 1.0;
		return dst;
	}
	/**
	 * Dehomogenize each element of a two dimensional array src into a same-sized destination array dst.
	 * @param dst
	 * @param src
	 * @return	dst
	 */
	public static double[][] dehomogenize(double[][] dst, double[][] src)	{
		// assert dim checks
		int sl = src.length;
		if (dst == null) dst = new double[sl][src[0].length-1];
		int dl = dst.length;
		if (dl != sl)	{
			throw new IllegalArgumentException("Invalid dimensions");
		}
		for (int i = 0; i<sl; ++i)	dehomogenize(dst[i], src[i]);
		return dst;
	}
	
	 public static double[] homogenize(double[] dst, double[] src )	{
	 	int n = src.length;
	 	double[] to;
	 	if (dst != null)	{
	 		if (dst.length != n+1)	{
	 			throw new IllegalArgumentException("dst must be length (n+1)");
	 		} 
	 		to = dst;
	 	} else 
	 		to = new double[n+1];
		System.arraycopy(src,0,to,0,n);
		to[n] = 1.0;
		return to;
	 }
	 
	 public static double[][] homogenize(double[][] dst, double[][] src)	{
	 	  if (dst == null)	dst = new double[src.length][src[0].length+1];
	 	  int n = Math.min(dst.length, src.length);
	 	  for (int i = 0; i<n; ++i)	{
	 	  	homogenize(dst[i], src[i]);
	 	  }
	 	  return dst;
	 }
	 
		public static double[][] polarize(double[][] polar, double[][] p, int signature)	{
			if (polar == null)	polar = new double[p.length][];
			if (polar.length != p.length)	 throw new IllegalArgumentException("dst has invalid length");
			int n = p.length;
			for (int i = 0; i<n; ++i)	{
				polar[i] = polarize(polar[i], p[i], signature);
			}
			return polar;
		}
		
		public static double[] polarize(double[] polar, double[] p, int signature)	{
			if (polar == null)	polar = (double[]) p.clone();
			else System.arraycopy(p,0,polar, 0, p.length);
			// last element is multiplied by the signature!
			switch (signature)	{
				case Pn.ELLIPTIC:
					// self-polar!
					break;
				case Pn.EUCLIDEAN:
					polar[polar.length - 1]  = 0d;
					break;
				case Pn.HYPERBOLIC:
					polar[polar.length-1] *= -1;
					break;
			}
			return polar;
		}
		
	/**
	 * This has to handle the exception euclidean case, that the polar of any point is
	 * the ideal plane. Otherwise it just returns {@link #polarize(double[], double[], int)}.
	 * @param object
	 * @param ds
	 * @param signature
	 * @return
	 */
	public static double[] polarizePoint(double[] dst, double[] plane, int signature) {
		if (signature == Pn.EUCLIDEAN)	{
			// there is only one polar plane: the plane at infinity
			if (dst == null) dst = new double[plane.length];
			for (int i = 0; i< (dst.length-1); ++i) dst[i] = 0.0;
			dst[dst.length-1] = -1.0;
			return dst;
		}
		return polarize(dst, plane, signature);
	}
	
	/**
	 * This just calls {@link #polarize(double[], double[], int)} since the polar plane of
	 * a point is just Q.point, where Q is the absolute quadric
	 * 
	 * @param dst
	 * @param ds
	 * @param signature
	 * @return
	 */public static double[] polarizePlane(double[] dst, double[] point, int signature) {
		return polarize(dst, point, signature);
	}
	

	
	/**
	 * Like the method {@link Rn#calculateBounds(double[][], double[][]) calculateBounds} in class Rn, but
	 * dehomogenizes the points before computing the bound. 
	 * @param bounds	double[2][3]
	 * @param vlist		double[][3]	or double[][4]
	 * @return bounds
	 */
	public static double[][] calculateBounds(double[][] bounds, double[][] vlist) {
		// TODO Auto-generated method stub
		int vl = vlist[0].length;
		int bl = bounds[0].length;
		double[] tmp = new double[vl-1];
		// assert dim check
		//if (bounds.length != 2 && bounds[0].length != n)	bounds = new double[2][n];
		if (vl - 1 > bl) return null;		// TODO error
	
		for (int i =0; i<vl-1; ++i)	{
			bounds[0][i] = Double.MAX_VALUE;
			bounds[1][i] = -Double.MAX_VALUE;
		}
		for (int i=vl-1; i<bl; ++i) {
			bounds[0][i] = bounds[1][i] = 0.0;
		}
		for (int i=0; i< vlist.length; ++i)		{
			if (vlist[i][vl-1] == 0.0 ) continue;		// skip over points at infinity
			Pn.dehomogenize(tmp, vlist[i]);
			Rn.max(bounds[1], bounds[1], tmp);
			Rn.min(bounds[0], bounds[0], tmp);
		
		}
		return bounds;
	}
	/**
	 * Returns the inner product of the two vectors for the given metric.
	 * @param dst
	 * @param src
	 * @param sig
	 * @return	the inner product
	 */
	public static double innerProduct(double dst[], double[] src, int sig)	{
		// assert dim checks
		double sum = 0;
		if (src.length != dst.length)		{
			throw new IllegalArgumentException("Incompatible lengths");
		}
		int n = dst.length;
		
		for (int i = 0; i< n-1; ++i)	sum += dst[i] * src[i];
		double ff = dst[n-1] * src[n-1];
		
		switch(sig)	{
			case HYPERBOLIC:		// signature (n-1,1)
				sum -= ff;
				break;
			case EUCLIDEAN:		// signature (n-1, 0)				
				if (!(ff == 1.0 || ff == 0.0))	sum /= ff;
				break;
			case ELLIPTIC:		// signature (n, 0)
				sum += ff;	
		}
		return sum;
	}

	public static double innerProductPoints(double[] dst, double[] src, int sig)	{
		return innerProduct(dst, src, sig);
	}
	
	public static double innerProductPlanes(double dst[], double[] src, int sig)	{
		if (sig != Pn.EUCLIDEAN) return innerProduct(dst, src, sig);
		int n = src.length;
		double sum = 0;
		for (int i = 0; i< n-1; ++i)	sum += dst[i] * src[i];
		return sum;
	}

	/**
	 * @param src
	 * @param sig
	 * @return	square of norm
	 */
	public static double normSquared(double[] src, int sig)	{
		return innerProduct(src, src, sig);
	}

	/**
	 * Calculates the norm of the vector in the given metric <i>sig</i>.  Actually returns the absolute 
	 * value of the norm, since hyperbolic points can have imaginary norm.
	 * @param src
	 * @param sig
	 * @return	the norm
	 */public static double norm(double[] src, int sig)	{
		return Math.sqrt(Math.abs(normSquared(src,sig)));
	}

	 public static double[] setToLength(double[] dst, double[] src, double length, int sig)	{
		// assert dim checks
		if (dst == null) dst = new double[src.length];
	 	if (dst.length != src.length)	
	 		throw new IllegalArgumentException("Incompatible lengths");
		if (sig == EUCLIDEAN)	dehomogenize(dst, src);
		else					System.arraycopy(src,0,dst,0,dst.length);
		
		double ll = norm(dst, sig);
		if (ll == 0.0)	return dst;
		ll = length/ll;
		Rn.times(dst, ll, dst);
		if (sig == EUCLIDEAN && dst[dst.length-1] != 0.0) 	dst[dst.length-1] = 1.0;
		return dst;
	}
	
	/**
	 * @param verts
	 * @param verts2
	 * @param d
	 * @param euclidean2
	 */
	public static double[][] setToLength(double[][] dst, double[][] src, double d, int signature) {
		// assert dim checks
		int sl = src.length;
		if (dst == null) dst = new double[sl][src[0].length-1];
		int dl = dst.length;
		if (dl != sl)	{
			throw new IllegalArgumentException("Incompatible lengths");
		}
		for (int i = 0; i<sl; ++i)	setToLength(dst[i], src[i], d, signature);
		return dst;
	}
	
	/**
	 * Normalizes the vector <i>src</i> to have unit length (either 1 or <i>i</i>), or, If <i>signature</i> 
	 * is EUCLIDEAN, then the input vector is dehomogenized.  To set a Euclidean vector to length 1, use
	 * {@link #setToLength(double[], double[], double, int)}.  This method is only
	 * valid for homogeneous coordinates. Use {@link de.jreality.math.Rn#normalize(double[], double[]) 
	 * if you are using dehomogenous coordinates (e.g., representing points in R3 using 3-vectors). \
	 * 
	 * @param dst
	 * @param src
	 * @param sig
	 * @return	dst
	 */
	 public static double[] normalize(double[] dst, double[] src, int sig)	{
	 	if (dst == null) dst = new double[src.length];
	 	if (sig == EUCLIDEAN) return dehomogenize(dst, src);
	 	return setToLength(dst, src, 1.0, sig);
	 }
	 
	 public static double[][] normalize(double[][] dst, double[][] src, int sig)	{
	 	if (dst == null)	dst = new double[src.length][src[0].length];
		if (dst.length != src.length)	{
			throw new IllegalArgumentException("Incompatible lengths");
		}
	 	int n = dst.length;
	 	for (int i = 0; i<n; ++i)	{
	 	  	normalize(dst[i], src[i], sig);
	 	}
	 	return dst;
	 }
	 
	 public static double[] normalize(double[] dst, double[] dvec, double[] src, double[] svec, int signature)	{
	 	if (dst == null)		dst = new double[src.length];
	 	if (signature == EUCLIDEAN)	{
	 		dehomogenize(dst, src);
	 		dehomogenize(dvec, svec);
	 		dvec[dvec.length-1] = 0.0;
	 		return dst;
	 	}
	 	Pn.normalize(dst, src, signature);
	 	dvec = projectToTangentSpace(dvec, dst, svec, signature);
	 	normalize(dvec, dvec, signature);
	 	return dst;
	 }
	 
	 public static double[] normalizePoint(double[] dst, double[] src, int signature){
	 	return normalize(dst, src,signature);
	 }
	/**
	 * Normalize a plane (a vector of length n) so that the direction vector (the first n-1 coordinates) has 
	 * euclidean length 1 but represents the same projective point.
	 * @param dst
	 * @param src
	 * @return
	 */
	public static double[]  normalizePlane(double[]  dst, double[]  src, int signature)	{
		if (signature != EUCLIDEAN)	{
			return normalize(dst, src, signature);
		}
		int n = src.length;
		if (dst == null) dst = new double[n];
		double[] foo = new double[n-1];
		System.arraycopy(src, 0, foo, 0, n-1);
		double ll = Rn.euclideanNorm(foo);
		if (ll == 0) return null;
		Rn.times(dst,1.0/ll, src);
		
		return dst;
	}
	
	public static double[] normalizePlane(double[] dst, double[] src)	{
		return normalizePlane(dst, src, EUCLIDEAN);
	}

	 public static double[] centroid(double[] average, double[][] points, int signature)	{
	 	if (average == null) average = new double[points[0].length];
	 	double[] tmp = new double[average.length];
	 	for (int i = 0; i<points.length; ++i)	{
	 		normalize(tmp, points[i], signature);
	 		Rn.add(average, tmp, average);
	 	}
	 	Rn.times(average, 1.0/points.length, average);
	 	normalize(average, average, signature);
	 	return average;
	 }
	 
	public static double[] midPlane(double[] midp, double[] pl1, double[] pl2, int signature)	{
		if (midp == null)	midp = new double[4];
		double[] pt1, pt2;
		pt1 = normalizePlane(null,pl1, signature);
		pt2 = normalizePlane(null,pl2, signature);
		linearInterpolation(midp, pt1, pt2, .5, signature);
		return midp;
	}

	/**
	 * Calculate the distance between the two points <i>u</i> and <i>v</i>. In hyperbolic
	 * geometry distances may be imaginary; here we only return the absolute value of the distance.
	 * @param u
	 * @param v
	 * @param sig
	 * @return	the distance
	 */
	public static double distanceBetween(double[] u, double[] v, int sig)	{
		// assert dim checks
		double d = 0;
		int n = u.length;
		switch(sig)	{
			default:
				// error: no such signature.  fall through to euclidean case
			case EUCLIDEAN:
				double ul, vl, ulvl, tmp;
				ul = u[n-1]; vl = v[n-1]; ulvl = ul*vl;
				for (int i = 0; i<n-1; ++i)		{
					tmp = ul*v[i] - vl * u[i];
					d += tmp*tmp;
				}
				d = Math.sqrt(d);
				if ( !(d==0 || d == 1.0))	d /= ulvl;
				break;
			case HYPERBOLIC:
				double uu, uv, vv;
				uu = innerProduct(u, u, sig);
				vv = innerProduct(v, v, sig);
				uv = innerProduct(u, v, sig);
				if (uu == 0 || vv == 0) 	// error: infinite distance
					return (Double.MAX_VALUE);
				double k =  (uv)/Math.sqrt(Math.abs(uu*vv));
				d = acosh(k);
				break;
			case ELLIPTIC:
				uu = innerProduct(u, u, sig);
				vv = innerProduct(v, v, sig);
				uv = innerProduct(u, v, sig);
				if (uu == 0 || vv == 0) 	// error: infinite distance
					return (Double.MAX_VALUE);
				d = Math.acos( (uv)/Math.sqrt(Math.abs(uu*vv)));
				break;
			}
		return d;
	}
	
	public static double angleBetween(double[] u, double[]v, int sig)	{
		double uu = innerProductPlanes(u, u, sig);
		double vv = innerProductPlanes(v, v, sig);
		double uv = innerProductPlanes(u, v, sig);
		if (uu == 0 || vv == 0) 	// error: infinite distance
			return (Double.MAX_VALUE);
		double f =  uv/Math.sqrt(Math.abs(uu*vv));
		if (f > 1.0) f = 1.0;
		if (f < -1.0) f = -1.0;
		double d = Math.acos(f);
		return d;
	}
	
	/**
	 * Fill a gap in the Java math library!
	 * @param arg
	 * @return	inverse sinh of arg
	 */
	public static double cosh(double x)	{
		return .5*(Math.exp(x) + Math.exp(-x));
	}

	public static double sinh(double x)	{
		return .5*(Math.exp(x) - Math.exp(-x));
	}
	
	public static double tanh(double x)	{
		return sinh(x)/cosh(x);
	}

	public static double acosh(double x)	{
		return Math.log(Math.abs(x)+Math.sqrt(x*x-1));
	}

	public static double asinh(double x)	{
		return Math.log(x+Math.sqrt(x*x+1));
	}
	
	// can this be right?
	public static double atanh(double x)	{
		return asinh(x)/acosh(x);
	}
	/**
	 * Linear interpolate respecting the given metric <i>sig</i>.  That is, find the point <i>p</i> in the linear
	 * span of <i>u</i> and <i>v</i> such that the distance(u,p):distance(u,v) = t.  For the euclidean case
	 * this is equivalent to an ordinary linear interpolation: <i>dst = (1-t)u + tv </i>.
	 * @param dst
	 * @param u
	 * @param v
	 * @param t
	 * @param sig
	 * @return	dst
	 */public static double[] linearInterpolation(double[] dst, double[] u, double[] v, double t, int sig)	{
		// assert dim check
		double dot = 0.0, angle, s0 = 0.0, s1= 0.0, s2;
		if (dst == null) dst = new double[u.length];
		double[] uu = new double[dst.length];
		double[] vv = new double[dst.length];
		int realSignature = sig;		// points in hyperbolic metric can behave like elliptic points

		if (sig != EUCLIDEAN)	{
			normalize(uu, u, sig);
			normalize(vv, v, sig);			
		} else {
			uu = u;  vv = v;
		}
		
		// find the proper factors s0 and s1 to perform a traditional linear combination
		switch(sig)	{
			case PROJECTIVE:
			case EUCLIDEAN:
				s0 = 1-t;
				s1 = t;
				break;
			case HYPERBOLIC:
				dot = innerProduct(uu,vv,sig);
				if (Math.abs(dot) <= 1.0)	realSignature = Pn.ELLIPTIC;
				break;
			case ELLIPTIC:
				dot = innerProduct(uu,vv,sig);
				if (dot > 1.0) dot = 1.0;
				if (dot < -1.0) dot = -1.0;
				break;
		}
		if (realSignature == Pn.ELLIPTIC)	{
			angle = Math.acos(dot);
			s2 = Math.sin(angle);
			if (s2 != 0.0)	{
				s0 = Math.sin((1-t) * angle)/s2;
				s1 = Math.sin(t*angle) /s2;
			} else { s0 = 1.0; s1 = 0.0; }			
		} else if (realSignature == Pn.HYPERBOLIC)	{
			angle = acosh(dot);
			s2 =  sinh(angle);
			if (s2 != 0.0)	{
				s0 = sinh((1-t) * angle)/s2;
				s1 = sinh(t*angle) /s2;
			}	else { s0 = 1.0; s1 = 0.0; }				
			
		}
		return Rn.linearCombination(dst, s0, uu, s1, vv);
	}
	
	/**
	 * Drag a tangent vector <i>sdir</i> based at point <i>src</i> with initial direction given by <i>sdir</i>, a distance
	 * of <i>length</i> in the given metric.  The resulting point and tangent vector is returned in <i>ddir</i> and <i>dst</i>,
	 * respectively.  Warning: the source point and tangent vector are assumed to be normalized.  
	 * Use {@link #normalize(double[], double[], double[], double[], int)} 
	 * to perform this normalization before calling this method.
	 * @param dst
	 * @param ddir
	 * @param src
	 * @param sdir
	 * @param length
	 * @param signature
	 * @return
	 */
	public static double[] dragTangentVector(double[] dst, double[] ddir, double[] src, double[] sdir, double  length, int signature)
	{
	    double  c, s;
	    double[] tdir;

	    //ASSERT( (dst && ddir && src && sdir), OE_NULLPTR, OE_DEFAULT, "null pointer argument",
	        //return dst);
	    int n = src.length;
	    if (n != sdir.length)	{
			throw new IllegalArgumentException("Invalid dimensions");
	    }
	    
	    if (ddir != null && ddir.length != n)	{
			throw new IllegalArgumentException("Invalid dimensions");
	    }
	    
	    if (dst == null)	{
	    		dst = new double[n];
	    }
	    switch (signature)     {
	      case  Pn.EUCLIDEAN:
	      	tdir = setToLength(null, sdir, length, signature); 
	      	tdir[n-1] = 0.0;
	        Rn.add(dst, src, tdir);
	        if (ddir != null) Rn.copy(ddir, sdir);
	        break;

	      case Pn.HYPERBOLIC:
			c = cosh( length);
			s = sinh( length);
			Rn.linearCombination(dst, c, src, s, sdir);
			if (ddir != null) Rn.linearCombination(ddir,s, src, c, sdir);
			break;
	
	      case Pn.ELLIPTIC:
			c = Math.cos( length);
			s = Math.sin( length);
			Rn.linearCombination(dst, c, src, s, sdir);
			if (ddir != null) Rn.linearCombination(ddir,-s, src, c, sdir);
			break;
	    }
	    return dst;
	} 
	
	/**
	 * Drag the tangent vector sourceTangent based at sourcePoint to a tangent vector based at dstPoint.
	 * @param ds
	 * @param ds2
	 * @param ds3
	 * @param ds4
	 * @param signature
	 */
	public static double[] dragTangentVector(double[] dstTangent, double[] sourcePoint, double[] sourceTangent, double[] dstPoint, int signature) {
		double d = distanceBetween(sourcePoint, dstPoint, signature);
		if (dstTangent == null) dstTangent = new double[sourcePoint.length];
		dragTangentVector(null, dstTangent, sourcePoint, sourceTangent, d, signature);
		return dstTangent;
		}

	/**
	 * Calculate the point lying a distance length from p0 in the direction p1. 
	 * p1 is first converted into a tangent vector (by projection) and then
	 * {@link #dragTangentVector(double[], double[], double[], double[], double, int) is then called.
	 * 
	 * @param result
	 * @param p0
	 * @param p1
	 * @param length
	 * @param signature
	 * @return
	 */
	public static double[] dragTowards(double[] result, double[] p0, double[] p1, double  length, int signature)
	{
		double[] np0 = new double[p0.length]; //normalize(null, p0, signature);
		double[] np1 = new double[p1.length];
		//System.out.println("Dragging 1"+Rn.toString(p0)+"to "+Rn.toString(p1));
		normalize(np0, p0, signature);
		projectToTangentSpace(np1, np0, p1, signature);
		normalize(np1, np1, signature);
		//System.out.println("Dragging 2"+Rn.toString(np0)+"to "+Rn.toString(np1));
		return dragTangentVector(result, null, np0, np1, length, signature);
	}
	
	/**
	 * @param result
	 * @param point
	 * @param tangentToBe
	 * @param signature
	 * @return
	 */
	public static double[] projectToTangentSpace(double[] result, double[] point, double[] tangentToBe, int signature)	{
		 // TODO rephrase this method in terms of the following two
		int n = point.length;
		if (result == null) result = new double[n];
		if (signature == EUCLIDEAN)	{
			polarizePlane(result, tangentToBe, signature);
			return result;
		}
		double factor = innerProduct(point, tangentToBe, signature);
		double pp = innerProduct(point, point, signature);
		if (pp != 0.0)	factor = factor / pp;
		Rn.subtract(result, tangentToBe, Rn.times(null, factor, point ));
		return result;
	}
	public static double[] projectOnto(double[] result, double[] master, double[] victim, int signature)	{
		if (master.length != victim.length)	{
			throw new IllegalArgumentException("Arguments must be same dimension");
		}
		int n = master.length;
		if (result == null) result = new double[n];
//		if (signature == EUCLIDEAN)	{
//			// set the last coordinate to 0: becomes a direction
//			polarizePlane(result, victim, signature);
//			return result;
//		}
		double factor = innerProductPlanes(master, victim, signature);
		double pp = innerProductPlanes(master, master, signature);
		if (pp != 0.0)	factor = factor / pp;
		Rn.times(result, factor, master );
		return result;
	}

	public static double[] projectOntoComplement(double[] result, double[] master, double[] victim, int signature)	{
		return Rn.subtract(result, victim, projectOnto(null, master, victim, signature));
	}

	public double[] makeHarmonicHarmology(double[] dst, double[] center, double[] axis, int signature){ 
		if (dst == null) dst = new double[center.length*center.length];
	     double f = 1.0/Rn.innerProduct(center, axis); 
	     for (int i = 0; i<3; ++i)  {    
	         for (int j = 0; j<3; ++j) {
	              dst[3*i+j] = (i==j? 1 : 0) - 2 * f * center[i] * axis[j];
	         }
	     }
	     return dst;
	 
	}
}		