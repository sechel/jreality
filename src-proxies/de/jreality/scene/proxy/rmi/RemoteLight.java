
package de.jreality.scene.proxy.rmi;

import java.awt.Color;
import java.rmi.RemoteException;

public interface RemoteLight extends RemoteSceneGraphNode
{
  public abstract Color getColor() throws RemoteException;
  public abstract void setColor(Color color) throws RemoteException;
  /**
   * Get this light's intensity
   * @return double the intensity
   */
  public abstract double getIntensity() throws RemoteException;
  /**
   * Sets the intensity.
   * @param intensity the intensity
   */
  public abstract void setIntensity(double intensity) throws RemoteException;
  /**
   * @return Returns wether the light is global for the scene.
   */
  public abstract boolean isGlobal() throws RemoteException;
  /**
   * @param global: setting wether the light is global for the scene.
   */
  public abstract void setGlobal(boolean global) throws RemoteException;
}