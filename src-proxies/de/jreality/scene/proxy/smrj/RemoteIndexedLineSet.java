package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;
import java.util.Map;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public interface RemoteIndexedLineSet extends RemotePointSet, de.jreality.scene.proxy.rmi.RemoteIndexedLineSet
{
  /**
   * The number of edges defines the length of all data lists associated
   * with edge attributes.
   */
  public abstract int getNumEdges() throws RemoteException;
  /**
   * Sets the number of edges, implies removal of all previously defined
   * edge attributes.
   * @param numEdges the number of edges to set >=0
   */
  public abstract void setNumEdges(int numEdges) throws RemoteException;
  public abstract DataList getEdgeAttributes(Attribute attr) throws RemoteException;
  public abstract void setEdgeAttributes(DataListSet dls) throws RemoteException;
  public abstract void setEdgeAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setEdgeCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  public abstract void setEdgeCountAndAttributes(DataListSet dls) throws RemoteException;
  /**
   * this method calls either setEdgeAttributes(..) or setEdgeCountAndAttributes(..)
   * depending on whether the DataList length fits to the current number of vertices 
   */
  public abstract void setAndCheckEdgeCountAndAttributes(Attribute attr, DataList dl) throws RemoteException;
  /**
   * this method calls either setEdgeAttributes(..) or setEdgeCountAndAttributes(..)
   * depending on whether the DataLists lengths fits to the current number of vertices 
   */
  public abstract void setAndCheckEdgeCountAndAttributes(DataListSet dls) throws RemoteException;

  	/**
   * @deprecated
   */
  public abstract DataListSet getEdgeAttributes() throws RemoteException;
}