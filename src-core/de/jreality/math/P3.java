/*
 * Created on May 27, 2004
 *
 */
package de.jreality.math;

import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingConstants;
import de.jreality.util.*;

/**
 * Static methods for geometry of  real projective space.
 * 
 * @see de.jreality.math.Rn  for method conventions and representation of matrices.
 * @see de.jreality.math.Pn  for other methods applicable in n-dimensional projective space.
 * @author Charles Gunn
 *
 */
public class P3 extends Pn {

	private final static double[] hzaxis = {0,0,1,1};

	final static double[] xaxis = {1,0,0};
	final static double[] yaxis = {0,1,0};
	final static double[] zaxis = {0,0,-1};

	public static double[] p3involution = makeStretchMatrix(null, new double[]{-1d,-1d,-1d,1d});

	 
   private P3()	{
		super();
	}

	/**
	 * 
	 * @param m
	 * @param transV
	 * @param rotQ
	 * @param stretchRotQ
	 * @param stretchV
	 * @param isFlipped
	 * @param sig
	 * @return
	 */
	public static double[] composeMatrixFromFactors(double[] m, double[] transV, Quaternion rotQ, 
		Quaternion stretchRotQ, double[] stretchV, boolean isFlipped, int sig)
	{
		// assert dim checks
		double[] transT 	= new double[16],
				rotT 	= new double[16],
				stretchRotT = new double[16],
				stretchT = new double[16],
				tmp	= new double[3];
	
		if (transV == null || rotQ == null || stretchV == null)	{
			throw new IllegalArgumentException("Null argument");
		}
			
	
		P3.makeTranslationMatrix(transT, transV, sig);
		Quaternion.quaternionToRotationMatrix(rotT, rotQ);
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(rotT));
	
		/* for now we ignore the stretch rotation */
		Quaternion.quaternionToRotationMatrix(stretchRotT, stretchRotQ);
	
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(stretchT));
		if (isFlipped == true)	{
			Rn.times(tmp, -1.0, stretchV);
		} else {
			System.arraycopy(stretchV, 0, tmp, 0, 3);
		}
		Rn.setDiagonalMatrix(stretchT, tmp);
		//LoggingSystem.getLogger().log(Level.FINER,"Stretch"+isFlipped+Rn.matrixToString(stretchT));
	
		/* what ** should **  happen with the "stretch rotataion */
		//Rn.conjugate(stretchT, stretchT, stretchRotT);
		Rn.times( m, rotT, stretchT);
		Rn.times(m, transT, m);
	
		return m;
	}

	// TODO really diagonalize the quadratic form Q instead of assuming it's diagonal
	/**
	 * see Graphics Gems IV, p. 207 for details 
	 * "polar matrix decomposition" yields a factorization of the form
	 * A = TRFUSU'
	 * where	T	translation
	 *		F	flip  (x[i] -> -x[i], i=0,1,2)
	 *		R	rotation
	 *		U	"stretch rotation"
	 *		S	scale
	 *		U'	U transpose
	 *
	 * Additional argument for composeMatrixFromFactors
	 *		centerV	center of rotation
	 *
	 * 
	 *	Currently U is assumed to be the identity, and signature must be EUCLIDEAN.
	 *
	 * @param m
	 * @param transV
	 * @param rotQ
	 * @param stretchRotQ
	 * @param stretchV
	 * @param isFlipped
	 * @param sig
	 * @return
	 */
	public static double[] factorMatrix(double[] m, double[] transV, Quaternion rotQ, Quaternion stretchRotQ, double[] stretchV, boolean isFlipped[], int sig)
	{
		double[] itransT = new double[16], 
			transT = new double[16], 
			tmp = new double[16],
			M3 = new double[9], 
			Q3 = new double[9], 
			S3 = new double[9];
		double det;
	
		//ASSERT( (vT && qR && qSR && vS && isFlipped && A), OE_NULLPTR, OE_DEFAULT, " ", return A;)
	
		/* see if it's a reflection */
		det = Rn.determinant(m);
		//System.out.println("Det ="+det);
		isFlipped[0] = (det < 0); 
	
		/* first extract the translation part */
		Rn.matrixTimesVector(transV, m, Pn.originP3);
		if (sig == Pn.EUCLIDEAN && transV[3] == 0.0)	{
			throw new IllegalArgumentException("bad translation vector");
		}
		P3.makeTranslationMatrix(transT, transV, sig);
		Rn.inverse(itransT, transT);
		// undo the translation first
		Rn.times(tmp, itransT, m);
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(tmp));
	
		/* next polar decompose M */
		Rn.extractSubmatrix(M3, tmp, 0, 2, 0, 2);
		if (isFlipped[0] == true) Rn.times(M3, -1.0, M3);
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(M3));
		Rn.polarDecompose(Q3, S3, M3);
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(Q3));
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(S3));
	
		/* we pretend that we have a diagonal scale matrix */
		stretchV[0] = S3[0];	stretchV[1] = S3[4];	stretchV[2] = S3[8];
	
		Quaternion.rotationMatrixToQuaternion(rotQ, Q3);
		/* and that the other rotation is the identity */
		stretchRotQ.setValue(1.0, 0.0,  0.0, 0.0);
	
		return m;
	}

	/**
	 * Calculate the intersection point of the line determined by <i>p1</i> and <i>p2</i> with <i> plane</i>.
	 * @param point
	 * @param p1
	 * @param p2
	 * @param plane
	 * @return The intersection point
	 */public static double[] lineIntersectPlane(double[] dst, double[] p1, double[] p2, double[] plane)	{
		if (plane.length != 4)	{
			throw new IllegalArgumentException("lineIntersectPlane: plane has invalid dimension");
		}
		double[] point1 , point2;
		if (dst == null || dst.length != 4)	dst = new double[4];
		if (p1.length == 3) 	{
			point1 = new double[4];
			System.arraycopy(p1, 0, point1, 0, 3);
			point1[3] = 1.0;
		} else point1 = p1;
		if (p2.length == 3) 	{
			point2 = new double[4];
			System.arraycopy(p2, 0, point2, 0, 3);
			point2[3] = 1.0;
		} else point2 = p2;
		double k1 = Rn.innerProduct(point1, plane);
		double k2 = Rn.innerProduct(point2, plane);
		// both points lie in the plane!
		if (k1 == 0.0 && k2 == 0.0)	{
//			LoggingSystem.getLogger(P3.class).log(Level.WARNING,"lineIntersectPlane: Line lies in plane");
			System.arraycopy(p1, 0, dst, 0, Math.min(p1.length,dst.length));
		} else {
			double[] tmp = new double[4];
			Rn.linearCombination(tmp, k2, point1, -k1, point2);
			Pn.dehomogenize(dst, tmp);
		}
//		LoggingSystem.getLogger(P3.class).log(Level.FINER,"k1 is "+k1+" and k2 is "+k2);
//		LoggingSystem.getLogger(P3.class).log(Level.FINER,"p1 is "+Rn.toString(p1)+" and p2 is "+Rn.toString(p2));
//		LoggingSystem.getLogger(P3.class).log(Level.FINER,"result is "+Rn.toString(dst));
		return dst;
	}

	public static double[] lineJoinPoint (double[] plane, double[] p1, double[] p2, double[] point)	{
		return lineIntersectPlane(plane, p1, p2, point);
	}

	/**
	 * Generate a glide reflection in the plane through the origin whose normal vector is given by vec
	 * Under construction.
	 * @param m
	 * @param vec
	 * @return
	 */
	protected static double[] makeGlideReflectionMatrix(double[] m, double[] vector, double d)	{
		// TODO finish writing this
		
		double[] v = new double[4];
		double[] mat = new double[16];
		if (m == null)	m = new double[16];
		System.arraycopy(vector, 0, v, 0, vector.length);
		v[3] = 0.0;
		P3.makeReflectionMatrix(mat,v, Pn.EUCLIDEAN);
		Rn.setEuclideanNorm(v,d, v);
		v[3] = 1.0;
		double[] tm = P3.makeTranslationMatrix(null,v,Pn.EUCLIDEAN);
		Rn.times(m,tm,mat);
		return m;
	}

	/**
	 * Creates an isometry that carries the <i>from</i> vector to the origin; and takes the 
	 * normalized <i>to</i> vector to the (homogeneous) vector <i>(0,0,-1,0)</i>. The orthogonal complement of these two
	 * vectors is rotated by <i>roll</i> degrees.  This transformation represents the <i>worldToCamera<i>
	 * transformation for a camera whose position in world coordinates is the point <i>from</i> and which is pointed to look in
	 * the direction of <i>to</i>.
	 * @param m
	 * @param from
	 * @param to
	 * @param roll
	 * @param sig
	 * @return
	 */public static double[] makeLookatMatrix(double[] m, double[] from, double[] to, double roll, int sig)	{
		// assert dim checks
		double[] newto = new double[4];
		double[] tm1 = new double[16];
		double[] tm2 = new double[16];
		double[] tm3 = new double[16];
		if (m == null) m = new double[16];

		P3.makeTranslationMatrix(tm1, from,  sig);
		//LoggingSystem.getLogger().log(Level.FINER,Rn.matrixToString(tm1));
		Rn.inverse(tm1, tm1);		// tm1 brings from to (0,0,0,1)
		//ystem.out.println(Rn.matrixToString(tm1));
		Rn.matrixTimesVector(newto, tm1, to);
//		LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.toString(newto));
//		P3.makeNewZMatrix(tm3, newto);
//		System.out.println("newZMatrix: "+Rn.toString(Rn.matrixTimesVector(null, tm3, newto)));
		P3.makeRotationMatrix(tm2, newto, P3.zaxis);
//		System.out.println("rotationMatrix: "+Rn.toString(Rn.matrixTimesVector(null, tm3, newto)));
		// the matrix m takes to -> newto -> zaxis
		Rn.times(m, tm2, tm1);
		if (roll != 0)	{
			P3.makeRotationMatrix(tm1, P3.zaxis, roll);
			Rn.times(m, tm1, m);
		}
//		System.out.println("lookat: "+Rn.toString(Rn.matrixTimesVector(null, m, from)));
//		System.out.println("rotationMatrix: "+Rn.toString(Rn.matrixTimesVector(null, Rn.times(null, tm2, tm1), from)));
//		System.out.println("lookat: "+Rn.toString(Rn.matrixTimesVector(null, m, to)));
//		System.out.println("rotationMatrix: "+Rn.toString(Rn.matrixTimesVector(null, Rn.times(null, tm2, tm1), to)));
		return m;
	}


	/**
	 * 
	 * @param m
	 * @param viewport
	 * @param n
	 * @param f
	 * @return
	 */
	public static double[] makeOrthographicProjectionMatrix(double[] m, Rectangle2D viewport, double near, double far)	{
		// assert dim checks
		double l = viewport.getMinX();
		double r = viewport.getMaxX();
		double b = viewport.getMinY();
		double t = viewport.getMaxY();
		if (m == null) m = new double[16];
		Rn.setIdentityMatrix(m);
		m[0] = 2/(r-l);
		m[5] = 2/(t-b);
		m[10] = -2/(far-near);
		m[3] = -(r+l)/(r-1);
		m[7] = -(t+b)/(t-b);
		m[11] = -(far+near)/(far-near);
		return m;
	}

	/**
	 * Generate a 4x4 perspective projection matrix based on the parameters.
	 * @param dst			matrix to put the result
	 * @param viewport	The viewport of the camera (normalized to lie in z = -1 plane)
	 * @param n			near clipping plane
	 * @param f			far clipping plane
	 * @return
	 */public static double[] makePerspectiveProjectionMatrix(double[] dst, Rectangle2D viewport, double near, double far)	{
		// assert dim checks
	 	if (dst == null) dst = new double[16];
		double an = Math.abs(near);
		double l = viewport.getMinX() * an;
		double r = viewport.getMaxX() * an;
		double b = viewport.getMinY() * an;
		double t = viewport.getMaxY() * an;
		Rn.setIdentityMatrix(dst);
		dst[0] = 2*near/(r-l);
		dst[5] = 2*near/(t-b);
		dst[10] = (far+near)/(near-far);
		dst[15] = 0.0;
		dst[2] = (r+l)/(r-l);
		dst[6] = (t+b)/(t-b);
		dst[11] = 2*near*far/(near-far);
		dst[14] = -1.0;
		return dst;
	}


	/**
	 * Construct a projective reflection that fixes the element <i>plane</i> considered
	 * as a pole/polar point/plane pair.  That is, the fixed elements of the transformation are
	 * the point <i>vec</i> and the polar plane <i>Q.vec</i> where Q is the diagonal
	 * matrix representing the absolute quadric of the given signature.  Such a transformation
	 * is also known as a harmonic involution.
	 * <b>Warning</b> Under construction!
	 * @param m
	 * @param vec
	 * @param sig
	 * @return
	 */
	public static double[] makeReflectionMatrix(double[] m, double[] plane, int sig)	{
		// TODO assert checks m.length == 16 and vec.length = 3 or 4
		if (plane.length != 4)	{
			throw new IllegalArgumentException("makeReflectionMatrix: Invalid argument");
		}
		double[] reflectionMatrix = null;
		double[] fixedPlane = ((double[]) plane.clone());
		double[] polarPoint = null; // = (double[]) fixedPlane.clone();
		if (m == null) 	reflectionMatrix = new double[16];
		else 			reflectionMatrix = m;
		Rn.setIdentityMatrix(reflectionMatrix);
		polarPoint = Pn.polarizePlane(null, fixedPlane, sig);
		Pn.setToLength(polarPoint, polarPoint,1.0, sig);
	
		switch (sig)	{
			case Pn.ELLIPTIC:
			case Pn.HYPERBOLIC:
				Pn.normalize(fixedPlane, fixedPlane,  sig);
				break;
			case Pn.EUCLIDEAN:		// this is not optimal, I think; but it works
				Pn.normalizePlane(fixedPlane, fixedPlane);
				break;
		}
		
		for (int i = 0; i<4; ++i)	{
			for (int j = 0; j<4; ++j)	{
				reflectionMatrix[i*4 + j] = reflectionMatrix[i*4+j] - 2 * fixedPlane[j]*polarPoint[i];
			}
		}
		return reflectionMatrix;
	}

	/**
	 * Generate a rotation matrix fixing the origin (0,0,0,1) around the given axis with the given angle.
	 * The matrix is a 4x4 matrix, which differs from the identity matrix only in the upper 3x3 corner.
	 * 
	 * @param m		the target matrix
	 * @param axis	double[3]
	 * @param angle
	 * @return
	 */
	public static double[] makeRotationMatrix(double[] m, double[] axis, double angle)	{
		double[] u = new double[3];
		if (m == null) 	m = new double[16];
		if (axis.length < 3)	{
			throw new IllegalArgumentException("Axis is wrong size");
		}
		System.arraycopy(axis,0,u,0,3);
		Rn.normalize(u,u);
		double c = Math.cos(angle);
		double s = Math.sin(angle);
		double v = 1.0 - c;
		
		Rn.setIdentityMatrix(m);
		m[0] = u[0] * u[0] * v + c;
		m[4] = u[0] * u[1] * v + u[2] * s;
		m[8] = u[0] * u[2] * v - u[1] * s;
	
		m[1] = u[1] * u[0] * v - u[2] * s;
		m[5] = u[1] * u[1] * v + c;
		m[9] = u[1] * u[2] * v + u[0] * s;
	
		m[2] = u[2] * u[0] * v + u[1] * s;
		m[6] = u[2] * u[1] * v - u[0] * s;
		m[10] = u[2] * u[2] * v + c;
		
		return m;
	}

	/**
	 * @param object
	 * @param earthPhi
	 * @return
	 */
	public static double[] makeRotationMatrixX(double[] mat, double angle) {
		double[] axis = {1.0, 0.0, 0.0};
		return makeRotationMatrix(mat, axis, angle);
	}
	public static double[] makeRotationMatrixY(double[] mat, double angle) {
		double[] axis = {0.0, 1.0, 0.0};
		return makeRotationMatrix(mat, axis, angle);
	}
	public static double[] makeRotationMatrixZ(double[] mat, double angle) {
		double[] axis = {0.0, 0.0, 1.0};
		return makeRotationMatrix(mat, axis, angle);
	}
	/**
	 * Generate a rotation matrix which fixes the origin (0,0,0,1) and carries the vector <i>from</i> to the vector <i>to</i>.
	 * The output matrix is 4x4.  <i>from</i> and <i>to</i> are 3-vectors.
	 * 
	 * @param m		double[16]
	 * @param from	double[3]
	 * @param to	double[3]
	 * @return
	 */
	public static double[] makeRotationMatrix(double[] m, double[] from, double[] to)	{
		// assert dim checks; only valid for P3
		if (from.length < 3 || to.length < 3)	{
			throw new IllegalArgumentException("Input vectors too short");
		}
		double[][] vecs = new double[3][3];
		System.arraycopy(from,0,vecs[0],0,3);
		System.arraycopy(to,0,vecs[1],0,3);
		Rn.normalize(vecs[0], vecs[0]);
		Rn.normalize(vecs[1], vecs[1]);
		double angle = Math.acos(Rn.innerProduct(vecs[0], vecs[1]));
		Rn.crossProduct(vecs[2], vecs[0], vecs[1]);
		Rn.normalize(vecs[2], vecs[2]);
		return P3.makeRotationMatrix(m, vecs[2], angle);
	}

	 /**
	  * Calculate a rotation matrix in the given metric which rotates a given <i>angle</i> about the axis
	  * determined by <i>p1</i> and <i>p2</i>.
	  * @param m
	  * @param p1
	  * @param p2
	  * @param angle
	  * @param sig
	  * @return
	  */public static double[] makeRotationMatrix(double[] m, double[] p1, double[] p2, double angle, int sig)	{
		// assert dim checks; only valid for P3
		if (p1.length < 3 || p2.length < 3)	{
			throw new IllegalArgumentException("Points too short");
		}
	 	if (m == null) m = new double[16];
		double[] tmat = P3.makeTranslationMatrix(null, p1, sig);
		double[] invtmat = Rn.inverse(null, tmat);
		double[] ip2 = new double[4];
		Rn.matrixTimesVector(ip2, invtmat, p2);
		double[] foo = P3.makeRotationMatrix(null, ip2, angle);
		Rn.conjugateByMatrix(m,foo,tmat);
		return m;
	}

		/**
		 * Construct a diagonal matrix with the given entries.  They should be all positive.
		 * @param dst
		 * @param scales
		 * @return	dst
		 */
		// isometries start here	
		public static double[] makeStretchMatrix(double[] dst, double[] stretchV)	{
			// assert dim checks
			if (dst == null) dst = new double[16];
			int n = Rn.sqrt(dst.length);
			int ll = Math.min(n, stretchV.length);
			Rn.setIdentityMatrix(dst);
			for (int i = 0; i<ll; ++i)	{
				dst[i*n+i] = stretchV[i];
			}
			return dst;
		}
		
		public static double[] makeStretchMatrix(double[] dst, double stretch)	{
			// assert dim checks
			if (dst == null) dst = new double[16];
			int n = Rn.sqrt(dst.length);
			double[] stretchV = new double[n];
			Rn.setToValue(stretchV, stretch, stretch, stretch, 1.0);
			return makeStretchMatrix(dst, stretchV);
		}

		private static double[] makeStretchMatrix(double[] dst, double xscale, double yscale, double zscale) {
			// TODO Auto-generated method stub
			if (dst == null) dst = new double[16];
			Rn.setIdentityMatrix(dst);
			dst[0] = xscale;
			dst[5] = yscale;
			dst[10] = zscale;
			return dst;
		}

	/**
	 * Calculate a translation matrix in the given metric which carries the point <i>from</i> to the point <i>to</i>.
	 * @param dst
	 * @param from
	 * @param to
	 * @param sig
	 * @return
	 */
	  public static double[] makeTranslationMatrix(double[] dst, double[] from, double[] to, int sig)	{
		// assert dim checks
		if (dst == null) 	dst = new double[16];
		double[] TP = P3.makeTranslationMatrix(null, from, sig);
		double[] iTP = Rn.inverse(null, TP);
		double[] toPrime = Rn.matrixTimesVector(null, iTP, to );
		P3.makeTranslationMatrix(dst, toPrime, sig);
		Rn.conjugateByMatrix(dst, dst, TP);
		return dst;
	}

	/**
	 * Calculate a translation in the given geometry which carries the origin of P3 (0,0,0,1) to the input <i>point</i>.
	 * @param mat
	 * @param tvec
	 * @param sig
	 * @return
	 */public static double[] makeTranslationMatrixOld(double[] mat, double[] p, int sig)	{
		// assert dim checks
		double[] tmp = new double[4];		
		double[] foo = new double[3];
		double[] m;
		if (mat == null)		m = new double[16];
		else					m = mat;
		double[] rot = new double[16];
		double[] mtmp = new double[16];
		double[] point = null;
		if (p.length == 3)	{
			point = new double[4];
			System.arraycopy(p,0,point,0,3);
			point[3] = 1.0;
		} else point = p;
		
		switch(sig)		{
			case Pn.EUCLIDEAN:
				Rn.setIdentityMatrix(m);
				if (point.length == 4){
					Pn.dehomogenize(point, point);
					if (point[3] == 0.0) point[3] = 1.0;
				}
				for (int i = 0; i < 3; ++i)	{
					m[i*4 + 3] = point[i];
				}
				break;
				
			case Pn.HYPERBOLIC:
				if (Pn.innerProduct(point, point, Pn.HYPERBOLIC) > 0.0)	{
					double k = (point[3] * point[3] - .0001)/Rn.innerProduct(point,point,3);
					k = Math.sqrt(k);
					for (int i = 0; i<3; ++i) point[i] *= k;
				}
			case Pn.ELLIPTIC:
				Rn.setIdentityMatrix(mtmp);
				Pn.normalize(tmp, point, sig); 
				System.arraycopy(tmp, 0, foo, 0, 3);
				double d = Rn.innerProduct(foo, foo);
				mtmp[11] = Math.sqrt(d);
				if (sig == Pn.ELLIPTIC) 	mtmp[14] = -mtmp[11];
				else					mtmp[14] = mtmp[11];
				mtmp[10] = mtmp[15] = tmp[3];
				P3.makeRotationMatrix(rot, P3.hzaxis, tmp);
				Rn.conjugateByMatrix(m, mtmp, rot);
				break;
	
			default:		// error!
				break;
		}
		return m;
	}

	private static boolean debug = false;
	public static double[] makeTranslationMatrix(double[] mat, double[] t, int sig)	{
		if (mat == null) mat = new double[16];
		double[] to = null;
		if (t.length == 3)	to = Pn.homogenize(null, t);
		else if (t.length == 4) to = (double[]) t.clone();
		if (to == null || (sig == EUCLIDEAN && to[3] == 0.0))	{
			throw new IllegalArgumentException("Infinite euclidean translation vector");
		}
		Pn.normalize(to, to, sig);
		if (to[3] < 0) Rn.times(to, -1.0, to);
		double f = 1.0/(1+to[3]);
		for (int i = 0; i<3; ++i)	{
			for (int j = 0; j<3; ++j)	{
				mat[i*4+j] = ((i == j) ? 1.0 : 0.0 ) - sig * f  * to[i]*to[j];
			}
		}
		for (int i = 0; i<4; ++i)	mat[4*i+3] = to[i];
		for (int i = 0; i<3; ++i) mat[12+i] = -sig*mat[4*i+3];
		if (debug)	{
			double[] oldm = makeTranslationMatrixOld(null, t, sig);
			if (! Rn.equals(mat, oldm, 10E-8)) {
				Logger log = LoggingSystem.getLogger(P3.class);
				log.log(Level.WARNING,"Incompatible results:");
				log.log(Level.WARNING,"Signature is "+sig);
				log.log(Level.WARNING,"To vector is "+Rn.toString(to));
				log.log(Level.WARNING,"New: \n"+Rn.matrixToString(mat));
				log.log(Level.WARNING,"Old: \n"+Rn.matrixToString(oldm));
				
			}
			
		}
		return mat;
	}

	/**
	 * Extract a matrix from the <i>src</i> input matrix, such that it fixes the input position <i>point</i>.
	 * @param dst
	 * @param src
	 * @param point
	 * @param signature
	 * @return
	 */public static double[] extractOrientationMatrix(double[] dst, double[] src, double[] point, int signature)	{
		if (dst == null) dst = new double[16];

		double[] image = Rn.matrixTimesVector(null, src, point);
		double[] translate = P3.makeTranslationMatrix(null, image, signature);
		Rn.times(dst, Rn.inverse(null, translate), src );
//		System.out.println("The input matrix is "+Rn.matrixToString(src));
//		System.out.println("The orientation matrix is "+Rn.matrixToString(dst));
		return dst;
	}
	
	private static double[] perpendicularBisector(double[] dst, double[] p1, double[]p2)	{
		// TODO assert dim checks
		if (dst == null) dst = new double[4];
		double[] midpoint = new double[4];
		Rn.add(midpoint,p1,p2);
		Rn.times(midpoint, .5, midpoint);
		Pn.dehomogenize(midpoint, midpoint);
		Rn.subtract(dst, p2, p1);
		dst[3] = -(dst[0]*midpoint[0] + dst[1] * midpoint[1] + dst[2]*midpoint[2]);
		return dst;
	}

	public static double[] perpendicularBisector(double[] dst, double[] p1, double[]p2, int signature)	{
		// TODO assert dim checks
		if (p1.length != 4 || p2.length != 4)	{
			throw new IllegalArgumentException("Input points must be homogeneous vectors");
		}
		if (signature == Pn.EUCLIDEAN) return P3.perpendicularBisector(dst, p1, p2);
		if (dst == null) dst = new double[4];
		double[] midpoint = new double[4];
		Pn.linearInterpolation(midpoint,p1,p2, .5, signature);
		double[] polarM = Pn.polarize(null, midpoint, signature);
		double[] pb = P3.lineIntersectPlane(null, p1, p2, polarM);
		Pn.polarize(dst, pb, signature);
		if (Rn.innerProduct(dst,p1) > 0)	Rn.times(dst, -1.0, dst);
		return dst;
	}

	/**
	 * Construct the homogeneous plane coordinates for the plane containing the three points <i>(p1, p2, p3)</i>.
	 * The method does not check for degenerate conditions.
	 * @param plane	double[4]
	 * @param p1	double[3] or double[4]
	 * @param p2	double[3] or double[4]
	 * @param p3	double[3] or double[4]
	 * @return		plane
	 */public static double[] planeFromPoints(double[] planeIn, double[] p1, double[]p2, double[]p3)	{
		if (p1.length < 3 || p2.length <3 || p3.length < 3)	{
			throw new IllegalArgumentException("Input points must be homogeneous vectors");
		}
		double[] plane;
		if (planeIn == null) 	plane = new double[4];
		else					plane = planeIn;
//		double[] mat = new double[16];
//		mat[3] = mat[7] = mat[11] = 1.0;
//		System.arraycopy(p1, 0, mat, 0, p1.length);
//		System.arraycopy(p2, 0, mat, 4, p2.length);
//		System.arraycopy(p3, 0, mat, 8, p3.length);
//		for (int i =0; i<4; ++i)	{
//			int scale = (i%2 == 0) ? 1 : -1;
//			plane[i] = scale * Rn.determinant(Rn.submatrix(mat, 3, i));
//		} 
		//dehomogenize(plane, plane);
		if (p1.length == 3 || p2.length == 3 || p3.length == 3)	{
			plane[0] = p1[1]*(p2[2]-p3[2]) - p1[2]*(p2[1]-p3[1]) + (p2[1]*p3[2]-p2[2]*p3[1]);
			plane[1] = p1[0]*(p2[2]-p3[2]) - p1[2]*(p2[0]-p3[0]) + (p2[0]*p3[2]-p2[2]*p3[0]);
			plane[2] = p1[0]*(p2[1]-p3[1]) - p1[1]*(p2[0]-p3[0]) + (p2[0]*p3[1]-p2[1]*p3[0]);
			plane[3] = p1[0]*(p2[1]*p3[2]-p2[2]*p3[1]) - p1[1]*(p2[0]*p3[2]-p2[2]*p3[0]) + p1[2]*(p2[0]*p3[1]-p2[1]*p3[0]);			
//			throw new IllegalStateException("Can't work with 3-vectors");
		} else {
			plane[0] = p1[1]*(p2[2]*p3[3]-p2[3]*p3[2]) - p1[2]*(p2[1]*p3[3]-p2[3]*p3[1]) + p1[3]*(p2[1]*p3[2]-p2[2]*p3[1]);
			plane[1] = p1[0]*(p2[2]*p3[3]-p2[3]*p3[2]) - p1[2]*(p2[0]*p3[3]-p2[3]*p3[0]) + p1[3]*(p2[0]*p3[2]-p2[2]*p3[0]);
			plane[2] = p1[0]*(p2[1]*p3[3]-p2[3]*p3[1]) - p1[1]*(p2[0]*p3[3]-p2[3]*p3[0]) + p1[3]*(p2[0]*p3[1]-p2[1]*p3[0]);
			plane[3] = p1[0]*(p2[1]*p3[2]-p2[2]*p3[1]) - p1[1]*(p2[0]*p3[2]-p2[2]*p3[0]) + p1[2]*(p2[0]*p3[1]-p2[1]*p3[0]);			
		}
		plane[0] *= -1;
		plane[2] *= -1;
		return plane;
	}


	public static double[] pointFromPlanes(double[] point, double[] p1, double[]p2, double[]p3)	{
		return planeFromPoints(point, p1, p2, p3);
	}
	
	public static double[] pluckerCoordinates(double[] dst, double[] p0, double[] p1)	{
		if (p0.length != 4 || p1.length != 4) {
			throw new IllegalArgumentException("Input points must be homogeneous vectors");
		}
		double[] coords;
		if (dst == null)	coords = new double[6];
		else 				coords = dst;
		coords[0] = p0[0]*p1[1] - p0[1]*p1[0];
		coords[1] = p0[0]*p1[2] - p0[2]*p1[0];
		coords[2] = p0[0]*p1[3] - p0[3]*p1[0];
		coords[3] = p0[1]*p1[2] - p0[2]*p1[1];
		coords[4] = p0[3]*p1[1] - p0[1]*p1[3];
		coords[5] = p0[2]*p1[3] - p0[3]*p1[2];
		return coords;
	}
	public static double[] Q_HYPERBOLIC, Q_EUCLIDEAN, Q_ELLIPTIC;
	private static double[][] Q_LIST;
	static {
		Q_HYPERBOLIC = Rn.identityMatrix(4);
		Q_HYPERBOLIC[15] = -1.0;
		Q_EUCLIDEAN = Rn.identityMatrix(4);
		Q_EUCLIDEAN[15] = 0.0;
		Q_ELLIPTIC = Rn.identityMatrix(4);
		Q_LIST = new double[3][];
		Q_LIST[0] = Q_HYPERBOLIC;
		Q_LIST[1] = Q_EUCLIDEAN;
		Q_LIST[2] = Q_ELLIPTIC;
	}
	public static double[] orthonormalizeMatrix(double[] dst, double[] m, double tolerance, int signature)		{
		if (dst == null) dst = new double[16];
		double[] diagnosis = Rn.subtract(null, Q_LIST[signature+1], 
				Rn.times(null, Rn.transpose(null, m), Rn.times(null, Q_LIST[signature+1], m )));
//		if (Rn.maxNorm(diagnosis) < tolerance)		{
//			return null;
//		}
		boolean mydebug = false;
		if (mydebug)	{
			LoggingSystem.getLogger(P3.class).log(Level.FINER,"m =");
			LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(m, -1));
			LoggingSystem.getLogger(P3.class).log(Level.FINER,"Original is");
			LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(diagnosis, -1));			
		}
		double[][] basis = new double[4][4];
		double[] Q = Q_LIST[signature+1];
		// the columns of m are the basis vectors (image of canonical basis under the isometry)
		for (int i = 0; i<4; ++i)	 for (int j = 0; j<4; ++j)	basis[i][j] = m[j*4+i];
		// first orthogonalize
		for (int i = 0; i<3; ++i)		
			for (int j = i+1; j<4; ++j)	{
				if (Q[5*j] == 0.0) continue;
				if (Math.abs(diagnosis[4*i+j]) > tolerance)	{
					Pn.projectOntoComplement(basis[j], basis[i], basis[j], signature);
				}
			}
		// then normalize
		for (int i = 0; i<4; ++i)		{
			if (Q[5*i] != 0.0)	Pn.normalizePlane(basis[i], basis[i],  signature);
			for (int j = 0; j<4; ++j)		dst[j*4+i] = basis[i][j];
		}
		// TODO figure out how to avoid this clean-up for euclidean case
		if (signature == EUCLIDEAN)	{
			for (int i = 0; i<4; ++i)	{dst[12+i] = 0.0;  dst[4*i+3] = m[4*i+3]; }
		}
		// for now just print out the table of inner products
		diagnosis = Rn.subtract(null, Q, 
				Rn.times(null, Rn.transpose(null, dst), Rn.times(null, Q_LIST[signature+1],dst )));
		if (mydebug)	{
			LoggingSystem.getLogger(P3.class).log(Level.FINER,"dst =");
			LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(dst, -1));
			LoggingSystem.getLogger(P3.class).log(Level.FINER,"Revised is");
			LoggingSystem.getLogger(P3.class).log(Level.FINER,Rn.matrixToString(diagnosis, -1));			
		}
		return dst;
	}

	public static double[] calculateBillboardMatrix(double[] result, 
			double xscale, 
			double yscale, 				// scaling factors for the billboard
			double[] xyzOffset,			// an offset in "billboard" coordinate system
			int alignment,	     		// alignment of billboard in compass-direction from anchor point (using SwingConstants)
			double[] cameraToObject, 	// the transformation from camera to object coordinates
			double[] point, 			// the position of the anchor point in object coordinate system
			int signature)	{
		if (result == null) result = new double[16];
		// TODO the following call perhaps should return a determinant-1 matrix (throw out scaling)
	    double[] orientation = extractOrientationMatrix(null, cameraToObject, Pn.originP3, signature);
	    double[] scale = makeStretchMatrix(null, xscale, yscale, 1.0);
	    //calculate translation for alignment
	    double align=0, valign=0;  // default
	    switch (alignment) {
	    	case SwingConstants.NORTH  : align=-xscale/2; break;
	    	case SwingConstants.EAST   : valign=-yscale/2; break;
	    	case SwingConstants.SOUTH  : align=-xscale/2; valign=-yscale; break;
	    	case SwingConstants.WEST   : align=-xscale;   valign=-yscale/2; break;
	    	case SwingConstants.CENTER : align=-xscale/2; valign=-yscale/2; break;
	    	//case SwingConstants.NORTH_EAST : default
	    	case SwingConstants.SOUTH_EAST : valign=-yscale; break;
	    	case SwingConstants.SOUTH_WEST : align=-xscale; valign=-yscale; break;
	    	case SwingConstants.NORTH_WEST : align=-xscale; break;
	    }
	    double[] euclideanTranslation = makeTranslationMatrix(null, Rn.add(null, xyzOffset, new double[]{align, valign, 0, 0}), EUCLIDEAN);
	    double[] pointTranslation = makeTranslationMatrix(null, point, signature);

	    Rn.times(result, pointTranslation, Rn.times(null, orientation, Rn.times(null, euclideanTranslation, scale)));
		return result;
	}

	public static double[] pluckerToMatrix(double[] m, double[] pl) {
		if (m == null) m = new double[16];
		m[0] = m[5] = m[10] = m[15] = 0.0;
		m[4] = -(m[1] = pl[0]);
		m[8] = -(m[2] = pl[1]);
		m[12] = -(m[3] = pl[2]);
		m[9] = -(m[6] = pl[3]);
		m[13] = -(m[7] = -pl[4]);		// here's the strange minus sign! (p42 instead of p24 in formulas)
		m[14] = -(m[11] = pl[5]);
		return m;
		
	}

	public static double[] polarizePlucker(double[] dst, double[] src) {
		if (dst == null) dst = new double[6];
		double[] tmp = null;
		if (dst == src)  tmp = new double[6];
		else tmp = dst;
		for (int i = 0; i<6; ++i) tmp[5-i] = src[i];
		if (src == dst) System.arraycopy(tmp, 0, dst, 0,  6);
		return dst;
	}

	public static double orientation(double[] to, double[] up, double[] upNoRoll) {
		double det = 0.0;
		double[] mat = new double[16];
		System.arraycopy(to, 0, mat,0,4);
		System.arraycopy(up, 0, mat, 4, 4);
		System.arraycopy(upNoRoll,0,mat,8,4);
		mat[15] = 1.0;
		return Rn.determinant(mat);
	}

}
