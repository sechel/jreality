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
 * This class is not in its final form yet!
 * @version 1.0
 * @author <a href="mailto:hoffmann@math.tu-berlin.de">Tim Hoffmann</a>
 */
public class IndexedLineSet extends PointSet
{
  protected DataListSet edgeAttributes;

  public IndexedLineSet()
  {
    this(0, 0);
  }
  public IndexedLineSet(int numPoints)
  {
    this(numPoints, 0);
  }
  public IndexedLineSet(int numPoints, int numEdges)
  {
    super(numPoints);
    edgeAttributes=new DataListSet(numEdges);
  }

  /**
   * The number of edges defines the length of all data lists associated
   * with edge attributes.
   */
  public int getNumEdges()
  {
    startReader();
    try {
      return edgeAttributes.getListLength();
    } finally {
      finishReader();
    }
  }

  /**
   * Sets the number of edges, implies removal of all previously defined
   * edge attributes.
   * @param numEdges the number of edges to set >=0
   */
  public void setNumEdges(int numEdges)
  {
    checkReadOnly();
    startWriter();
    try {
      edgeAttributes.reset(numEdges);
    } finally {
    	finishWriter();
    }
  }

  /**
   * Returns a read-only view to all currently defined edge attributes.
   * You can copy all currently defined edge attributes to another
   * IndexedLineSet using
   * <code>target.setEdgeAttributes(source.getEdgeAttributes())</code>
   * These attributes are copied then, not shared. Thus modifying either
   * source or target afterwards will not affect the other.
   * @see setEdgeAttributes(DataListSet)
   * @see getVertexAttributes()
   * @see getGeometryAttributes()
   */
  public DataListSet getEdgeAttributes() {
    startReader();
    try {
      return edgeAttributes.readOnly();
    } finally {
      finishReader();
    }
  }

  public DataList getEdgeAttributes(Attribute attr) {
    startReader();
    try {
      return edgeAttributes.getList(attr);
    } finally {
      finishReader();
    }
  }

  public void setEdgeAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(edgeAttributes, dls, false);
    fireGeometryChanged(null, dls.storedAttributes(), null, null);
    finishWriter();
  }

  public void setEdgeAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(edgeAttributes, attr, dl, false);
    fireGeometryChanged(null, Collections.singleton(attr), null, null);
    finishWriter();
  }

  public void setEdgeCountAndAttributes(Attribute attr, DataList dl) {
    checkReadOnly();
    startWriter();
    setAttrImpl(edgeAttributes, attr, dl, true);
    fireGeometryChanged(null, Collections.singleton(attr), null, null);
    finishWriter();
  }

  public void setEdgeCountAndAttributes(DataListSet dls) {
    checkReadOnly();
    startWriter();
    setAttrImpl(edgeAttributes, dls, true);
    fireGeometryChanged(null, dls.storedAttributes(), null, null);
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
  static void superAccept(IndexedLineSet g, SceneGraphVisitor v)
  {
    g.superAccept(v);
  }
  private void superAccept(SceneGraphVisitor v)
  {
    super.accept(v);
  }
}
