
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

public interface RemoteSpotLight extends RemotePointLight
{
  public abstract double getConeAngle() throws RemoteException;
  public abstract void setConeAngle(double coneAngle) throws RemoteException;
  public abstract double getConeDeltaAngle() throws RemoteException;
  public abstract double getDistribution() throws RemoteException;
  /**
   * Sets the coneDeltaAngle. This angle gives the width of the smooth falloff of the light's intensity towards the 
   * edge of the coneAngle.
   * @param coneDeltaAngle The coneDeltaAngle to set
   */
  public abstract void setConeDeltaAngle(double coneDeltaAngle) throws RemoteException;
  /**
   * Sets the distribution. This is the regular falloff of the lights intensity towards the edge of the cone
   * it is an exponent. 
   * @param distribution The distribution to set
   */
  public abstract void setDistribution(double distribution) throws RemoteException;
}