
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

public interface RemotePointLight extends RemoteLight
{
  public abstract double getFalloffA0() throws RemoteException;
  public abstract double getFalloffA1() throws RemoteException;
  public abstract double getFalloffA2() throws RemoteException;
  public abstract void setFalloffA0(double falloffA0) throws RemoteException;
  public abstract void setFalloffA1(double falloffA1) throws RemoteException;
  public abstract void setFalloffA2(double falloffA2) throws RemoteException;
  public abstract void setFalloff(double[] atten) throws RemoteException;
  public abstract String getShadowMap() throws RemoteException;
  public abstract boolean isUseShadowMap() throws RemoteException;
  public abstract void setShadowMap(String shadowMap) throws RemoteException;
  public abstract void setUseShadowMap(boolean useShadowMap) throws RemoteException;
  public abstract int getShadowMapX() throws RemoteException;
  public abstract int getShadowMapY() throws RemoteException;
  public abstract void setShadowMapX(int shadowMapX) throws RemoteException;
  public abstract void setShadowMapY(int shadowMapY) throws RemoteException;
}