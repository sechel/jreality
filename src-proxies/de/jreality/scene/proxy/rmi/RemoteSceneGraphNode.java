
package de.jreality.scene.proxy.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteSceneGraphNode extends Remote
{
  public abstract boolean isReadOnly() throws RemoteException;
  public abstract String getName() throws RemoteException;
  public abstract void setName(String string) throws RemoteException;
  public abstract List getChildNodes() throws RemoteException;
}