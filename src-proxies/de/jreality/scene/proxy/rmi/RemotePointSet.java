
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;
import java.util.Map;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemotePointSet extends RemoteGeometry
{
  /**
   * The number of vertices defines the length of all data lists associated
   * with vertex attributes.
   */
  public abstract int getNumPoints() throws RemoteException;
  /**
   * Sets the number of vertices, implies removal of all previously defined
   * vertex attributes.
   * @param numVertices the number of vertices to set >=0
   */
  public abstract void setNumPoints(int numVertices) throws RemoteException;
  public abstract DataList getVertexAttributes(Attribute attr) throws RemoteException;
  public abstract void setVertexAttributes(DataListSet dls) throws RemoteException;
  public abstract void setVertexAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setVertexCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setVertexCountAndAttributes(DataListSet dls) throws RemoteException;
  /**
   * this method calls either setVertexAttributes(..) or setVertexCountAndAttributes(..)
   * depending on whether the DataList length fits to the current number of vertices 
   */
  public abstract void setAndCheckVertexCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  /**
   * this method calls either setVertexAttributes(..) or setVertexCountAndAttributes(..)
   * depending on whether the DataLists length fits to the current number of vertices 
   */
  public abstract void setAndCheckVertexCountAndAttributes(DataListSet dls) throws RemoteException;
  /**
   * This is available short-term until someone writes a more specific get method.
   * @deprecated
   */
  	public abstract DataListSet getVertexAttributes() throws RemoteException;
}