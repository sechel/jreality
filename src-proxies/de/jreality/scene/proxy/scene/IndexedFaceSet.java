package de.jreality.scene.proxy.scene;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.Iterator;

import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.util.Lock;

public class IndexedFaceSet extends de.jreality.scene.IndexedFaceSet implements
        RemoteIndexedFaceSet {
    
    public void setFaceCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, faceAttributes, dls, true);
        fireGeometryChanged(null, null, dls.keySet(), null);
    }
    
    public void setFaceAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, faceAttributes, dls, dls.getListLength() != faceAttributes.getListLength());
        fireGeometryChanged(null, null, dls.keySet(), null);
    }

    public void setFaceAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, faceAttributes, attr, dl, length != faceAttributes.getListLength());
        fireGeometryChanged(null, null, Collections.singleton(attr), null);
    }

    public void setFaceCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, faceAttributes, attr, dl, true);
        fireGeometryChanged(null, null, Collections.singleton(attr), null);
    }

    public void setEdgeCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, true);
        fireGeometryChanged(null, dls.keySet(), null, null);
    }
    
    public void setEdgeAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, dls.getListLength() != edgeAttributes.getListLength());
        fireGeometryChanged(null, dls.keySet(), null, null);
    }
    
    public void setEdgeAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, edgeAttributes, attr, dl, length != edgeAttributes.getListLength());
        fireGeometryChanged(null, Collections.singleton(attr), null, null);
    }

    public void setEdgeCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, attr, dl, true);
        fireGeometryChanged(null, Collections.singleton(attr), null, null);
    }
    
    public void setVertexCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, dls, true);
        fireGeometryChanged(dls.keySet(), null, null, null);
    }
    
    public void setVertexAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, dls, dls.getListLength() != vertexAttributes.getListLength());
        fireGeometryChanged(dls.keySet(), null, null, null);
    }
    
    public void setVertexAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, vertexAttributes, attr, dl, length != vertexAttributes.getListLength());
        fireGeometryChanged(Collections.singleton(attr), null, null, null);
    }

    public void setVertexCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, attr, dl, true);
        fireGeometryChanged(Collections.singleton(attr), null, null, null);
    }

}
