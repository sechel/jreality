
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;
import java.util.Map;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemoteGeometry extends RemoteSceneGraphNode
{
  public abstract Map getGeometryAttributes() throws RemoteException;
  public abstract Object getGeometryAttributes(Attribute key) throws RemoteException;
  public abstract void setGeometryAttributes(Map dls) throws RemoteException;
  public abstract void setGeometryAttributes(Attribute attr, Object o) throws RemoteException;
}