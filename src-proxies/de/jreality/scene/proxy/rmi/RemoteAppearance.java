
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;

/**
 * 
 */
public interface RemoteAppearance extends RemoteSceneGraphNode
{
  public abstract Object getAttribute(String key) throws RemoteException;
  public abstract Object getAttribute(String key, Class type) throws RemoteException;
  public abstract void setAttribute(String key, Object value) throws RemoteException;
  public abstract void setAttribute(String key, Object value, Class declaredType) throws RemoteException;
  public abstract void setAttribute(String key, double value) throws RemoteException;
  public abstract void setAttribute(String key, float value) throws RemoteException;
  public abstract void setAttribute(String key, int value) throws RemoteException;
  public abstract void setAttribute(String key, long value) throws RemoteException;
  public abstract void setAttribute(String key, boolean value) throws RemoteException;
  public abstract void setAttribute(String key, char value) throws RemoteException;
}