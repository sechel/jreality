package de.jreality.scene.proxy.smrj;

import java.rmi.RemoteException;
import java.util.Collections;

import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedFaceSet extends de.jreality.scene.proxy.rmi.IndexedFaceSet implements RemoteIndexedFaceSet {

  WritableDataList verticesDL, vertexNormalsDL;
  private double[] vertices;
  private double[] vertexNormals;

      public void setVertices(ByteBufferWrapper data, int vertexSize) {
//      	System.out.println("IndexedFaceSet.setVertices()");
          if (verticesDL == null) {
              verticesDL = vertexAttributes.getWritableList(Attribute.COORDINATES);
              if (verticesDL != null) vertices = (double[]) verticesDL.getData();
//              System.out.println("init vertex data");
          }
          if (vertices == null || data.getDoubleLength() != vertices.length) {
//          	System.out.println("creating new vertex list.");
              vertices = new double[data.getDoubleLength()];
              verticesDL = vertexAttributes.addWritable(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY.inlined(vertexSize), vertices);
          }
          nodeLock.writeLock();
          data.getReadBuffer().asDoubleBuffer().get(vertices);
          nodeLock.writeUnlock();
          fireGeometryChanged(Collections.singleton(Attribute.COORDINATES), null, null, null);
      }
            
      public void setVertexNormals(ByteBufferWrapper data, int normalSize) {
//      	System.out.println("IndexedFaceSet.setVertexNormals()");
          if (vertexNormalsDL == null) {
              vertexNormalsDL = vertexAttributes.getWritableList(Attribute.NORMALS);
              if (vertexNormalsDL != null) vertexNormals = (double[]) vertexNormalsDL.getData();
          }
          if (vertexNormals == null || data.getDoubleLength() != vertexNormals.length) {
              vertexNormals = new double[data.getDoubleLength()];
              vertexNormalsDL = vertexAttributes.addWritable(Attribute.NORMALS, StorageModel.DOUBLE_ARRAY.inlined(normalSize), vertexNormals);
          }
          nodeLock.writeLock();
          data.getReadBuffer().asDoubleBuffer().get(vertexNormals);
          nodeLock.writeUnlock();
          fireGeometryChanged(Collections.singleton(Attribute.NORMALS), null, null, null);
      }


}
