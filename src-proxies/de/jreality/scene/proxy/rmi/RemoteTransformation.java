
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

public interface RemoteTransformation extends RemoteSceneGraphNode
{
  /**
   * @return	a copy of the current matrix
   */
  public abstract double[] getMatrix() throws RemoteException;
  /**
   * Copy the current matrix into <i>aMatrix</i> and return it.
   * @param aMatrix
   * @return	the filled in matrix
   */
  public abstract double[] getMatrix(double[] aMatrix) throws RemoteException;
  /**
   * Copy the contents of <i>aMatrix</i> into the current matrix.
   * @param aMatrix
   */
  public abstract void setMatrix(double[] aMatrix) throws RemoteException;
}