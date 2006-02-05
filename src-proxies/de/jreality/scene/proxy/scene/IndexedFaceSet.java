package de.jreality.scene.proxy.scene;

import java.util.Collections;

import de.jreality.scene.data.*;

public class IndexedFaceSet extends de.jreality.scene.IndexedFaceSet implements
        RemoteIndexedFaceSet {
    
    public void setFaceCountAndAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(faceAttributes, dls, true);
        fireGeometryChanged(null, null, dls.storedAttributes(), null);
      } finally {
        finishWriter();
      }
    }
    
    public void setFaceAttributes(DataListSet dls) {
      startWriter();
      try {
        PointSet.setAttrImp(faceAttributes, dls, dls.getListLength() != faceAttributes.getListLength());
        fireGeometryChanged(null, null, dls.storedAttributes(), null);
      } finally {
        finishWriter();
      }
    }

    public void setFaceAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(faceAttributes, attr, dl, length != faceAttributes.getListLength());
        fireGeometryChanged(null, null, Collections.singleton(attr), null);
      } finally {
        finishWriter();
      }
    }

    public void setFaceCountAndAttributes(Attribute attr, DataList dl) {
      startWriter();
      try {
        PointSet.setAttrImp(faceAttributes, attr, dl, true);
        fireGeometryChanged(null, null, Collections.singleton(attr), null);
      } finally {
        finishWriter();
      }
    }

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
        PointSet.setAttrImp( vertexAttributes, attr, dl, true);
        fireGeometryChanged(Collections.singleton(attr), null, null, null);
      } finally {
        finishWriter();
      }
    }

}
