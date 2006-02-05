package de.jreality.scene.proxy.scene;

import java.util.Collections;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedLineSet extends de.jreality.scene.IndexedLineSet implements
        RemoteIndexedLineSet {

    public void setEdgeCountAndAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(edgeAttributes, dls, true);
        fireGeometryChanged(null, dls.storedAttributes(), null, null);
      } finally {
        finishWriter();
      }
    }
    
    public void setEdgeAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(edgeAttributes, dls, dls.getListLength() != edgeAttributes.getListLength());
        fireGeometryChanged(null, dls.storedAttributes(), null, null);
      } finally {
        finishWriter();
      }
    }
    
    public void setEdgeAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(edgeAttributes, attr, dl, length != edgeAttributes.getListLength());
        fireGeometryChanged(null, Collections.singleton(attr), null, null);
      } finally {
        finishWriter();
      }
    }

    public void setEdgeCountAndAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        PointSet.setAttrImp(edgeAttributes, attr, dl, true);
        fireGeometryChanged(null, Collections.singleton(attr), null, null);
      } finally {
        finishWriter();
      }
    }
    
    public void setVertexCountAndAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(vertexAttributes, dls, true);
        fireGeometryChanged(dls.storedAttributes(), null, null, null);
      } finally {
        finishWriter();
      }
    }
    
    public void setVertexAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(vertexAttributes, dls, dls.getListLength() != vertexAttributes.getListLength());
        fireGeometryChanged(dls.storedAttributes(), null, null, null);
      } finally {
        finishWriter();
      }
    }
    
    public void setVertexAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(vertexAttributes, attr, dl, length != vertexAttributes.getListLength());
        fireGeometryChanged(Collections.singleton(attr), null, null, null);
      } finally {
        finishWriter();
      }
    }

    public void setVertexCountAndAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        PointSet.setAttrImp(vertexAttributes, attr, dl, true);
        fireGeometryChanged(Collections.singleton(attr), null, null, null);
      } finally {
        finishWriter();
      }
    }

}
