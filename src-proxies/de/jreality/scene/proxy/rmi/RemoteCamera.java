
package de.jreality.scene.proxy.rmi;

import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;

/**
 * 
 */
public interface RemoteCamera extends RemoteSceneGraphNode
{
  public abstract double getAspectRatio() throws RemoteException;
  public abstract double getFar() throws RemoteException;
  public abstract double getFieldOfView() throws RemoteException;
  public abstract double getFocus() throws RemoteException;
  public abstract double getNear() throws RemoteException;
  public abstract Rectangle2D getViewPort() throws RemoteException;
  public abstract void setAspectRatio(double ar) throws RemoteException;
  public abstract void setNear(double d) throws RemoteException;
  public abstract void setFar(double d) throws RemoteException;
  public abstract void setFieldOfView(double d) throws RemoteException;
  public abstract void setFocus(double d) throws RemoteException;
  public abstract void setViewPort(double x, double y, double w, double h) throws RemoteException;
  public abstract boolean isOnAxis() throws RemoteException;
  public abstract boolean isPerspective() throws RemoteException;
  public abstract void setOnAxis(boolean b) throws RemoteException;
  public abstract void setPerspective(boolean b) throws RemoteException;
  public abstract int getSignature() throws RemoteException;
  public abstract void setSignature(int i) throws RemoteException;
  public abstract double getEyeSeparation() throws RemoteException;
  public abstract void setEyeSeparation(double eyeSeparation) throws RemoteException;
  /**
   * The orientation matrix describes the transformation in
   * camera coordinate system which describes the orientation of
   * the head throws RemoteException; the "standard" position is that the eyes are on the
   * x-axis, up is the y-axis, and z is the direction of projection
   * The orientation matrix is used for cameras such as those in the
   * PORTAL.
   * @return the orientationMatrix.
   */
  public abstract double[] getOrientationMatrix() throws RemoteException;
  public abstract void setOrientationMatrix(double[] orientationMatrix) throws RemoteException;
  public abstract boolean isStereo() throws RemoteException;
  public abstract void setStereo(boolean isStereo) throws RemoteException;
}