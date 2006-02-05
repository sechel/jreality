/*
 * Created on Dec 30, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.scene;

import java.io.Serializable;

import de.jreality.math.*;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationEventMulticaster;
import de.jreality.scene.event.TransformationListener;
/**
 * @author Charles Gunn
 *
 *	<p>
 *The Transformation class is built around a 4x4 matrix. The class provides a variety of methods
 * for setting and getting the transformation.  One instance can handle a series of transformations, based on the so-called polar
 * decomposition. See {@link <a href="www.cs.wisc.edu/graphics/Courses/cs-838-2002/ Papers/polar-decomp.pdf">Duff and Shoemaker paper</a>.
 * To be exact, the matrix M is factored as the matrix product M=T*R*S.  Note that matrices act on
 * column vectors which stand to the right of the matrix.  S is a "stretch" or "scale" matrix -- a diagonal matrix.  R is an arbitrary
 * rotation of Euclidean 3-space, and T is a translation.  <p>
 * 
 * Users may set the matrix directly, then the factors will be computed and are accessible.  Or, the user can set one or more 
 * of the factors, and the corresponding matrix is calculated and made available.  The update mechanism either decomposes or composes 
 * the matrix depending on the type of the most recent "setter" called by the user. <p>
 * 
 * This class is designed to work with any of the classical homogeneous geometries: euclidean, elliptic, or hyperbolic.  The variable
 * {\it signature} controls which geometry is active.  [Note: Probably should be constructor parameter and not allowed to change]. <p>
 * 
 * By default the origin (0,0,0,1) is the fixed point of the scale and rotation part of the transformation.  It is however possible
 * to specity another <i>center</i> (see {@link #setCenter}.  The resulting matrix is then T*C*R*S*IC where C is the translation taking the origin to C, and
 * IC is its inverse. The fixed point for the rotation and stretch is then  <i>center</i>. <p>
 * 
 * It is also possible to work with reflections, since any reflection can be factored as T*R*S*G where G is 
 * the diagonal matrix {-1,-1,-1,1} (that is, reflection around the origin). A matrix is considered a reflection if 
 * its determinant is negative. <p>
 * 
 * The matrix in general belongs to the matrix group GL(4,R).  
 * It is also possible to query the matrix to find out if it belongs to the subgroup SL(4,R) of matrices with determinant +/- 1. 
 * See {@link #getIsSpecial()}.<p>
 * 
 * See also {@link Pn} for a collection of static methods useful for generating 4x4 matrices for specific purposes.<p>
 * 
 * <b>Warning!</b> The matrix is stored as type <code>double[16]</code>, not <code>double[4][4]</code>, due to efficiency 
 * concerns arising from the way Java implements multi-dimensional arrays.<p>
 *  
 * <b>Warning</b> Angles are measured in radians.<p>
 * 
 * <b>Warning</b> The factorization will have to be modified to work with non-Euclidean isometries. For example, non-euclidean geometries
 * do not allow a "stretch" transformation.<p>
 * 
 * */
public class Transformation extends SceneGraphNode implements Cloneable, Serializable {
	private transient TransformationListener transformationListener;
	protected double[] theMatrix;		/**
	 * @deprecated
	 */
	protected double[] 				// the matrix transform
			translationVector,	
			centerVector,
			stretchVector,
			rotationAxis,
			centerMatrix, 
			invCenterMatrix;
	
	protected double[] 			defaultMatrix;
	/**
	 * @deprecated
	 */
	protected Quaternion rotationQ, 
			stretchRotationQ;
	/**
//	 * @deprecated
	 */
	protected int signature;
	/**
	 * @deprecated
	 */
	protected boolean	factorHasChanged,
			matrixHasChanged,
			isFactored,
			isIdentity,
			isSpecial,
			isReflection,
			useCenter,
			doFactor;
	protected boolean isEditable;

	/**
	 * @deprecated
	 */
	static final double TOLERANCE = 10E-8;
	
	/**
	 * Generate a new transform with given signature and matrix
	 * If <i>m</i> is null, use identity matrix.  
	 * @param signature		See {@link Pn}.
	 * @param m
	 */
	public Transformation(int signature, double[] m) {
		super();
		// TODO need to consider a clone() method
		if (m == null)	{
			theMatrix = Rn.identityMatrix(4);
			isIdentity = isSpecial = true;		
		}
		else			{
			theMatrix = (double[]) m.clone();
			matrixHasChanged = true;
		} 
		defaultMatrix = null;
		translationVector = new double[4];
		stretchVector = new double[4];
		rotationAxis = new double[3];
		rotationQ = new Quaternion(1.0, 0.0, 0.0, 0.0);
		stretchRotationQ = new Quaternion(1.0, 0.0, 0.0, 0.0);
		matrixHasChanged = true;
		isEditable = true;
		useCenter = false;
		doFactor = true;
		this.signature = signature;
		update();
	}
	
	/**
	 * @deprecated
	 */
	public Transformation(int signature)	{
		this(signature, null);
	}
	
	public Transformation(double[] m)	{
		this(Pn.EUCLIDEAN, m);
	}
	
	public Transformation()	{
		this(Pn.EUCLIDEAN, null);
	}

	/** (non-Javadoc)
	 * @see java.lang.Object#clone()
	 * @deprecated
	 */
	public Object clone() throws CloneNotSupportedException {
		try {
			Transformation copy = (Transformation) super.clone();
			if (theMatrix !=null) copy.theMatrix = (double[]) theMatrix.clone();
			if (defaultMatrix !=null) copy.defaultMatrix = (double[]) defaultMatrix.clone();
			if (centerMatrix !=null) copy.centerMatrix = (double[]) centerMatrix.clone();
			if (invCenterMatrix !=null) copy.invCenterMatrix = (double[]) invCenterMatrix.clone();
			if (translationVector != null) copy.translationVector = (double[]) translationVector.clone();
			if (centerVector!= null) copy.centerVector = (double[]) centerVector.clone();
			if (stretchVector != null) copy.stretchVector = (double[]) stretchVector.clone();
			if (rotationAxis != null) copy.rotationAxis = (double[]) rotationAxis.clone();
			if (rotationQ != null) copy.rotationQ = (Quaternion) rotationQ.clone();
			if (stretchRotationQ != null) copy.stretchRotationQ = (Quaternion) stretchRotationQ.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @deprecated
	 */
	public Transformation getInverse()	{
		Transformation inv;
		try {
			inv = (Transformation) this.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		double[] invM = new double[16];
		Rn.inverse(invM, inv.getMatrix());
		inv.setMatrix(invM);
		inv.update();
		return inv;
	}
	/**
	 * Reset the matrix to the currently stored default matrix. See {@link #setDefaultMatrix()}.
	 *
	 */public  void resetMatrix()
	{
		if (defaultMatrix == null) Rn.setIdentityMatrix(theMatrix);
		else System.arraycopy(defaultMatrix, 0, theMatrix,0, theMatrix.length);
		matrixHasChanged = true;
		update();
	}


	/**
	 * Copies the current matrix into the default matrix, so it can be restored if necessary (see {@link #resetMatrix()}.
	 *
	 */public void setDefaultMatrix()
	{
		defaultMatrix = ((double[]) theMatrix.clone());
	}

	/**
	 * Sets the default matrix for this Transformation to the contents of <i>aMatrix</i>.
	 * @param aMatrix
	 */public void setDefaultMatrix(double[] aMatrix)
	{
		System.arraycopy(aMatrix, 0, defaultMatrix,0, theMatrix.length);
		fireTransformationChanged();
	}

	/**
	 * 
	 * @return 	the current default matrix
	 */public double[] getDefaultMatrix()
	{
		return defaultMatrix;
	}

	/**
	 * 
	 * @return	a copy of the current matrix
	 */public double[] getMatrix()
	{
	 	return getMatrix(null);
	}

	/**
	 * Copy the current matrix into <i>aMatrix</i> and return it.
	 * @param aMatrix
	 * @return	the filled in matrix
	 */public double[] getMatrix(double[] aMatrix)
	{
		if (aMatrix!= null &&  aMatrix.length != 16) {
			System.err.println("Invalid argument");
			return null;
		}
	 	if (aMatrix == null) aMatrix = new double[16];
	 	synchronized(theMatrix)	{
			System.arraycopy(theMatrix, 0, aMatrix, 0, 16);
	 	}
		return aMatrix;
	}

	/**
	 * Copy the contents of <i>aMatrix</i> into the current matrix.
	 * @param aMatrix
	 */
	public void setMatrix(double[] aMatrix)
	{
		if (!isEditable) return;
		// sometimes this is just used to register the change
		synchronized(theMatrix)	{
			System.arraycopy(aMatrix, 0, theMatrix, 0, aMatrix.length);
			matrixHasChanged = true;
			update();
		}			
	}

	/**
	 * 
	 * Invoke {@link #multiplyOnRight(double[])} on the matrix attached to <i>aTform</i>.
	 * @param aTform
	 * @deprecated
	 */
	public void multiplyOnRight( Transformation aTform )
	{
		synchronized(theMatrix)	{
			concatenate(aTform.getMatrix(), true);			
		}
	}
		
	/**
	 * Invoke {@link #multiplyOnLeft(double[])} on the matrix attached to <i>aTform</i>.
	 * @param aTform
	 * @deprecated
	 */public void multiplyOnLeft( Transformation aTform )
	{
	 	synchronized(theMatrix)	{
			concatenate(aTform.getMatrix(), false);	 		
	 	}
	}
		
	/**
	 * Let M be the current matrix. Then form the matrix product M*T and store it in M.
	 * 
	 * @param aMatrix
//	 * @deprecated
	 */
	public void multiplyOnRight( double[] T)
	{
	 	synchronized(theMatrix)	{
	 		concatenate(T, true);
	 	}
	}
		
	/**
	 * Let M be the current matrix. Then form the matrix product T*M and store it in M.
	 * @param aMatrix
//	 * @deprecated
	 */public void multiplyOnLeft( double[] T)
	{
	 	synchronized(theMatrix)	{
	 		concatenate(T, false);
	 	}
	}
		
		/**
		 * @deprecated
		 */
	private void concatenate(double[] aMatrix , boolean onRight)
		{
		if (!isEditable) return;
		synchronized(theMatrix)	{
			if (onRight)  Rn.times(theMatrix,theMatrix, aMatrix);
			else	 Rn.times(theMatrix,aMatrix, theMatrix);
			matrixHasChanged = true;
			update();			
		}
	}

	/**
	 * 
	 * @return	<code>true</code> if the matrix has negative determinant.
	 * @deprecated
	 */public boolean getIsReflection()
	{
	 	//return Rn.determinant(theMatrix) < 0.0 ;
		return isReflection;
	}

	/**
	 * Set the matrix to be a reflection based on the value of <i>aval</i>. (This is a somewhat questionable method.-cg)
	 * @param aVal
	 * @deprecated
	 */public void setIsReflection(boolean aVal)
	{
		if (!isEditable) return;
		synchronized(this)	{
			if (aVal == isReflection) return;
			isReflection = aVal;
			factorHasChanged = true;
			update();
			System.out.println("IsReflection ="+isReflection);			
		}
	}

	/**
	 * 
	 * @return	<code>true</code> if the transform has been set to respect a separate center for its rotation and 
	 * stretch factors. @see #setCenter(double[]), and introductory remarks on this class.
	 * @deprecated
	 */public boolean getUseCenter()
	{
		return useCenter;
	}

	/**
	 * Set whether the transform uses a separate center for rotation and stretch factors.  @see #setCenter(double[]).
	 * @param aVal
	 * @deprecated	Use {@link #setCenter(double[]) with null argument to turn off using center.
	 */
	public void setUseCenter(boolean aVal)
	{
		if (!isEditable) return;
		if (centerVector == null)	{
			//System.err.println("Transform: setUseCenter: First set center Vector");
			useCenter = false;
			return;
		}
		if (useCenter == aVal) return;
		useCenter = aVal;
		factorHasChanged = true;
		if (useCenter)	{
			if (centerMatrix == null)	centerMatrix = new double[16];
			if (invCenterMatrix == null) 	invCenterMatrix = new double[16];
		}
		update();
	}


	/**
	 * 
	 * @return	<code>true</code> if this instance is editable.
//	 * @deprecated
	 */
	public boolean getIsEditable()
	{
		return isEditable;
	}

	/**
	 * Set whether this transform can be edited.  Default: true.
	 * @param aVal
//	 * @deprecated
	 */public void setIsEditable(boolean aVal)
	{
		synchronized(this) {
			isEditable = aVal;
		}
	}

	/**
	 * Is this transformation the identity?
	 * @return
	 * @deprecated
	 */public boolean getIsIdentity()
	{ 
		if (Rn.isIdentityMatrix(theMatrix, TOLERANCE) ) isIdentity = true;
		else isIdentity = false;
		return isIdentity;
	}
	
	/**
	 * @return	<code>true</code> if the matrix is to be factored.  Default: true.
	 * @deprecated
	 */public boolean getDoFactor() 
	{ 
		return doFactor; 
	}

	/**
	 * Sets whether the matrix is to be factored into its factors. See {@link Transformation}.
	 * @param aVal
	 * @deprecated
	 */public void setDoFactor( boolean aVal)
	{
	 	synchronized(this)	{
			doFactor = aVal;
			// this can also be used to request updating of factorization
			if (doFactor) update();	
	 	}
	}

	/**
	 * Is the determinant 1 or -1? (Or within {@link Transformation#TOLERANCE}.
	 * @return
	 * @deprecated
	 */public boolean getIsSpecial()
	{
		if ( matrixHasChanged || factorHasChanged) update();
		return isSpecial;
	}

	/**
	 * See {@link Pn}, {@link Pn#ELLIPTIC}, {@link Pn#EUCLIDEAN}, and {@link Pn#HYPERBOLIC}.
	 * @return	the metric signature
//	 * @deprecated
	 */public int getSignature()	
	{
		return signature;
	}
	
	/**
	 * Sets the metric signature of this transform. See {@link Pn}.
	 * @param aSig
//	 * @deprecated
	 */public void setSignature( int aSig)
	{
 		if (!isEditable)	return;
	 	synchronized(this)	{
	 		if (signature == aSig)	return;
			signature = aSig;
			//System.out.println("Changing signatures is dangerous:");// resetting to identity");
			//setMatrix(Rn.identityMatrix(4));
			matrixHasChanged = true;
			update();
	 	}
	}


	/**
	 * Invoke {@link #setCenter(double[], boolean)} with the second parameter <code>false</code>.
	 * @param aVec
	 * @deprecated
	 */public void setCenter( double[] aVec)
	{
			setCenter(aVec, false);
	}

	/**
	 * Set the <i>center</i> of the transformation.  See the class description above for
	* a description.  If <i>keepMatrix</i> is <code>true</code>, then the value of the transformation will
	* be left unchanged; the <i>translation</i> factor will be adjusted to achieve 
	* this effect.  If it is not, then the other factors will be left unchanged 
	* and the resulting matrix will take on a new value.
	* Side effect: 	{@link #setUseCenter(boolean)} is called with parameter <code>true</code>.
	 * @param aVec			the position of the center (as a 3-vector or homogeneous 4-vector)
	 * @param keepMatrix	whether to preseve the value of the matrix
	 * @deprecated
	 */
	public void setCenter(double[] aVec, boolean keepMatrix)
	{
		if (!isEditable)	return;
		synchronized(this)	{
			if (aVec == null) {useCenter = false; return;}
			useCenter = true;
			if (centerVector == null)			centerVector = new double[4];
			if (centerMatrix == null)	 		centerMatrix = new double[16];
			if (invCenterMatrix == null)		invCenterMatrix = new double[16];
			centerVector[3] = 1.0;
			System.arraycopy(aVec,0,centerVector,0,aVec.length);
			P3.makeTranslationMatrix(centerMatrix, centerVector, signature);
			Rn.inverse(invCenterMatrix, centerMatrix);

			if (keepMatrix)	{
				matrixHasChanged = true;
				factorHasChanged = false;
			}else {
				matrixHasChanged = false;
				factorHasChanged = true;
			}
			update();			
		}
	}

	/**
	 * @return	 the center vector (as homogeneous 4-vector).
	 * @deprecated
	 */public double[]  getCenter()
	{
		return centerVector;
	}

	/**
	 * Set the translation factor with the three components <i>tx, ty, tz</i>.
	 * @param tx
	 * @param ty
	 * @param tz
	 * @deprecated
	 */public void setTranslation( double tx, double ty, double tz)
	{
		if (!isEditable)	return;
		synchronized(this)	{
			if (signature != Pn.EUCLIDEAN) {
				System.err.println("Transform: setTranslation: Invalid signature");
				return;
			}
			//ASSERT( aTransV, OE_NULLPTR, OE_DEFAULT, "", return nil);
			translationVector[0] = tx;
			translationVector[1] = ty;
			translationVector[2] = tz;
			translationVector[3] = 1.0;
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * Set the translation part of the transform with the vector <i>aTransV</i>. The length of <i>aTransV</i> 
	 * must be less than or equal to 4. 
	 * @param aTransV
	 * @deprecated
	 */public void setTranslation( double[]  aTransV)
	{
		if (!isEditable)	return;
		//ASSERT( aTransV, OE_NULLPTR, OE_DEFAULT, "", return nil);
		synchronized(this)	{
			if (aTransV.length == 4 && signature == Pn.EUCLIDEAN && aTransV[3] == 0.0)	{
				throw new IllegalArgumentException("Invalid euclidean translation");
			}
			int n = Math.min(aTransV.length, 4);
			System.arraycopy(aTransV,0,translationVector,0, n);
			System.arraycopy(Pn.originP3, n, translationVector, n, 4-n);
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * Get the translation vector for this transform
	 * @return		double[4]
	 * @deprecated
	 */public double[]  getTranslation()
	{
		if ( matrixHasChanged) update();
		return translationVector;
	}

	/**
	 * Set the rotation axis of this transformation using the three components <i>(ax, ay, ax)</i>.
	 * @param ax
	 * @param ay
	 * @param az
	 * @deprecated
	 */public void setRotationAxis( double ax, double ay, double az)
	{
	 		double[] axis = new double[3];
			axis[0] = ax; axis[1] = ay; axis[2] = az;
			setRotation(getRotationAngle(), axis);
	}

	/**
	 * Set the rotation axis of this transformation using the 3-vector <i>axis</i>.
	 * @param axis
	 * @deprecated
	 */
	 public void setRotationAxis( double[]  axis)
	{
		setRotation(getRotationAngle(), axis);
	}

	/**
	 * Set the rotation angle for this transformation. 
	 * @param angle		The angle measured in radians.
	 * @deprecated
	 */public void setRotationAngle( double angle)
	{
		setRotation( angle, getRotationAxis());
	}
     
	/**
	 * Set the angle and the axis simulataneously.
	 * @param angle
	 * @param axis
	 * @deprecated
	 */public void setRotation( double angle,double[]  axis )	
	{
		if (!isEditable)	return;
	 	synchronized(this)	{
	 		Quaternion.makeRotationQuaternionAngle(rotationQ, angle, axis);
			factorHasChanged = true;
			update();
	 	}
	}

	/**
	 * Set the angle and the axis (= (ax, ay, az)) simulataneously.
	 * @param angle
	 * @param axis
	 * @deprecated
	 */public void setRotation( double angle, double ax, double ay, double az)	
	{
		if (!isEditable)	return;
		double[] axis = new double[3];
		axis[0] = ax; axis[1] = ay; axis[2] = az;
		setRotation(angle, axis);
	}

	/**
	 * Set the rotation for this transformation using the unit quaternion <i>aQ</i>.
	 * @param aQ
	 * @deprecated
	 */public void setRotation(Quaternion aQ)	
	{
		if (!isEditable)	return;
		//ASSERT( aQ, OE_NULLPTR, OE_DEFAULT, "", return nil);
		//if ( Quaternion.equalsRotation(rotationQ, aQ, TOLERANCE)) return;
		synchronized(this)	{
			Quaternion.copy(rotationQ, aQ);
			Quaternion.normalizeRotation(rotationQ, rotationQ);
			getRotationAxis();
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * 
	 * @return	double[3]	the rotation axis.
	 * @deprecated
	 */public double[]  getRotationAxis()
	{
		return Rn.normalize(rotationAxis,Quaternion.IJK(rotationAxis, rotationQ));
	}
     
     /**
      * 
      * @return	the rotation angle (in radians)
      * @deprecated
      */
	public double getRotationAngle()
	{
		double angle = 2.0 * Math.acos(rotationQ.re);
		return angle;
	}
     
     /**
      * Get the rotation specified as a unit quaternion
      * @return
      * @deprecated
      */
	public Quaternion getRotationQuaternion()
	{
		return rotationQ;
	}

/* this is all probably unnecessary, at least for now   31.12.03
	public void setStretchRotationAxis( double[]  axis
	{
		return [self setStretchRotation( axis angle( [self getStretchRotationAngle]];
	}

	public setStretchRotationAngle( (float) aFloat
	{
		return [self setStretchRotation( [self getStretchRotationAxis] angle( aFloat];
	}
    
	public setStretchRotation( double[]  axis  angle( (float) aFloat
	{
		float c,s;
		if (!isEditable)	return self;
		ASSERT( axis, OE_NULLPTR, OE_DEFAULT, "", return nil);
		c = cos(.5 * aFloat);
		s = sin(.5 * aFloat);
		laNormVec3f(axis, axis);
		Rn.times(axis, axis, (double ) s);
		quMakef(&theStretchRotation, c, axis->v[0], axis->v[1], axis->v[2]);
		quNormf(&theStretchRotation, &theStretchRotation);
		factorHasChanged = true
		return update();
	}

	public double[]  getStretchRotationAxis
	{
		OuVec3f axis;
		System.arraycopy(&axis, &theStretchRotation.q.R);
		if ( laAbsSqrVec3f(&axis) )	{
			laNormVec3f(&theStretchRotAxis, &axis); 
		}
		return &theStretchRotAxis;
	}
     
	public (float) getStretchRotationAngle
	{
		float theAngle;
		OuQuaternionf *rot = [self getStretchRotation];
		theAngle = 2 * acos(  (double) RR(rot));
	#ifdef __linux__
		{double foo;
		theAngle = 2*M_PI * modf(theAngle/(2*M_PI), &foo) ;
		}
	#else
		theAngle = fmodf(theAngle, (float) 2 * M_PI);
	#endif
		return theAngle;
	}
     
	public setStretchRotation( Quaternion aQ;
	{
		if (isFrozen)	return self;
		ASSERT( aQ, OE_NULLPTR, OE_DEFAULT, "", return nil);
		theStretchRotation = *aQ;
		factorHasChanged = true
		return update();
	}

	public Quaternion getStretchRotation()
	{
		return stretchRotationQ;
	}
*/

	/**
	 * Set the stretch vector associated to this transform using the factor <i>stretch</i> for all three dimensions.
	 * @param stretch
	 * @deprecated	
	 */
	public void setStretch( double  stretch)
	{
		if (!isEditable)	return;
		//ASSERT( aS, OE_NULLPTR, OE_DEFAULT, "", return nil);
		synchronized(this)	{
			stretchVector[0] = stretch;
			stretchVector[1] = stretch;
			stretchVector[2] = stretch;
			stretchVector[3] = 1.0;
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * Set the stretch factor using the the vector <i>(sx, sy, sz)</i>
	 * @param sx
	 * @param sy
	 * @param sz
	 * @deprecated
	 */public void setStretch( double  sx, double sy, double sz)
	{
		if (!isEditable)	return;
		synchronized(this)	{
			stretchVector[0] = sx;
			stretchVector[1] = sy;
			stretchVector[2] = sz;
			stretchVector[3] = 1.0;
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * Set the stretch using the 3-vector </i>stretchV</i>.
	 * @param sV
	 * @deprecated
	 */
	public void setStretch( double[]  stretchV)
	{
		if (!isEditable)	return;
		//ASSERT( aS, OE_NULLPTR, OE_DEFAULT, "", return nil);
		synchronized(this)	{
			System.arraycopy( stretchV,0,stretchVector,0, Math.min(stretchV.length,stretchV.length));
			if (stretchV.length == 3) 	stretchVector[3] = 1.0;
			factorHasChanged = true;
			update();			
		}
	}

	/**
	 * Return the stretch vector for this transformation. Default: (1,1,1).
	 * @return	double[3]
	 * @deprecated
	 */public double[]  getStretch()
	{
		if (matrixHasChanged && doFactor) update();
		return stretchVector;
	}

	/**
	 * Updates the current state of the transformation.  If a factor was most recently changed, then the
	 * matrix is updated.  Otherwise, if the matrix has changed since the last invocation, the factorization
	 * is updated.
	 * @deprecated
	 *
	 */public void update()
	{
		boolean[] isFlipped = new boolean[1];
		double[] MC = new double[16], TTmp;
		synchronized(this)	{
			if (factorHasChanged )	{
				isFlipped[0]  = isReflection;
				P3.composeMatrixFromFactors(theMatrix, translationVector, rotationQ, stretchRotationQ, stretchVector, isReflection, signature);
				if (useCenter)	{
					Rn.times(theMatrix, theMatrix, invCenterMatrix);
					Rn.times(theMatrix, centerMatrix, theMatrix);
				} 
			}	else if (matrixHasChanged && doFactor)	{
				if (useCenter)	{  // coule use Rn.conjugate but don't want to recalculate inverse each time ...
					Rn.times(MC, theMatrix, centerMatrix);
					Rn.times(MC, invCenterMatrix, MC);
					TTmp = MC;
				}else	
					TTmp = theMatrix;
				P3.factorMatrix(TTmp, translationVector, rotationQ, stretchRotationQ, stretchVector, isFlipped, signature);	
				isReflection = isFlipped[0];
			}
			isSpecial = Rn.isSpecialMatrix(theMatrix, TOLERANCE);
			if (matrixHasChanged || factorHasChanged)			fireTransformationChanged();
			matrixHasChanged = factorHasChanged = false;
		}
	}

	public void addTransformationListener(TransformationListener listener) {
		transformationListener=
		  TransformationEventMulticaster.add(transformationListener, listener);
	}
	public void removeTransformationListener(TransformationListener listener) {
		transformationListener=
		  TransformationEventMulticaster.remove(transformationListener, listener);
	}

	/**
	 * Tell the outside world that this transformation has changed.
	 * This methods takes no parameters and is equivalent
	 * to "everything has/might have changed".
	 */
	protected void fireTransformationChanged() {
	  final TransformationListener l=transformationListener;
	  if(l != null) l.transformationMatrixChanged(new TransformationEvent(this));
	}

	public static void superAccept(Transformation t, SceneGraphVisitor visitor) {
		t.superAccept(visitor);
	}

	private void superAccept(SceneGraphVisitor v) {
	  super.accept(v);
	}

	public void accept(SceneGraphVisitor v)	{
		v.visit(this);
	}
}
