
package de.jreality.scene.proxy.rmi;

import java.rmi.RemoteException;
import java.util.Map;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemoteIndexedFaceSet extends RemoteIndexedLineSet
{
  public abstract int getNumFaces() throws RemoteException;
  /**
   * Sets the number of face, implies removal of all previously defined
   * face attributes.
   * @param numVertices the number of vertices to set >=0
   */
  public abstract void setNumFaces(int numFaces) throws RemoteException;
  public abstract DataList getFaceAttributes(Attribute key) throws RemoteException;
  public abstract void setFaceAttributes(DataListSet dls) throws RemoteException;
  public abstract void setFaceAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setFaceCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setFaceCountAndAttributes(DataListSet dls) throws RemoteException;
  /**
   * this method calls either setFaceAttributes(..) or setFaceCountAndAttributes(..)
   * depending on whether the DataList length fits to the current number of vertices 
   */
  public abstract void setAndCheckFaceCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  /**
   * this method calls either setEdgeAttributes(..) or setFaceCountAndAttributes(..)
   * depending on whether the DataLists lengths fits to the current number of vertices 
   */
  public abstract void setAndCheckFaceCountAndAttributes(DataListSet dls) throws RemoteException;
 
   /**
   * @deprecated ??
   */
  public abstract DataListSet getFaceAttributes() throws RemoteException;
}