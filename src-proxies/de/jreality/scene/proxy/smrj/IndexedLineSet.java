package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;

import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedLineSet extends de.jreality.scene.IndexedLineSet implements RemoteIndexedLineSet {

	public void setAndCheckEdgeCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumEdges() == dl.size()) setEdgeAttributes(attr, dl);
		else setEdgeCountAndAttributes(attr, dl);
	}
	public void setAndCheckEdgeCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumEdges() == dls.getListLength()) setEdgeAttributes(dls);
		else setEdgeCountAndAttributes(dls);
	}
	public void setAndCheckVertexCountAndAttributes(Attribute attr, DataList dl) throws RemoteException {
		if (getNumPoints() == dl.size()) setVertexAttributes(attr, dl);
		else setVertexCountAndAttributes(attr, dl);
	}
	public void setAndCheckVertexCountAndAttributes(DataListSet dls) throws RemoteException {
		if (getNumPoints() == dls.getListLength()) setVertexAttributes(dls);
		else setVertexCountAndAttributes(dls);
	}
    
  private double[] vertices;
  private double[] vertexNormals;

      public void setVertices(ByteBufferWrapper data, int vertexSize) {
          System.out.println("PointSet.setVertices()");
          if (vertices == null || data.getDoubleLength() != vertices.length) {
              WritableDataList dl = new WritableDataList(StorageModel.DOUBLE_ARRAY.inlined(vertexSize), data);
              vertices = (double[]) dl.getData();
              setVertexCountAndAttributes(Attribute.COORDINATES, dl);
          } else {
              nodeLock.writeLock();
              data.getReadBuffer().asDoubleBuffer().get(vertices);
              nodeLock.writeUnlock();
          }
      }
      
      public void setVertexNormals(ByteBufferWrapper data, int normalSize) {
          System.out.println("PointSet.setVertexNormals()");
          if (vertexNormals == null || data.getDoubleLength() != vertexNormals.length) {
              WritableDataList dl = new WritableDataList(StorageModel.DOUBLE_ARRAY.inlined(normalSize), data);
              vertexNormals = (double[]) dl.getData();
              setVertexCountAndAttributes(Attribute.NORMALS, dl);
          } else {
              nodeLock.writeLock();
              data.getReadBuffer().asDoubleBuffer().get(vertexNormals);
              nodeLock.writeUnlock();
          }
      }

}
