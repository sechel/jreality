package de.jreality.scene.proxy.scene;

import java.util.Collections;
import java.util.Iterator;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.ByteBufferList;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.data.WritableDataList;

public class PointSet extends de.jreality.scene.PointSet implements RemotePointSet {
    
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

  final static void setAttrImp(DataListSet target, DataListSet data, boolean replace) {
    if(replace) target.reset(data.getListLength());
    for(Iterator i=data.storedAttributes().iterator(); i.hasNext(); ) {
      Attribute a=(Attribute)i.next();
      PointSet.setAttrImp(target, a, data.getList(a), false);
    }
  }
  
  final static void setAttrImp(DataListSet target, Attribute a, DataList d, boolean replace) {
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
  }

}
