
package de.jreality.scene;

import java.util.*;

import de.jreality.scene.data.*;
import de.jreality.scene.event.GeometryEvent;
import de.jreality.scene.event.GeometryEventMulticaster;
import de.jreality.scene.event.GeometryListener;

/**
 * A geometry leaf.
 */
public abstract class Geometry extends SceneGraphNode {

  protected Map geometryAttributes=Collections.EMPTY_MAP;
  private transient GeometryListener geometryListener;
  
  protected transient Set changedGeometryAttributes=new HashSet();
  protected transient Set changedVertexAttributes=new HashSet();
  protected transient Set changedEdgeAttributes=new HashSet();
  protected transient Set changedFaceAttributes=new HashSet();

  /**
   * Returns a read-only view to all currently defined geometry attributes.
   * You can copy all currently defined geometry attributes to another
   * geometry using
   * <code>target.setGeometryAttributes(source.getGeometryAttributes())</code>
   * These attributes are copied then, not shared. Thus modifying either
   * source or target afterwards will not affect the other.
   * @see setGeometryAttributes(DataListSet)
   */
  public Map getGeometryAttributes() {
    startReader();
    try {
      return geometryAttributes.isEmpty()?
        Collections.EMPTY_MAP: Collections.unmodifiableMap(geometryAttributes);
    } finally {
      finishReader();
    }
  }

  public Object getGeometryAttributes(Attribute key) {
    startReader();
    try {
      return geometryAttributes.get(key);
    } finally {
      finishReader();
    }
  }

  public Object getGeometryAttributes(String name) {
    startReader();
    try {
      return getGeometryAttributes(Attribute.attributeForName(name));
    } finally {
      finishReader();
    }
  }
  
  public void setGeometryAttributes(Map dls) {
    checkReadOnly();
    if(dls.isEmpty()) return;
    startWriter();
    try {
      if(geometryAttributes==Collections.EMPTY_MAP)
        geometryAttributes=new HashMap(dls.size());
      for (Iterator it= dls.entrySet().iterator(); it.hasNext();)
      {
        Map.Entry entry=(Map.Entry)it.next();
        final Object key=entry.getKey(), value=entry.getValue();
        if(value!=null)
          geometryAttributes.put(key, value);
        else
          geometryAttributes.remove(key);
      }
      fireGeometryChanged(null, null, null, dls.keySet());
    } finally {
      finishWriter();
    }
  }

  public void setGeometryAttributes(String attributeName, Object value) {
    setGeometryAttributes(Attribute.attributeForName(attributeName), value);
  }
  
  public void setGeometryAttributes(Attribute attr, Object value) {
    checkReadOnly();
    startWriter();
    try {
      if(geometryAttributes==Collections.EMPTY_MAP)
        geometryAttributes=new HashMap();
      if(value!=null)
        geometryAttributes.put(attr, value);
      else
        geometryAttributes.remove(attr);
      fireGeometryChanged(null, null, null, Collections.singleton(attr));
    } finally {
      finishWriter();
    }
  }

  final void setAttrImpl(DataListSet target, DataListSet data, boolean replace) {
    if(replace) target.reset(data.getListLength());
    for(Iterator i=data.storedAttributes().iterator(); i.hasNext(); ) {
      Attribute a=(Attribute)i.next();
      setAttrImpl(target, a, data.getList(a), false);
    }
  }
  
  final void setAttrImpl(DataListSet target, Attribute a, DataList d, boolean replace) {
    if (d == null) {
      if (replace) throw new IllegalArgumentException("datalist==null for setting list length");
      target.remove(a);
    } else {
      WritableDataList w;
      if(replace) { target.reset(d.size()); w=null; } 
      else w=target.getWritableList(a);
      if(w==null) w=target.addWritable(a, d.getStorageModel());
      d.copyTo(w);
    }
  }

  public void addGeometryListener(GeometryListener listener) {
    startReader();
    geometryListener=GeometryEventMulticaster.add(geometryListener, listener);
    finishReader();
  }

  public void removeGeometryListener(GeometryListener listener) {
    startReader();
    geometryListener=GeometryEventMulticaster.remove(geometryListener, listener);
    finishReader();
  }

  /**
   * collect changed attributes
   */
  protected void fireGeometryChanged(Set vertexAttributeKeys,
    Set edgeAttributeKeys, Set faceAttributeKeys, Set geomAttributeKeys) {
    if (vertexAttributeKeys != null) changedVertexAttributes.addAll(vertexAttributeKeys);
    if (edgeAttributeKeys != null) changedEdgeAttributes.addAll(edgeAttributeKeys);
    if (faceAttributeKeys != null) changedFaceAttributes.addAll(faceAttributeKeys);
    if (geomAttributeKeys != null) changedGeometryAttributes.addAll(geomAttributeKeys);
  }

  protected void writingFinished() {
    fireGeometryChangedImpl(changedVertexAttributes, changedEdgeAttributes, changedFaceAttributes, changedGeometryAttributes);
    changedVertexAttributes.clear();
    changedEdgeAttributes.clear();
    changedFaceAttributes.clear();
    changedGeometryAttributes.clear();
  }
  
  /**
   * Tell the outside world that this geometry has changed.
   */
  protected void fireGeometryChangedImpl(Set vertexAttributeKeys,
    Set edgeAttributeKeys, Set faceAttributeKeys, Set geomAttributeKeys) {
    final GeometryListener l=geometryListener;
    if(l != null) l.geometryChanged(new GeometryEvent(this, vertexAttributeKeys,
      edgeAttributeKeys, faceAttributeKeys, geomAttributeKeys));
  }

  public void accept(SceneGraphVisitor v) {
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
  }
  
  static void superAccept(Geometry g, SceneGraphVisitor v) {
    g.superAccept(v);
  }

  private void superAccept(SceneGraphVisitor v) {
    super.accept(v);
  }
}
