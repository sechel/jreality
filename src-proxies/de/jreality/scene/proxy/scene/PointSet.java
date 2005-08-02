package de.jreality.scene.proxy.scene;

import java.util.Collections;
import java.util.Iterator;

import de.jreality.scene.Lock;
import de.jreality.scene.data.*;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

public class PointSet extends de.jreality.scene.PointSet implements
        RemotePointSet {
    
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

    final static void setAttrImp(Lock nodeLock, DataListSet target, DataListSet data, boolean replace) {
        nodeLock.writeLock();
        try {
          if(replace) target.reset(data.getListLength());
          for(Iterator i=data.storedAttributes().iterator(); i.hasNext(); ) {
            Attribute a=(Attribute)i.next();
            PointSet.setAttrImp(nodeLock, target, a, data.getList(a), false);
          }
        } finally {
          nodeLock.writeUnlock();
        }
      }
    
    final static void setAttrImp(Lock nodeLock, DataListSet target, Attribute a, DataList d, boolean replace) {
        nodeLock.writeLock();
        try {
            StorageModel sm = d.getStorageModel();
            ByteBufferList bbl = null;
            int length = d.size();
            boolean isBBList = d instanceof ByteBufferList;
          if (isBBList) {
              bbl = (ByteBufferList) d;
              sm = bbl.getCoveredModel();
              length = bbl.getCoveredLength();
          }
          WritableDataList w;
          if(replace) { target.reset(length); w=null; } 
          else w=target.getWritableList(a);
          if(w==null) {
              w=isBBList?target.addWritable(a, sm, bbl.createFittingDataObject()) : target.addWritable(a, sm);
          }
          d.copyTo(w);
        } finally {
          nodeLock.writeUnlock();
        }
      }

}
