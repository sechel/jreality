/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ?AS IS?
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.jreality.scene;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.WritableDataList;
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
