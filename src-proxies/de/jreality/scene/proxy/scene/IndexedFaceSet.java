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
    }
    
    public void setFaceAttributes(DataListSet dls) {
        PointSet.setAttrImp(nodeLock, faceAttributes, dls, dls.getListLength() != faceAttributes.getListLength());
    }

    public void setFaceAttributes(Attribute attr, DataList dl) {
        int length = (dl instanceof ByteBufferList) ? ((ByteBufferList)dl).getCoveredLength() : dl.size();
        PointSet.setAttrImp(nodeLock, faceAttributes, attr, dl, length != faceAttributes.getListLength());
    }

    public void setFaceCountAndAttributes(Attribute attr, DataList dl) {
        PointSet.setAttrImp(nodeLock, faceAttributes, attr, dl, true);
    }

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
