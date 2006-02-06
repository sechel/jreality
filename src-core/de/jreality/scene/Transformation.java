/*
 * Created on Dec 30, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.jreality.scene;

import de.jreality.math.*;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationEventMulticaster;
import de.jreality.scene.event.TransformationListener;
/**
 * 
 * A almost clean Transformation class, do not use anything else than set/getMatrix.
 * 
 * For doing projective geometry, see {@link de.jreality.math.MatrixBuilder} and
 * {@link de.jreality.math.FactoredMatrix}.
 * 
 * TODO: finally solve defaultMatrix and signature! and remove Clonable!!
 * 
 * @author Charles Gunn, weissman
 */
public class Transformation extends SceneGraphNode implements Cloneable {

  private transient TransformationListener transformationListener;
  
  private static final double[] ID = Rn.identityMatrix(4);
  
	protected double[] theMatrix;
  
  /**
   * @deprecated
   */
	protected double[] defaultMatrix;

  /**
   * @deprecated
   */
  protected int signature;

  private transient boolean matrixChanged;

	/**
	 * Generate a new transform with given signature and matrix
	 * If <i>m</i> is null, use identity matrix.  
	 * @param signature		See {@link Pn}.
	 * @param m
	 */
	public Transformation(int signature, double[] m) {
		if (m == null)	theMatrix = Rn.identityMatrix(4);
    else theMatrix = (double[]) m.clone();
		this.signature = signature;
	}
	
	public Transformation(int signature)	{
		this(signature, null);
	}
	
	public Transformation(double[] m)	{
		this(Pn.EUCLIDEAN, m);
	}
	
	public Transformation()	{
		this(Pn.EUCLIDEAN, null);
	}

	/** 
	 * @deprecated
	 */
	public Object clone() throws CloneNotSupportedException {
		Transformation copy = (Transformation) super.clone();
		if (theMatrix !=null) copy.theMatrix = (double[]) theMatrix.clone();
		if (defaultMatrix !=null) copy.defaultMatrix = (double[]) defaultMatrix.clone();
    copy.signature = signature;
		return copy;
	}
	
	/**
	 * Reset the matrix to the currently stored default matrix. See {@link #setDefaultMatrix()}.
	 * @deprecated
	 */
  public  void resetMatrix() {
    startWriter(); // need to lock here since we check defaultMatrix == null
    try {
      setMatrix(defaultMatrix == null ? ID : defaultMatrix);
    } finally {
      finishWriter();
    }
	}


	/**
	 * Copies the current matrix into the default matrix, so it can be restored if necessary (see {@link #resetMatrix()}.
   * @deprecated
	 */
  public void setDefaultMatrix() {
    checkReadOnly();
    startWriter();
    try {
  		setDefaultMatrix(theMatrix);
      //fireTransformationChanged();
    } finally {
      finishWriter();
    }
	}

	/**
	 * Sets the default matrix for this Transformation to the contents of <i>aMatrix</i>.
	 * @param aMatrix
   * @deprecated
	 */
  public void setDefaultMatrix(double[] aMatrix) {
     checkReadOnly();
     startWriter();
     try {
       if (defaultMatrix == null) defaultMatrix=new double[16];
       System.arraycopy(aMatrix, 0, defaultMatrix,0, theMatrix.length);
       //fireTransformationChanged();
     } finally {
       finishWriter();
     }
	}

	/**
	 * 
	 * @return 	the current default matrix
   * @deprecated
	 */
  public double[] getDefaultMatrix() {
     startReader();
     try {
       return (double[]) defaultMatrix.clone();
     } finally {
       finishReader();
     }
	}

	/**
	 * 
	 * @return	a copy of the current matrix
	 */
  public double[] getMatrix() {
    return getMatrix(null);
	}

	/**
	 * Copy the current matrix into <i>aMatrix</i> and return it.
	 * @param aMatrix
	 * @return	the filled in matrix
	 */
   public double[] getMatrix(double[] aMatrix) {
     startReader();
     try {
  		if (aMatrix!= null &&  aMatrix.length != 16) {
  			throw new IllegalArgumentException("lenght != 16");
  		}
  	 	if (aMatrix == null) aMatrix = new double[16];
  	 	System.arraycopy(theMatrix, 0, aMatrix, 0, 16);
  	 	return aMatrix;
     } finally {
       finishReader();
     }
	}

	/**
	 * Assign <i>aMatrix</i> to this Transformation.
	 * @param aMatrix
	 */
	public void setMatrix(double[] aMatrix) {
		checkReadOnly();
    startWriter();
    try {
  		System.arraycopy(aMatrix, 0, theMatrix, 0, aMatrix.length);
      fireTransformationChanged();
    } finally {
      finishWriter();
    }
	}

	/**
	 * See {@link Pn}, {@link Pn#ELLIPTIC}, {@link Pn#EUCLIDEAN}, and {@link Pn#HYPERBOLIC}.
	 * @return	the metric signature
	 * @deprecated
	 */
  public int getSignature()	{
    startReader();
    try {
      return signature;
    } finally {
      finishReader();
    }
	}
	
	/**
	 * Sets the metric signature of this transform. See {@link Pn}.
	 * @param aSig
	 * @deprecated
	 */
  public void setSignature( int aSig)	{
 		checkReadOnly();
    startWriter();
    try {
   		if (signature == aSig)	return;
  		signature = aSig;
      fireTransformationChanged();
    } finally {
      finishWriter();
    }
	}

	public void addTransformationListener(TransformationListener listener) {
    startReader();
		transformationListener=TransformationEventMulticaster.add(transformationListener, listener);
    finishReader();
	}
	public void removeTransformationListener(TransformationListener listener) {
    startReader();
		transformationListener=TransformationEventMulticaster.remove(transformationListener, listener);
    finishReader();
	}

	/**
	 * Tell the outside world that this transformation has changed.
	 * This methods takes no parameters and is equivalent
	 * to "everything has/might have changed".
	 */
  protected void writingFinished() {
    if (matrixChanged && transformationListener != null) 
      transformationListener.transformationMatrixChanged(new TransformationEvent(this));
    matrixChanged=false;
  };
  
	protected void fireTransformationChanged() {
	  matrixChanged=true;
	}
  
	public static void superAccept(Transformation t, SceneGraphVisitor visitor) {
		t.superAccept(visitor);
	}

	private void superAccept(SceneGraphVisitor v) {
	  super.accept(v);
	}

	public void accept(SceneGraphVisitor v)	{
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
	}

  
  // COMPATIBILITY METHODS - - all deprecated!!

  /**
   * @deprecated
   */
  public void multiplyOnRight( Transformation aTform ) {
    Matrix m = new Matrix(this);
    m.multiplyOnRight(aTform.getMatrix());
    setMatrix(m.getArray());
  }
    
  /**
   * @deprecated
   */
  public void multiplyOnLeft( Transformation aTform ) {
     Matrix m = new Matrix(this);
     m.multiplyOnLeft(aTform.getMatrix());
     setMatrix(m.getArray());
  }
    
  /**
   * @deprecated
   */
  public void multiplyOnRight( double[] T) {
    Matrix m = new Matrix(this);
    m.multiplyOnRight(T);
    setMatrix(m.getArray());
  }
    
  /**
   * @deprecated
   */
  public void multiplyOnLeft( double[] T) {
     Matrix m = new Matrix(this);
     m.multiplyOnLeft(T);
     setMatrix(m.getArray());
  }

  /**
   * @deprecated
   */
  public double[] getTranslation() {
    return new FactoredMatrix(this).getTranslation();
  }

  /**
   * @deprecated
   */
  public double[] getStretch() {
    return new FactoredMatrix(this).getStretch();
  }

  /**
   * @deprecated
   */
  public Quaternion getRotationQuaternion() {
    return new FactoredMatrix(this).getRotationQuaternion();
  }

  /**
   * @deprecated
   */
  public void setTranslation(double[] translation) {
    MatrixBuilder.init(new Matrix(this), signature).translate(translation).assignTo(theMatrix);
  }

  /**
   * @deprecated
   */
  public void setTranslation(double x, double y, double z) {
    setTranslation(new double[]{x, y, z});
  }

  /**
   * @deprecated
   */
  public void setStretch(double[] stretch) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setStretch(stretch);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public void setStretch(double stretch) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setStretch(stretch);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public void setRotation(Quaternion rot) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setRotation(rot);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public void setRotation(double d, double i, double j, double k) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setRotation(d, i, j, k);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public void setRotation(double d, double[] es) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setRotation(d, es);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public void setCenter(double[] center, boolean b) {
    FactoredMatrix fm = new FactoredMatrix(this);
    fm.setCenter(center, false);
    setMatrix(fm.getArray());
  }

  /**
   * @deprecated
   */
  public double getRotationAngle() {
    return new FactoredMatrix(this).getRotationAngle();
  }

}
