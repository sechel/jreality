package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;

import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedLineSet extends de.jreality.scene.proxy.rmi.IndexedLineSet implements RemoteIndexedLineSet {

  private double[] vertices;
  private double[] vertexNormals;

      public void setVertices(ByteBufferList data, int vertexSize) {
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
      
      public void setVertexNormals(ByteBufferList data, int normalSize) {
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
