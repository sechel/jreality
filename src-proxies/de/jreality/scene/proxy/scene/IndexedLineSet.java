package de.jreality.scene.proxy.scene;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.util.Lock;

public class IndexedLineSet extends de.jreality.scene.IndexedLineSet implements
        RemoteIndexedLineSet {

    public void setEdgeCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, true);
    }
    
    public void setEdgeAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, dls, dls.getListLength() != edgeAttributes.getListLength());
    }
    
    public void setEdgeAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, edgeAttributes, attr, dl, length != edgeAttributes.getListLength());
    }

    public void setEdgeCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, edgeAttributes, attr, dl, true);
    }
    
    public void setVertexCountAndAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, dls, true);
    }
    
    public void setVertexAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, dls, dls.getListLength() != vertexAttributes.getListLength());
    }
    
    public void setVertexAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, vertexAttributes, attr, dl, length != vertexAttributes.getListLength());
    }

    public void setVertexCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, vertexAttributes, attr, dl, true);
    }

}
