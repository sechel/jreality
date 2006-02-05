/*
 * Created on Dec 8, 2003
 *
 * This file is part of the jReality package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.scene;

import java.util.Collections;

import de.jreality.scene.data.*;

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
