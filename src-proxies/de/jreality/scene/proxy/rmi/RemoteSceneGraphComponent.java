
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;
import java.util.List;

public interface RemoteSceneGraphComponent extends RemoteSceneGraphNode
{
  public abstract void setGeometry(RemoteGeometry g) throws RemoteException;
  public abstract void addChild(RemoteSceneGraphComponent sgc) throws RemoteException;
  public abstract RemoteSceneGraphComponent getRemoteChildComponent(int index) throws RemoteException;
  public abstract int getChildComponentCount() throws RemoteException;
  public abstract void removeChild(RemoteSceneGraphComponent sgc) throws RemoteException;
  public abstract void setTransformation(RemoteTransformation newTrans) throws RemoteException;
  public abstract void setAppearance(RemoteAppearance newApp) throws RemoteException;
  public abstract RemoteAppearance getRemoteAppearance() throws RemoteException;
  public abstract RemoteCamera getRemoteCamera() throws RemoteException;
  public abstract void setCamera(RemoteCamera newCamera) throws RemoteException;
  public abstract RemoteLight getRemoteLight() throws RemoteException;
  public abstract void setLight(RemoteLight newLight) throws RemoteException;
  public abstract RemoteTransformation getRemoteTransformation() throws RemoteException;
  public abstract List getChildNodes() throws RemoteException;
  public abstract RemoteGeometry getRemoteGeometry() throws RemoteException;
//  public abstract void addTool(Tool tool) throws RemoteException;
//  public abstract void removeTool(Tool tool) throws RemoteException;
//  public abstract List getTools() throws RemoteException;
  public abstract void add(RemoteSceneGraphNode newChild) throws RemoteException;
  public abstract void remove(RemoteSceneGraphNode newChild) throws RemoteException;
}