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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
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

import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DataListSet;

/**
 * A set of points in 3 space.
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 *
 */
public class PointSet extends Geometry //implements GeometryListener
{
  protected DataListSet vertexAttributes;
  
  public PointSet()
  {
    this(0);
  }

  public PointSet(int numPoints)
  {
	super();
	vertexAttributes= new DataListSet(numPoints);
 }

  /**
   * The number of vertices defines the length of all data lists associated
   * with vertex attributes.
   */
  public int getNumPoints()
  {
    startReader();
    try {
      return vertexAttributes.getListLength();
    } finally {
      finishReader();
    }
  }

  /**
   * Sets the number of vertices, implies removal of all previously defined
   * vertex attributes.
   * @param numVertices the number of vertices to set >=0
   */
  public void setNumPoints(int numVertices)
  {
    checkReadOnly();
    startWriter();
    try {
      vertexAttributes.reset(numVertices);
    } finally {
      finishWriter();
    }
  }

  /**
   * Returns a read-only view to all currently defined vertex attributes.
   * You can copy all currently defined vertex attributes to another
   * PointSet using
   * <code>target.setVertexAttributes(source.getVertexAttributes())</code>
   * These attributes are copied then, not shared. Thus modifying either
   * source or target afterwards will not affect the other.
   * @see setVertexAttributes(DataListSet)
   * @see getGeometryAttributes()
   */
  public DataListSet getVertexAttributes()
  {
    startReader();
    try {
      return vertexAttributes.readOnly();
    } finally {
      finishReader();
    }
  }

  public DataList getVertexAttributes(Attribute attr) {
    startReader();
    try {
      return vertexAttributes.getList(attr);
    } finally {
      finishReader();
    }
  }

  public void setVertexAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(vertexAttributes, dls, false);
    fireGeometryChanged(dls.storedAttributes(), null, null, null);
    finishWriter();
  }

  public void setVertexAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(vertexAttributes, attr, dl, false);
    fireGeometryChanged(Collections.singleton(attr), null, null, null);
    finishWriter();
  }

  public void setVertexCountAndAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(vertexAttributes, attr, dl, true);
    fireGeometryChanged(Collections.singleton(attr), null, null, null);
    finishWriter();
  }

  public void setVertexCountAndAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(vertexAttributes, dls, true);
    fireGeometryChanged(dls.storedAttributes(), null, null, null);
    finishWriter();
  }

  public void accept(SceneGraphVisitor v)
  {
    startReader();
    try {
      v.visit(this);
    } finally {
      finishReader();
    }
  }
  static void superAccept(PointSet ps, SceneGraphVisitor v)
  {
    ps.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v)
  {
    super.accept(v);
  }

}
