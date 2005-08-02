package de.jreality.scene.proxy.scene;

import java.util.Collections;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class IndexedLineSet extends de.jreality.scene.IndexedLineSet implements
        RemoteIndexedLineSet {

    public void setEdgeCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, true);
        fireGeometryChanged(null, dls.storedAttributes(), null, null);
    }
    
    public void setEdgeAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, dls.getListLength() != edgeAttributes.getListLength());
        fireGeometryChanged(null, dls.storedAttributes(), null, null);
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
        fireGeometryChanged(dls.storedAttributes(), null, null, null);
    }
    
    public void setVertexAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, dls, dls.getListLength() != vertexAttributes.getListLength());
        fireGeometryChanged(dls.storedAttributes(), null, null, null);
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
